package ch.awae.eeprom_programmer.cli.internals


import ch.awae.eeprom_programmer.backend.api.*

class ConsoleLoggingProgrammer(val backer: Programmer) : Programmer {

    private class SteppedProgressBar(val width: Int, val limit: Int) {
        private var state = 0

        fun step() {
            state++
        }

        override fun toString(): String {
            val filled = (state * width) / limit
            val empty = width - filled
            return "[" + "|".repeat(filled) + " ".repeat(empty) + "] $state/$limit"
        }

    }

    override fun readByte(address: Int): UByte {
        return backer.readByte(address)
    }

    override fun writeByte(address: Int, data: UByte) {
        backer.writeByte(address, data)
    }

    override fun readPage(address: Int): ByteArray {
        return backer.readPage(address)
    }

    override fun writePage(address: Int, data: ByteArray, startFrom: Int) {
        backer.writePage(address, data, startFrom)
    }

    override fun dumpMemory(type: ChipType, progressCallback: () -> Unit): ByteArray {
        val progress = SteppedProgressBar(64, type.size / 64)
        print("reading chip $progress")
        val contents = backer.dumpMemory(type) {
            progress.step()
            print("\rreading chip $progress")
            progressCallback()
        }
        println()
        return contents
    }

    override fun flashChip(type: ChipType, data: ByteArray, progressCallback: () -> Unit) {
        val progress = SteppedProgressBar(64, data.size / 64)
        print("writing chip $progress")
        backer.flashChip(type, data) {
            progress.step()
            print("\rwriting chip $progress")
            progressCallback()
        }
        println()
    }

    override fun eraseChip(type: ChipType, progressCallback: () -> Unit) {
        val progress = SteppedProgressBar(64, type.size / 64)
        print("erasing chip $progress")
        backer.eraseChip(type) {
            progress.step()
            print("\rerasing chip $progress")
            progressCallback()
        }
        println()
    }

    override fun lockChip() {
        print("locking chip...")
        backer.lockChip()
        println(" ok")
    }

    override fun unlockChip() {
        print("unlocking chip...")
        backer.unlockChip()
        println(" ok")
    }

    override fun identifyType(): ChipType {
        print("determining chip type...")
        val type = backer.identifyType()
        println(" $type")
        return type
    }

    override fun rawCommand(command: String): String? {
        return backer.rawCommand(command)
    }

}