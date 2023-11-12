package diamondtaco.tcl.commands

import com.mojang.brigadier.StringReader
import com.mojang.brigadier.context.CommandContext
import diamondtaco.tcl.Either

interface ArgumentType<T, C> {
    fun readInput(input: StringReader): T?
    fun getCompletions(input: String, context: CommandContext<C>): List<String>
}