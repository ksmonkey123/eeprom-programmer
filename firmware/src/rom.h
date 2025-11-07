#pragma once
#include "common.h"

namespace rom {
void startAccess();
void endAccess();

void startWriteCycle();
void endWriteCycle();

byte read(address address);
void write(address address, byte data);
}  // namespace rom
