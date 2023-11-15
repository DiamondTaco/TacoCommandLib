package diamondtaco.tcl.commands

import com.mojang.brigadier.StringReader
import com.mojang.brigadier.context.CommandContext
import diamondtaco.tcl.lib.FuzzyMatcher
import diamondtaco.tcl.lib.Parser
import diamondtaco.tcl.readUntil
import net.minecraft.command.CommandException
import net.minecraft.server.command.ServerCommandSource
import net.minecraft.text.Text


data class StackFlags(val flags: Set<Char>)


class FlagParser(var flagSpec: ArgumentSet) : Parser<ParsedArgGroup> {
    private val longFlags = flagSpec.flags.map { it.long }.toSet()
    private val longArgs = flagSpec.args.map { it.long }.toSet()

    private val shortFlags = flagSpec.flags.mapNotNull { it.short }.toSet()
    private val shortArgs = flagSpec.args.mapNotNull { it.short }.toSet()

    private val matcher = FuzzyMatcher(longFlags + longArgs)
    override fun parseReader(reader: StringReader): ParsedArgGroup {
        val input = reader.readUntil(' ', '=')

        println("Parsing...")

        if (input.startsWith("--")) {
            return when (val long = input.drop(2)) {
                in longFlags -> LongFlag(long)
                in longArgs -> LongArg(long)
                else -> throw CommandException(Text.literal("Unrecognized long identifier."))
            }
        } else if (input.startsWith("-")) {
            val shorts = input.drop(1).toList()

            return when {
                shorts.isEmpty() -> throw CommandException(Text.literal(""))
                input.length - 1 != shorts.size -> throw CommandException(Text.literal("Can't have repeated flags."))
                shorts.any { it !in shortFlags + shortArgs } -> throw CommandException(Text.literal("Unrecognized flag."))

                shorts.last() in shortArgs -> ShortFlagArg(shorts.dropLast(1).toSet(), shorts.last())
                else -> ShortFlags(shorts.toSet())
            }
        } else {
            throw CommandException(Text.literal(""))
        }
    }

    override fun getCompletions(context: CommandContext<ServerCommandSource>, input: String): List<String> {
        return when {
            input.startsWith("--") -> matcher.getMatches(input.drop(2))
                .map { if (it in longFlags) "--$it" else "--$it=" }

            input.startsWith("-") -> listOf("--") + (shortFlags + shortArgs).filterNot { it in input }
                .map { if (it in shortFlags) "$input$it" else "$input$it=" }

            else -> emptyList()
        }
    }
}
