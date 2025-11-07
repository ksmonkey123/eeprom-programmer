#pragma once
#include "common.h"

enum RomInterfaceState { IDLE, READ, WRITE };

class RomInterface {
   private:
    RomInterfaceState state;

   public:
    RomInterface();
    ~RomInterface();

    byte read(address address);
    void write(address address, byte data);
};
