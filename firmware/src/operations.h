#pragma once
#include "common.h"

struct WriteResult {
    bool success;
    address error_address;
    byte error_expected;
    byte error_actual;
};

struct SparsePageElement {
    byte offset;
    byte data;
};

enum ChipType { SMALL_SOCKET, LARGE_SOCKET };

namespace ops {
byte byteRead(address address);
WriteResult byteWrite(address address, byte data);

void pageRead(address address, byte* dest);
WriteResult pageWrite(address address, const byte* data);
WriteResult pageSparseWrite(address address, const SparsePageElement* elements, int nelements);

void lockSDP();
void unlockSDP();

WriteResult identifyType(ChipType* dest);
}  // namespace ops
