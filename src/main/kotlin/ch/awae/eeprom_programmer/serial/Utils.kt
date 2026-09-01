package ch.awae.eeprom_programmer.serial

fun Int.hex(width: Int): String {
    val sb = StringBuilder(width)

    var remaining = this
    for (i in 0 until width) {
        sb.append(nibbleHexLookup(remaining and 0xF))
        remaining = remaining shr 4
    }

    return sb.toString()
}

fun Byte.hex(): String = toInt().hex(2)

private fun nibbleHexLookup(nibble: Int): Char {
    return "0123456789ABCDEF"[nibble]
}