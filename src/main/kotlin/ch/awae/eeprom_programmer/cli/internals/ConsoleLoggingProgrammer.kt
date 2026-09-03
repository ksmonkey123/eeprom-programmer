package ch.awae.eeprom_programmer.cli.internals


import ch.awae.binfiles.BinaryFile
import ch.awae.eeprom_programmer.toFractionalSeconds
import ch.awae.eeprom_programmer.programmer.ChipType
import ch.awae.eeprom_programmer.programmer.Programmer
import ch.awae.eeprom_programmer.programmer.ProgressReport
import java.io.PrintWriter
import kotlin.time.measureTimedValue

class ConsoleLoggingProgrammer(val backer: Programmer, val out: PrintWriter = PrintWriter(System.out)) : Programmer {

    override fun dumpMemory(type: ChipType, progressCallback: (ProgressReport) -> Unit): Result<ByteArray> {
        return runWithProgressReport("reading chip", progressCallback) { backer.dumpMemory(type, it) }
    }

    override fun flashChip(type: ChipType, file: BinaryFile, progressCallback: (ProgressReport) -> Unit): Result<Unit> {
        return runWithProgressReport("writing chip", progressCallback) { backer.flashChip(type, file, it) }
    }

    override fun eraseChip(type: ChipType, progressCallback: (ProgressReport) -> Unit): Result<Unit> {
        return runWithProgressReport("erasing chip", progressCallback) { backer.eraseChip(type, it) }
    }

    override fun lockChip(): Result<Unit> {
        return runWithSimpleReport("locking chip", backer::lockChip)
    }

    override fun unlockChip(): Result<Unit> {
        return runWithSimpleReport("unlocking chip", backer::unlockChip)
    }

    override fun identifyType(): Result<ChipType> {
        return runWithSimpleReport("identifying chip type", backer::identifyType, ChipType::title)
    }

    private fun <T> runWithProgressReport(
        operationTitle: String,
        progressCallback: (ProgressReport) -> Unit,
        implementation: ((ProgressReport) -> Unit) -> Result<T>,
    ): Result<T> {
        val progress = ProgressBar(64)
        out.print("$operationTitle... $progress")
        out.flush()
        val (result, timing) = measureTimedValue {
            implementation {
                progress.set(it)
                out.print("\r$operationTitle $progress")
                out.flush()
                progressCallback(it)
            }
        }
        result.onSuccess {
            out.println(" (" + timing.toFractionalSeconds() + ") ok")
            out.flush()
        }
        result.onFailure {
            out.println(" (" + timing.toFractionalSeconds() + ") error")
        }
        return result
    }

    private fun <T> runWithSimpleReport(
        operationTitle: String,
        implementation: () -> Result<T>,
        resultMapper: (T) -> String? = { null }
    ): Result<T> {
        out.print("$operationTitle...")
        out.flush()
        val result = implementation()
        result.onSuccess {
            val valueString = resultMapper(result.getOrThrow())

            if (valueString != null) {
                out.println(" $valueString")
            } else {
                out.println(" ok")
            }
        }
        result.onFailure {
            out.println(" error")
        }
        out.flush()
        return result
    }

}

