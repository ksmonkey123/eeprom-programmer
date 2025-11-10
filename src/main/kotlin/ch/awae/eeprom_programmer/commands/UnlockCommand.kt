package ch.awae.eeprom_programmer.commands

import ch.awae.eeprom_programmer.*
import picocli.CommandLine.*

@Command(name = "unlock", description = ["remove write protection lock from EEPROM"])
class UnlockCommand : Runnable {

    @ParentCommand
    lateinit var cli: EepromCLI

    override fun run() {
        val programmer = cli.programmerFactory()

        if(cli.options.lock) {
            println("WARNING: lock option is set but will be ignored!")
        }

        print("unlocking chip...")
        programmer.unlockChip()
        println(" ok")

        println("done")
    }

}