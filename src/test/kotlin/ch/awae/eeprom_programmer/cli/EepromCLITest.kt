package ch.awae.eeprom_programmer.cli

import ch.awae.eeprom_programmer.programmer.ChipType
import ch.awae.eeprom_programmer.programmer.Programmer
import io.mockk.*
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test
import org.junit.jupiter.params.ParameterizedTest
import org.junit.jupiter.params.provider.EnumSource
import picocli.CommandLine
import java.io.PrintWriter
import java.io.StringWriter

class EepromCLITest {

    private lateinit var programmer: Programmer
    private lateinit var cli: CommandLine
    private lateinit var output: StringWriter

    @BeforeEach
    fun setup() {
        output = StringWriter()
        programmer = mockk()
        cli = EepromCLI.initCLI { programmer }
        cli.out = PrintWriter(output)
    }

    @Test
    fun testLock() {
        every { programmer.lockChip() } returns Result.success(Unit)
        cli.execute("lock")

        assertEquals("locking chip... ok\ndone\n", output.toString())
        verify(exactly = 1) { programmer.lockChip() }
        confirmVerified(programmer)
    }

    @Nested
    inner class Unlock {

        @Test
        fun testUnlock() {
            every { programmer.unlockChip() } returns Result.success(Unit)
            cli.execute("unlock")

            assertEquals("unlocking chip... ok\ndone\n", output.toString())
            verify(exactly = 1) { programmer.unlockChip() }
            confirmVerified(programmer)
        }

        @Test
        fun testUnlockWithLockFlag() {
            every { programmer.unlockChip() } returns Result.success(Unit)
            cli.execute("--lock", "unlock")

            assertEquals(
                "WARNING: lock option is set but will be ignored!\nunlocking chip... ok\ndone\n",
                output.toString()
            )
            verify(exactly = 1) { programmer.unlockChip() }
            confirmVerified(programmer)
        }
    }

    @Nested
    inner class Erase {

        @ParameterizedTest
        @EnumSource(ChipType::class)
        fun testEraseWithoutType(type: ChipType) {
            every { programmer.identifyType() } returns Result.success(type)
            every { programmer.eraseChip(type, any()) } returns Result.success(Unit)

            cli.execute("erase")

            verifySequence {
                programmer.identifyType()
                programmer.eraseChip(type, any())
            }
        }

        @ParameterizedTest
        @EnumSource(ChipType::class)
        fun testEraseWithoutTypeWithUnlock(type: ChipType) {
            every { programmer.unlockChip() } returns Result.success(Unit)
            every { programmer.identifyType() } returns Result.success(type)
            every { programmer.eraseChip(type, any()) } returns Result.success(Unit)

            cli.execute("-u", "erase")

            verifySequence {
                programmer.unlockChip()
                programmer.identifyType()
                programmer.eraseChip(type, any())
            }
        }

        @ParameterizedTest
        @EnumSource(ChipType::class)
        fun testEraseWithoutTypeWithLock(type: ChipType) {
            every { programmer.lockChip() } returns Result.success(Unit)
            every { programmer.identifyType() } returns Result.success(type)
            every { programmer.eraseChip(type, any()) } returns Result.success(Unit)

            cli.execute("-l", "erase")

            verifySequence {
                programmer.identifyType()
                programmer.eraseChip(type, any())
                programmer.lockChip()
            }
        }

        @ParameterizedTest
        @EnumSource(ChipType::class)
        fun testEraseWithoutTypeWithUnlockAndLock(type: ChipType) {
            every { programmer.unlockChip() } returns Result.success(Unit)
            every { programmer.lockChip() } returns Result.success(Unit)
            every { programmer.identifyType() } returns Result.success(type)
            every { programmer.eraseChip(type, any()) } returns Result.success(Unit)

            cli.execute("-ul", "erase")

            verifySequence {
                programmer.unlockChip()
                programmer.identifyType()
                programmer.eraseChip(type, any())
                programmer.lockChip()
            }
        }

        @Test
        fun testEraseWithType() {
            every { programmer.eraseChip(ChipType.AT28C256, any()) } returns Result.success(Unit)

            cli.execute("--wide", "erase")

            verifySequence {
                programmer.eraseChip(ChipType.AT28C256, any())
            }
        }

        @Test
        fun testEraseWithTypeWithUnlock() {
            every { programmer.unlockChip() } returns Result.success(Unit)
            every { programmer.eraseChip(ChipType.AT28C64B, any()) } returns Result.success(Unit)

            cli.execute("-u", "--narrow", "erase")

            verifySequence {
                programmer.unlockChip()
                programmer.eraseChip(ChipType.AT28C64B, any())
            }
        }

        @Test
        fun testEraseWithTypeWithLock() {
            every { programmer.lockChip() } returns Result.success(Unit)
            every { programmer.eraseChip(ChipType.AT28C64B, any()) } returns Result.success(Unit)

            cli.execute("-ln", "erase")

            verifySequence {
                programmer.eraseChip(ChipType.AT28C64B, any())
                programmer.lockChip()
            }
        }

        @Test
        fun testEraseWithTypeWithUnlockAndLock() {
            every { programmer.unlockChip() } returns Result.success(Unit)
            every { programmer.lockChip() } returns Result.success(Unit)
            every { programmer.eraseChip(ChipType.AT28C256, any()) } returns Result.success(Unit)

            cli.execute("-ulw", "erase")

            verifySequence {
                programmer.unlockChip()
                programmer.eraseChip(ChipType.AT28C256, any())
                programmer.lockChip()
            }
        }

    }
}