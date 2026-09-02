package ch.awae.eeprom_programmer.serial

import ch.awae.eeprom_programmer.programmer.ComDevice
import java.nio.CharBuffer
import java.util.concurrent.CompletableFuture
import java.util.concurrent.TimeUnit

class SerialComDevice(val writer: (String) -> Unit) : ComDevice {

    private enum class State {
        IDLE, SYNCHRONIZED, INVALID
    }

    @Volatile
    private var state = State.IDLE

    @Volatile
    private var future: CompletableFuture<Result<String?>>? = null

    private fun synchronize() {
        if (state != State.IDLE) return
        repeat(100) {
            writer("SYN")
            Thread.sleep(10)
            if (state != State.IDLE) return
        }
        state = State.INVALID
        error("synchronization failed")
    }

    @Synchronized
    override fun sendCommand(command: String): String? {
        synchronize()

        if (state == State.INVALID) error("device invalid due to previous failure")

        if (future != null) error("command already in progress")

        val future = CompletableFuture<Result<String?>>()
        this.future = future

        writer(command)
        try {
            return future.get(10, TimeUnit.SECONDS).getOrThrow()
        } finally {
            // clean up the future
            this.future = null
        }
    }

    fun onDataReceived(data: ByteArray) {
        data.forEach { onByteReceived(it) }
    }

    private val receiveBuffer = CharBuffer.allocate(512)

    private fun onByteReceived(byte: Byte) {
        when (val c = byte.toUByte().toInt().toChar()) {
            '\r' -> return
            '\n' -> {
                val line = receiveBuffer.flip().toString()
                receiveBuffer.clear()
                handleLine(line)
            }

            else -> receiveBuffer.put(c)
        }
    }

    private fun handleLine(line: String) {
        if (state == State.INVALID) {
            error("device invalid due to previous failure")
        }

        // SYN_ACKs (*) can arrive at any time
        if (line == "*") {
            state = State.SYNCHRONIZED
            return
        }

        // if not yet synchronized, we treat everything we read as garbage
        if (state == State.IDLE) return

        val future = future ?: run {
            state = State.INVALID
            error("received unexpected line: $line")
        }

        // we are synchronized, now we can process the actual line
        val completed = if (line.first() == '+') {
            // positive result
            future.complete(Result.success(line.substring(1).takeIf { it.isNotEmpty() }))
        } else {
            future.complete(Result.failure(Exception("received invalid line: $line")))
        }

        if (!completed) {
            state = State.INVALID
            error("received unexpected line: $line")
        }
    }

}