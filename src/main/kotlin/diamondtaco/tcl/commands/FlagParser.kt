package diamondtaco.tcl.commands

import com.mojang.brigadier.StringReader
import com.mojang.brigadier.context.CommandContext
import diamondtaco.tcl.lib.Parser
import net.minecraft.command.CommandException
import net.minecraft.server.command.ServerCommandSource
import net.minecraft.text.Text


data class StackFlags(val flags: Set<Char>)

class FlagParser(private val stacked: Set<Char>) : Parser<StackFlags> {
    override fun parseReader(reader: StringReader): StackFlags {
        val flagRaw = reader.readUnquotedString().drop(1)
        val flags = StackFlags(flagRaw.toSortedSet())

        if (flagRaw.length != flags.flags.size) throw CommandException(Text.literal("Can't have repeated flags."))
        if (flags.flags.any { it !in stacked }) throw CommandException(Text.literal("Unrecognized flag."))

        return flags
    }

    override fun getCompletions(context: CommandContext<ServerCommandSource>, input: String): List<String> {
        val typed = input.ifEmpty { "-" }

        return if (!typed.startsWith('-')) emptyList()
        else stacked.filterNot { it in typed }.map { typed + it }
    }
}


//
//class FlagArgumentType(val stacked: Set<Char>) : ArgumentType<StackFlags> {
//    override fun parse(reader: StringReader?): StackFlags {
//        val flagRaw = reader!!.readUnquotedString().drop(1)
//        val flags = StackFlags(flagRaw.toSortedSet())
//
//        if (flagRaw.length != flags.flags.size) throw CommandException(Text.literal("Can't have repeated flags."))
//        if (flags.flags.any { it !in stacked }) throw CommandException(Text.literal("Unrecognized flag."))
//
//        return flags
//    }
//
//    override fun <S : Any?> listSuggestions(
//        context: CommandContext<S>?,
//        builder: SuggestionsBuilder?
//    ): CompletableFuture<Suggestions> {
//        val typed = builder!!.input.drop(builder.start).ifEmpty { "-" }
//        if (!typed.startsWith('-')) return builder.buildFuture()
//
//        return stacked
//            .filterNot { it in typed }
//            .map { typed + it }
//            .fold(builder) { acc, s -> acc.suggest(s) }
//            .buildFuture()
//    }
//}
//
//class FlagProperties(val flags: Set<Char>) : ArgumentTypeProperties<FlagArgumentType> {
//    override fun createType(commandRegistryAccess: CommandRegistryAccess?): FlagArgumentType {
//        return FlagArgumentType(flags)
//    }
//
//    override fun getSerializer(): ArgumentSerializer<FlagArgumentType, *> {
//        return FlagSerializer()
//    }
//}
//
//class FlagSerializer : ArgumentSerializer<FlagArgumentType, FlagProperties> {
//    override fun writePacket(properties: FlagProperties?, buf: PacketByteBuf?) {
//        buf!!.writeBytes(properties!!.flags.map { it.code.toByte() }.toByteArray())
//    }
//
//    override fun fromPacket(buf: PacketByteBuf?): FlagProperties {
//        return FlagProperties(buf!!.readByteArray().map { it.toInt().toChar() }.toSet())
//    }
//
//    override fun getArgumentTypeProperties(argumentType: FlagArgumentType?): FlagProperties {
//        return FlagProperties(argumentType!!.stacked)
//    }
//
//    override fun writeJson(properties: FlagProperties?, json: JsonObject?) {
//        TODO("Not yet implemented")
//    }
//}










