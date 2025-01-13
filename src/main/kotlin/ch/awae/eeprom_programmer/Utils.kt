package ch.awae.eeprom_programmer

import org.slf4j.*

fun Int.hex(padding: Int = 2) = toString(16).padStart(padding, '0').uppercase()

inline fun <reified T> T.createLogger() : Logger = LoggerFactory.getLogger(T::class.java)