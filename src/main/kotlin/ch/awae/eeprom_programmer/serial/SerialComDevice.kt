package ch.awae.eeprom_programmer.serial

import ch.awae.eeprom_programmer.programmer.ComDevice
import java.nio.CharBuffer
import java.util.concurrent.CompletableFuture
import java.util.concurrent.TimeUnit

class SerialComDevice(val writer: (ByteArray) -> Unit) : ComDevice {

    @Volatile
    private var isSynchronized = false

    @Volatile
    private var future: CompletableFuture<Result<String?>>? = null

    private fun synchronize() {
        repeat(100) {
            writeLine("SYN")
            Thread.sleep(10)
            if (isSynchronized) return
        }
        error("synchronization failed")
    }

    @Synchronized
    override fun sendCommand(command: String): String? {
        synchronize()

        if (future != null) error("command already in progress")

        val future = CompletableFuture<Result<String?>>()
        this.future = future

        writeLine(command)
        try {
            return future.get(10, TimeUnit.SECONDS).getOrThrow()
        } finally {
            // clean up the future
            this.future = null
        }
    }

    private val LINE_BREAK_ARRAY = byteArrayOf(0x0a)

    private fun writeLine(string: String) {
        writer(string.toByteArray(Charsets.UTF_8))
        writer(LINE_BREAK_ARRAY)
    }

    fun onDataReceived(data: ByteArray) {
        data.forEach { onByteReceived(it) }
    }

    private val receiveBuffer = CharBuffer.allocate(512)

    private fun onByteReceived(byte: Byte) {
        when (val c = byte.toUByte().toInt().toChar()) {
            '\r' -> return
            '\n' -> {
                receiveBuffer
                val line = receiveBuffer.flip().toString()
                receiveBuffer.clear()
                handleLine(line)
            }

            else -> receiveBuffer.put(c)
        }
    }

    private fun handleLine(line: String) {
        // SYN_ACKs (*) can arrive at any time
        if (line == "*") {
            isSynchronized = true
            return
        }

        // if not yet synchronized, we treat everything we read as garbage
        if (!isSynchronized) return

        val future = future ?: error("received unexpected line: $line")

        // we are synchronized, now we can process the actual line
        val completed = if (line.first() == '+') {
            // positive result
            future.complete(Result.success(line.substring(1).takeIf { it.isNotEmpty() }))
        } else {
            future.complete(Result.failure(Exception("received invalid line: $line")))
        }

        if (!completed) {
            error("received unexpected line: $line")
        }
    }

}