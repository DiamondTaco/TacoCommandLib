package io.github.diamondtaco.commands

import io.github.diamondtaco.commands.CommandBuilder.Companion.command
import io.github.diamondtaco.defualt.BooleanParser
import io.github.diamondtaco.lib.Parser
import net.minecraft.server.command.ServerCommandSource

@Api
val fooCommand = command("foo") {
    addToggle("long-a", 'a')
    addArgument("long-b", 'b', BooleanParser())


    executes {
        if (getToggle('a')) {
            println("a toggled")
        }

        val y = getArgument("long-b")
            .getOrElse { return@executes Result.failure(it) }
                as Boolean

        println("argument y was provided, and was $y")

        Result.success("Command executed.")
    }
}

class CommandBuilder private constructor(private val name: String) {
    companion object {
        fun command(name: String, init: CommandBuilder.() -> Unit): Command {
            val bar = CommandBuilder(name)
            bar.init()
            return bar.build()
        }
    }


    private val toggles = mutableSetOf<Toggle>()
    private val args = mutableSetOf<Argument<Parser<*>>>()
    private var executor: ((FlagSet<*>, ServerCommandSource) -> Result<String>)? = null

    @Api
    fun addToggle(long: String) = toggles.add(Toggle(FlagName(long)))

    @Api
    fun addToggle(long: String, short: Char) = toggles.add(Toggle(FlagName(long, short)))

    @Api
    fun addArgument(long: String, parser: Parser<*>) = args.add(Argument(FlagName(long), parser))

    @Api
    fun addArgument(long: String, short: Char, parser: Parser<*>) = args.add(Argument(FlagName(long, short), parser))

    @Api
    fun executes(executor: FlagSet<*>.(ServerCommandSource) -> Result<String>) {
        if (this.executor == null) {
            this.executor = executor
        } else {
            throw Exception("Can't set executor more than once")
        }
    }

    private fun build(): Command = Command(name, FlagSet(toggles, args), executor!!)
}
