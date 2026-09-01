package ch.awae.eeprom_programmer.programmer

enum class ChipType(val size: Int, val title: String, val internalIdentifier: String) {
    AT28C64B(8 * 1024, "AT28C64B (Socket)", "SS"),
    AT28C256(32 * 1024, "AT28C256 (Socket)", "LS"),
    ;

    override fun toString(): String = title
}