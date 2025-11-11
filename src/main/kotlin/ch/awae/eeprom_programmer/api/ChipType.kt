package ch.awae.eeprom_programmer.api

enum class ChipType(val size: Int, val title: String) {
    AT28C64B(8 * 1024, "AT28C64B (Socket)"),
    AT28C256(32 * 1024, "AT28C256 (Socket)"),
    ;

    override fun toString(): String = title
}