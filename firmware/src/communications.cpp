#include "communications.h"

#include "status_indicator.h"

void initCommunications(HardwareSerial& serial) {
    // if (serial) {
    // we already initialized, nothing to do
    //     return;
    //}

    serial.begin(230400);
    while (!serial) {
        // wait for serial port to be available
    }
    leds::indicateConnected();
}

int consumeUntilNextLineBreak(HardwareSerial& serial);
char readNextCharBlocking(HardwareSerial& serial);

Communications::Communications(HardwareSerial& channel) : channel(channel) {
    initCommunications(channel);
}

Print& Communications::getOutput() { return channel; }

int Communications::receiveNextCommand(char* buffer, int limit) {
    int buffer_length = 0;
    while (true) {
        char c = readNextCharBlocking(channel);

        if (c != '\n') {
            // if buffer already full, we need to throw everything out.
            if (buffer_length < limit) {
                // write into buffer.
                buffer[buffer_length++] = c;
            } else {
                leds::setErrorIndicator(true);
                int overflow = consumeUntilNextLineBreak(channel);
                channel.print(F("-SYNTAX ERROR: COMMAND BUFFER OVERFLOW: "));
                for (int i = 0; i < limit; i++) {
                    channel.print(buffer[i]);
                }
                channel.print(F("... ("));
                channel.print(overflow);
                channel.println(F(" more chars)"));
                buffer_length = 0;
            }
            continue;
        }

        // we have a new line, pre-parse command.
        if (buffer[0] == 'S' && buffer[1] == 'Y' && buffer[2] == 'N' &&
            buffer[3] == '_' && buffer_length == 6) {
            // handle sync directly.
            channel.print(buffer[0]);
            channel.print(buffer[1]);
            channel.print(buffer[2]);
            channel.print(buffer[3]);
            channel.print(buffer[4]);
            channel.print(buffer[5]);
            channel.println();
            // reset buffer
            buffer_length = 0;
            continue;
        }

        if (buffer_length == 0) {
            channel.println(F("-INVALID COMMAND: EMPTY LINE"));
            continue;
        }

        return buffer_length;
    }
}

// helper functions
char readNextCharBlocking(HardwareSerial& serial) {
    while (true) {
        if (serial.available() == 0) {
            // use idle time waiting on new data for seeding the RNG
            random();

            continue;
        }

        // we have data (ignore null char and \r)
        char c = serial.read();
        if (c == 0 || c == '\r') {
            continue;
        }

        return c;
    }
}

int consumeUntilNextLineBreak(HardwareSerial& serial) {
    int counter = 1;
    while (true) {
        char c = readNextCharBlocking(serial);
        if (c == '\n') {
            return counter;
        } else {
            counter++;
        }
    }
}
