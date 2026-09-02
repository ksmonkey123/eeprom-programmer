package ch.awae.eeprom_programmer.cli.internals


import ch.awae.binfiles.BinaryFile
import ch.awae.eeprom_programmer.programmer.ChipType
import ch.awae.eeprom_programmer.programmer.Programmer
import ch.awae.eeprom_programmer.programmer.ProgressReport
import java.io.PrintWriter

class ConsoleLoggingProgrammer(val backer: Programmer, val out: PrintWriter = PrintWriter(System.out)) : Programmer {

    override fun dumpMemory(type: ChipType, progressCallback: (ProgressReport) -> Unit): Result<ByteArray> {
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
        out.flush()
        return contents
    }

    override fun flashChip(type: ChipType, file: BinaryFile, progressCallback: (ProgressReport) -> Unit): Result<Unit> {
        val progress = ProgressBar(64)
        out.print("writing chip...")
        out.flush()
        return backer.flashChip(type, file) {
            progress.set(it)
            out.print("\rwriting chip $progress")
            out.flush()
            progressCallback(it)
        }.also {
            out.println()
            out.flush()
        }
    }

    override fun eraseChip(type: ChipType, progressCallback: (ProgressReport) -> Unit): Result<Unit> {
        val progress = ProgressBar(64)
        out.print("erasing chip $progress")
        out.flush()
        return backer.eraseChip(type) {
            progress.set(it)
            out.print("\rerasing chip $progress")
            out.flush()
            progressCallback(it)
        }.also {
            out.println()
            out.flush()
        }
    }

    override fun lockChip(): Result<Unit> {
        out.print("locking chip...")
        out.flush()
        return backer.lockChip().also {
            it.onSuccess {
                out.println(" ok")
                out.flush()
            }
            it.onFailure {
                out.println(" error")
                out.flush()
            }
        }
    }

    override fun unlockChip(): Result<Unit> {
        out.print("unlocking chip...")
        out.flush()
        return backer.unlockChip().also {
            it.onSuccess {
                out.println(" ok")
                out.flush()
            }
            it.onFailure {
                out.println(" error")
                out.flush()
            }
        }
    }

    override fun identifyType(): Result<ChipType> {
        out.print("determining chip type...")
        out.flush()
        return backer.identifyType().also {
            it.onSuccess {
                out.println(" ${it.title}")
                out.flush()
            }
            it.onFailure {
                out.println(" error")
                out.flush()
            }
        }
    }

}

