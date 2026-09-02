package ch.awae.eeprom_programmer

import java.io.PrintWriter

fun Int.hex(width: Int): String {
    val sb = StringBuilder(width)
    for (i in 0 until width) {
        val slice = this.shr(4 * (width - i - 1))
        sb.append(nibbleHexLookup(slice and 0xf))
    }
    return sb.toString()
}

fun Byte.hex(): String = toInt().hex(2)

private fun nibbleHexLookup(nibble: Int): Char {
    return "0123456789ABCDEF"[nibble]
}

fun <T> Result<T>.unwrapLogged(out: PrintWriter) : T {
    if (this.isFailure) {
        out.println()
        out.println("=================")
        out.println("${this.exceptionOrNull()?.message}")
        out.println("=================")
        out.flush()
    }
    return this.getOrThrow()
}