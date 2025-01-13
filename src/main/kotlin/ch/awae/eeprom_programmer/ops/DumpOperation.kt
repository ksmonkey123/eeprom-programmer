package ch.awae.eeprom_programmer.ops

import ch.awae.eeprom_programmer.api.*
import org.springframework.stereotype.*
import java.nio.file.*
import kotlin.system.*

@Component
class DumpOperation(
    val programmer: Programmer
) : Operation(
    command = "dump",
    helpText = "dump chip content to file",
    paramLabels = arrayOf("file"),
) {

    override fun execute(args: List<String>) {
        val fileName = args.firstOrNull() ?: run {
            println("error: missing parameter [file]")
            exitProcess(1)
        }

        println("reading chip content...")
        val dump = programmer.dumpMemory()
        println("writing to file... ($fileName)")

        Files.write(Path.of(fileName), dump)
        println("done")
    }

}