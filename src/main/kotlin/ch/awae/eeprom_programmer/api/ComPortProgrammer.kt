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

    override fun dumpMemory(): ByteArray {
        val dump = ByteArray(8192)

        for (page in 0..127) {
            val startAddress = page * 64
            val pageContent = readLine(startAddress)
            pageContent.copyInto(dump, destinationOffset = startAddress)
        }

        return dump
    }

    override fun flashChip(data: ByteArray) {
        if (data.size < 8192) throw java.lang.IllegalArgumentException("less than 8KiB provided")

        for (i in 0..127) {
            writePage(i * 64, data, i * 64)
        }
    }

    override fun eraseChip() {
        flashChip(ByteArray(8192) { -1 })
    }

}