package diamondtaco.tcl.commands

import com.mojang.brigadier.StringReader
import com.mojang.brigadier.context.CommandContext
import com.mojang.brigadier.tree.CommandNode
import diamondtaco.tcl.lib.MarshalSerializer
import diamondtaco.tcl.lib.Parser
import net.fabricmc.fabric.api.command.v2.ArgumentTypeRegistry
import net.minecraft.server.command.CommandManager.argument
import net.minecraft.server.command.CommandManager.literal
import net.minecraft.server.command.ServerCommandSource
import net.minecraft.text.Style
import net.minecraft.text.Text
import net.minecraft.util.Formatting
import net.minecraft.util.Identifier

class Command(
    val name: String,
    val inputFlagSet: FlagSet<Parser<*>>,
    val callback: (FlagSet<*>, ServerCommandSource) -> Result<String>,
) {
    companion object {
        init {
            ArgumentTypeRegistry.registerArgumentType(
                Identifier("tcl", "tcl_internal_type"),
                MetaParser::class.java,
                MarshalSerializer()
            )
        }
    }

    private val parserAmount = inputFlagSet.toggles.size + inputFlagSet.args.size * 2

    private interface State {
        data class Flag(val flagSet: FlagSet<Parser<*>>) : State
        data class Object<T>(val argId: FlagName, val value: T) : State
    }

    fun toBrigadierNode(): CommandNode<ServerCommandSource> {
        val root = literal(name).executes { context ->
            val output = callback(FlagSet<Nothing>(emptySet(), emptySet()), context.source)

            context.source.sendMessage(output.fold(
                { success -> Text.literal(success) },
                { failure ->
                    Text.literal(failure.localizedMessage).setStyle(Style.EMPTY.withColor(Formatting.RED))
                }
            ))

            if (output.isSuccess) 1 else -1
        }.build()

        val subNodes = generateSequence(MetaParser(null) to 0) { (parser, idx) -> MetaParser(parser) to idx + 1 }
            .take(parserAmount)
            .map { (l, _) -> l }
            .toMutableList()
            .asReversed()
            .apply { first().state = State.Flag(inputFlagSet) }
            .asSequence()
            .mapIndexed { idx, parser -> argument("Arg$idx", parser) }
            .onEach {
                it.executes { context ->
                    val output = callback(context.toMutableFlagSet(parserAmount), context.source)

                    output.fold(
                        { success -> context.source.sendMessage(Text.literal(success)) },
                        { failure -> context.source.sendError(Text.literal(failure.localizedMessage)) }
                    )

                    if (output.isSuccess) 1 else 0
                }
            }
            .map { it.build() }
            .toList()

        root.addChild(subNodes.first())

        subNodes.zipWithNext().forEach { (l, r) -> l.addChild(r) }

        return root
    }

    private inner class MetaParser(val next: MetaParser?) : Parser<Any> {
        lateinit var state: State
        override fun parseReader(reader: StringReader): Any {
            val state = state // freeze state so kotlin accepts the smart cast

            return if (state is State.Flag) {
                val flagParser = FlagParser(state.flagSet)
                when (val flag = flagParser.parseReader(reader)) {
                    is ParsedFlag.LongToggle -> {
                        next?.state = State.Flag(state.flagSet.without(flag.toggle))
                        setOf(Toggle(inputFlagSet.getFlagId(flag.toggle)!!))
                    }

                    is ParsedFlag.ShortToggles -> {
                        next?.state = State.Flag(state.flagSet.without(*flag.toggles.toCharArray()))
                        flag.toggles.map { Toggle(inputFlagSet.getFlagId(it)!!) }.toSet()
                    }

                    is ParsedFlag.LongArg -> {
                        next?.state = State.Object(
                            inputFlagSet.getFlagId(flag.arg)!!,
                            inputFlagSet.getArgument(flag.arg).getOrThrow()
                        )
                        next?.next?.state = State.Flag(state.flagSet.without(flag.arg))
                    }

                    is ParsedFlag.ShortTogglesArg -> {
                        next?.state = State.Object(
                            inputFlagSet.getFlagId(flag.arg)!!,
                            inputFlagSet.getArgument(flag.arg).getOrThrow()
                        )
                        next?.next?.state = State.Flag(state.flagSet.without(*flag.toggles.toCharArray(), flag.arg))
                        flag.toggles.map { Toggle(inputFlagSet.getFlagId(it)!!) }.toSet()
                    }
                }
            } else if (state is State.Object<*> && state.value is Parser<*>) {
                val obj = state.value.parseReader(reader)!!
                Argument(inputFlagSet.getFlagId(state.argId.long)!!, obj)
            } else {
                throw Exception("balls happened ??????")
            }
        }

        override fun getCompletions(context: CommandContext<ServerCommandSource>, input: String): List<String> {
            return when (val state = state) {
                is State.Flag -> FlagParser(state.flagSet).getCompletions(context, input)
                is State.Object<*> -> (state.value as? Parser<*>)?.getCompletions(context, input) ?: emptyList()
                else -> emptyList()
            }
        }
    }
}