package ch.awae.eeprom_programmer.cli.commands

import ch.awae.eeprom_programmer.cli.*
import ch.awae.eeprom_programmer.cli.internals.*
import picocli.CommandLine.*

@Command(name = "lock", description = ["apply write protection lock to EEPROM"])
class LockCommand : Runnable {

    @ParentCommand
    lateinit var cli: EepromCLI

    override fun run() {
        val programmer = ConsoleLoggingProgrammer(cli.programmerFactory())

        programmer.lockChip()

        println("done")
    }

}