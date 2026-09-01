package ch.awae.eeprom_programmer.cli.internals


import ch.awae.binfiles.BinaryFile
import ch.awae.eeprom_programmer.programmer.ChipType
import ch.awae.eeprom_programmer.programmer.Programmer
import ch.awae.eeprom_programmer.programmer.ProgressReport

class ConsoleLoggingProgrammer(val backer: Programmer) : Programmer {

    override fun dumpMemory(type: ChipType, progressCallback: (ProgressReport) -> Unit): ByteArray {
        val progress = ProgressBar(64)
        print("reading chip $progress")
        val contents = backer.dumpMemory(type) {
            progress.set(it)
            print("\rreading chip $progress")
            progressCallback(it)
        }
        println()
        return contents
    }

    override fun flashChip(type: ChipType, file: BinaryFile, progressCallback: (ProgressReport) -> Unit) {
        val progress = ProgressBar(64)
        print("writing chip...")
        backer.flashChip(type, file) {
            progress.set(it)
            print("\rwriting chip $progress")
            progressCallback(it)
        }
        println()
    }

    override fun eraseChip(type: ChipType, progressCallback: (ProgressReport) -> Unit) {
        val progress = ProgressBar(64)
        print("erasing chip $progress")
        backer.eraseChip(type) {
            progress.set(it)
            print("\rerasing chip $progress")
            progressCallback(it)
        }
        println()
    }

    override fun lockChip() {
        print("locking chip...")
        backer.lockChip()
        print(" ok\n")
    }

    override fun unlockChip() {
        print("unlocking chip...")
        backer.unlockChip()
        print(" ok\n")
    }

    override fun identifyType(): ChipType {
        print("determining chip type...")
        val type = backer.identifyType()
        print(" ok\n")
        return type
    }

}

