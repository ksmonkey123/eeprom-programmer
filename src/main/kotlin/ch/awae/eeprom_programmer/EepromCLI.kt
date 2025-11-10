package ch.awae.eeprom_programmer

import ch.awae.eeprom_programmer.api.*
import ch.awae.eeprom_programmer.commands.*
import picocli.CommandLine.*

@Command(
    name = "eeprom", mixinStandardHelpOptions = true, version = ["1.1.0"],
    sortOptions = false,
    subcommands = [
        EraseCommand::class,
        LockCommand::class,
        UnlockCommand::class,
        DumpCommand::class,
    ]
)
class EepromCLI(
    val programmerFactory: () -> Programmer
) {

    @Mixin
    lateinit var options: WriteOptions

}