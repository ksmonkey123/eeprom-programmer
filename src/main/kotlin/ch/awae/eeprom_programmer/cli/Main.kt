package ch.awae.eeprom_programmer.cli

import ch.awae.eeprom_programmer.programmer.ComDeviceProgrammer
import ch.awae.eeprom_programmer.programmer.Programmer
import ch.awae.eeprom_programmer.serial.JscSerialAdapter
import java.io.PrintWriter
import kotlin.system.exitProcess

fun createProgrammer(out: PrintWriter): Programmer = ComDeviceProgrammer(JscSerialAdapter.findAndConnect(out))

fun main(args: Array<String>) {
    exitProcess(EepromCLI.initCLI(::createProgrammer).execute(*args))
}