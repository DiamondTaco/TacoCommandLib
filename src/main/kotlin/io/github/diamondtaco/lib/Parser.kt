package io.github.diamondtaco.lib

import com.mojang.brigadier.StringReader
import com.mojang.brigadier.arguments.ArgumentType
import com.mojang.brigadier.context.CommandContext
import com.mojang.brigadier.context.StringRange
import com.mojang.brigadier.suggestion.Suggestion
import com.mojang.brigadier.suggestion.Suggestions
import com.mojang.brigadier.suggestion.SuggestionsBuilder
import net.minecraft.server.command.ServerCommandSource
import java.io.Serializable
import java.util.concurrent.CompletableFuture

fun <T> ArgumentType<T>.toParser(): Parser<T> = let {
    object : Parser<T> {
        override fun parseReader(reader: StringReader): T = it.parse(reader)

        override fun getCompletions(context: CommandContext<ServerCommandSource>, input: String): List<String> {
            return it.listSuggestions(context, SuggestionsBuilder(input, 0))!!.get().list.map { it.text }
        }
    }
}


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

        val range = StringRange(builder.start, builder.input.length)

        return CompletableFuture.completedFuture(Suggestions(range, completions.map { Suggestion(range, it) }))

//        return completions.fold(builder) { b, s -> b.suggest(s) }.buildFuture()
    }
}