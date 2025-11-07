#include "command_executor.h"
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

address parseAddress(const char* buffer, Print& output) {
    address result = hexToAddress(buffer);
    if (result == (address)-1) {
        output.print("-BAD ADDRESS VALUE: ");
        output.print(buffer[0]);
        output.print(buffer[1]);
        output.print(buffer[2]);
        output.println(buffer[3]);
    }
    return result;
}

address parsePageAddress(const char* buffer, Print& output) {
    address result = parseAddress(buffer, output);
    // address invalid
    if (result == (address)-1) {
        return result;
    }
    // validate page boundary
    if ((result & 0x003f) > 0) {
        output.print("-ADDRESS MUST BE AT PAGE START: ");
        output.print(buffer[0]);
        output.print(buffer[1]);
        output.print(buffer[2]);
        output.println(buffer[3]);
        return (address)-1;
    }
    // everything is ok
    return result;
}

// max length is 134. set buffer to 135 to allow for injection of a null
// terminator
static char buffer[135];

void setup() {}

void loop() {
    Communications comms(Serial);
    Print& output = comms.getOutput();
    CommandExecutor cmd(output);

    while (true) {
        int length = comms.receiveNextCommand(buffer, 134);
        Print& output = comms.getOutput();
        if (buffer[0] == 'l') {
            cmd.lock();
        } else if (buffer[0] == 'u') {
            cmd.unlock();
        } else if (buffer[0] == 'r' && length == 5) {
            address adr = parseAddress(buffer + 1, output);
            if (adr != (address)-1) {
                cmd.read(adr);
            }
        } else if (buffer[0] == 'w' && length == 8 && buffer[5] == ':') {
            address adr = parseAddress(buffer + 1, output);
            if (adr != (address)-1) {
                cmd.write(adr, hexToByte(buffer + 6));
            }
        } else if (buffer[0] == 'p' && length == 5) {
            address adr = parsePageAddress(buffer + 1, output);
            if (adr != (address)-1) {
                cmd.pageRead(adr);
            }
        } else if (buffer[0] == 'x' && length == (1 + 4 + 1 + (64 * 2)) &&
                   buffer[5] == ':') {
            address adr = parsePageAddress(buffer + 1, output);
            if (adr != (address)-1) {
                byte data[64];
                for (byte i = 0; i < 64; i++) {
                    data[i] = hexToByte(buffer + 6 + (2 * i));
                }
                cmd.pageWrite(adr, data);
            }
        } else {
            output.print("-UNSUPPORTED OR MALFORMED COMMAND: ");
            for (int i = 0; i < length; i++) {
                output.print(buffer[i]);
            }
            output.println();
        }
    }
}
