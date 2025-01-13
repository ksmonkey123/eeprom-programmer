package ch.awae.eeprom_programmer.ops

abstract class Operation(val command: String, val helpText: String, vararg val paramLabels: String) {

    abstract fun execute(args: List<String>)

}
