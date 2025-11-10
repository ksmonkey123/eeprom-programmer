#include "command_executor.h"

#include "operations.h"
#include "utils.h"

bool parseAddress(const char* buffer, address* dest, bool pageAddress,
                  Print& output) {
    bool success = hexToAddress(buffer, dest);
    // bad address format
    if (!success) {
        output.print(F("-BAD ADDRESS VALUE: "));
        output.print(buffer[0]);
        output.print(buffer[1]);
        output.print(buffer[2]);
        output.println(buffer[3]);
        return false;
    }
    // validate page boundary if necessary
    if (pageAddress && ((*dest & 0x003f) > 0)) {
        output.print(F("-ADDRESS MUST BE AT PAGE START: "));
        output.print(buffer[0]);
        output.print(buffer[1]);
        output.print(buffer[2]);
        output.println(buffer[3]);
        return false;
    }
    return true;
}

bool parseData(const char* buffer, byte* dest, Print& output) {
    bool success = hexToByte(buffer, dest);
    if (!success) {
        output.print(F("-BAD DATA VALUE: "));
        output.print(buffer[0]);
        output.print(buffer[1]);
    }
    return success;
}

bool parseDataBlock(const char* buffer, byte* dest, int bytes, Print& output) {
    for (int i = 0; i < bytes; i++) {
        if (!parseData(buffer + i, dest + i, output)) {
            return false;
        }
    }
    return true;
}

bool validateLength(int actual, int expected, Print& output) {
    if (actual != expected) {
        output.print(F("-ILLEGAL COMMAND LENGTH. EXPECTED "));
        output.print(expected);
        output.print(F(" CHARS BUT RECEIVED "));
        output.print(actual);
        output.println(" CHARS.");
        return false;
    }
    return true;
}

bool validateChar(const char* args, int position, char expected,
                  Print& output) {
    if (args[position] != expected) {
        output.print(F("-UNEXPECTED CHARACTER "));
        output.print(args[position]);
        output.print(F(" FOUND AT POSITION "));
        output.print(position);
        output.print(F(". EXPECTED "));
        output.println(expected);
        return false;
    }
    return true;
}

void sendWriteResult(WriteResult& result, Print& output) {
    if (result.success) {
        output.println('+');
    } else {
        output.print(F("-WRITE CHECK ERROR: ADDRESS "));
        printAddress(result.error_address, output);
        output.print(F(" EXPECTED "));
        printData(result.error_expected, output);
        output.print(F(" BUT READ "));
        printData(result.error_actual, output);
        output.println();
    }
}

CommandExecutor::CommandExecutor(Print& output) : output(output) {};

void CommandExecutor::lock(const char* args, int len) {
    if (validateLength(len, 0, output)) {
        ops::lockSDP();
        output.println('+');
    }
}

void CommandExecutor::unlock(const char* args, int len) {
    if (validateLength(len, 0, output)) {
        ops::unlockSDP();
        output.println('+');
    }
}

void CommandExecutor::sizeTest(const char* args, int len) {
    if (validateLength(len, 0, output)) {
        ChipSize size;
        WriteResult result = ops::sizeTest(&size);
        if (result.success) {
            switch (size) {
                case SMALL:
                    output.println("+S");
                    break;
                case LARGE:
                    output.println("+L");
                    break;
                default:
                    output.println("-Invalid Test Result");
                    break;
            }
        } else {
            sendWriteResult(result, output);
        }
    }
}

void CommandExecutor::read(const char* args, int len) {
    address adr;
    if (validateLength(len, 4, output) &&
        parseAddress(args, &adr, false, output)) {
        byte data = ops::byteRead(adr);
        output.print('+');
        printData(data, output);
        output.println();
    }
}

void CommandExecutor::write(const char* args, int len) {
    address adr;
    byte data;
    if (validateLength(len, 7, output) &&
        parseAddress(args, &adr, false, output) &&
        validateChar(args, 4, ':', output) &&
        parseData(args + 5, &data, output)) {
        WriteResult result = ops::byteWrite(adr, data);
        sendWriteResult(result, output);
    }
}

void CommandExecutor::pageRead(const char* args, int len) {
    address adr;
    if (validateLength(len, 4, output) &&
        parseAddress(args, &adr, true, output)) {
        byte data[64];
        ops::pageRead(adr, data);
        output.print('+');
        for (byte i = 0; i < 64; i++) {
            printData(data[i], output);
        }
        output.println();
    }
}

void CommandExecutor::pageWrite(const char* args, int len) {
    address adr;
    byte data[64];
    if (validateLength(len, 133, output) &&
        parseAddress(args, &adr, true, output) &&
        validateChar(args, 4, ':', output) &&
        parseDataBlock(args + 5, data, 64, output)) {
        WriteResult result = ops::pageWrite(adr, data);
        sendWriteResult(result, output);
    }
}
