package ch.awae.eeprom_programmer.programmer

import ch.awae.binfiles.BinaryFile
import ch.awae.binfiles.DataFragment
import ch.awae.eeprom_programmer.hex
import java.util.*

class ComDeviceProgrammer(private val comDevice: ComDevice) : Programmer {

    private fun readPage(address: Int): ByteArray {
        require(address % 64 == 0) { "address must be start of a page" }

        val result = comDevice.sendCommand("r${address.hex(4)}").getOrThrow()
            ?: error("read command expects a response")

        return result.chunked(2)
            .map { it.toInt(16).toByte() }
            .toByteArray()
    }

    override fun dumpMemory(type: ChipType, progressCallback: (ProgressReport) -> Unit): Result<ByteArray> {
        return runCatching {
            val dump = ByteArray(type.size)

            for (page in (0..<type.size).step(64)) {
                val pageContent = readPage(page)
                pageContent.copyInto(dump, destinationOffset = page)
                progressCallback(ProgressReport(page / 64 + 1, type.size / 64))
            }

            dump
        }
    }

    override fun flashChip(type: ChipType, file: BinaryFile, progressCallback: (ProgressReport) -> Unit): Result<Unit> {
        return runCatching {
            require(file.currentSize <= type.size) { "file size (${file.currentSize} bytes) exceeds chip capacity (${type.size} bytes)" }

            val fragments = file.fragments(64).toList()
            fragments.forEachIndexed { i, fragment ->
                writeFragment(fragment)
                progressCallback(ProgressReport(i + 1, fragments.size))
            }
        }
    }

    private fun writeFragment(fragment: DataFragment) {
        val address = fragment.position and 0xffc0
        val offset = fragment.position and 0x003f
        val endPadding = 64 - (fragment.length + offset)

        val sb = StringBuilder("w${address.hex(4)}:")

        sb.append("..".repeat(offset))
        sb.append(HexFormat.of().withUpperCase().formatHex(fragment.data))
        sb.append("..".repeat(endPadding))

        comDevice.sendCommand(sb.toString())
    }

    override fun eraseChip(type: ChipType, progressCallback: (ProgressReport) -> Unit): Result<Unit> {
        return flashChip(type, BinaryFile(ByteArray(type.size) { -1 }), progressCallback)
    }

    override fun lockChip(): Result<Unit> {
        return comDevice.sendCommand("l").map { }
    }

    override fun unlockChip(): Result<Unit> {
        return comDevice.sendCommand("u").map { }
    }

    override fun identifyType(): Result<ChipType> {
        return comDevice.sendCommand("i").mapCatching { identifier ->
            ChipType.entries.find { it.internalIdentifier == identifier }
                ?: error("bad test response: $identifier")
        }
    }

}