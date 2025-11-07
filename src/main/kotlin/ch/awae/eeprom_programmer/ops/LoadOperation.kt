package ch.awae.eeprom_programmer.ops

import ch.awae.eeprom_programmer.api.*
import java.nio.file.*
import kotlin.system.*

class LoadOperation(
    val programmer: Programmer
) : Operation(
    command = "load",
    helpText = "write file content to the chip",
    paramLabels = arrayOf("file"),
) {

    override fun execute(args: List<String>) {
        val fileName = args.firstOrNull() ?: run {
            println("error: missing parameter [file]")
            exitProcess(1)
        }

        println("reading file... ($fileName)")
        val data = Files.readAllBytes(Paths.get(fileName))

        if (data.size != 8192) {
            println("error: invalid file content: size must be exactly 8192 bytes. (actual: ${data.size})")
            exitProcess(1)
        }

        println("writing data to chip...")
        programmer.flashChip(ChipType.AT28C64B, data) {}
        println("done")
    }

}
