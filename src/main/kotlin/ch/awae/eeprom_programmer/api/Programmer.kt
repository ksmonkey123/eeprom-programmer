package ch.awae.eeprom_programmer.api

interface Programmer {

    fun readByte(address: Int): UByte

    fun writeByte(address: Int, data: UByte)

    /**
     * Reads an entire 64bit page of memory
     *
     * @param address starting address of a memory page. The lowest 6 bits must be zero. (address must be divisible by 64)
     */
    fun readLine(address: Int): ByteArray

    /**
     * Writes an entire 64bit page of memory
     *
     * @param address starting address of a memory page. The lowest 6 bits must be zero. (address must be divisible by 64)
     * @param data the data to write. the length must be at least 64 items more than 'startFrom'.
     * only the first 64 items are written
     */
    fun writePage(address: Int, data: ByteArray, startFrom: Int = 0)

    /**
     * Dumps the entire memory of the chip, returning a 8192 or 32768 element byte array
     */
    fun dumpMemory(type: ChipType): ByteArray

    /**
     * Loads the entire chip.
     *
     * @param data the data to write. The length must match the chip size
     */
    fun flashChip(type: ChipType, data: ByteArray, progressCallback: (Int) -> Unit)

    /**
     * Write 0xff to each address
     */
    fun eraseChip(type: ChipType, progressCallback: (Int) -> Unit)

    fun lockChip()

    fun unlockChip()

    fun rawCommand(command: String): String?

}
