#include "commands.h"
#include "operations.h"
#include "communications.h"
#include "utils.h"

void cmd::lock() {
    ops::lockSDP();
    comms::sendResponse(nullptr);
}

void cmd::unlock() {
    ops::unlockSDP();
    comms::sendResponse(nullptr);
}

void cmd::read(address address) {
    byte data = ops::byteRead(address);
    char result[3];
    byteToHex(data, result);
    result[2] = '\0';
    comms::sendResponse(result);
}

void cmd::write(address address, byte data) {
    WriteResult result = ops::byteWrite(address, data);
    if (result.success) {
        comms::sendResponse(nullptr);
    } else {
        
    }
}