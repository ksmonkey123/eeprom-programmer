package ch.awae.eeprom_programmer.backend.com

interface ComDevice {

    fun sendCommand(command: String): String?

}