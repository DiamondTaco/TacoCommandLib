package diamondtaco.tcl.commands

import com.mojang.brigadier.context.CommandContext

@Suppress("UNCHECKED_CAST")
fun CommandContext<*>.toMutableFlagSet(parseAmount: Int): FlagSet<Any> {
    val toggles = mutableSetOf<Toggle>()
    val arguments = mutableSetOf<Argument<Any>>()

    run {
        for (i in 0..parseAmount) {
            val gotArg = runCatching {
                getArgument("Arg$i", Any::class.java)
            }.getOrElse { return@run }

            if (gotArg is Set<*>) {
                toggles.addAll(gotArg.filterIsInstance<Toggle>())
            } else if (gotArg is Argument<*>) {
                arguments.add(gotArg as Argument<Any>)
            }
        }
    }

    return FlagSet(toggles, arguments)
}
