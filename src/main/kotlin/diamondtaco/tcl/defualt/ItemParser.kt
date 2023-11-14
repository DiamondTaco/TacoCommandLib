package diamondtaco.tcl.defualt

import com.mojang.brigadier.StringReader
import com.mojang.brigadier.context.CommandContext
import diamondtaco.tcl.lib.FuzzyMatcher
import diamondtaco.tcl.lib.Parser
import diamondtaco.tcl.readUntil
import net.minecraft.registry.Registries
import net.minecraft.server.command.ServerCommandSource

class ItemParser : Parser<String> {
//    private val strings = setOf(
//        "minecraft:dirt_block",
//        "minecraft:grass_block",
//        "minecraft:firework_rocket",
//        "minecraft:black_wool",
//        "minecraft:black_concrete",
//        "minecraft:diamond_sword",
//        "minecraft:diamond_axe",
//    )

//    private val strings = Registries.BLOCK.map { "minecraft:${it.asItem()}" }.toSet()
    private val strings = Registries.ITEM.map { "minecraft:$it" }.toSet()


    private val matcher = FuzzyMatcher(strings)

    override fun parseReader(reader: StringReader): String {
        return reader.readUntil(' ').takeIf { it in strings }!!
    }

    override fun getCompletions(context: CommandContext<ServerCommandSource>, input: String): List<String> {
        val x = matcher.getMatches(input)
//        val x = listOf("zeta", "alpha")
//        println(x)
        return x
    }
}