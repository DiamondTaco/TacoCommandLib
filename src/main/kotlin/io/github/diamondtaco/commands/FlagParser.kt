package io.github.diamondtaco.commands

import com.mojang.brigadier.StringReader
import com.mojang.brigadier.context.CommandContext
import io.github.diamondtaco.lib.FuzzyMatcher
import io.github.diamondtaco.lib.Parser
import io.github.diamondtaco.readWhile
import net.minecraft.command.CommandException
import net.minecraft.server.command.ServerCommandSource
import net.minecraft.text.Text


internal class FlagParser<T>(flagSpec: FlagSet<T>) : Parser<ParsedFlag> {
    private val longFlags: Set<String> = flagSpec.toggles.map { it.id.long }.toSet()
    private val longArgs: Set<String> = flagSpec.args.map { it.id.long }.toSet()

    private val shortFlags: Set<Char> = flagSpec.toggles.mapNotNull { it.id.short }.toSet()
    private val shortArgs: Set<Char> = flagSpec.args.mapNotNull { it.id.short }.toSet()

    private val matcher = FuzzyMatcher(longFlags + longArgs)
    override fun parseReader(reader: StringReader): ParsedFlag {
        val input = reader.readWhile(*ALLOWED_CHARACTERS.toCharArray())

        if (input.startsWith("--")) {
            return when (val long = input.drop(2)) {
                in longFlags -> ParsedFlag.LongToggle(long)
                in longArgs -> ParsedFlag.LongArg(long)
                else -> throw CommandException(Text.literal("Unrecognized long identifier."))
            }
        } else if (input.startsWith("-")) {
            val shorts = input.drop(1).toList()

            return when {
                shorts.isEmpty() -> throw CommandException(Text.literal(""))
                input.length - 1 != shorts.toSet().size -> throw CommandException(Text.literal("Can't have repeated flags."))
                shorts.any { it !in shortFlags + shortArgs } -> throw CommandException(Text.literal("Unrecognized flag."))
                (shortArgs intersect shorts.dropLast(1).toSet()).isNotEmpty() ->
                    throw CommandException(Text.literal("Can't have args with flags behind them."))

                shorts.last() in shortArgs -> ParsedFlag.ShortTogglesArg(shorts.dropLast(1).toSet(), shorts.last())

                else -> ParsedFlag.ShortToggles(shorts.toSet())
            }
        } else {
            throw CommandException(Text.literal(""))
        }
    }

    override fun getCompletions(context: CommandContext<ServerCommandSource>, input: String): List<String> {
        return when {
            input.startsWith("--") -> matcher.getMatches(input.drop(2)).map { "--$it" }

            input.startsWith("-") ->
                if ((shortArgs intersect input.toSet()).isNotEmpty()) emptyList()
                else listOf("--") + (shortFlags + shortArgs)
                    .filterNot { it in input }
                    .map { "$input$it" }

            else -> emptyList()
        }
    }

    override fun toString(): String {
        return "FlagParser"
    }

    companion object {
        @Suppress("SpellCheckingInspection")
        const val ALLOWED_CHARACTERS = "abcdefghijklmnopqrstuvwxyzABCDEFGHIJKLMNOPQRSTUVWXYZ0123456789_-"
    }
}
