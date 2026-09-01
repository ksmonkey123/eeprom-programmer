package ch.awae.eeprom_programmer.cli

import ch.awae.eeprom_programmer.cli.commands.*
import ch.awae.eeprom_programmer.programmer.Programmer
import picocli.CommandLine
import picocli.CommandLine.Command
import picocli.CommandLine.Mixin

@Command(
    name = "eeprom", mixinStandardHelpOptions = true, version = ["1.1.0"],
    sortOptions = false,
    subcommands = [
        EraseCommand::class,
        LockCommand::class,
        UnlockCommand::class,
        DumpCommand::class,
        FlashCommand::class,
    ]
)
class EepromCLI(
    val programmerFactory: () -> Programmer
) {

    @Mixin
    lateinit var options: WriteOptions

    companion object {
        fun initCLI(programmerFactory: () -> Programmer): CommandLine {
            return CommandLine(EepromCLI(programmerFactory)).setCaseInsensitiveEnumValuesAllowed(true)
        }
    }

}