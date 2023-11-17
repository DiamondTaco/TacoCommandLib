package io.github.diamondtaco.lib

import com.google.gson.JsonObject
import net.minecraft.command.CommandRegistryAccess
import net.minecraft.command.argument.serialize.ArgumentSerializer
import net.minecraft.network.PacketByteBuf
import java.io.ByteArrayInputStream
import java.io.ByteArrayOutputStream
import java.io.ObjectInputStream
import java.io.ObjectOutputStream

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

class MarshalProperties<T>(val parser: Parser<T>) : ArgumentSerializer.ArgumentTypeProperties<Parser<T>> {
    override fun createType(commandRegistryAccess: CommandRegistryAccess?): Parser<T> = parser

    override fun getSerializer(): MarshalSerializer<T> = MarshalSerializer()
}
