package ch.awae.eeprom_programmer.commands

import ch.awae.eeprom_programmer.*
import picocli.CommandLine.*

@Command(name = "erase", description = ["write 0xff into the entire EEPROM, effectively erasing it"])
class EraseCommand : Runnable {

    @ParentCommand
    lateinit var cli: EepromCLI

    override fun run() {
        val programmer = ConsoleLoggingProgrammer(cli.programmerFactory())

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

        println("done")
    }

}