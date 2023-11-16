package diamondtaco.tcl.commands

import com.mojang.datafixers.util.Either
import diamondtaco.tcl.lib.Parser


/**
 * Terminology:
 * - Flags start with a `-` or `--` and are used to specify data.
 * - Toggles don't accept any data, and essentially act as boolean Arguments.
 * - Arguments accept data as an input.
 */
class FlagMap<T> private constructor(
    private val shortToggleSet: Map<Char, String>,
    private val longToggleSet: Map<String, Char?>,
    private val shortArgumentSet: Map<Char, Pair<String, T>>,
    private val longArgumentSet: Map<String, Pair<Char?, T>>,
) {
    companion object {
        fun fromArguments(arguments: Set<Argument<*>>): FlagMap<Parser<*>> {
            val (toggles, args) = arguments.partition { it.type == null }

            val longToggles = toggles.associate { it.name.long to it.name.short }
            val shortToggles = toggles.filterNot { it.name.short == null }.associate { it.name.short!! to it.name.long }

            val longArgs = args.associate { it.name.long to Pair(it.name.short, it.type!!) }
            val shortArgs = args.filterNot { it.name.short == null }.associate { it.name.short!! to Pair(it.name.long, it.type!!) }

            return FlagMap(shortToggles, longToggles, shortArgs, longArgs)
        }
    }

    fun getFlag(long: String): Pair<ArgumentName, T?>? {
        return when (long) {
            in longToggleSet -> ArgumentName(long, longToggleSet[long]) to null
            in longArgumentSet -> ArgumentName(long, longArgumentSet[long]!!.first) to longArgumentSet[long]!!.second
            else -> null
        }
    }

    fun getFlag(short: Char): Pair<ArgumentName, T?>? {
        return when (short) {
            in shortToggleSet -> ArgumentName(shortToggleSet[short]!!, short) to null
            in shortArgumentSet -> ArgumentName(shortArgumentSet[short]!!.first, short) to shortArgumentSet[short]!!.second
            else -> null
        }
    }
}
