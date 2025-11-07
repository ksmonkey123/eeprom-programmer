#include "rom.h"

#include "status_indicator.h"

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

void initRom() {
    PAGE_BUS_D = 0x7f;
    ADDRESS_BUS_D = 0xff;
    DATA_BUS_D = 0x00;
    DATA_BUS_O = 0x00;
    digitalWrite(CHIP_ENABLE_PIN, true);
    digitalWrite(OUTPUT_ENABLE_PIN, true);
    digitalWrite(WRITE_ENABLE_PIN, true);
    pinMode(CHIP_ENABLE_PIN, OUTPUT);
    pinMode(OUTPUT_ENABLE_PIN, OUTPUT);
    pinMode(WRITE_ENABLE_PIN, OUTPUT);
}

void rom::startAccess() {
    initRom();
    leds::setActiveIndicator(true);
    digitalWrite(CHIP_ENABLE_PIN, false);
    digitalWrite(OUTPUT_ENABLE_PIN, false);
}

void rom::endAccess() {
    digitalWrite(OUTPUT_ENABLE_PIN, true);
    digitalWrite(CHIP_ENABLE_PIN, true);
    leds::setActiveIndicator(false);
}

void rom::startWriteCycle() {
    leds::setWriteIndicator(true);
    digitalWrite(OUTPUT_ENABLE_PIN, true);
    DATA_BUS_D = 0xff;
    DATA_BUS_O = 0x00;
}

void rom::endWriteCycle() {
    // we need a delay at the end of a write cycle to let the chip finish up.
    delay(10);
    DATA_BUS_D = 0x00;
    DATA_BUS_O = 0x00;
    digitalWrite(OUTPUT_ENABLE_PIN, false);
    leds::setWriteIndicator(false);
}

byte rom::read(address address) {
    PAGE_BUS_O = (address >> 8) & 0x007f;
    ADDRESS_BUS_O = address & 0x00ff;
    return DATA_BUS_I;
}

void rom::write(address address, byte data) {
    PAGE_BUS_O = (address >> 8) & 0x007f;
    ADDRESS_BUS_O = address & 0x00ff;
    DATA_BUS_O = data;
    // short pulse enables page writes
    digitalWrite(WRITE_ENABLE_PIN, false);
    digitalWrite(WRITE_ENABLE_PIN, true);
}