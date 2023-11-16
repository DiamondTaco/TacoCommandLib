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
    private val states = generateSequence { State.Unreached }.take(parserAmount).toMutableList<State>()

    private sealed interface State {
        data class Flag(val argumentSet: ArgumentSet) : State
        data class Argument(val parser: Parser<*>) : State
        data object Unreached : State
    }


    fun toBrigadierCommand(): CommandNode<ServerCommandSource> {
        val x = argument("", MetaParser(3)).build()
        return x
    }

    val flagParser = arguments.partition { it.type == null }
        .run { FlagParser(ArgumentSet(first.map { it.name }.toSet(), second.map { it.name }.toSet())) }

    // Brigadier does a full command reparse every time an earlier argument is changed, so we are safe to do this.
    inner class MetaParser(private val idx: Int) : ArgumentType<Any> {
        override fun parse(reader: StringReader?): Any {
            reader!!
            return when (val state = states[idx]) {
                is State.Flag -> state.run {
                    flagParser.flagSpec = argumentSet
                    when (val flag = flagParser.parseReader(reader)) {
                        is ShortFlags -> {
                            states[idx + 1] = State.Flag(argumentSet.without(flag.flags))
                        }
                        is LongFlag -> {
                            states[idx + 1] = State.Flag(argumentSet.without(flag.flag))
                        }
                        is LongArg -> {
                            states[idx + 1] = State.Argument(arguments.first { it.name.long == flag.arg }.type!!)
                            states[idx + 2] = State.Flag(argumentSet.without(flag.arg))
                        }
                        is ShortFlagArg -> {
                            states[idx + 1] = State.Argument(arguments.first { it.name.short == flag.arg }.type!!)
                            states[idx + 2] = State.Flag(argumentSet.without(flag.flags))
                        }
                    }
                }
                is State.Argument -> state.parser.parseReader(reader)!!
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