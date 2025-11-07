package ch.awae.eeprom_programmer.commands

import ch.awae.eeprom_programmer.*
import picocli.CommandLine.*

@Command(name = "erase", description = ["write 0xff into the entire eeprom, effectively erasing it"])
class EraseCommand : Runnable {

    @ParentCommand
    lateinit var cli: EepromCLI

    override fun run() {
        val programmer = cli.programmerFactory()

        val type = cli.sizeSelection.size().defaultType

        if (cli.unlock) {
            print("unlocking chip...")
            programmer.unlockChip()
            println(" ok")
        }


        val progress = SteppedProgressBar(64, type.size / 64)
        print("erasing chip $progress")
        programmer.eraseChip(type) {
            progress.step()
            print("\rerasing chip $progress")
        }
        println()

        if (cli.lock) {
            print("locking chip...")
            programmer.lockChip()
            println(" ok")
        }

        println("done")
    }

}