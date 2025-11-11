package ch.awae.eeprom_programmer.api

interface Programmer {

    fun readByte(address: Int): UByte

    fun writeByte(address: Int, data: UByte)

    /**
     * Reads an entire 64bit page of memory
     *
     * @param address starting address of a memory page. The lowest 6 bits must be zero. (address must be divisible by 64)
     */
    fun readPage(address: Int): ByteArray

    /**
     * Writes an entire 64bit page of memory
     *
     * @param address starting address of a memory page. The lowest 6 bits must be zero. (address must be divisible by 64)
     * @param data the data to write. the length must be at least 64 items more than 'startFrom'.
     * only the first 64 items are written
     */
    fun writePage(address: Int, data: ByteArray, startFrom: Int = 0)

    /**
     * Dumps the entire memory of the chip, returning a 8192 or 32768 element byte array.
     *
     * @param progressCallback is called after every 64 byte block.
     */
    fun dumpMemory(type: ChipType, progressCallback: () -> Unit = {}): ByteArray

    /**
     * Loads the entire chip.
     *
     * @param data the data to write. The length must match the chip size.
     * @param progressCallback is called after every 64 byte block.
     */
    fun flashChip(type: ChipType, data: ByteArray, progressCallback: () -> Unit = {})

    /**
     * Write 0xff to each address
     *
     * @param progressCallback is called after every 64 byte block.
     */
    fun eraseChip(type: ChipType, progressCallback: () -> Unit = {})

    fun lockChip()

    fun unlockChip()

    fun identifyType(): ChipType

    fun rawCommand(command: String): String?

}
