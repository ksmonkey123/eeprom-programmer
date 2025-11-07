#include "communications.h"
#include "status_indicator.h"

static bool initialized = false;

void initCommunications() {
    if (initialized) {
        return;
    }

    Serial.begin(230400);
    while (!Serial) {
        // wait for serial port to be available
    }

    leds::indicateConnected();
}

int consumeUntilNextLineBreak();
char readNextCharBlocking();

int comms::receiveNextCommand(char* buffer, int limit) {
    initCommunications();

    int buffer_length = 0;
    while (true) {
        char c = readNextCharBlocking();

        if (c != '\n') {
            // if buffer already full, we need to throw everything out.
            if (buffer_length < limit) {
                // write into buffer.
                buffer[buffer_length++] = c;
            } else {
                leds::setErrorIndicator(true);
                int overflow = consumeUntilNextLineBreak();
                Serial.print("-SYNTAX ERROR: COMMAND BUFFER OVERFLOW: ");
                for (int i = 0; i < limit; i++) {
                    Serial.print(buffer[i]);
                }
                Serial.print("... (");
                Serial.print(overflow);
                Serial.print(" more chars)");
                Serial.println();
                buffer_length = 0;
            }
            continue;
        }

        // we have a new line, pre-parse command.
        if (buffer[0] == 'S' && buffer[1] == 'Y' && buffer[2] == 'N' &&
            buffer[3] == '_' && buffer_length == 6) {
            // handle sync directly.
            Serial.print(buffer[0]);
            Serial.print(buffer[1]);
            Serial.print(buffer[2]);
            Serial.print(buffer[3]);
            Serial.print(buffer[4]);
            Serial.print(buffer[5]);
            Serial.println();
            // reset buffer
            buffer_length = 0;
            continue;
        }

        if (buffer_length == 0) {
            Serial.print("-INVALID COMMAND: EMPTY LINE");
            Serial.println();
            continue;
        }

        return buffer_length;
    }
}

// helper functions
char readNextCharBlocking() {
    while (true) {
        if (Serial.available() == 0) {
            continue;
        }

        // we have data (ignore null char and \r)
        char c = Serial.read();
        if (c == 0 || c == '\r') {
            continue;
        }

        return c;
    }
}

int consumeUntilNextLineBreak() {
    int counter = 1;
    while (true) {
        char c = readNextCharBlocking();
        if (c == '\n') {
            return counter;
        } else {
            counter++;
        }
    }
}

void comms::sendResponse(const char* buffer) {
    Serial.print('+');
    if (buffer != nullptr) {
        Serial.print(buffer);
    }
    Serial.println();
}

void comms::sendError(const char* buffer) {
    Serial.print('-');
    if (buffer != nullptr) {
        Serial.print(buffer);
    }
    Serial.println();
}

void comms::sendError(const char** buffer, int buffer_size) {
    Serial.print('-');
    for (int i = 0; i < buffer_size; i++) {
        if (buffer[i] != nullptr) {
            Serial.print(buffer[i]);
        }
    }
    Serial.println();
}