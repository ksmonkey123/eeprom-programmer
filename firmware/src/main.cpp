#include <Arduino.h>

#include "command_parsing.h"
#include "communications.h"
#include "initializers.h"
#include "status_indicator.h"
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

void setup() {
    initCommunications();
    initIndicators();
    initRom();

    while (true) {
        Command* command = receiveNextCommand();
        Response* response = executeCommand(command);
        sendResponse(response);
        delete command;
        delete response;
    }
}

static char buffer[256];
static int buffer_length = 0;

void processBuffer();

void loop() {
    if (Serial.available() == 0) {
        return;
    }

    // we have data
    char c = Serial.read();
    if (c == 0 || c == '\r') {
        return;
    }

    // process char
    if (c == '\n') {
        // end of command -> process
        processBuffer();
        buffer_length = 0;
        return;
    }

    // fill buffer
    if (buffer_length >= 256) {
        // buffer overflow -> complain and filter
        buffer_length = 0;
        return;
    }

    buffer[buffer_length++] = c;
}

void processSync();
void processReadCommand();
void processWriteCommand();
void processPageReadCommand();
void processPageWriteCommand();
void processLockCommand();
void processUnlockCommand();

void read(int address);
void write(int address, byte data);
void pageRead(int address);
void pageWrite(int address, byte* data);

void chipEnable(bool state);
void selectAddress(int addr);

void processBuffer() {
    // TODO
    if (buffer[0] == 'S' && buffer[1] == 'Y' && buffer[2] == 'N' &&
        buffer[3] == '_' && buffer_length == 6) {
        processSync();
    } else if (buffer[0] == 'r') {
        processReadCommand();
    } else if (buffer[0] == 'w') {
        processWriteCommand();
    } else if (buffer[0] == 'p') {
        processPageReadCommand();
    } else if (buffer[0] == 'x') {
        processPageWriteCommand();
    } else if (buffer[0] == 'l' && buffer_length == 1) {
        processLockCommand();
    } else if (buffer[0] == 'u' && buffer_length == 1) {
        processUnlockCommand();
    } else {
        setErrorIndicator(true);
        Serial.print("-SYNTAX ERROR: INVALID COMMAND: ");
        for (int i = 0; i < buffer_length; i++) {
            Serial.print(buffer[i]);
        }
        Serial.println();
    }
}

void processSync() {
    for (int i = 0; i < 6; i++) {
        Serial.print(buffer[i]);
    }
    Serial.println();
    setErrorIndicator(false);
}

void processReadCommand() {
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

    // address OK, execute read
    read(adr);
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

// -------------------------------------------------------
// CHIP LOGIC

void read(int address) {
    chipEnable(true);
    selectAddress(address);
    digitalWrite(OUTPUT_ENABLE_PIN, false);
    byte readData = DATA_BUS_I;
    digitalWrite(OUTPUT_ENABLE_PIN, true);
    chipEnable(false);

    Serial.write("+");
    if (readData < 16) {
        Serial.print('0');
    }
    Serial.println(readData, HEX);
    setErrorIndicator(false);
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

void _quickSingleWrite(int address, byte data) {
    selectAddress(address);
    DATA_BUS_O = data;
    digitalWrite(WRITE_ENABLE_PIN, false);
    digitalWrite(WRITE_ENABLE_PIN, true);
}

void processUnlockCommand() {
    setWriteIndicator(true);

    DATA_BUS_D = 0xff;
    chipEnable(true);

    _quickSingleWrite(0x5555, 0xaa);
    _quickSingleWrite(0x2aaa, 0x55);
    _quickSingleWrite(0x5555, 0x80);
    _quickSingleWrite(0x5555, 0xaa);
    _quickSingleWrite(0x2aaa, 0x55);
    _quickSingleWrite(0x5555, 0x20);

    delay(10);

    DATA_BUS_D = 0x00;
    DATA_BUS_O = 0x00;

    chipEnable(false);
    setErrorIndicator(false);
    setWriteIndicator(false);

    Serial.println("+");
}

void processLockCommand() {
    setWriteIndicator(true);

    DATA_BUS_D = 0xff;
    chipEnable(true);

    _quickSingleWrite(0x5555, 0xaa);
    _quickSingleWrite(0x2aaa, 0x55);
    _quickSingleWrite(0x5555, 0xa0);

    delay(10);

    DATA_BUS_D = 0x00;
    DATA_BUS_O = 0x00;

    chipEnable(false);

    setWriteIndicator(false);
    setErrorIndicator(false);
    Serial.println("+");
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

void selectAddress(int addr) {
    PAGE_BUS_O = (addr >> 8) & 0x007f;
    ADDRESS_BUS_O = addr & 0x00ff;
}

void chipEnable(bool state) {
    digitalWrite(CHIP_ENABLE_PIN, !state);
    setActiveIndicator(state);
}
