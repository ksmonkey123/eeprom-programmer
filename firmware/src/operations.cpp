#include "operations.h"

#include "rom.h"

WriteResult createError(address address, byte expected, byte actual) {
    WriteResult error;
    error.success = false;
    error.error_address = address;
    error.error_expected = expected;
    error.error_actual = actual;
    return error;
}

byte ops::byteRead(address address) {
    rom::startAccess();
    byte result = rom::read(address);
    rom::endAccess();
    return result;
}

WriteResult ops::byteWrite(address address, byte data) {
    rom::startAccess();
    rom::startWriteCycle();
    rom::write(address, data);
    rom::endWriteCycle();
    byte result = rom::read(address);
    rom::endAccess();

    if (data != result) {
        return createError(address, data, result);
    } else {
        return WriteResult{.success = true};
    }
}

void ops::pageRead(address address, byte* dest) {
    rom::startAccess();
    for (int i = 0; i < 64; i++) {
        dest[i] = rom::read(address + i);
    }
    rom::endAccess();
}

WriteResult ops::pageWrite(address address, const byte* data) {
    rom::startAccess();
    rom::startWriteCycle();
    for (int i = 0; i < 64; i++) {
        rom::write(address + i, data[i]);
    }
    rom::endWriteCycle();
    // verify data
    WriteResult result = WriteResult{.success = true};
    for (int i = 0; i < 64; i++) {
        byte readback = rom::read(address + i);
        if (data[i] != readback) {
            result = createError(address, data[i], readback);
            break;
        }
    }
    rom::endAccess();
    return result;
}

void ops::lockSDP() {
    rom::startAccess();
    rom::startWriteCycle();
    rom::write(0x5555, 0xaa);
    rom::write(0x2aaa, 0x55);
    rom::write(0x5555, 0xa0);
    rom::endWriteCycle();
    rom::endAccess();
}

void ops::unlockSDP() {
    rom::startAccess();
    rom::startWriteCycle();
    rom::write(0x5555, 0xaa);
    rom::write(0x2aaa, 0x55);
    rom::write(0x5555, 0x80);
    rom::write(0x5555, 0xaa);
    rom::write(0x2aaa, 0x55);
    rom::write(0x5555, 0x20);
    rom::endWriteCycle();
    rom::endAccess();
}
