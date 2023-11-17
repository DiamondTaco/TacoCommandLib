package io.github.diamondtaco

import com.mojang.brigadier.StringReader


fun StringReader.readWhile(vararg allowed: Char): String {
    val readChars = StringBuilder()
    do {
        readChars.append(read())
    } while (canRead() && peek() in allowed)
    return readChars.toString()
}

