package ch.awae.eeprom_programmer

import ch.awae.eeprom_programmer.api.*
import ch.awae.eeprom_programmer.com.*
import picocli.*
import kotlin.system.*

fun createProgrammer(): Programmer = ComPortProgrammer(JscComDevice.findAndConnect())

fun main(args: Array<String>) {
    exitProcess(CommandLine(EepromCLI(::createProgrammer)).execute(*args))
}