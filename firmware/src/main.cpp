#include "../include/command_executor.h"
#include "common.h"
#include "communications.h"

#define ADDRESS_BUS_O PORTF
#define ADDRESS_BUS_D DDRF

#define PAGE_BUS_O PORTA
#define PAGE_BUS_D DDRA

#define DATA_BUS_O PORTK
#define DATA_BUS_I PINK
#define DATA_BUS_D DDRK

void setup() {
    // no special init
}

__attribute__((noreturn)) void loop() {
    static char buffer[134];

    Communications comms(Serial);
    Print &output = comms.getOutput();
    CommandExecutor cmd(output);

    while (true) {
        int length = comms.receiveNextCommand(buffer, 134);

        char const *args = buffer + 1;
        int len = length - 1;

        switch (buffer[0]) {
            case 'l':
                cmd.lock(len);
                break;
            case 'u':
                cmd.unlock(len);
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
            case 's':
                cmd.pageSparseWrite(args, len);
                break;
            case 'i':
                cmd.identifyType(args, len);
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
