package ch.awae.eeprom_programmer.cli.internals

import ch.awae.binfiles.BinaryFile
import ch.awae.eeprom_programmer.programmer.ChipType
import ch.awae.eeprom_programmer.programmer.Programmer
import ch.awae.eeprom_programmer.programmer.ProgressReport
import io.mockk.confirmVerified
import io.mockk.every
import io.mockk.mockk
import io.mockk.verify
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.junit.jupiter.params.ParameterizedTest
import org.junit.jupiter.params.provider.EnumSource
import kotlin.random.Random
import kotlin.test.assertContentEquals

class ConsoleLoggingProgrammerTest {

    val backer = mockk<Programmer>()
    val subject = ConsoleLoggingProgrammer(backer)

    @BeforeEach
    fun setup() {
        every { backer.lockChip() } returns Result.success(Unit)
        every { backer.unlockChip() } returns Result.success(Unit)
        every { backer.identifyType() } returns Result.success(ChipType.AT28C64B)
    }

    @Test
    fun `lockChip passthrough`() {
        subject.lockChip()
        verify(exactly = 1) { backer.lockChip() }
        confirmVerified(backer)
    }

    @Test
    fun `unlockChip passthrough`() {
        subject.unlockChip()
        verify(exactly = 1) { backer.unlockChip() }
        confirmVerified(backer)
    }

    @ParameterizedTest
    @EnumSource(ChipType::class)
    fun `identifyType passthrough`(type: ChipType) {
        every { backer.identifyType() } returns Result.success(type)
        val result = subject.identifyType()
        assertEquals(type, result.getOrThrow())
        verify(exactly = 1) { backer.identifyType() }
        confirmVerified(backer)
    }

    @ParameterizedTest
    @EnumSource(ChipType::class)
    fun `eraseChip passthrough and callback`(type: ChipType) {
        every { backer.eraseChip(type, any()) } answers {
            @Suppress("UNCHECKED_CAST") val callback = (this.args[1] as (ProgressReport) -> Unit)
            repeat(type.size / 64) {
                callback(ProgressReport(it + 1, type.size / 64))
            }
            Result.success(Unit)
        }

        var count = 0
        subject.eraseChip(type) { count++ }
        assertEquals(type.size / 64, count)

        verify(exactly = 1) { backer.eraseChip(type, any()) }
        confirmVerified(backer)
    }

    @ParameterizedTest
    @EnumSource(ChipType::class)
    fun `dumpMemory passthrough and callback`(type: ChipType) {
        val data = Random.nextBytes(type.size)
        every { backer.dumpMemory(type, any()) } answers {
            @Suppress("UNCHECKED_CAST") val callback = (this.args[1] as (ProgressReport) -> Unit)
            repeat(type.size / 64) {
                callback(ProgressReport(it + 1, type.size / 64))
            }
            Result.success(data)
        }

        var count = 0
        val result = subject.dumpMemory(type) { count++ }
        assertEquals(type.size / 64, count)
        assertContentEquals(data, result.getOrThrow())

        verify(exactly = 1) { backer.dumpMemory(type, any()) }
        confirmVerified(backer)
    }

    @ParameterizedTest
    @EnumSource(ChipType::class)
    fun `flashChip passthrough and callback`(type: ChipType) {
        val file = BinaryFile()

        every { backer.flashChip(type, file, any()) } answers {
            @Suppress("UNCHECKED_CAST") val callback = (this.args[2] as (ProgressReport) -> Unit)
            repeat(100) { i ->
                callback(ProgressReport(i + 1, 100))
            }
            Result.success(Unit)
        }

        var count = 0
        subject.flashChip(type, file) { count++ }
        assertEquals(100, count)

        verify(exactly = 1) { backer.flashChip(type, file, any()) }
        confirmVerified(backer)
    }

}