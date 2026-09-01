package ch.awae.eeprom_programmer.cli.internals

import ch.awae.eeprom_programmer.programmer.ProgressReport
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test

class ProgressBarTest {

    @Test
    fun testProgressBar() {
        val progressBar = ProgressBar(10)
        assertEquals("[          ] ", progressBar.toString())
        (0..100).forEach {
            progressBar.set(ProgressReport(it, 100))
            assertEquals("[${"|".repeat(it / 10)}${" ".repeat(10 - it / 10)}] ${it}/100", progressBar.toString())
        }
    }

}