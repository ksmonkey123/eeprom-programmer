package ch.awae.eeprom_programmer.serial

import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertNull
import org.junit.jupiter.api.assertThrows
import java.util.*
import kotlin.concurrent.thread
import kotlin.test.assertEquals
import kotlin.test.assertIs
import kotlin.test.assertTrue
import kotlin.time.Duration.Companion.milliseconds
import kotlin.time.Duration.Companion.seconds
import kotlin.time.TimedValue
import kotlin.time.measureTimedValue

class SerialComDeviceTest {

    @Test
    fun testInitFails() {
        val lines = mutableListOf<String>()

        val device = SerialComDevice { lines.add(it) }

        val result = measureTimedValue {
            runCatching {
                device.sendCommand("test")
            }
        }

        // should try for 1 second
        assertTrue(result.duration > 1.seconds)

        // should send up to 100 SYN messages, and not the test message
        assertEquals(100, lines.size)
        lines.forEachIndexed { index, string ->
            assertEquals("SYN", string, "expected SYN as message $index")
        }

        // command fails
        assertTrue(result.value.getOrThrow().isFailure)
    }

    @Test
    fun testInitSuccess() {
        val lines = Collections.synchronizedList(mutableListOf<String>())

        val device = SerialComDevice { lines.add(it) }

        // in another thread, send an ACK after 0.2 seconds.
        thread(start = true) {
            Thread.sleep(200)
            device.onDataReceived("*\n".toByteArray())
        }

        // in another thread, wait until the test command has actually been sent, then send a response
        thread(start = true) {
            while (lines.lastOrNull() != "test") {
                Thread.sleep(1)
            }
            device.onDataReceived("+\n".toByteArray())
        }

        val result = measureTimedValue {
            runCatching {
                device.sendCommand("test")
            }
        }

        // should try for at least 0.2 seconds but then continue quite fast
        assertTrue(result.duration > 200.milliseconds)
        assertTrue(result.duration < 300.milliseconds)

        lines.forEachIndexed { index, string ->
            if (index < lines.lastIndex)
                assertEquals("SYN", string, "expected SYN as message $index")
            if (index == lines.lastIndex)
                assertEquals("test", string, "expected command as message $index")
        }

        // command fails
        assertTrue(result.value.isSuccess)
        assertNull(result.value.getOrThrow().getOrThrow())
    }

    @Test
    fun testGarbageIgnoredBeforeSync() {
        val lines = Collections.synchronizedList(mutableListOf<String>())

        val device = SerialComDevice { lines.add(it) }

        // in another thread, send an ACK after 0.2 seconds.
        thread(start = true) {
            Thread.sleep(100)
            device.onDataReceived("garbage*\n".toByteArray())
            Thread.sleep(100)
            device.onDataReceived("*\n".toByteArray())
        }

        // in another thread, wait until the test command has actually been sent, then send a response
        thread(start = true) {
            while (lines.lastOrNull() != "test") {
                Thread.sleep(1)
            }
            device.onDataReceived("+\n".toByteArray())
        }

        val result = measureTimedValue {
            runCatching {
                device.sendCommand("test")
            }
        }

        // should try for at least 0.2 seconds but then continue quite fast
        assertTrue(result.duration > 200.milliseconds)
        assertTrue(result.duration < 300.milliseconds)

        lines.forEachIndexed { index, string ->
            if (index < lines.lastIndex)
                assertEquals("SYN", string, "expected SYN as message $index")
            if (index == lines.lastIndex)
                assertEquals("test", string, "expected command as message $index")
        }

        // command fails
        assertTrue(result.value.isSuccess)
        assertNull(result.value.getOrThrow().getOrThrow())
    }

    @Test
    fun testCarriageReturnIgnored() {
        val lines = Collections.synchronizedList(mutableListOf<String>())

        val device = SerialComDevice { lines.add(it) }

        // in another thread, send an ACK after 0.2 seconds.
        thread(start = true) {
            Thread.sleep(200)
            device.onDataReceived("*\n".toByteArray())
        }

        // in another thread, wait until the test command has actually been sent, then send a response
        thread(start = true) {
            while (lines.lastOrNull() != "test") {
                Thread.sleep(1)
            }
            device.onDataReceived("+result\r\n".toByteArray())
        }

        val result = measureTimedValue {
            runCatching {
                device.sendCommand("test")
            }
        }

        // should try for at least 0.2 seconds but then continue quite fast
        assertTrue(result.duration > 200.milliseconds)
        assertTrue(result.duration < 300.milliseconds)

        lines.forEachIndexed { index, string ->
            if (index < lines.lastIndex)
                assertEquals("SYN", string, "expected SYN as message $index")
            if (index == lines.lastIndex)
                assertEquals("test", string, "expected command as message $index")
        }

        // command fails
        assertTrue(result.value.isSuccess)
        assertEquals("result", result.value.getOrThrow().getOrThrow())
    }

    @Test
    fun testAdditionalAcksAreIgnored() {
        val lines = Collections.synchronizedList(mutableListOf<String>())

        val device = SerialComDevice { lines.add(it) }

        // in one thread, send ACKS every 10 milliseconds
        thread(start = true) {
            repeat(10) {
                Thread.sleep(10)
                device.onDataReceived("*\n".toByteArray())
            }
        }

        // in another thread, wait until the test command has actually been sent, then send a response
        thread(start = true) {
            while (lines.last() != "test") {
                Thread.sleep(10)
            }
            // wait additional time to "allow" for additional acks
            Thread.sleep(100)
            device.onDataReceived("+\n".toByteArray())
        }

        val result = measureTimedValue {
            runCatching {
                device.sendCommand("test")
            }
        }

        // should try for at least 0.5 seconds but then continue quite fast
        assertTrue(result.duration > 100.milliseconds)
        assertTrue(result.duration < 200.milliseconds)

        lines.forEachIndexed { index, string ->
            if (index < lines.lastIndex)
                assertEquals("SYN", string, "expected SYN as message $index")
            if (index == lines.lastIndex)
                assertEquals("test", string, "expected command as message $index")
        }

        // command fails
        assertTrue(result.value.isSuccess)
        assertNull(result.value.getOrThrow().getOrThrow())
    }

    @Test
    fun testCommandsAreProcessedSequentially() {
        val lines = Collections.synchronizedList(mutableListOf<String>())

        val device = SerialComDevice { lines.add(it) }

        // in one thread, send ACKS every 10 milliseconds
        thread(start = true) {
            while (lines.isEmpty() || lines.last() == "SYN") {
                device.onDataReceived("*\n".toByteArray())
                Thread.sleep(1)
            }
        }

        // thread listens for 'cmd_a' and sends the response after 200ms
        thread(start = true) {
            while (lines.lastOrNull() != "cmd_a") {
                Thread.sleep(1)
            }
            Thread.sleep(200)
            device.onDataReceived("+result_a\n".toByteArray())
        }

        // thread listens for 'cmd_b' and sends the response immediately
        thread(start = true) {
            while (lines.lastOrNull() != "cmd_b") {
                Thread.sleep(1)
            }
            Thread.sleep(100)
            device.onDataReceived("+result_b\n".toByteArray())
        }

        var resultA: TimedValue<Result<Result<String?>>>? = null
        var resultB: TimedValue<Result<Result<String?>>>? = null

        // thread sends 'cmd_a' immediately
        thread(start = true) {
            resultA = measureTimedValue {
                runCatching {
                    device.sendCommand("cmd_a")
                }
            }
        }

        // thread sends 'cmd_b' after a delay
        thread(start = true) {
            resultB = measureTimedValue {
                runCatching {
                    Thread.sleep(100)
                    device.sendCommand("cmd_b")
                }
            }
        }

        // wait for both "sender threads" to complete
        while (resultA == null || resultB == null) {
            // loop
            Thread.yield()
        }

        // should try for at least 0.2 seconds but then continue quite fast
        assertTrue(resultA.duration > 200.milliseconds)
        assertTrue(resultA.duration < 300.milliseconds)
        assertTrue(resultB.duration > 300.milliseconds)
        assertTrue(resultB.duration < 400.milliseconds)

        lines.forEachIndexed { index, string ->
            if (index < lines.lastIndex - 1)
                assertEquals("SYN", string, "expected SYN as message $index")
            if (index == lines.lastIndex - 1)
                assertEquals("cmd_a", string, "expected cmd_a as message $index")
            if (index == lines.lastIndex)
                assertEquals("cmd_b", string, "expected cmd_b as message $index")
        }

        assertEquals("result_a", resultA.value.getOrThrow().getOrThrow())
        assertEquals("result_b", resultB.value.getOrThrow().getOrThrow())
    }

    @Test
    fun testUnexpectedLine() {
        val lines = Collections.synchronizedList(mutableListOf<String>())

        val device = SerialComDevice { lines.add(it) }

        device.onDataReceived("*\n".toByteArray())
        assertThrows<IllegalStateException> {
            device.onDataReceived("unexpected\n".toByteArray())
        }
    }

    @Test
    fun testCommandWithFailure() {
        val lines = Collections.synchronizedList(mutableListOf<String>())

        val device = SerialComDevice { lines.add(it) }

        // in another thread, send an ACK after 0.2 seconds.
        thread(start = true) {
            Thread.sleep(200)
            device.onDataReceived("*\n".toByteArray())
        }

        // in another thread, wait until the test command has actually been sent, then send a response
        thread(start = true) {
            while (lines.lastOrNull() != "test") {
                Thread.sleep(1)
            }
            device.onDataReceived("error\n".toByteArray())
        }

        val result = measureTimedValue {
            runCatching {
                device.sendCommand("test")
            }
        }

        // should try for at least 0.2 seconds but then continue quite fast
        assertTrue(result.duration > 200.milliseconds)
        assertTrue(result.duration < 300.milliseconds)

        lines.forEachIndexed { index, string ->
            if (index < lines.lastIndex)
                assertEquals("SYN", string, "expected SYN as message $index")
            if (index == lines.lastIndex)
                assertEquals("test", string, "expected command as message $index")
        }

        // command fails
        assertTrue(result.value.getOrThrow().isFailure)
    }

    @Test
    fun testUnexpectedMessageInvalidatesDevice() {
        val lines = Collections.synchronizedList(mutableListOf<String>())

        val device = SerialComDevice { lines.add(it) }

        // force init
        device.onDataReceived("*\n".toByteArray())

        // in another thread, wait until the test command has actually been sent, then send a response
        thread(start = true) {
            while (lines.lastOrNull() != "test") {
                Thread.sleep(1)
            }
            device.onDataReceived("+\n".toByteArray())
            device.onDataReceived("error\n".toByteArray())
        }

        device.sendCommand("test")
        Thread.sleep(100)
        val result2 = device.sendCommand("test2")
        assertTrue(result2.isFailure)
        assertIs<IllegalStateException>(result2.exceptionOrNull())
    }


}