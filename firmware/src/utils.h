#pragma once
#include "common.h"

/**
 * convert 2 hex chars into an 8-bit address.
 */
byte hexToByte(const char* input);

/**
 * convert the lower 4 bits of a byte into a single hex char.
 */
char lowNibbleToHexChar(byte value);

/**
 * convert 4 hex chars into a 16-bit address.
 */
address hexToAddress(const char* input);
