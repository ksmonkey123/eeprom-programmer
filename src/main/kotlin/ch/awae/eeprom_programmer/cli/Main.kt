package ch.awae.eeprom_programmer.cli

import ch.awae.eeprom_programmer.backend.Programmer
import ch.awae.eeprom_programmer.serial.ComPortProgrammer
import ch.awae.eeprom_programmer.serial.JscComDevice
import picocli.*
import kotlin.system.*

fun createProgrammer(): Programmer = ComPortProgrammer(JscComDevice.findAndConnect())

fun main(args: Array<String>) {
    exitProcess(CommandLine(EepromCLI(::createProgrammer)).setCaseInsensitiveEnumValuesAllowed(true).execute(*args))
}