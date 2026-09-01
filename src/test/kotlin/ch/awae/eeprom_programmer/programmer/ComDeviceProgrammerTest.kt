package ch.awae.eeprom_programmer.programmer

import ch.awae.binfiles.BinaryFile
import ch.awae.eeprom_programmer.hex
import io.mockk.confirmVerified
import io.mockk.every
import io.mockk.mockk
import io.mockk.verify
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows
import org.junit.jupiter.params.ParameterizedTest
import org.junit.jupiter.params.provider.EnumSource
import kotlin.random.Random
import kotlin.test.assertContentEquals
import kotlin.test.assertEquals

class ComDeviceProgrammerTest {

    @Test
    fun `locking should simply send l`() {
        val device = mockk<ComDevice>()
        every { device.sendCommand("l") } returns null

        val programmer = ComDeviceProgrammer(device)

        programmer.lockChip()

        verify(exactly = 1) { device.sendCommand("l") }
        confirmVerified(device)
    }

    @Test
    fun `unlocking should simply send u`() {
        val device = mockk<ComDevice>()
        every { device.sendCommand("u") } returns null

        val programmer = ComDeviceProgrammer(device)

        programmer.unlockChip()

        verify(exactly = 1) { device.sendCommand("u") }
        confirmVerified(device)
    }

    @ParameterizedTest
    @EnumSource(ChipType::class)
    fun `type identification works`(type: ChipType) {
        val device = mockk<ComDevice>()
        every { device.sendCommand("i") } returns type.internalIdentifier

        val programmer = ComDeviceProgrammer(device)

        val result = programmer.identifyType()
        assertEquals(type, result)

        verify(exactly = 1) { device.sendCommand("i") }
        confirmVerified(device)
    }

    @Test
    fun `type identification fails for invalid response`() {
        val device = mockk<ComDevice>()
        every { device.sendCommand("i") } returns "XX"

        val programmer = ComDeviceProgrammer(device)

        assertThrows<IllegalStateException> { programmer.identifyType() }

        verify(exactly = 1) { device.sendCommand("i") }
        confirmVerified(device)
    }

    @ParameterizedTest
    @EnumSource(ChipType::class)
    fun `dump memory`(type: ChipType) {
        val expectedData = Random.nextBytes(type.size)

        val device = mockk<ComDevice>()
        every { device.sendCommand(any()) } answers {
            val cmd = args[0] as String
            require(cmd.startsWith("p"))
            val address = cmd.substring(1).toInt(16)
            val slice = expectedData.sliceArray(address until address + 64)
            val sb = StringBuilder()
            slice.forEach { sb.append(it.hex()) }
            sb.toString()
        }

        val programmer = ComDeviceProgrammer(device)
        val result = programmer.dumpMemory(type)
        assertEquals(type.size, result.size)
        assertContentEquals(expectedData, result)
    }

    @ParameterizedTest
    @EnumSource(ChipType::class)
    fun `dump memory, invalid response`(type: ChipType) {
        val device = mockk<ComDevice>()
        every { device.sendCommand(any()) } returns null

        val programmer = ComDeviceProgrammer(device)
        assertThrows<IllegalStateException> {
            programmer.dumpMemory(type)
        }
    }

    @ParameterizedTest
    @EnumSource(ChipType::class)
    fun `erase chip, every byte is set to 0xff`(type: ChipType) {
        val device = mockk<ComDevice>()
        val capture = mutableListOf<String>()
        every { device.sendCommand(capture(capture)) } returns null
        val programmer = ComDeviceProgrammer(device)
        programmer.eraseChip(type)

        val expected = (0..<type.size / 64).map { "s${(it * 64).hex(4)}:" + "FF".repeat(64) }

        assertEquals(expected, capture)
    }

    @Test
    fun `write data fragments`() {
        val device = mockk<ComDevice>()
        val capture = mutableListOf<String>()
        every { device.sendCommand(capture(capture)) } returns null
        val programmer = ComDeviceProgrammer(device)

        val file = BinaryFile()

        // full fragment
        repeat(64) { index ->
            file.addByte(index, (255 - index).toByte())
        }

        // partial fragments
        file.addByte(64, 0x01.toByte())
        file.addByte(65, 0x02.toByte())
        file.addByte(127, 0x03.toByte())

        programmer.flashChip(ChipType.AT28C64B, file)

        val expected = listOf(
            "s0000:FFFEFDFCFBFAF9F8F7F6F5F4F3F2F1F0EFEEEDECEBEAE9E8E7E6E5E4E3E2E1E0DFDEDDDCDBDAD9D8D7D6D5D4D3D2D1D0CFCECDCCCBCAC9C8C7C6C5C4C3C2C1C0",
            "s0040:0102............................................................................................................................",
            "s0040:..............................................................................................................................03",
        )

        assertEquals(expected, capture)
    }

    @Test
    fun `write too large`() {
        val device = mockk<ComDevice>()
        val programmer = ComDeviceProgrammer(device)

        val file = BinaryFile()
        file.addByte(0x2000, 0x01.toByte())

        assertThrows<IllegalArgumentException> {
            programmer.flashChip(ChipType.AT28C64B, file)
        }
    }

    @Test
    fun `write large file to large device`() {
        val device = mockk<ComDevice>()
        every { device.sendCommand(any()) } returns null
        val programmer = ComDeviceProgrammer(device)

        val file = BinaryFile()
        file.addByte(0x2000, 0x01.toByte())

        programmer.flashChip(ChipType.AT28C256, file)

        verify(exactly = 1) { device.sendCommand("s2000:01..............................................................................................................................") }
        confirmVerified(device)
    }

}