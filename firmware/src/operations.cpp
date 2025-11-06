#include "operations.h"

#include "rom.h"

WriteResult createError(int address, byte expected, byte actual) {
    WriteResult error;
    error.success = false;
    error.error_address = address;
    error.error_expected = expected;
    error.error_actual = actual;
}

byte byteRead(int address) {
    startAccess();
    byte result = read(address);
    endAccess();
    return result;
}

WriteResult byteWrite(int address, byte data) {
    startAccess();
    startWriteCycle();
    write(address, data);
    endWriteCycle();
    byte result = read(address);
    endAccess();

    if (data != result) {
        return createError(address, data, result);
    } else {
        return WriteResult{.success = true};
    }
}

void pageRead(int address, byte* dest) {
    startAccess();
    for (int i = 0; i < 64; i++) {
        dest[i] = read(address + i);
    }
    endAccess();
}

WriteResult pageWrite(int address, const byte* data) {
    startAccess();
    startWriteCycle();
    for (int i = 0; i < 64; i++) {
        write(address + i, data[i]);
    }
    endWriteCycle();
    // verify data
    WriteResult result = WriteResult{.success = true};
    for (int i = 0; i < 64; i++) {
        byte readback = read(address + i);
        if (data[i] != readback) {
            endAccess();
            result = createError(address, data[i], readback);
            break;
        }
    }
    endAccess();
    return result;
}

void lockSDP() {
    startAccess();
    startWriteCycle();
    write(0x5555, 0xaa);
    write(0x2aaa, 0x55);
    write(0x5555, 0xa0);
    endWriteCycle();
    endAccess();
}

void unlockSDP() {
    startAccess();
    startWriteCycle();
    write(0x5555, 0xaa);
    write(0x2aaa, 0x55);
    write(0x5555, 0x80);
    write(0x5555, 0xaa);
    write(0x2aaa, 0x55);
    write(0x5555, 0x20);
    endWriteCycle();
    endAccess();
}