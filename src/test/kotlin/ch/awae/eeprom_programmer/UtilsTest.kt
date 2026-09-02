package ch.awae.eeprom_programmer

import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test
import org.junit.jupiter.params.ParameterizedTest
import org.junit.jupiter.params.provider.Arguments
import org.junit.jupiter.params.provider.MethodSource
import kotlin.time.Duration.Companion.milliseconds
import kotlin.time.Duration.Companion.seconds

class UtilsTest {

    @ParameterizedTest
    @MethodSource("byteCombinations")
    fun testByteToHex(value: Byte, expected: String) {
        assertEquals(expected, value.hex())
    }

    @ParameterizedTest
    @MethodSource("intCombinations")
    fun testIntToHex(value: Int, length: Int, expected: String) {
        assertEquals(expected, value.hex(length))
    }

    @Test
    fun testDurationFormat() {
        assertEquals("0.0s", 0.milliseconds.toFractionalSeconds())
        assertEquals("0.0s", 5.milliseconds.toFractionalSeconds())
        assertEquals("0.0s", 50.milliseconds.toFractionalSeconds())
        assertEquals("0.1s", 123.milliseconds.toFractionalSeconds())
        assertEquals("1.0s", 1.seconds.toFractionalSeconds())
        assertEquals("1.0s", (1.seconds + 23.milliseconds).toFractionalSeconds())
        assertEquals("12.3s", (12.seconds + 345.milliseconds).toFractionalSeconds())
        assertEquals("75.0s", (75.seconds + 8.milliseconds).toFractionalSeconds())
        assertEquals("120.0s", 120.seconds.toFractionalSeconds())
    }

    companion object {
        @JvmStatic
        fun byteCombinations() = (0..255).map {
            Arguments.of(it.toByte(), it.toString(16).padStart(2, '0').uppercase())
        }

        @JvmStatic
        fun intCombinations() = listOf(
            Arguments.of(123, 4, "007B"),
            Arguments.of(1234, 4, "04D2"),
            Arguments.of(12345, 4, "3039"),
            Arguments.of(123456, 4, "E240"),
            Arguments.of(123456, 5, "1E240"),
        )
    }

}