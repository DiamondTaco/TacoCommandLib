package diamondtaco.tcl.commands

import diamondtaco.tcl.lib.Parser


/**
 * Terminology:
 * - Flags start with a `-` or `--` and are used to specify data.
 * - Toggles don't accept any data, and essentially act as boolean Arguments.
 * - Arguments accept data as an input.
 */
class FlagMap<T> private constructor(
    @Api internal val shortToggleSet: Map<Char, String>,
    @Api internal val longToggleSet: Map<String, Char?>,
    @Api internal val shortArgumentSet: Map<Char, Pair<String, T>>,
    @Api internal val longArgumentSet: Map<String, Pair<Char?, T>>,
) {
    companion object {
        @Api
        fun <T> fromFlagSet(set: FlagSet<T>) = set.run {
            FlagMap(
                toggles.filter { it.id.short != null }.associate { it.id.short!! to it.id.long },
                toggles.associate { it.id.long to it.id.short },
                args.filter { it.id.short != null }.associate { it.id.short!! to Pair(it.id.long, it.value!!) },
                args.associate { it.id.long to Pair(it.id.short, it.value!!) },
            )
        }
    }

    fun getFlag(long: String): Pair<FlagName, T?>? {
        return when (long) {
            in longToggleSet -> FlagName(long, longToggleSet[long]) to null
            in longArgumentSet -> FlagName(long, longArgumentSet[long]!!.first) to longArgumentSet[long]!!.second
            else -> null
        }
    }

    fun getFlag(short: Char): Pair<FlagName, T?>? {
        return when (short) {
            in shortToggleSet -> FlagName(shortToggleSet[short]!!, short) to null
            in shortArgumentSet -> FlagName(shortArgumentSet[short]!!.first, short) to shortArgumentSet[short]!!.second
            else -> null
        }
    }
}
