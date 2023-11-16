package diamondtaco.tcl.commands

import diamondtaco.tcl.commands.CommandBuilder.Companion.command
import diamondtaco.tcl.defualt.BooleanParser
import diamondtaco.tcl.lib.Parser


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
    private var executor: ((FlagSet<*>) -> Result<String>)? = null

    fun addToggle(long: String) = toggles.add(Toggle(FlagName(long)))
    fun addToggle(long: String, short: Char) = toggles.add(Toggle(FlagName(long, short)))

    fun addArgument(long: String, parser: Parser<*>) = args.add(Argument(FlagName(long), parser))
    fun addArgument(long: String, short: Char, parser: Parser<*>) = args.add(Argument(FlagName(long, short), parser))

    fun executes(executor: FlagSet<*>.() -> Result<String>) {
        if (this.executor == null) {
            this.executor = executor
        } else {
            throw Exception("Can't set executor more than once")
        }
    }

    private fun build(): Command = runCatching {
        Command(name, FlagSet(toggles, args), executor!!)
    }.getOrThrow()
}
