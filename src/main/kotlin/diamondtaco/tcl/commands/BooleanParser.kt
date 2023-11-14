package diamondtaco.tcl.commands

import com.mojang.brigadier.StringReader
import com.mojang.brigadier.context.CommandContext
import net.minecraft.server.command.ServerCommandSource


class BooleanParser : Parser<Boolean> {
    override fun parseReader(reader: StringReader): Boolean {
        return reader.readBoolean()
    }

    override fun getCompletions(context: CommandContext<ServerCommandSource>, input: String): List<String> {
        return listOf("true", "false")
    }
}
