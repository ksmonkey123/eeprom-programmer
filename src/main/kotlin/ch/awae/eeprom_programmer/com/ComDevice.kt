package ch.awae.eeprom_programmer.com

interface ComDevice {

    fun sendCommand(command: String): String?

}