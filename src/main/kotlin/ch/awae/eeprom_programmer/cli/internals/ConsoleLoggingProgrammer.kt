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
        out.flush()
        val contents = backer.dumpMemory(type) {
            progress.set(it)
            out.print("\rreading chip $progress")
            out.flush()
            progressCallback(it)
        }
        out.println()
        return contents
    }

    override fun flashChip(type: ChipType, file: BinaryFile, progressCallback: (ProgressReport) -> Unit) {
        val progress = ProgressBar(64)
        out.print("writing chip...")
        out.flush()
        backer.flashChip(type, file) {
            progress.set(it)
            out.print("\rwriting chip $progress")
            out.flush()
            progressCallback(it)
        }
        out.println()
    }

    override fun eraseChip(type: ChipType, progressCallback: (ProgressReport) -> Unit) {
        val progress = ProgressBar(64)
        out.print("erasing chip $progress")
        out.flush()
        backer.eraseChip(type) {
            progress.set(it)
            out.print("\rerasing chip $progress")
            out.flush()
            progressCallback(it)
        }
        out.println()
    }

    override fun lockChip() {
        out.print("locking chip...")
        out.flush()
        backer.lockChip()
        out.println(" ok")
    }

    override fun unlockChip() {
        out.print("unlocking chip...")
        out.flush()
        backer.unlockChip()
        out.println(" ok")
    }

    override fun identifyType(): ChipType {
        out.print("determining chip type...")
        out.flush()
        val type = backer.identifyType()
        out.println(" ${type.title}")
        return type
    }

}

