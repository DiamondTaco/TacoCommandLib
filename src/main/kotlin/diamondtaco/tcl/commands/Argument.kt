package diamondtaco.tcl.commands

import com.mojang.brigadier.StringReader
import com.mojang.brigadier.context.CommandContext
import com.mojang.brigadier.context.StringRange
import com.mojang.brigadier.suggestion.Suggestion
import com.mojang.brigadier.suggestion.Suggestions
import com.mojang.brigadier.suggestion.SuggestionsBuilder
import diamondtaco.tcl.lib.Parser
import net.minecraft.command.CommandException
import net.minecraft.text.Text
import java.util.concurrent.CompletableFuture


data class ArgumentName(val long: String, val short: Char? = null)

data class ArgumentSet(val flags: Set<ArgumentName>, val args: Set<ArgumentName>) : Sequence<ArgumentName> {
    constructor(sequence: Sequence<Argument<*>>) : this(
        sequence.filter { it.type == null }.map { it.name }.toSet(),
        sequence.filter { it.type != null }.map { it.name }.toSet(),
    )

    fun without(shorts: Set<Char>) = ArgumentSet(
        flags.filter { it.short !in shorts }.toSet(),
        args.filter { it.short !in shorts }.toSet(),
    )

    fun without(long: String) = ArgumentSet(
        flags.filter { it.long !in long }.toSet(),
        args.filter { it.long !in long }.toSet(),
    )

    override fun iterator(): Iterator<ArgumentName> = (flags + args).iterator()
}


data class Argument<T>(val name: ArgumentName, val type: Parser<T>? = null)
data class ParsedArgument<T>(val name: ArgumentName, val value: T? = null)

sealed interface ParsedArgGroup
data class ShortFlags(val flags: Set<Char>) : ParsedArgGroup
data class ShortFlagArg(val flags: Set<Char>, val arg: Char) : ParsedArgGroup
data class LongFlag(val flag: String) : ParsedArgGroup
data class LongArg(val arg: String) : ParsedArgGroup

