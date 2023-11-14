package diamondtaco.tcl.commands

import com.mojang.brigadier.StringReader
import com.mojang.brigadier.context.CommandContext
import net.minecraft.server.command.ServerCommandSource

class BlockParser : Parser<String> {
    private val strings = setOf(
        "minecraft:dirt_block",
        "minecraft:grass_block",
        "minecraft:firework_rocket",
        "minecraft:black_wool",
        "minecraft:black_concrete",
        "minecraft:diamond_sword",
        "minecraft:diamond_axe",
    )


    private val matcher = FuzzyMatcher(strings)

    override fun parseReader(reader: StringReader): String {
        return reader.readUnquotedString().takeIf { it in strings }!!
    }

    override fun getCompletions(context: CommandContext<ServerCommandSource>, input: String): List<String> {
        return matcher.getMatches(input)
    }
}