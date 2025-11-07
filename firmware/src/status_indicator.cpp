#include "status_indicator.h"

#define CHIP_ENABLE_LED 32
#define WRITE_LED 31
#define ERROR_LED 33

void leds::setActiveIndicator(bool state) {
    pinMode(CHIP_ENABLE_LED, OUTPUT);
    digitalWrite(CHIP_ENABLE_LED, state);
}

void leds::setWriteIndicator(bool state) {
    pinMode(WRITE_LED, OUTPUT);
    digitalWrite(WRITE_LED, state);
}

void leds::setErrorIndicator(bool state) {
    pinMode(ERROR_LED, OUTPUT);
    digitalWrite(ERROR_LED, state);
}

void leds::indicateConnected() {
    setActiveIndicator(true);
    setWriteIndicator(true);
    setErrorIndicator(true);
    delay(100);
    setActiveIndicator(false);
    setWriteIndicator(false);
    setErrorIndicator(false);
}