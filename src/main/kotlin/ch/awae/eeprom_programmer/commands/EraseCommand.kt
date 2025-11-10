package ch.awae.eeprom_programmer.commands

import ch.awae.eeprom_programmer.*
import picocli.CommandLine.*

@Command(name = "erase", description = ["write 0xff into the entire EEPROM, effectively erasing it"])
class EraseCommand : Runnable {

    @ParentCommand
    lateinit var cli: EepromCLI

    override fun run() {
        val programmer = cli.programmerFactory()

        var type = cli.options.sizeSelection?.type()

        if (cli.options.unlock) {
            print("unlocking chip...")
            programmer.unlockChip()
            println(" ok")
        }

        if (type == null) {
            print("determining chip size...")
            type = programmer.sizeTest()
            println(" $type")
        }

        val progress = SteppedProgressBar(64, type.size / 64)
        print("erasing chip $progress")
        programmer.eraseChip(type) {
            progress.step()
            print("\rerasing chip $progress")
        }
        println()

        if (cli.options.lock) {
            print("locking chip...")
            programmer.lockChip()
            println(" ok")
        }

        println("done")
    }

}