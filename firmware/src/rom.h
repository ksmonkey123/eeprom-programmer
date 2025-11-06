#pragma once
#include <Arduino.h>

void startAccess();
void endAccess();

void startWriteCycle();
void endWriteCycle();

byte read(int address);
void write(int address, byte data);
