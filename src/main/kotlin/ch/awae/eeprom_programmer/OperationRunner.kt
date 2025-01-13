package ch.awae.eeprom_programmer

import ch.awae.eeprom_programmer.ops.*
import org.springframework.boot.*
import org.springframework.stereotype.*

@Component
class OperationRunner(
    val operations: List<Operation>,
) : CommandLineRunner {

    override fun run(vararg args: String) {
        val chosenCommand = args.firstOrNull()

        val op = operations.find { it.command == chosenCommand }

        if (op == null) {
            showHelp(chosenCommand)
        } else {
            println("running command $chosenCommand")
            println()
            op.execute(args.drop(1))
        }
    }

    fun showHelp(chosenCommand: String?) {
        println()
        if (chosenCommand != null) {
            println("unknown command: $chosenCommand")
            println()
        }
        println("supported commands:")

        operations.forEach { op ->
            val sb = StringBuilder("  ")
            sb.append(op.command)

            op.paramLabels.forEach {
                sb.append(" [$it]")
            }

            sb.append(" - ${op.helpText}")
            println(sb.toString())
        }
    }

}