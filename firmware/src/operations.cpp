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

    for (byte i = 0; i < 64; i++) {
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
            return createError(address + i, data[i], readback);
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

WriteResult ops::identifyType(ChipType* dest) {
    RomInterface interface;
    byte readback;

    address adr = random(0x0000, 0x2000);
    byte data = interface.read(adr);
    byte inverse = ~data;

    // if there's different data in the "high" block, it is a large chip, no
    // further testing required.
    if (data != interface.read(adr + 0x2000)) {
        *dest = LARGE_SOCKET;
        return createSuccess();
    }

    // modify low byte, check if high byte changes too
    interface.write(adr, inverse);
    readback = interface.read(adr);
    if (readback != inverse) {
        return createError(adr, inverse, readback);
    }

    if (data == interface.read(adr + 0x2000)) {
        // high byte unchanged -> large
        *dest = LARGE_SOCKET;
    } else {
        // high byte changed too -> small
        *dest = SMALL_SOCKET;
    }

    // restore data
    interface.write(adr, data);
    readback = interface.read(adr);
    if (readback != data) {
        return createError(adr, data, readback);
    }
    return createSuccess();
}