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

void setup() {}

void loop() {
    static char buffer[134];

    Communications comms(Serial);
    Print& output = comms.getOutput();
    CommandExecutor cmd(output);

    while (true) {
        int length = comms.receiveNextCommand(buffer, 134);

        char* args = buffer + 1;
        int len = length - 1;

        switch (buffer[0]) {
            case 'l':
                cmd.lock(args, len);
                break;
            case 'u':
                cmd.unlock(args, len);
                break;
            case 'r':
                cmd.read(args, len);
                break;
            case 'w':
                cmd.write(args, len);
                break;
            case 'p':
                cmd.pageRead(args, len);
                break;
            case 'x':
                cmd.pageWrite(args, len);
                break;
            default:
                output.print(F("-UNSUPPORTED OR MALFORMED COMMAND: "));
                for (int i = 0; i < length; i++) {
                    output.print(buffer[i]);
                }
                output.println();
                break;
        }
    }
}
