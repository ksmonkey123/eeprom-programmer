package ch.awae.eeprom_programmer.commands

import ch.awae.eeprom_programmer.*
import picocli.CommandLine.*
import java.io.*
import java.nio.file.*

@Command(name = "flash", description = ["write a binary file to the EEPROM"])
class FlashCommand : Runnable {

    @ParentCommand
    lateinit var cli: EepromCLI

    @Parameters(index = "0", paramLabel = "<file>", description = ["the output file path"])
    lateinit var file: File

    override fun run() {
        if (!file.canRead()) {
            println("ERROR: file ${file.canonicalPath} cannot be read or does not exist!")
            return
        }

        print("reading file ${file.canonicalPath}...")
        val data = Files.readAllBytes(file.toPath())
        println(" ${data.size} bytes")

        val programmer = ConsoleLoggingProgrammer(cli.programmerFactory())

        if (cli.options.unlock) {
            programmer.unlockChip()
        }

        val type = cli.options.sizeSelection?.type() ?: programmer.identifyType()

        require(data.size <= type.size) { "Cannot write to EEPROM. File too large" }

        val writeBuffer = if (data.size % 64 != 0) {
            // we need to pad the "broken" final line
            val startOfFinalLine = (data.size / 64) * 64
            val overflowBytes = data.size % 64

            print("reading data to supply the missing ${64 - overflowBytes} bytes to fill the last page...")
            val page = programmer.readPage(startOfFinalLine)
            println(" ok")

            val buffer = ByteArray(((data.size / 64) * 64) + 64)
            data.copyInto(buffer)
            page.copyInto(buffer, data.size, overflowBytes, 64)
            println("prepared ${buffer.size} bytes to write to chip")
            buffer
        } else {
            data
        }

        programmer.flashChip(type, writeBuffer)

        if (cli.options.lock) {
            programmer.lockChip()
        }

        println("done")
    }
}