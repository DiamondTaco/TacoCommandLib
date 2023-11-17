package diamondtaco.tcl.defualt

import com.mojang.brigadier.StringReader
import com.mojang.brigadier.context.CommandContext
import diamondtaco.tcl.lib.FuzzyMatcher
import diamondtaco.tcl.lib.Parser
import diamondtaco.tcl.readWhile
import net.minecraft.command.CommandException
import net.minecraft.item.Item
import net.minecraft.registry.Registries
import net.minecraft.server.command.ServerCommandSource
import net.minecraft.text.Text
import net.minecraft.util.Identifier

class ItemParser : Parser<Item> {
    private val strings = Registries.ITEM.map { "minecraft:$it" }.toSet()
    private val matcher = FuzzyMatcher(strings)

    override fun parseReader(reader: StringReader): Item {
        val itemName = reader.readWhile(*(('a'..'z').toList() + ':' + '_').toCharArray())

        if (itemName !in strings) throw CommandException(Text.literal("Couldn't find item $itemName."))

        return Registries.ITEM.get(Identifier(itemName))
    }

    override fun getCompletions(context: CommandContext<ServerCommandSource>, input: String): List<String> {
        return matcher.getMatches(input)
    }
}