#pragma once
#include "common.h"

struct WriteResult {
    bool success;

    struct {
        address error_address;
        byte error_expected;
        byte error_actual;
    } error;
};

struct SparsePageElement {
    byte offset;
    byte data;
};

enum ChipType { SMALL_SOCKET, LARGE_SOCKET };

namespace ops {
    void pageRead(address address, byte *dest);

    WriteResult pageWrite(address address, const SparsePageElement *elements, int nelements);

    void lockSDP();

    void unlockSDP();

    WriteResult identifyType(ChipType *dest);
} // namespace ops
