package ch.awae.eeprom_programmer.cli.commands

import ch.awae.eeprom_programmer.cli.EepromCLI
import ch.awae.eeprom_programmer.cli.internals.ConsoleLoggingProgrammer
import ch.awae.eeprom_programmer.unwrapLogged
import picocli.CommandLine.*
import picocli.CommandLine.Model.CommandSpec

@Command(name = "erase", description = ["write 0xff into the entire EEPROM, effectively erasing it"])
class EraseCommand : Runnable {

    @Spec
    lateinit var spec: CommandSpec

    @ParentCommand
    lateinit var cli: EepromCLI

    override fun run() {
        val out = spec.commandLine().out
        val programmer = ConsoleLoggingProgrammer(cli.programmerFactory(out), out)

        var type = cli.options.sizeSelection?.type()

        if (cli.options.unlock) {
            programmer.unlockChip().unwrapLogged(out)
        }

        if (type == null) {
            type = programmer.identifyType().unwrapLogged(out)
        }

        programmer.eraseChip(type).unwrapLogged(out)

        if (cli.options.lock) {
            programmer.lockChip().unwrapLogged(out)
        }

        out.println("done")
        out.flush()
    }

}