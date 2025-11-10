package ch.awae.eeprom_programmer.commands

import ch.awae.eeprom_programmer.*
import picocli.CommandLine.*

@Command(name = "lock", description = ["apply write protection lock to EEPROM"])
class LockCommand : Runnable {

    @ParentCommand
    lateinit var cli: EepromCLI

    override fun run() {
        val programmer = cli.programmerFactory()

        print("locking chip...")
        programmer.lockChip()
        println(" ok")

        println("done")
    }

}