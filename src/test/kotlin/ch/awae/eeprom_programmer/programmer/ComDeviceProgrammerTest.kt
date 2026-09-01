package ch.awae.eeprom_programmer.programmer

import io.mockk.confirmVerified
import io.mockk.every
import io.mockk.mockk
import io.mockk.verify
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows
import org.junit.jupiter.params.ParameterizedTest
import org.junit.jupiter.params.provider.EnumSource
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


}