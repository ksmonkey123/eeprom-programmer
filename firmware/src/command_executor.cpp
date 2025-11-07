#include "command_executor.h"

#include "operations.h"
#include "utils.h"

CommandExecutor::CommandExecutor(Print& output) : output(output) {};

void CommandExecutor::lock() {
    ops::lockSDP();
    output.println('+');
}

void CommandExecutor::unlock() {
    ops::unlockSDP();
    output.println('+');
}

void CommandExecutor::read(address address) {
    byte data = ops::byteRead(address);
    output.print('+');
    output.print(lowNibbleToHexChar(data >> 4));
    output.print(lowNibbleToHexChar(data));
    output.println();
}

void sendWriteResult(WriteResult& result, Print& output) {
    if (result.success) {
        output.println('+');
    } else {
        output.print("-WRITE CHECK ERROR: ADDRESS ");
        output.print(lowNibbleToHexChar(result.error_address >> 12));
        output.print(lowNibbleToHexChar(result.error_address >> 8));
        output.print(lowNibbleToHexChar(result.error_address >> 4));
        output.print(lowNibbleToHexChar(result.error_address));
        output.print(" EXPECTED ");
        output.print(lowNibbleToHexChar(result.error_expected >> 4));
        output.print(lowNibbleToHexChar(result.error_expected));
        output.print(" BUT READ ");
        output.print(lowNibbleToHexChar(result.error_actual >> 4));
        output.print(lowNibbleToHexChar(result.error_actual));
        output.println();
    }
}

void CommandExecutor::write(address address, byte data) {
    WriteResult result = ops::byteWrite(address, data);
    sendWriteResult(result, output);
}

void CommandExecutor::pageRead(address address) {
    byte data[64];
    ops::pageRead(address, data);

    output.print('+');
    for (byte i = 0; i < 64; i++) {
        output.print(lowNibbleToHexChar(data[i] >> 4));
        output.print(lowNibbleToHexChar(data[i]));
    }
    output.println();
}

void CommandExecutor::pageWrite(address address, const byte* data) {
    WriteResult result = ops::pageWrite(address, data);
    sendWriteResult(result, output);
}
