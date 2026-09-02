package ch.awae.eeprom_programmer.cli.commands

import ch.awae.eeprom_programmer.cli.EepromCLI
import ch.awae.eeprom_programmer.cli.internals.ConsoleLoggingProgrammer
import picocli.CommandLine.*
import picocli.CommandLine.Model.CommandSpec

@Command(name = "lock", description = ["apply write protection lock to EEPROM"])
class LockCommand : Runnable {

    @Spec
    lateinit var spec: CommandSpec

    @ParentCommand
    lateinit var cli: EepromCLI

    override fun run() {
        val out = spec.commandLine().out
        val programmer = ConsoleLoggingProgrammer(cli.programmerFactory(), out)

        programmer.lockChip()

        out.println("done")
        out.flush()
    }

}