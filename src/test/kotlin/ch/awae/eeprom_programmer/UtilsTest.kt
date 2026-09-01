package ch.awae.eeprom_programmer

import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.params.ParameterizedTest
import org.junit.jupiter.params.provider.Arguments
import org.junit.jupiter.params.provider.MethodSource

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