package ch.awae.eeprom_programmer.cli.commands

import ch.awae.eeprom_programmer.cli.EepromCLI
import ch.awae.eeprom_programmer.cli.internals.ConsoleLoggingProgrammer
import picocli.CommandLine.Command
import picocli.CommandLine.ParentCommand

@Command(name = "unlock", description = ["remove write protection lock from EEPROM"])
class UnlockCommand : Runnable {

    @ParentCommand
    lateinit var cli: EepromCLI

    override fun run() {
        val programmer = ConsoleLoggingProgrammer(cli.programmerFactory())

        if (cli.options.lock) {
            print("WARNING: lock option is set but will be ignored!\n")
        }

        programmer.unlockChip()

        print("done\n")
    }

}