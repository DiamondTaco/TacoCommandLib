package diamondtaco.tcl.commands


data class Toggle(val id: FlagName)
data class Argument<T>(val id: FlagName, val value: T)

data class FlagName(val long: String, val short: Char? = null)

sealed interface ParsedFlag {
    data class ShortToggles(val toggles: Set<Char>) : ParsedFlag
    data class ShortTogglesArg(val toggles: Set<Char>, val arg: Char) : ParsedFlag
    data class LongToggle(val toggle: String) : ParsedFlag
    data class LongArg(val arg: String) : ParsedFlag
}

