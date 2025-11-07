package ch.awae.eeprom_programmer.ops

import ch.awae.eeprom_programmer.com.*
import java.io.*

class LiveOperation(
    val comDevice: ComDevice,
) : Operation(
    "live",
    helpText = "live console to the programmer",
) {

    override fun execute(args: List<String>) {
        val input = BufferedReader(InputStreamReader(System.`in`))
        println("enter command:")

        while (true) {
            print("> ")
            val line = input.readLine() ?: break
            try {
                println(comDevice.sendCommand(line) ?: "(ok)")
            } catch (e: Exception) {
                println("ERROR: ${e.message}")
            }
        }
    }

}