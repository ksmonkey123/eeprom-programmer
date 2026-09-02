package ch.awae.eeprom_programmer.serial

import ch.awae.eeprom_programmer.programmer.ComDevice
import com.fazecast.jSerialComm.SerialPort
import com.fazecast.jSerialComm.SerialPortDataListener
import com.fazecast.jSerialComm.SerialPortEvent

object JscSerialAdapter {
    fun findAndConnect(): ComDevice {
        val port = findById(0x2341, 0x0042) // Mega 2560 R3
            ?: findById(0x2341, 0x0010) // Mega 2560
            ?: findById(0x2341) // any other Arduino Board
            ?: error("unable to find any compatible board")

        port.setComPortParameters(230400, 8, 1, 0)
        port.setComPortTimeouts(SerialPort.TIMEOUT_NONBLOCKING, 0, 0)

        val comDevice = SerialComDevice {
            port.writeBytes(it, it.size)
        }

        port.addDataListener(object : SerialPortDataListener {
            override fun getListeningEvents(): Int {
                return SerialPort.LISTENING_EVENT_PORT_DISCONNECTED + SerialPort.LISTENING_EVENT_DATA_RECEIVED
            }

            override fun serialEvent(event: SerialPortEvent) {
                if ((event.eventType and SerialPort.LISTENING_EVENT_PORT_DISCONNECTED) > 0) {
                    port.closePort()
                }

                if ((event.eventType and SerialPort.LISTENING_EVENT_DATA_RECEIVED) > 0) {
                    val data = event.receivedData ?: error("expected data in LISTENING_EVENT_DATA_RECEIVED")
                    comDevice.onDataReceived(data)
                }
            }
        })

        if (!port.openPort()) {
            error("Could not open port")
        }

        Thread.sleep(1000)

        return comDevice
    }

    private fun findById(vendorId: Int, productId: Int? = null): SerialPort? {
        val candidates = SerialPort.getCommPorts()
            .filter { it.vendorID == vendorId && (productId == null || it.productID == productId) }

        return when (candidates.size) {
            0 -> null
            1 -> candidates.first()
            else -> error("multiple devices found with vendor ID $vendorId and product ID $productId. cannot decide")
        }
    }
}
