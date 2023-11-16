package diamondtaco.tcl.commands

import com.mojang.brigadier.StringReader
import com.mojang.brigadier.context.CommandContext
import com.mojang.brigadier.tree.CommandNode
import diamondtaco.tcl.lib.Parser
import net.minecraft.server.command.CommandManager.argument
import net.minecraft.server.command.CommandManager.literal
import net.minecraft.server.command.ServerCommandSource

class Command(
    val name: String,
    val inputFlagSet: FlagSet<Parser<*>>,
    val callback: (FlagSet<*>) -> Result<String>,
) {
    private val parserAmount = inputFlagSet.toggles.size + inputFlagSet.args.size * 2
    private val states = (0..parserAmount).map { null }.toMutableList<State?>()

    private val outputFlagSet = FlagSet<Any>(mutableSetOf(), mutableSetOf())

    private interface State {
        data class Flag(val flagSet: FlagSet<Parser<*>>) : State
        data class Object<T>(val argId: FlagName, val value: T) : State
    }

    fun toBrigadierNode(): CommandNode<ServerCommandSource> {
        val root = literal(name).build()

        val subNodes = generateSequence(MetaParser(null) to 0) { (parser, idx) -> MetaParser(parser) to idx + 1 }
            .take(parserAmount)
            .map { (l, _) -> l }
            .toMutableList()
            .asReversed()
            .apply { first().state = State.Flag(inputFlagSet) }
            .asSequence()
            .mapIndexed { idx, parser -> argument("Pos$idx", parser) }
            .onEach { it.executes { if (callback(outputFlagSet).isFailure) 1 else 0 } }
            .map { it.build() }
            .apply { zipWithNext().onEach { (l, r) -> l.addChild(r) } }
            .toList()

        val subroot = argument("0", MetaParser(0)).executes {
            if (callback(outputFlagSet).isFailure) 1 else 0
        }.build()



        TODO()
    }

    private inner class MetaParser(val next: MetaParser?) : Parser<Unit> {
        lateinit var state: State
        override fun parseReader(reader: StringReader) {
            val state = state // freeze state so kotlin accepts the smart cast

            if (state is State.Flag) {
                val flagParser = FlagParser(state.flagSet)
                when (val flag = flagParser.parseReader(reader)) {
                    is ParsedFlag.LongToggle -> {
                        next?.state = State.Flag(state.flagSet.without(flag.toggle))

                        outputFlagSet.toggles.add(Toggle(inputFlagSet.getFlagId(flag.toggle)!!))
                    }

                    is ParsedFlag.ShortToggles -> {
                        next?.state = State.Flag(state.flagSet.without(*flag.toggles.toCharArray()))

                        outputFlagSet.toggles.addAll(flag.toggles.map { Toggle(inputFlagSet.getFlagId(it)!!) })
                    }

                    is ParsedFlag.LongArg -> {
                        next?.state = State.Object(
                            outputFlagSet.getFlagId(flag.arg)!!,
                            inputFlagSet.getArgument(flag.arg).getOrThrow()
                        )

                        next?.next?.state = State.Flag(state.flagSet.without(flag.arg))
                    }

                    is ParsedFlag.ShortTogglesArg -> {
                        next?.state = State.Object(
                            outputFlagSet.getFlagId(flag.arg)!!,
                            inputFlagSet.getArgument(flag.arg).getOrThrow()
                        )

                        next?.next?.state = State.Flag(state.flagSet.without(*flag.toggles.toCharArray(), flag.arg))

                        outputFlagSet.toggles.addAll(flag.toggles.map { Toggle(inputFlagSet.getFlagId(it)!!) })
                    }
                }
            } else if (state is State.Object<*> && state.value is Parser<*>) {

                val obj = state.value.parseReader(reader)!!
                outputFlagSet.args.add(Argument(inputFlagSet.getFlagId(state.argId.long)!!, obj))
            } else {
                throw Exception("balls happened ??????")
            }
        }

        override fun getCompletions(context: CommandContext<ServerCommandSource>, input: String): List<String> {
            return emptyList()
        }
    }
}