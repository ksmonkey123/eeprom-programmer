#include "status_indicator.h"
#include "initializers.h"

#define CHIP_ENABLE_LED 32
#define WRITE_LED 31
#define ERROR_LED 33

void initIndicators() {
  digitalWrite(CHIP_ENABLE_LED, true);
  digitalWrite(ERROR_LED, true);
  digitalWrite(WRITE_LED, true);
  pinMode(CHIP_ENABLE_LED, OUTPUT);
  pinMode(ERROR_LED, OUTPUT);
  pinMode(WRITE_LED, OUTPUT);
  delay(100);
  digitalWrite(CHIP_ENABLE_LED, false);
  digitalWrite(ERROR_LED, false);
  digitalWrite(WRITE_LED, false);
}

void setActiveIndicator(bool state) { digitalWrite(CHIP_ENABLE_LED, state); }

void setWriteIndicator(bool state) { digitalWrite(WRITE_LED, state); }

void setErrorIndicator(bool state) { digitalWrite(ERROR_LED, state); }
