package ch.awae.eeprom_programmer.programmer

import ch.awae.binfiles.BinaryFile
import ch.awae.eeprom_programmer.hex
import io.mockk.confirmVerified
import io.mockk.every
import io.mockk.mockk
import io.mockk.verify
import org.junit.jupiter.api.Test
import org.junit.jupiter.params.ParameterizedTest
import org.junit.jupiter.params.provider.EnumSource
import kotlin.random.Random
import kotlin.test.assertContentEquals
import kotlin.test.assertEquals
import kotlin.test.assertIs
import kotlin.test.assertTrue

class ComDeviceProgrammerTest {

    @Test
    fun `locking should simply send l`() {
        val device = mockk<ComDevice>()
        every { device.sendCommand("l") } returns Result.success(null)

        val programmer = ComDeviceProgrammer(device)

        programmer.lockChip()

        verify(exactly = 1) { device.sendCommand("l") }
        confirmVerified(device)
    }

    @Test
    fun `unlocking should simply send u`() {
        val device = mockk<ComDevice>()
        every { device.sendCommand("u") } returns Result.success(null)

        val programmer = ComDeviceProgrammer(device)

        programmer.unlockChip()

        verify(exactly = 1) { device.sendCommand("u") }
        confirmVerified(device)
    }

    @ParameterizedTest
    @EnumSource(ChipType::class)
    fun `type identification works`(type: ChipType) {
        val device = mockk<ComDevice>()
        every { device.sendCommand("i") } returns Result.success(type.internalIdentifier)

        val programmer = ComDeviceProgrammer(device)

        val result = programmer.identifyType()
        assertEquals(type, result.getOrThrow())

        verify(exactly = 1) { device.sendCommand("i") }
        confirmVerified(device)
    }

    @Test
    fun `type identification fails for invalid response`() {
        val device = mockk<ComDevice>()
        every { device.sendCommand("i") } returns Result.success("XX")

        val programmer = ComDeviceProgrammer(device)

        val result = programmer.identifyType()

        assertTrue(result.isFailure)
        assertIs<IllegalStateException>(result.exceptionOrNull())

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
            require(cmd.startsWith("r"))
            val address = cmd.substring(1).toInt(16)
            val slice = expectedData.sliceArray(address until address + 64)
            val sb = StringBuilder()
            slice.forEach { sb.append(it.hex()) }
            Result.success(sb.toString())
        }

        val programmer = ComDeviceProgrammer(device)
        val result = programmer.dumpMemory(type)
        assertEquals(type.size, result.getOrThrow().size)
        assertContentEquals(expectedData, result.getOrThrow())
    }

    @ParameterizedTest
    @EnumSource(ChipType::class)
    fun `dump memory, invalid response`(type: ChipType) {
        val device = mockk<ComDevice>()
        every { device.sendCommand(any()) } returns Result.success(null)

        val programmer = ComDeviceProgrammer(device)
        val result = programmer.dumpMemory(type)
        assertTrue(result.isFailure)
        assertIs<IllegalStateException>(result.exceptionOrNull())
    }

    @ParameterizedTest
    @EnumSource(ChipType::class)
    fun `erase chip, every byte is set to 0xff`(type: ChipType) {
        val device = mockk<ComDevice>()
        val capture = mutableListOf<String>()
        every { device.sendCommand(capture(capture)) } returns Result.success(null)
        val programmer = ComDeviceProgrammer(device)
        programmer.eraseChip(type)

        val expected = (0..<type.size / 64).map { "w${(it * 64).hex(4)}:" + "FF".repeat(64) }

        assertEquals(expected, capture)
    }

    @Test
    fun `write data fragments`() {
        val device = mockk<ComDevice>()
        val capture = mutableListOf<String>()
        every { device.sendCommand(capture(capture)) } returns Result.success(null)
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
            "w0000:FFFEFDFCFBFAF9F8F7F6F5F4F3F2F1F0EFEEEDECEBEAE9E8E7E6E5E4E3E2E1E0DFDEDDDCDBDAD9D8D7D6D5D4D3D2D1D0CFCECDCCCBCAC9C8C7C6C5C4C3C2C1C0",
            "w0040:0102............................................................................................................................",
            "w0040:..............................................................................................................................03",
        )

        assertEquals(expected, capture)
    }

    @Test
    fun `write too large`() {
        val device = mockk<ComDevice>()
        val programmer = ComDeviceProgrammer(device)

        val file = BinaryFile()
        file.addByte(0x2000, 0x01.toByte())

        val result = programmer.flashChip(ChipType.AT28C64B, file)
        assertTrue(result.isFailure)
        assertIs<IllegalArgumentException>(result.exceptionOrNull())
    }

    @Test
    fun `write large file to large device`() {
        val device = mockk<ComDevice>()
        every { device.sendCommand(any()) } returns Result.success(null)
        val programmer = ComDeviceProgrammer(device)

        val file = BinaryFile()
        file.addByte(0x2000, 0x01.toByte())

        programmer.flashChip(ChipType.AT28C256, file)

        verify(exactly = 1) { device.sendCommand("w2000:01..............................................................................................................................") }
        confirmVerified(device)
    }

}