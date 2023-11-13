package diamondtaco.tcl.commands

class Command<T>(val arguments: List<Argument<T>>) {
    fun toBrigadierCommand(): com.mojang.brigadier.Command<Unit> {
        TODO()
    }
}