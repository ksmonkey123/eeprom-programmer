#pragma once
#include "common.h"

/**
 * convert the lower 4 bits of a byte into a single hex char.
 */
char lowNibbleToHexChar(byte value);

/**
 * convert 2 hex chars into an 8-bit number.
 * in Case of any error, `dest` is not touched.
 *
 * @param input the char buffer to read from. reads exactly 2 chars from this
 * buffer.
 * @param dest where to write the calculated value.
 * @returns `true` if successful.
 */
bool hexToByte(const char* input, byte* dest);

/**
 * convert 4 hex chars into a 16-bit address.
 * in case of any error, `dest` is not touched.
 *
 * @param input the char buffer to read from. reads exactly 4 chars from this
 * buffer.
 * @param dest where to write the calculated value.
 * @returns `true` if successful.
 */
bool hexToAddress(const char* input, address* dest);
