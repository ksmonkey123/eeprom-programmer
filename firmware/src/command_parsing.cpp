#include "command_parsing.h"

#include "operations.h"
#include "utils.h"

Response* createHappyResponse() {
    return new Response { .success = true, .payload = "+", .payload_size = 1};
}

Response* executeCommand(const Command* command) {
    // parse no-arg operations
    if (command->operation == 'l' && command->params_size == 0) {
        lockSDP();
        return createHappyResponse();
    }
    if (command->operation == 'u' && command->params_size == 0) {
        unlockSDP();
        return createHappyResponse();
    }

    // anything else requires at least a 4 char address
    if (command->params_size < 4) {
        return new Response { .success = false, .payload = "SYNTAX ERROR: READ COMMAND REQUIRES EXACTLY 4 ADDRESS CHARS", .payload_size = 59 };
    }
    
}