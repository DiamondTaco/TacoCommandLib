package diamondtaco.tcl

import com.mojang.brigadier.StringReader


fun StringReader.readUntil(vararg delimiters: Char): String {
    val readChars = StringBuilder()
    do {
        readChars.append(read())
    } while (canRead() && peek() !in delimiters)
    return readChars.toString()
}

