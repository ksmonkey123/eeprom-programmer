#pragma once
#include "common.h"

/**
 * convert 2 hex chars into an 8-bit address.
 */
byte hexToByte(const char* input);

/**
 * convert 4 hex chars into a 16-bit address.
 */
address hexToAddress(const char* input);

/**
 * convert a single byte into a 2 char hex string. The hex chars are written
 * to dest.
 */
void byteToHex(byte value, char* dest);

/**
 *  convert a 16 bit address into a 4 char hex string. The hex chars are written
 * to dest.
 */
void addressToHex(address value, char* dest);

/**
 * extract chars from a buffer and return them as a null-terminated string.
 * 
 * @returns null-terminated substring. returned object is owned by caller!
 */
char* substring(const char* buffer, int length);
