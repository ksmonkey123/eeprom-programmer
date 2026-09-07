#include "../include/operations.h"

#include "rom_interface.h"

static WriteResult createError(const address address, const byte expected, const byte actual) {
    return WriteResult{
        .success = false,
        .error = {
            .error_address = address,
            .error_expected = expected,
            .error_actual = actual,
        }
    };
}

static WriteResult createSuccess() {
    return WriteResult{
        .success = true,
    };
}

void ops::pageRead(const address address, byte *dest) {
    RomInterface interface;

    for (byte i = 0; i < 64; i++) {
        dest[i] = interface.read(address + i);
    }
}

WriteResult ops::pageWrite(const address address, const SparsePageElement *elements, const int nelements) {
    RomInterface interface;

    for (int i = 0; i < nelements && i < 64; i++) {
        interface.write(address + elements[i].offset, elements[i].data);
    }

    // verify
    for (int i = 0; i < nelements && i < 64; i++) {
        const byte readback = interface.read(address + elements[i].offset);
        if (elements[i].data != readback) {
            return createError(address + elements[i].offset, elements[i].data,
                               readback);
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

WriteResult ops::identifyType(ChipType *dest) {
    RomInterface interface;

    const auto adr = static_cast<address>(random(0x0000, 0x2000));
    const byte data = interface.read(adr);
    const byte inverse = ~data;

    // if there's different data in the "high" block, it is a large chip, no
    // further testing required.
    if (data != interface.read(adr + 0x2000)) {
        *dest = LARGE_SOCKET;
        return createSuccess();
    }

    // modify low byte, check if high byte changes too
    interface.write(adr, inverse);
    byte readback = interface.read(adr);
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
