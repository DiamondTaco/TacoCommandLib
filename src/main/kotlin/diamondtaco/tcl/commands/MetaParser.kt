package diamondtaco.tcl.commands

class MetaParser {

}
//data class WrappedObject(val obj: Any?, val clazz: Class<*>)
//
//
//data class ParsedCommand()
//
//class Command(
//    val name: String,
//    val arguments: List<Argument<*>>,
//    val onExecuteCommand: (List<ParsedArgument<*>>) -> Result<Unit>
//) {
//    private val parserAmount = arguments.flatMap {
//        listOf(
//            1,
//            if (it.name.short == null) 0 else 1,
//            if (it.type == null) 0 else 1
//        )
//    }.sum()
//
//    private val parsers = (0..parserAmount).map { MetaParser(it) }
//    private val states = parsers.map { State.Unreached }.toMutableList<State>()
//    private val parsed = parsers.map<_, ParsedArgument<*>?> { null }
//
//    private sealed interface State {
//        data class Flag(val argumentSet: ArgumentSet) : State
//        data class Object(val parser: Parser<*>) : State
//        data object Unreached : State
//    }
//
//
//    fun toBrigadierCommand(): CommandNode<ServerCommandSource> {
//        val root = literal(name).build()
//
//        val nodes = parsers.map { parser ->
//            argument(parser.idx.toString(), parser).executes {
//                val outList = mutableListOf<ParsedArgument<*>>()
//
//                for ((idx, state) in states.take(parser.idx).withIndex()) {
//                    when (state) {
//                        is State.Flag -> outList.add(parsed[idx]!!)
//                        is State.Object -> outList.add(parsed[idx]!!)
//                        State.Unreached -> break
//                    }
//                }
//
//
//                onExecuteCommand(outList)
//                1
//            }
//        }
//
//
//        return x
//    }
//
//    val flagParser = arguments.partition { it.type == null }
//        .run { FlagParser(ArgumentSet(first.map { it.name }.toSet(), second.map { it.name }.toSet())) }
//
//    // Brigadier does a full command reparse every time an earlier argument is changed, so we are safe to do this.
//    private inner class MetaParser(val idx: Int) : ArgumentType<Any?> {
//        private fun parseFlag(state: State.Flag, reader: StringReader): ParsedArgGroup {
//            flagParser.flagSpec = state.argumentSet
//            val flag = flagParser.parseReader(reader)
//            when (flag) {
//                is ShortFlags -> states[idx + 1] = State.Flag(state.argumentSet.without(flag.flags))
//
//                is LongFlag -> states[idx + 1] = State.Flag(state.argumentSet.without(flag.flag))
//
//                is LongArg -> {
//                    states[idx + 1] = State.Object(arguments.first { it.name.long == flag.arg }.type!!)
//                    states[idx + 2] = State.Flag(state.argumentSet.without(flag.arg))
//                }
//
//                is ShortFlagArg -> {
//                    states[idx + 1] = State.Object(arguments.first { it.name.short == flag.arg }.type!!)
//                    states[idx + 2] = State.Flag(state.argumentSet.without(flag.flags))
//                }
//            }
//            return flag
//        }
//
//
//        override fun parse(reader: StringReader?): Any? {
//            reader!!
//
//            val state = states[idx]
//            if (state is State.Flag) {
//                flagParser.flagSpec = state.argumentSet
//                val pag = flagParser.parseReader(reader)
//                when (pag) {
//                    is LongFlag -> states[idx + 1] = State.Flag(state.argumentSet.without(pag.flag))
//                    is ShortFlags -> states[idx + 1] = State.Flag(state.argumentSet.without(pag.flags))
//                    is LongArg -> {
//                        states[idx + 1] = State.Object(arguments.first { it.name.long == pag.arg }.type!!)
//                        states[idx + 2] = State.Flag(state.argumentSet.without(pag.arg))
//                    }
//
//                    is ShortFlagArg -> {
//                        states[idx + 1] = State.Object(arguments.first { it.name.short == pag.arg }.type!!)
//                        states[idx + 2] = State.Flag(state.argumentSet.without(pag.flags))
//                    }
//                }
//
//
//            } else if (state is State.Object) {
//                return null
//            }
//
//
//
//
//
//
//
//
//
//
//
//
//            return when (val state = states[idx]) {
//                is State.Flag -> parseFlag(state, reader)
//                is State.Object -> state.parser.parseReader(reader)
//                State.Unreached -> null
//            }
//        }
//
//        override fun <S : Any?> listSuggestions(
//            context: CommandContext<S>?,
//            builder: SuggestionsBuilder?
//        ): CompletableFuture<Suggestions> {
//            return super.listSuggestions(context, builder)
//        }
//    }
//}age diamondtaco.tcl.commands
//
//import com.mojang.brigadier.StringReader
//import com.mojang.brigadier.context.CommandContext
//import diamondtaco.tcl.lib.Parser
//import net.minecraft.server.command.ServerCommandSource