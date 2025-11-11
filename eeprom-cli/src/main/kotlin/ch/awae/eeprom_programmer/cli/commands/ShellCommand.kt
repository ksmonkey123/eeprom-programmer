package ch.awae.eeprom_programmer.cli.commands

import ch.awae.eeprom_programmer.cli.*
import picocli.CommandLine.*
import java.io.*

@Command(name = "shell", description = ["create a live shell for direct interaction with the programmer"])
class ShellCommand : Runnable {

    @ParentCommand
    lateinit var cli: EepromCLI

    override fun run() {
        val input = BufferedReader(InputStreamReader(System.`in`))
        val programmer = cli.programmerFactory()

        println("created shell. Press <Ctrl+D> to exit")

        while (true) {
            print("> ")
            val line = input.readLine() ?: break
            try {
                println(programmer.rawCommand(line) ?: "(ok)")
            } catch (e: Exception) {
                println("ERROR: ${e.message}")
            }
        }
    }

}