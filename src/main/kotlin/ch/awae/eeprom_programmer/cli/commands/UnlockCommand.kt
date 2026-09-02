package ch.awae.eeprom_programmer.cli.commands

import ch.awae.eeprom_programmer.cli.EepromCLI
import ch.awae.eeprom_programmer.cli.internals.ConsoleLoggingProgrammer
import picocli.CommandLine.*
import picocli.CommandLine.Model.CommandSpec

@Command(name = "unlock", description = ["remove write protection lock from EEPROM"])
class UnlockCommand : Runnable {

    @Spec
    lateinit var spec: CommandSpec

    @ParentCommand
    lateinit var cli: EepromCLI

    override fun run() {
        val out = spec.commandLine().out
        val programmer = ConsoleLoggingProgrammer(cli.programmerFactory(), out)

        if (cli.options.lock) {
            out.println("WARNING: lock option is set but will be ignored!")
        }

        programmer.unlockChip()

        out.println("done")
    }

}