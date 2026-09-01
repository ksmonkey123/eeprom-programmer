package ch.awae.eeprom_programmer.serial

interface ComDevice {

    fun sendCommand(command: String): String?

}