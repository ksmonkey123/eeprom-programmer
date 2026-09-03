#include "../include/command_executor.h"

#include "../include/operations.h"
#include "../include/utils.h"

static bool parseAddress(const char *buffer, address *dest, bool pageAddress,
                         Print &output) {
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

static bool parseData(const char *buffer, byte *dest, Print &output) {
    bool success = hexToByte(buffer, dest);
    if (!success) {
        output.print(F("-BAD DATA VALUE: "));
        output.print(buffer[0]);
        output.print(buffer[1]);
    }
    return success;
}

static bool parseSparseDataBlock(const char *buffer,
                                 const byte *currentData,
                                 SparsePageElement *dest,
                                 int *countDest,
                                 int bytes,
                                 Print &output) {
    *countDest = 0;
    for (int i = 0; i < bytes; i++) {
        if (buffer[2 * i] == '.' && buffer[2 * i + 1] == '.') {
            // skip element
            continue;
        }
        byte data;
        if (!parseData(buffer + (2 * i), &data, output)) {
            return false;
        }

        if (data == currentData[i]) {
            // data is identical, no need to update
            continue;
        }

        // we have found a new element
        dest[*countDest].offset = i;
        dest[*countDest].data = data;
        (*countDest)++;
    }
    return true;
}

static bool validateLength(int actual, int expected, Print &output) {
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

static bool validateChar(const char *args, int position, char expected,
                         Print &output) {
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

static void sendWriteResult(WriteResult &result, Print &output) {
    if (result.success) {
        output.println('+');
    } else {
        output.print(F("-WRITE CHECK ERROR: ADDRESS "));
        printAddress(result.error.error_address, output);
        output.print(F(" EXPECTED "));
        printData(result.error.error_expected, output);
        output.print(F(" BUT READ "));
        printData(result.error.error_actual, output);
        output.println();
    }
}

CommandExecutor::CommandExecutor(Print &output) : output(output) {
}

void CommandExecutor::lock(int len) {
    if (validateLength(len, 0, output)) {
        ops::lockSDP();
        output.println('+');
    }
}

void CommandExecutor::unlock(int len) {
    if (validateLength(len, 0, output)) {
        ops::unlockSDP();
        output.println('+');
    }
}

void printTypeResult(ChipType type, Print &output) {
    switch (type) {
        case SMALL_SOCKET:
            output.println(F("+SS"));
            break;
        case LARGE_SOCKET:
            output.println(F("+LS"));
            break;
        default:
            output.println(F("-INVALID TYPE"));
            break;
    }
}

void CommandExecutor::identifyType(int len) {
    if (validateLength(len, 0, output)) {
        ChipType size;
        WriteResult result = ops::identifyType(&size);
        if (result.success) {
            printTypeResult(size, output);
        } else {
            sendWriteResult(result, output);
        }
    }
}

void CommandExecutor::pageRead(const char *args, int len) {
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

static bool readCurrentData(address adr, byte *data) {
    ops::pageRead(adr, data);
    return true;
}

void CommandExecutor::pageWrite(const char *args, int len) {
    address adr;
    SparsePageElement elements[64];
    int nelements;
    byte currentData[64];

    if (validateLength(len, 133, output) &&
        parseAddress(args, &adr, true, output) &&
        validateChar(args, 4, ':', output) &&
        readCurrentData(adr, currentData) &&
        parseSparseDataBlock(args + 5, currentData, elements, &nelements, 64, output)) {
        WriteResult result = ops::pageWrite(adr, elements, nelements);
        sendWriteResult(result, output);
    }
}
