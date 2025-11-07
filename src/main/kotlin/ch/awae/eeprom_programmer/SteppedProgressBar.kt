package ch.awae.eeprom_programmer

class SteppedProgressBar(val width: Int, val limit: Int) {
    private var state = 0

    fun step() {
        state++
    }

    override fun toString(): String {
        val filled = (state * width) / limit
        val empty = width - filled
        return "[" + "|".repeat(filled) + " ".repeat(empty) + "] $state/$limit"
    }

}