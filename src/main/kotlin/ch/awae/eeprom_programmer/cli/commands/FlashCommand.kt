package ch.awae.eeprom_programmer.cli.commands

import ch.awae.binfiles.hex.HexFileReader
import ch.awae.eeprom_programmer.cli.EepromCLI
import ch.awae.eeprom_programmer.cli.internals.ConsoleLoggingProgrammer
import picocli.CommandLine.*
import picocli.CommandLine.Model.CommandSpec
import java.io.File
import java.nio.file.Files

@Command(name = "flash", description = ["write a binary file to the EEPROM"])
class FlashCommand : Runnable {

    @Spec
    lateinit var spec: CommandSpec

    @ParentCommand
    lateinit var cli: EepromCLI

    @Option(names = ["-e", "--erase"], description = ["erase chip before writing"])
    var erase: Boolean = false

    @Parameters(index = "0", paramLabel = "<file>", description = ["the output file path"])
    lateinit var file: File

    override fun run() {
        val out = spec.commandLine().out
        if (!file.canRead()) {
            out.println("ERROR: file ${file.canonicalPath} cannot be read or does not exist!")
            out.flush()
            return
        }

        out.print("reading file ${file.canonicalPath}...")
        out.flush()
        val file = HexFileReader(Files.newInputStream(file.toPath())).use { reader ->
            reader.read()!!
        }

        out.println(" ${file.currentSize} bytes")
        out.flush()

        val programmer = ConsoleLoggingProgrammer(cli.programmerFactory(out), out)

        if (cli.options.unlock) {
            programmer.unlockChip()
        }

        val type = cli.options.sizeSelection?.type() ?: programmer.identifyType()

        require(file.currentSize <= type.size) { "Cannot write to EEPROM. File too large" }

        if (this.erase) {
            programmer.eraseChip(type)
        }

        programmer.flashChip(type, file)

        if (cli.options.lock) {
            programmer.lockChip()
        }

        out.println("done")
        out.flush()
    }
}