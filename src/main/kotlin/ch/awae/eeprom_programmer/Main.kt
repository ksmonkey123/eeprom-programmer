package ch.awae.eeprom_programmer

import ch.awae.eeprom_programmer.api.*
import ch.awae.eeprom_programmer.com.*
import org.springframework.boot.*
import org.springframework.boot.autoconfigure.*
import org.springframework.context.annotation.*

@SpringBootApplication
class Main {

    @Bean
    fun comDevice(): ComDevice = JscComDevice.findAndConnect()

    @Bean
    fun programmer(comDevice: ComDevice): Programmer = ComPortProgrammer(comDevice)

}

fun main(args: Array<String>) {
    SpringApplication.run(Main::class.java, *args)
}