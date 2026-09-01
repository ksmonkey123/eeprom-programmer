package ch.awae.eeprom_programmer.serial

import com.fazecast.jSerialComm.SerialPort
import com.fazecast.jSerialComm.SerialPortTimeoutException
import java.io.BufferedReader
import java.io.InputStreamReader
import java.io.OutputStreamWriter
import java.io.PrintWriter

class JscComDevice(comPort: SerialPort) : ComDevice {
    private val tx: PrintWriter
    private val rx: BufferedReader

    init {
        comPort.setComPortParameters(230400, 8, 1, 0)
        comPort.setComPortTimeouts(SerialPort.TIMEOUT_READ_SEMI_BLOCKING or SerialPort.TIMEOUT_WRITE_BLOCKING, 1000, 0)

        if (!comPort.openPort()) {
            throw RuntimeException("Could not open port")
        }
        print("connection opened to ${comPort.systemPortPath}\n")
        print("waiting for device startup...")

        Thread.sleep(1000)

        tx = PrintWriter(OutputStreamWriter(comPort.outputStream))
        rx = BufferedReader(InputStreamReader(comPort.inputStream))

        syncWithDevice()
        print(" ok\n")
    }

    private var syncCounter = 0
    private fun syncWithDevice() {
        repeat(10) {
            val string = "SYN_${syncCounter.toString().padStart(2, '0')}"
            syncCounter = (syncCounter + 1) % 100

            tx.println(string)
            tx.flush()

            try {
                do {
                    val received = rx.readLine() ?: error("stream terminated")
                    // it is possible that we receive bad lines. eat them and only check the last line
                } while (received != string)
                // we have the correct SYN packet -> finish sync
                return
            } catch (_: SerialPortTimeoutException) {
                System.err.println("Timeout")
            }
        }
        throw SerialPortTimeoutException("unable to sync with device in time!")
    }

    override fun sendCommand(command: String): String? = synchronized(this) {
        tx.println(command)
        tx.flush()

        val response = rx.readLine() ?: throw IllegalStateException("stream terminated")
        if (response.startsWith('+')) {
            return response.substring(1).takeUnless { it.isEmpty() }
        } else {
            error("bad response: $response")
        }
    }

    companion object {
        fun findAndConnect(): ComDevice {
            val port = findById(0x2341, 0x0042) // Mega 2560 R3
                ?: findById(0x2341, 0x0010) // Mega 2560
                ?: findById(0x2341) // any other Arduino Board
                ?: error("unable to find any compatible board")
            return JscComDevice(port)
        }

        fun findById(vendorId: Int, productId: Int? = null): SerialPort? {
            val candidates = SerialPort.getCommPorts()
                .filter { it.vendorID == vendorId && (productId == null || it.productID == productId) }

            return when (candidates.size) {
                0 -> null
                1 -> candidates.first()
                else -> error("multiple devices found with vendor ID $vendorId and product ID $productId. cannot decide")
            }
        }
    }

}