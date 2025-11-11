package ch.awae.eeprom_programmer.cli.commands

import ch.awae.eeprom_programmer.backend.api.*
import ch.awae.eeprom_programmer.cli.*
import ch.awae.eeprom_programmer.cli.internals.*
import picocli.CommandLine.*
import java.io.*
import java.nio.file.*

@Command(name = "dump", description = ["read the entire EEPROM and write the contents to disk"])
class DumpCommand : Runnable {

    @ParentCommand
    lateinit var cli: EepromCLI

    @Parameters(index = "0", paramLabel = "<file>", description = ["the output file path"])
    lateinit var file: File

    override fun run() {
        val programmer = ConsoleLoggingProgrammer(cli.programmerFactory())

        if (cli.options.unlock) {
            programmer.unlockChip()
        }

        val type = cli.options.sizeSelection?.type()
        val assumedType = type ?: ChipType.AT28C256

        val contents = programmer.dumpMemory(assumedType)

        val output = if (type == null) {
            postProcessContents(contents)
        } else {
            contents
        }
        print("writing ${output.size} bytes to ${file.canonicalPath}...")
        Files.write(file.toPath(), output)
        println(" ok")
        println("done")
    }

    private fun postProcessContents(buffer: ByteArray): ByteArray {
        require(buffer.size == 32768)
        val buffer0 = buffer.sliceArray(0..8191)
        val buffer1 = buffer.sliceArray(8192..16383)
        val buffer2 = buffer.sliceArray(16384..24575)
        val buffer3 = buffer.sliceArray(24576..32767)

        if (buffer0.contentEquals(buffer1) && buffer0.contentEquals(buffer2) && buffer0.contentEquals(buffer3)) {
            // full 8k match
            return buffer0
        }

        // no match
        return buffer
    }
}