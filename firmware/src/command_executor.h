#pragma once
#include "common.h"

class CommandExecutor {
   private:
    Print& output;

   public:
    CommandExecutor(Print& output);

    void lock();
    void unlock();

    void read(address address);
    void write(address address, byte data);

    void pageRead(address address);
    void pageWrite(address address, const byte* data);
};
