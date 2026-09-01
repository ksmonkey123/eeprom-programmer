package ch.awae.eeprom_programmer.programmer

fun interface ComDevice {

    fun sendCommand(command: String): String?

}