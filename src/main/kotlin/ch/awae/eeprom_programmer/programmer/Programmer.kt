package ch.awae.eeprom_programmer.programmer

import ch.awae.binfiles.*

interface Programmer {

    /**
     * Dumps the entire memory of the chip, returning an 8192 or 32,768 element byte array.
     *
     * @param progressCallback is called after every 64-byte block.
     */
    fun dumpMemory(type: ChipType, progressCallback: () -> Unit = {}): ByteArray

    /**
     * Writes a [BinaryFile] to the chip.
     *
     * @param file the data to write.
     */
    fun flashChip(type: ChipType, file: BinaryFile, progressCallback: (ProgressReport) -> Unit = {})

    /**
     * Write 0xff to each address
     *
     * @param progressCallback is called after every 64-byte block.
     */
    fun eraseChip(type: ChipType, progressCallback: () -> Unit = {})

    fun lockChip()

    fun unlockChip()

    fun identifyType(): ChipType

}
