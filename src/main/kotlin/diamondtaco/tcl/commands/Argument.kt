package diamondtaco.tcl.commands

import com.mojang.brigadier.StringReader
import com.mojang.brigadier.context.CommandContext
import com.mojang.brigadier.context.StringRange
import com.mojang.brigadier.suggestion.Suggestion
import com.mojang.brigadier.suggestion.Suggestions
import com.mojang.brigadier.suggestion.SuggestionsBuilder
import net.minecraft.command.CommandException
import net.minecraft.text.Text
import java.util.concurrent.CompletableFuture

class Argument<T> private constructor(
    val long: String, val short: Char?,
    val type: ArgumentType<T, Unit>?,
) {
    constructor(long: String) : this(long, null, null)
    constructor(long: String, short: Char) : this(long, short, null)
    constructor(long: String, type: ArgumentType<T, Unit>) : this(long, null, type)
    constructor(long: String, short: Char, type: ArgumentType<T, Unit>) : this(long, short as Char?, type)
}

