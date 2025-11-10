package ch.awae.eeprom_programmer.commands

import ch.awae.eeprom_programmer.*
import ch.awae.eeprom_programmer.api.*
import picocli.CommandLine.*
import java.io.*

@Command(name = "dump", description = ["read the entire EEPROM and write the contents to disk"])
class DumpCommand : Runnable {

    @ParentCommand
    lateinit var cli: EepromCLI

    @Parameters(index = "0", paramLabel = "<file>", description = ["the output file path"])
    lateinit var file: File

    override fun run() {
        val programmer = cli.programmerFactory()

        var type = cli.options.sizeSelection?.type()

        val assumedType = type ?: ChipType.AT28C256

        val progress = SteppedProgressBar(64, assumedType.size / 64)
        print("reading chip $progress")
        val contents = programmer.dumpMemory(assumedType) {
            progress.step()
            print("\rreading chip $progress")
        }
        println()

        val output = if (type == null) {
            postProcessContents(contents)
        } else {
            contents
        }
        print("read ${output.size} bytes of data")

        TODO()
    }

    fun postProcessContents(buffer: ByteArray): ByteArray {
        require(buffer.size == 32768)
        val buffer0 = buffer.sliceArray(0..8191)
        val buffer1 = buffer.sliceArray(8192..16383)
        val buffer2 = buffer.sliceArray(16384..24575)
        val buffer3 = buffer.sliceArray(24576..32767)

        if (buffer0.contentEquals(buffer1) && buffer0.contentEquals(buffer2) && buffer0.contentEquals(buffer3)) {
            // full 8k match
            return buffer0
        }

        if (buffer0.contentEquals(buffer2) && buffer1.contentEquals(buffer3)) {
            // 16k match
            return buffer.sliceArray(0..16383)
        }

        // no match
        return buffer
    }
}