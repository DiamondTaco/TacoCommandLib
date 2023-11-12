package diamondtaco.tcl.commands

import com.mojang.brigadier.StringReader
import com.mojang.brigadier.context.CommandContext
import diamondtaco.tcl.Either
import kotlin.Pair

class BooleanArgumentType : ArgumentType<Boolean, Unit> {
    override fun readInput(input: StringReader): Boolean? {
        return when (input.readStringUntil(' ').lowercase()) {
            "true" -> true
            "false" -> false
            else -> return null
        }
    }

    override fun getCompletions(input: String, context: CommandContext<Unit>): List<String> {
        return listOf("true", "false")
    }
}