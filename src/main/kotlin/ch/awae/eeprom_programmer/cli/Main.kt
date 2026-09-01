package ch.awae.eeprom_programmer.cli

import ch.awae.eeprom_programmer.programmer.Programmer
import ch.awae.eeprom_programmer.programmer.ComDeviceProgrammer
import ch.awae.eeprom_programmer.serial.JscComDevice
import picocli.*
import kotlin.system.*

fun createProgrammer(): Programmer = ComDeviceProgrammer(JscComDevice.findAndConnect())

fun main(args: Array<String>) {
    exitProcess(CommandLine(EepromCLI(::createProgrammer)).setCaseInsensitiveEnumValuesAllowed(true).execute(*args))
}