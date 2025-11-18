package ch.awae.eeprom_programmer.backend.api

data class ProgressReport(
    val progress: Int,
    val total: Int,
)
