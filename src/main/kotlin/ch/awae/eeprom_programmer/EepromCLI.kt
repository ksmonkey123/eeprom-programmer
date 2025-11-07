package ch.awae.eeprom_programmer

import ch.awae.eeprom_programmer.api.*
import ch.awae.eeprom_programmer.commands.*
import picocli.CommandLine.*

enum class SelectedSize(val defaultType: ChipType) {
    WIDE(ChipType.AT28C256), NARROW(ChipType.AT28C64B), AUTO(ChipType.AT28C256)
}

fun EepromCLI.SizeSelection?.size(): SelectedSize {
    if (this == null) {
        return SelectedSize.AUTO
    }
    if (this.wide) {
        return SelectedSize.WIDE
    }
    if (this.narrow) {
        return SelectedSize.NARROW
    }
    return SelectedSize.AUTO
}

@Command(
    name = "eeprom", mixinStandardHelpOptions = true, version = ["1.1.0"],
    subcommands = [
        EraseCommand::class,
    ]
)
class EepromCLI(
    val programmerFactory: () -> Programmer
) {

    @Option(names = ["-u"], description = ["unlock chip before the operation"])
    var unlock = false

    @Option(names = ["-l"], description = ["lock chip at the end of the operation"])
    var lock = false

    @ArgGroup(exclusive = true, multiplicity = "0..1")
    var sizeSelection: SizeSelection? = null;

    class SizeSelection {
        @Option(names = ["-n"], description = ["treat the connected eeprom as narrow (8kB)"])
        var narrow = false

        @Option(names = ["-w"], description = ["treat the connected eeprom as wide (32kB)"])
        var wide = false

        @Option(names = ["-a"], description = ["auto-determine the eeprom width"])
        var auto = false
    }

}