#pragma once
#include "common.h"

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

/**
 * print the provided byte to the output as a 2-char hex value (with leading zeroes)
 */
void printData(byte value, Print& output);

/**
 * print the provided address to the output as a 4-char hex value (with leading zeroes)
 */
void printAddress(address value, Print& output);
