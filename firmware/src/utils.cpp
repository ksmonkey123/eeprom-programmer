#include "utils.h"

byte hexCharToHalfByte(char input) {
    switch (input) {
        case '0':
            return 0;
        case '1':
            return 1;
        case '2':
            return 2;
        case '3':
            return 3;
        case '4':
            return 4;
        case '5':
            return 5;
        case '6':
            return 6;
        case '7':
            return 7;
        case '8':
            return 8;
        case '9':
            return 9;
        case 'A':
            return 10;
        case 'B':
            return 11;
        case 'C':
            return 12;
        case 'D':
            return 13;
        case 'E':
            return 14;
        case 'F':
            return 15;
        default:
            return 255;
    }
}

char lowNibbleToHexChar(byte value) {
    switch (value & 0x0f) {
        case 0:
            return '0';
        case 1:
            return '1';
        case 2:
            return '2';
        case 3:
            return '3';
        case 4:
            return '4';
        case 5:
            return '5';
        case 6:
            return '6';
        case 7:
            return '7';
        case 8:
            return '8';
        case 9:
            return '9';
        case 10:
            return 'A';
        case 11:
            return 'B';
        case 12:
            return 'C';
        case 13:
            return 'D';
        case 14:
            return 'E';
        case 15:
            return 'F';
        default:
            return '\0';
    }
}

byte hexToByte(const char* input) {
    byte high = hexCharToHalfByte(*(input + 0));
    byte low = hexCharToHalfByte(*(input + 1));

    if (high == 255 || low == 255) {
        return -1;
    }

    return ((high << 4) & 0xf0) | (low & 0x0f);
}

address hexToAddress(const char* input) {
    address result = 0;
    for (byte i = 0; i < 4; i++) {
        byte nextHalfByte = hexCharToHalfByte(input[i]);
        if (nextHalfByte == 255) {
            return -1;
        }
        result = ((result << 4) & 0xfff0) | (nextHalfByte & 0x000f);
    }

    // addresses are only valid up to 0x7fff. If the leading bit is 1, we treat
    // it as bad.
    if (result > 0x7fff) {
        return -1;
    } else {
        return result;
    }
}
