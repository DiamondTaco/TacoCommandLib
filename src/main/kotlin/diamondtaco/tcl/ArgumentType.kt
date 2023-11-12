package diamondtaco.tcl

import java.io.StringReader

interface ArgumentType<T> {
    fun readInput(input: StringReader): Either<T, List<String>>
}