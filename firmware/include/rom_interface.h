#pragma once
#include "common.h"

enum RomInterfaceState { IDLE, READ, WRITE };

class RomInterface {
    RomInterfaceState state;

public:
    explicit RomInterface();

    ~RomInterface();

    byte read(address address);

    void write(address address, byte data);
};
