#include "operations.h"
#include "rom_interface.h"

WriteResult createError(address address, byte expected, byte actual) {
    WriteResult error;
    error.success = false;
    error.error_address = address;
    error.error_expected = expected;
    error.error_actual = actual;
    return error;
}

WriteResult createSuccess() {
    WriteResult result;
    result.success = true;
    return result;
}

byte ops::byteRead(address address) {
    RomInterface interface;

    return interface.read(address);
}

WriteResult ops::byteWrite(address address, byte data) {
    RomInterface interface;

    interface.write(address, data);
    byte readback = interface.read(address);

    if (readback != data) {
        return createError(address, data, readback);
    } else {
        return createSuccess();
    }
}

void ops::pageRead(address address, byte* dest) {
    RomInterface interface;

    for(byte i = 0; i < 64; i++) {
        dest[i] = interface.read(address + i);
    }
}

WriteResult ops::pageWrite(address address, const byte* data) {
    RomInterface interface;

    for (int i = 0; i < 64; i++) {
        interface.write(address + i, data[i]);
    }

    // verify
    for (int i = 0; i < 64; i++) {
        byte readback = interface.read(address + i);
        if (data[i] != readback) {
            return createError(address, data[i], readback);
        }
    }
    return createSuccess();
}

void ops::lockSDP() {
    RomInterface interface;

    interface.write(0x5555, 0xaa);
    interface.write(0x2aaa, 0x55);
    interface.write(0x5555, 0xa0);
}

void ops::unlockSDP() {
    RomInterface interface;

    interface.write(0x5555, 0xaa);
    interface.write(0x2aaa, 0x55);
    interface.write(0x5555, 0x80);
    interface.write(0x5555, 0xaa);
    interface.write(0x2aaa, 0x55);
    interface.write(0x5555, 0x20);
}
