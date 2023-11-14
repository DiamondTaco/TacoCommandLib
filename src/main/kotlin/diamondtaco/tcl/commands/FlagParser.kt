package diamondtaco.tcl.commands

import com.google.gson.JsonObject
import com.mojang.brigadier.StringReader
import com.mojang.brigadier.arguments.ArgumentType
import com.mojang.brigadier.context.CommandContext
import com.mojang.brigadier.suggestion.Suggestions
import com.mojang.brigadier.suggestion.SuggestionsBuilder
import net.minecraft.command.CommandException
import net.minecraft.command.CommandRegistryAccess
import net.minecraft.command.argument.serialize.ArgumentSerializer
import net.minecraft.command.argument.serialize.ArgumentSerializer.ArgumentTypeProperties
import net.minecraft.network.PacketByteBuf
import net.minecraft.server.command.ServerCommandSource
import net.minecraft.text.Text
import java.io.ByteArrayInputStream
import java.io.ByteArrayOutputStream
import java.io.ObjectInputStream
import java.io.ObjectOutputStream
import java.io.Serializable
import java.util.concurrent.CompletableFuture


interface Parser<T> : Serializable, ArgumentType<T> {
    fun parseReader(reader: StringReader): T
    fun getCompletions(context: CommandContext<ServerCommandSource>, input: String): List<String>

    override fun parse(reader: StringReader?): T = parseReader(reader!!)

    @Suppress("UNCHECKED_CAST")
    override fun <S : Any?> listSuggestions(
        context: CommandContext<S>?,
        builder: SuggestionsBuilder?
    ): CompletableFuture<Suggestions>? {
        val completions =
            getCompletions(context!! as CommandContext<ServerCommandSource>, builder!!.input.drop(builder.start))
        return completions.fold(builder) { b, s -> b.suggest(s) }.buildFuture()
    }
}

class MarshalSerializer<T> : ArgumentSerializer<Parser<T>, MarshalProperties<T>> {
    override fun writePacket(properties: MarshalProperties<T>?, buf: PacketByteBuf?) {
        val byteStream = ByteArrayOutputStream()
        ObjectOutputStream(byteStream).writeObject(properties!!.parser)
        buf!!.writeByteArray(byteStream.toByteArray())
    }

    @Suppress("UNCHECKED_CAST")
    override fun fromPacket(buf: PacketByteBuf?): MarshalProperties<T> =
        MarshalProperties(ObjectInputStream(ByteArrayInputStream(buf!!.readByteArray())).readObject() as Parser<T>)

    override fun getArgumentTypeProperties(argumentType: Parser<T>?): MarshalProperties<T> =
        MarshalProperties(argumentType!!)

    override fun writeJson(properties: MarshalProperties<T>?, json: JsonObject?) {
        TODO("Not yet implemented")
    }
}

class MarshalProperties<T>(val parser: Parser<T>) : ArgumentTypeProperties<Parser<T>> {
    override fun createType(commandRegistryAccess: CommandRegistryAccess?): Parser<T> = parser

    override fun getSerializer(): MarshalSerializer<T> = MarshalSerializer()
}


data class StackFlags(val flags: Set<Char>)

class FlagParser(private val stacked: Set<Char>) : Parser<StackFlags> {
    override fun parseReader(reader: StringReader): StackFlags {
        val flagRaw = reader!!.readUnquotedString().drop(1)
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










