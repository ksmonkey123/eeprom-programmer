#include "commands.h"
#include "common.h"
#include "communications.h"
#include "operations.h"
#include "utils.h"

#define ADDRESS_BUS_O PORTF
#define ADDRESS_BUS_D DDRF

#define PAGE_BUS_O PORTA
#define PAGE_BUS_D DDRA

#define DATA_BUS_O PORTK
#define DATA_BUS_I PINK
#define DATA_BUS_D DDRK

#define CHIP_ENABLE_PIN 10
#define OUTPUT_ENABLE_PIN 11
#define WRITE_ENABLE_PIN 12

address parseAddress(const char* buffer) {
    address result = hexToAddress(buffer);
    if (result == (address)-1) {
        char const* buffers[2];
        buffers[0] = "BAD ADDRESS VALUE: ";
        buffers[1] = substring(buffer, 4);
        comms::sendError(buffers, 2);
        delete buffers[1];
    }
    return result;
}

address parsePageAddress(const char* buffer) {
    address result = parseAddress(buffer);
    // address invalid
    if (result == (address)-1) {
        return result;
    }
    // validate page boundary
    if ((result & 0x003f) > 0) {
        char const* buffers[2];
        buffers[0] = "ADDRESS MUST BE AT PAGE START";
        buffers[1] = substring(buffer, 4);
        comms::sendError(buffers, 2);
        delete buffers[1];
        return (address)-1;
    }
    // everything is ok
    return result;
}

// max length is 134. set buffer to 135 to allow for injection of a null terminator
static char buffer[135];

int main() {
    while (true) {
        int length = comms::receiveNextCommand(buffer, 134);

        if (buffer[0] == 'l' && length == 1) {
            cmd::lock();
        } else if (buffer[0] == 'u' && length == 1) {
            cmd::unlock();
        } else if (buffer[0] == 'r' && length == 5) {
            address adr = parseAddress(buffer + 1);
            if (adr != (address)-1) {
                cmd::read(adr);
            }
        } else if (buffer[0] == 'w' && length == 8 && buffer[5] == ':') {
            address adr = parseAddress(buffer + 1);
            if (adr != (address)-1) {
                cmd::write(adr, hexToByte(buffer + 6));
            }
        } else if (buffer[0] == 'p' && length == 5) {
            address adr = parsePageAddress(buffer + 1);
            if (adr != (address)-1) {
                cmd::pageRead(adr);
            }
        } else if (buffer[0] == 'x' && length == (1 + 4 + 1 + (64 * 2)) &&
                   buffer[5] == ':') {
            address adr = parsePageAddress(buffer + 1);
            if (adr != (address)-1) {
                byte data[64];
                for (byte i = 0; i < 64; i++) {
                    data[i] = hexToByte(buffer + 6 + (2 * i));
                }
                cmd::pageWrite(adr, data);
            }
        } else {
            buffer[length] = '\0';
            char const* buffers[2];
            buffers[0] = "UNSUPPORTED OR MALFORMED COMMAND: ";
            buffers[1] = buffer;
            comms::sendError(buffers, 2);
        }
    }
}

void processPageReadCommand() {
    if (buffer_length != 5) {
        setErrorIndicator(true);
        Serial.println(
            "-SYNTAX ERROR: READ COMMAND REQUIRES EXACTLY 4 ADDRESS CHARS");
        return;
    }

    // extract address
    int adrHigh = hexToByte(buffer + 1);
    int adrLow = hexToByte(buffer + 3);

    if (adrHigh == -1 || adrLow == -1) {
        setErrorIndicator(true);
        Serial.println("-SYNTAX ERROR: ADDRESS REQUIRES 4 HEX DIGITS");
        return;
    }

    // assemble address
    int adr = ((adrHigh << 8) & 0xff00) | (adrLow & 0x00ff);

    if (adr < 0 || adr >= 0x8000) {
        setErrorIndicator(true);
        Serial.println("-SYNTAX ERROR: ADDRESS MUST BE IN RANGE 0000..7FFF");
        return;
    }

    if ((adr & 0x003f) > 0) {
        setErrorIndicator(true);
        Serial.println("-SYNTAX ERROR: ADDRESS MUST BE A PAGE START");
        return;
    }

    // address OK, execute read
    pageRead(adr);
}

void processWriteCommand() {
    if (buffer_length != 8) {
        setErrorIndicator(true);
        Serial.println(
            "-SYNTAX ERROR: WRITE COMMAND REQUIRES EXACTLY 4 ADDRESS CHARS, 1 "
            "SEPARATOR AND 2 DATA CHARS");
        return;
    }

    // extract address
    int adrHigh = hexToByte(buffer + 1);
    int adrLow = hexToByte(buffer + 3);

    if (adrHigh == -1 || adrLow == -1) {
        setErrorIndicator(true);
        Serial.println("-SYNTAX ERROR: ADDRESS REQUIRES 4 HEX DIGITS");
        return;
    }

    // assemble address
    int adr = ((adrHigh << 8) & 0xff00) | (adrLow & 0x00ff);

    if (adr < 0 || adr >= 0x8000) {
        setErrorIndicator(true);
        Serial.println("-SYNTAX ERROR: ADDRESS MUST BE IN RANGE 0000..7FFF");
        return;
    }

    // address OK
    if (buffer[5] != ':') {
        setErrorIndicator(true);
        Serial.println(
            "-SYNTAX ERROR: WRITE COMMAND NEEDS SEPARATOR AS SIXTH CHAR");
        return;
    }

    int data = hexToByte(buffer + 6);
    if (data == -1) {
        setErrorIndicator(true);
        Serial.println("-SYNTAX ERROR: DATA REQUIRES 2 HEX DIGITS");
        return;
    }

    write(adr, data & 0x00ff);
}

void processPageWriteCommand() {
    if (buffer_length != 134) {
        setErrorIndicator(true);
        Serial.println(
            "-SYNTAX ERROR: WRITE COMMAND REQUIRES EXACTLY 4 ADDRESS CHARS, 1 "
            "SEPARATOR AND 128 DATA CHARS");
        return;
    }

    // extract address
    int adrHigh = hexToByte(buffer + 1);
    int adrLow = hexToByte(buffer + 3);

    if (adrHigh == -1 || adrLow == -1) {
        setErrorIndicator(true);
        Serial.println("-SYNTAX ERROR: ADDRESS REQUIRES 4 HEX DIGITS");
        return;
    }

    // assemble address
    int adr = ((adrHigh << 8) & 0xff00) | (adrLow & 0x00ff);

    if (adr < 0 || adr >= 0x8000) {
        setErrorIndicator(true);
        Serial.println("-SYNTAX ERROR: ADDRESS MUST BE IN RANGE 0000..7FFF");
        return;
    }

    if ((adr & 0x003f) > 0) {
        setErrorIndicator(true);
        Serial.println("-SYNTAX ERROR: ADDRESS MUST BE A PAGE START");
        return;
    }

    // address OK
    if (buffer[5] != ':') {
        setErrorIndicator(true);
        Serial.println(
            "-SYNTAX ERROR: WRITE COMMAND NEEDS SEPARATOR AS SIXTH CHAR");
        return;
    }

    // build data buffer
    byte data[64];
    for (int i = 0; i < 64; i++) {
        data[i] = hexToByte(buffer + 6 + (2 * i));
    }

    pageWrite(adr, data);
}

void pageRead(int address) {
    Serial.write("+");

    chipEnable(true);
    digitalWrite(OUTPUT_ENABLE_PIN, false);
    for (int i = 0; i < 64; i++) {
        selectAddress(address + i);
        delay(1);
        byte readData = DATA_BUS_I;

        if (readData < 16) {
            Serial.print('0');
        }
        Serial.print(readData, HEX);
    }
    digitalWrite(OUTPUT_ENABLE_PIN, true);
    chipEnable(false);

    Serial.println();
    setErrorIndicator(false);
}

void write(int address, byte data) {
    chipEnable(true);
    selectAddress(address);
    setWriteIndicator(true);

    DATA_BUS_D = 0xff;
    DATA_BUS_O = data;

    digitalWrite(WRITE_ENABLE_PIN, false);
    digitalWrite(WRITE_ENABLE_PIN, true);
    delay(10);

    DATA_BUS_D = 0x00;
    DATA_BUS_O = 0x00;

    setWriteIndicator(false);

    digitalWrite(OUTPUT_ENABLE_PIN, false);
    byte readData = DATA_BUS_I;
    digitalWrite(OUTPUT_ENABLE_PIN, true);
    chipEnable(false);

    if (readData == data) {
        setErrorIndicator(false);
        Serial.println("+");
    } else {
        setErrorIndicator(true);
        Serial.print("-WRITE CHECK ERROR: ADDRESS ");
        Serial.print(address, HEX);
        Serial.print(" EXPECTED ");
        Serial.print(data, HEX);
        Serial.print(" BUT READ ");
        Serial.println(readData, HEX);
    }
}

void pageWrite(int address, byte* data) {
    chipEnable(true);
    setWriteIndicator(true);

    DATA_BUS_D = 0xff;
    for (int i = 0; i < 64; i++) {
        selectAddress(address + i);
        DATA_BUS_O = *(data + i);
        digitalWrite(WRITE_ENABLE_PIN, false);
        digitalWrite(WRITE_ENABLE_PIN, true);
    }
    delay(10);

    DATA_BUS_O = 0x00;

    setWriteIndicator(false);
    DATA_BUS_D = 0x00;

    // verify data
    digitalWrite(OUTPUT_ENABLE_PIN, false);

    for (int i = 0; i < 64; i++) {
        selectAddress(address + i);
        delay(1);
        byte readData = DATA_BUS_I;

        if (readData != *(data + i)) {
            setErrorIndicator(true);
            Serial.print("-WRITE CHECK ERROR: ADDRESS ");
            Serial.print(address + i, HEX);
            Serial.print(" EXPECTED ");
            Serial.print(*(data + i), HEX);
            Serial.print(" BUT READ ");
            Serial.println(readData, HEX);
            digitalWrite(OUTPUT_ENABLE_PIN, true);
            chipEnable(false);
            return;
        }
    }
    setErrorIndicator(false);
    Serial.println("+");
    digitalWrite(OUTPUT_ENABLE_PIN, true);
    chipEnable(false);
}
