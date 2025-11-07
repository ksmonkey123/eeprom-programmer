package ch.awae.eeprom_programmer.ops

import ch.awae.eeprom_programmer.api.*
import org.springframework.stereotype.*
import kotlin.system.*

@Component
class UnlockOperation(
    val programmer: Programmer
) : Operation(
    command = "unlock",
    helpText = "disables software write lock on the chip"
) {

    override fun execute(args: List<String>) {
        println("unlocking chip...")
        try {
            programmer.lockChip()
        } catch (e: Exception) {
            println("ERROR: ${e.message}")
            exitProcess(1)
        }
        println("done")
    }

}