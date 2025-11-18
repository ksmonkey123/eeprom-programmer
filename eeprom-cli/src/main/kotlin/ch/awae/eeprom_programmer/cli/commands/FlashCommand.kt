package ch.awae.eeprom_programmer.cli.commands

import ch.awae.binfiles.hex.*
import ch.awae.eeprom_programmer.cli.*
import ch.awae.eeprom_programmer.cli.internals.*
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
        val file = HexFileReader(Files.newInputStream(file.toPath())).use { reader ->
            reader.read()!!
        }

        println(" ${file.currentSize} bytes")

        val programmer = ConsoleLoggingProgrammer(cli.programmerFactory())

        if (cli.options.unlock) {
            programmer.unlockChip()
        }

        val type = cli.options.sizeSelection?.type() ?: programmer.identifyType()

        require(file.currentSize <= type.size) { "Cannot write to EEPROM. File too large" }

        programmer.flashChip(type, file)

        if (cli.options.lock) {
            programmer.lockChip()
        }

        println("done")
    }
}