package diamondtaco.tcl.commands

import com.mojang.brigadier.StringReader
import com.mojang.brigadier.arguments.ArgumentType
import com.mojang.brigadier.context.CommandContext
import com.mojang.brigadier.suggestion.Suggestions
import com.mojang.brigadier.suggestion.SuggestionsBuilder
import com.mojang.brigadier.tree.CommandNode
import diamondtaco.tcl.lib.Parser
import net.minecraft.server.command.CommandManager.argument
import net.minecraft.server.command.ServerCommandSource
import java.util.concurrent.CompletableFuture

class Command(val arguments: List<Argument<*>>) {
    private val parserAmount = arguments.flatMap {
        listOf(
            1,
            if (it.name.short == null) 0 else 1,
            if (it.type == null) 0 else 1
        )
    }.sum()

    private val parsers = (0..parserAmount).map { MetaParser(it) }
    private val states = generateSequence { State.Unreached }.take(parserAmount).toList<State>()

    private sealed interface State {
        data class Flag(val argumentSet: ArgumentSet) : State
        data class Argument(val parser: Parser<Any>) : State
        data object Unreached : State
    }


    fun toBrigadierCommand(): CommandNode<ServerCommandSource> {
        val x = argument("", MetaParser(3)).build()
        return x
    }

    val flagParser = arguments.partition { it.type == null }
        .run { FlagParser(ArgumentSet(first.map { it.name }.toSet(), second.map { it.name }.toSet())) }

    inner class MetaParser(private val idx: Int) : ArgumentType<Any> {
        override fun parse(reader: StringReader?): Any {
            reader!!
            return when (val state = states[idx]) {
                is State.Flag -> {
                    flagParser.flagSpec = state.argumentSet
                    val group = flagParser.parseReader(reader)
                    when (group) {
                        is LongArg -> ""
                        is LongFlag -> ""
                        is ShortFlagArg -> ""
                        is ShortFlags -> ""
                    }
                    flagParser.flagSpec.flags
                }
                is State.Argument -> state.parser.parseReader(reader)
                State.Unreached -> "Raise an error"
            }
        }

        override fun <S : Any?> listSuggestions(
            context: CommandContext<S>?,
            builder: SuggestionsBuilder?
        ): CompletableFuture<Suggestions> {
            return super.listSuggestions(context, builder)
        }
    }
}