#include "utils.h"

byte hexCharToHalfByte(char input) {
  switch (input) {
    case '0': return 0;
    case '1': return 1;
    case '2': return 2;
    case '3': return 3;
    case '4': return 4;
    case '5': return 5;
    case '6': return 6;
    case '7': return 7;
    case '8': return 8;
    case '9': return 9;
    case 'A': return 10;
    case 'B': return 11;
    case 'C': return 12;
    case 'D': return 13;
    case 'E': return 14;
    case 'F': return 15;
    default: return 255;
  }
}

int hexToByte(char* input) {
  byte high = hexCharToHalfByte(*(input + 0));
  byte low = hexCharToHalfByte(*(input + 1));

  if (high == 255 || low == 255) {
    return -1;
  }

  return ((high << 4) & 0x00f0) | (low & 0x000f);
}
