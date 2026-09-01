package ch.awae.eeprom_programmer.cli.internals

import ch.awae.eeprom_programmer.programmer.ProgressReport

class ProgressBar(val width: Int) {
    private var state: ProgressReport? = null

    fun set(state: ProgressReport) {
        this.state = state
    }

    override fun toString(): String {
        val filled = state?.let { (value, limit) -> (value * width) / limit } ?: 0
        val empty = width - filled
        val numerical = state?.let { (value, limit) -> "$value/$limit" } ?: ""
        return "[" + "|".repeat(filled) + " ".repeat(empty) + "] $numerical"
    }

}