package ch.awae.eeprom_programmer.cli

import ch.awae.eeprom_programmer.backend.api.*
import picocli.CommandLine.*

class WriteOptions {

    @Option(names = ["-u", "--unlock"], description = ["unlock EEPROM at the start of the operation"], order = 4)
    var unlock: Boolean = false

    @Option(names = ["-l", "--lock"], description = ["lock EEPROM at end of the operation"], order = 5)
    var lock: Boolean = false

    @ArgGroup(exclusive = true, multiplicity = "0..1")
    var sizeSelection: SizeSelection? = null

    class SizeSelection {

        enum class Size(val type: ChipType) {
            SMALL(ChipType.AT28C64B), LARGE(ChipType.AT28C256),
            NARROW(ChipType.AT28C64B), WIDE(ChipType.AT28C256),
        }

        @Option(
            names = ["-n", "--narrow"],
            description = ["treat the connected eeprom as narrow (8kB)"],
            order = 1
        )
        var narrow = false

        @Option(
            names = ["-w", "--wide"], description = ["treat the connected eeprom as wide (32kB)"],
            order = 2
        )
        var wide = false

        @Option(
            names = ["-s", "--size"],
            description = ["explicit size. Valid values: \${COMPLETION-CANDIDATES}"],
            order = 3,
        )
        var size: Size? = null

        fun type(): ChipType? {
            return size?.type
                ?: if (narrow) {
                    ChipType.AT28C64B
                } else if (wide) {
                    ChipType.AT28C256
                } else {
                    null
                }

        }
    }

}