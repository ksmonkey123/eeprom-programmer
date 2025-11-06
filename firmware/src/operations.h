#pragma once
#include <Arduino.h>

struct WriteResult {
    bool success;
    int error_address;
    byte error_expected;
    byte error_actual;
};

byte byteRead(int address);
WriteResult byteWrite(int address, byte data);

void pageRead(int address, byte* dest);
WriteResult pageWrite(int address, const byte* data);

void lockSDP();
void unlockSDP();
