package diamondtaco.tcl


/**
 * [here](https://medium.com/@Robert_Chrzanow/kotlins-missing-type-either-51602db80fda)
 * */
sealed class Either<T, U> {
    class Left<A, B>(val left: A) : Either<A, B>()
    class Right<A, B>(val right: B) : Either<A, B>()
}