package ch.awae.eeprom_programmer.api

import ch.awae.eeprom_programmer.*
import ch.awae.eeprom_programmer.com.*

class ComPortProgrammer(private val comDevice: ComDevice) : Programmer {

    override fun readByte(address: Int): UByte {
        val result = comDevice.sendCommand("r${address.hex(4)}")
            ?: throw NullPointerException("read command expects a response")

        return result.toInt(16).toUByte()
    }

    override fun readLine(address: Int): ByteArray {
        if (address % 64 != 0) throw java.lang.IllegalArgumentException("address must be start of a page")

        val result = comDevice.sendCommand("p${address.hex(4)}")
            ?: throw NullPointerException("read command expects a response")

        return result.chunked(2)
            .map { it.toInt(16).toByte() }
            .toByteArray()
    }

    override fun writeByte(address: Int, data: UByte) {
        comDevice.sendCommand("w${address.hex(4)}:${data.toInt().hex(2)}")
    }

    override fun writePage(address: Int, data: ByteArray, startFrom: Int) {
        if (address % 64 != 0) throw java.lang.IllegalArgumentException("address must be start of a page")
        if (data.size < 64 + startFrom) throw java.lang.IllegalArgumentException("less than 64 bytes provided")

        val sb = StringBuilder("x${address.hex(4)}:")

        for (i in 0..63) {
            sb.append(data[startFrom + i].toUByte().toInt().hex(2))
        }

        comDevice.sendCommand(sb.toString())
    }

    override fun dumpMemory(type: ChipType, progressCallback: () -> Unit): ByteArray {
        val dump = ByteArray(type.size)

        for (page in (0..<type.size).step(64)) {
            val pageContent = readLine(page)
            pageContent.copyInto(dump, destinationOffset = page)
            progressCallback()
        }

        return dump
    }

    override fun flashChip(type: ChipType, data: ByteArray, progressCallback: () -> Unit) {
        if (data.size != type.size) throw java.lang.IllegalArgumentException("bad data size. ${type.size} bytes expected")

        for (i in (0..<type.size).step(64)) {
            writePage(i, data, i)
            progressCallback()
        }
    }

    override fun eraseChip(type: ChipType, progressCallback: () -> Unit) {
        flashChip(type, ByteArray(type.size) { -1 }, progressCallback)
    }

    override fun lockChip() {
        comDevice.sendCommand("l");
    }

    override fun unlockChip() {
        comDevice.sendCommand("u");
    }

    override fun sizeTest(): ChipType {
        return when (val result = comDevice.sendCommand("t")) {
            "S" -> ChipType.AT28C64B
            "L" -> ChipType.AT28C256
            else -> throw IllegalArgumentException("bad test response: $result")
        }
    }

    override fun rawCommand(command: String): String? {
        return comDevice.sendCommand(command)
    }

}