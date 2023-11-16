package diamondtaco.tcl.commands

data class FlagSet<T>(val toggles: Set<Toggle>, val args: Set<Argument<T>>) {
    fun getToggle(long: String): Boolean = toggles.firstOrNull { it.id.long == long } != null
    fun getToggle(short: Char): Boolean = toggles.firstOrNull { it.id.short == short } != null
    fun getArgument(long: String): Result<T> = args.firstOrNull { it.id.long == long }?.let { Result.success(it.value) }
        ?: Result.failure(Exception("Couldn't find argument $long, or it wasn't supplied."))

    fun getArgument(short: Char): Result<T> = args.firstOrNull { it.id.short == short }?.let { Result.success(it.value) }
        ?: Result.failure(Exception("Couldn't find argument $short, or it wasn't supplied."))

    fun getFlagId(long: String): FlagName? = (toggles.map { it.id } + args.map { it.id }).firstOrNull { it.long == long }
    fun getFlagId(short: Char): FlagName? = (toggles.map { it.id } + args.map { it.id }).firstOrNull { it.short == short }

    fun without(long: String): FlagSet<T> = FlagSet(
        toggles.filter { it.id.long != long }.toMutableSet(),
        args.filter { it.id.long != long }.toMutableSet(),
    )

    fun without(vararg shorts: Char): FlagSet<T> = FlagSet(
        toggles.filter { it.id.short?.run { this !in shorts } ?: true }.toMutableSet(),
        args.filter { it.id.short?.run { this !in shorts } ?: true }.toMutableSet(),
    )
}