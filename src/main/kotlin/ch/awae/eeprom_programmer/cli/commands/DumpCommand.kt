package ch.awae.eeprom_programmer.cli.commands

import ch.awae.binfiles.BinaryFile
import ch.awae.binfiles.hex.HexFileWriter
import ch.awae.eeprom_programmer.cli.EepromCLI
import ch.awae.eeprom_programmer.cli.internals.ConsoleLoggingProgrammer
import ch.awae.eeprom_programmer.programmer.ChipType
import picocli.CommandLine.*
import picocli.CommandLine.Model.CommandSpec
import java.io.File
import java.nio.file.Files

@Command(name = "dump", description = ["read the entire EEPROM and write the contents to disk"])
class DumpCommand : Runnable {

    @Spec
    lateinit var spec: CommandSpec

    @ParentCommand
    lateinit var cli: EepromCLI

    @Parameters(index = "0", paramLabel = "<file>", description = ["the output file path"])
    lateinit var file: File

    override fun run() {
        val out = spec.commandLine().out
        val programmer = ConsoleLoggingProgrammer(cli.programmerFactory(), out)

        if (cli.options.unlock) {
            programmer.unlockChip()
        }

        val type = cli.options.sizeSelection?.type()
        val assumedType = type ?: ChipType.AT28C256

        val contents = programmer.dumpMemory(assumedType)


        out.print("post-processing data...")
        out.flush()
        val output = if (type == null) {
            postProcessContents(contents)
        } else {
            contents
        }

        val file = BinaryFile()
        output.forEachIndexed { index, value ->
            file.addByte(index, value)
        }
        out.println(" ok")

        out.print("writing to ${this.file.canonicalPath}...")
        out.flush()
        HexFileWriter(Files.newOutputStream(this.file.toPath())).use {
            it.write(file)
        }
        out.println(" ok\ndone")
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