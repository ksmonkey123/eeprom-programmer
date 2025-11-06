#include "communications.h"
#include "initializers.h"
#include "status_indicator.h"

void initCommunications() {
    Serial.begin(230400);
    while (!Serial) {
        // wait for serial port to be available
    }
}

// buffer needs space for 1 command char, 4 address chars, 1 separator and 128
// data chars for page writes
static char buffer[1 + 4 + 1 + 128];

int consumeUntilNextLineBreak();
char readNextCharBlocking();

Command* receiveNextCommand() {
    int buffer_length = 0;
    while (true) {
        char c = readNextCharBlocking();

        if (c != '\n') {
            // if buffer already full, we need to throw everything out.
            if (buffer_length <= sizeof(buffer)) {
                // write into buffer.
                buffer[buffer_length++] = c;
            } else {
                setErrorIndicator(true);
                int overflow = consumeUntilNextLineBreak();
                Serial.print("-SYNTAX ERROR: COMMAND BUFFER OVERFLOW: ");
                for (int i = 0; i < sizeof(buffer); i++) {
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

        // handle command
        Command* result = new Command;
        result->operation = buffer[0];
        result->params = (buffer + 1);
        result->params_size = buffer_length - 1;
        return result;
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
    int counter = 0;
    while (true) {
        char c = readNextCharBlocking();
        if (c == '\n') {
            return counter;
        } else {
            counter++;
        }
    }
}

void sendResponse(const Response* response) {
    if (response->success) {
        Serial.print('+');
    } else {
        Serial.print('-');
    }

    int length = response->payload_size;
    for (int i = 0; i < length; i++) {
        Serial.print(response->payload[i]);
    }
    Serial.println();
    setErrorIndicator(!response->success);
}