package ch.awae.eeprom_programmer.api

enum class ChipType(val size: Int) {
    AT28C64B(8 * 1024),
    AT28C256(32 * 1024),
}