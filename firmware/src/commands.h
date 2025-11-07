#pragma once
#include "common.h"

namespace cmd {

void lock();
void unlock();

void read(address address);
void write(address address, byte data);

void pageRead(address address);
void pageWrite(address address, const byte* data);

}  // namespace cmd
