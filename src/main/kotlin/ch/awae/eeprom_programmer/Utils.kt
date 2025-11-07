package ch.awae.eeprom_programmer

fun Int.hex(padding: Int = 2) = toString(16).padStart(padding, '0').uppercase()