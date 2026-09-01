package ch.awae.eeprom_programmer.cli.commands

import ch.awae.eeprom_programmer.cli.*
import ch.awae.eeprom_programmer.cli.internals.*
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
        val programmer = ConsoleLoggingProgrammer(cli.programmerFactory(), out)

        var type = cli.options.sizeSelection?.type()

        if (cli.options.unlock) {
            programmer.unlockChip()
        }

        if (type == null) {
            type = programmer.identifyType()
        }

        programmer.eraseChip(type)

        if (cli.options.lock) {
            programmer.lockChip()
        }

        out.print("done\n")
    }

}