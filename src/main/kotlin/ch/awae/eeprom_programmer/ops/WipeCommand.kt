package ch.awae.eeprom_programmer.ops

import ch.awae.eeprom_programmer.api.*
import org.springframework.stereotype.*
import kotlin.system.*

@Component
class WipeCommand(
    val programmer: Programmer
) : Operation(
    command = "wipe",
    helpText = "erases entire chip. all bytes are overwritten with 0xff"
) {

    override fun execute(args: List<String>) {
        println("erasing chip...")
        try {
            programmer.eraseChip()
        } catch (e: Exception) {
            println("ERROR: ${e.message}")
            exitProcess(1)
        }
        println("done")
    }

}