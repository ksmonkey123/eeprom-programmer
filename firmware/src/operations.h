#pragma once
#include "common.h"

struct WriteResult {
    bool success;
    address error_address;
    byte error_expected;
    byte error_actual;
};

enum ChipSize { SMALL, LARGE };

namespace ops {
byte byteRead(address address);
WriteResult byteWrite(address address, byte data);

void pageRead(address address, byte* dest);
WriteResult pageWrite(address address, const byte* data);

void lockSDP();
void unlockSDP();

ChipSize sizeTest();
}  // namespace ops
