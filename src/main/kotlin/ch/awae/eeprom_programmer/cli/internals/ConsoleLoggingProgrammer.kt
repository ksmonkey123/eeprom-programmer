package ch.awae.eeprom_programmer.cli.internals


import ch.awae.binfiles.BinaryFile
import ch.awae.eeprom_programmer.programmer.ChipType
import ch.awae.eeprom_programmer.programmer.Programmer
import ch.awae.eeprom_programmer.programmer.ProgressReport
import java.io.PrintWriter

class ConsoleLoggingProgrammer(val backer: Programmer, val out: PrintWriter = PrintWriter(System.out)) : Programmer {

    override fun dumpMemory(type: ChipType, progressCallback: (ProgressReport) -> Unit): ByteArray {
        val progress = ProgressBar(64)
        out.print("reading chip $progress")
        val contents = backer.dumpMemory(type) {
            progress.set(it)
            out.print("\rreading chip $progress")
            progressCallback(it)
        }
        out.println()
        return contents
    }

    override fun flashChip(type: ChipType, file: BinaryFile, progressCallback: (ProgressReport) -> Unit) {
        val progress = ProgressBar(64)
        out.print("writing chip...")
        backer.flashChip(type, file) {
            progress.set(it)
            out.print("\rwriting chip $progress")
            progressCallback(it)
        }
        out.println()
    }

    override fun eraseChip(type: ChipType, progressCallback: (ProgressReport) -> Unit) {
        val progress = ProgressBar(64)
        out.print("erasing chip $progress")
        backer.eraseChip(type) {
            progress.set(it)
            out.print("\rerasing chip $progress")
            progressCallback(it)
        }
        out.println()
    }

    override fun lockChip() {
        out.print("locking chip...")
        backer.lockChip()
        out.print(" ok\n")
    }

    override fun unlockChip() {
        out.print("unlocking chip...")
        backer.unlockChip()
        out.print(" ok\n")
    }

    override fun identifyType(): ChipType {
        out.print("determining chip type...")
        val type = backer.identifyType()
        out.print(" ok\n")
        return type
    }

}

