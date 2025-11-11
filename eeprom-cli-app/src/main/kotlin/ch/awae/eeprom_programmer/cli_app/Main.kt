package ch.awae.eeprom_programmer.cli_app

import ch.awae.eeprom_programmer.backend.api.*
import ch.awae.eeprom_programmer.backend.com.*
import ch.awae.eeprom_programmer.cli.*
import picocli.*
import kotlin.system.*

fun createProgrammer(): Programmer = ComPortProgrammer(JscComDevice.findAndConnect())

fun main(args: Array<String>) {
    exitProcess(CommandLine(EepromCLI(::createProgrammer)).setCaseInsensitiveEnumValuesAllowed(true).execute(*args))
}