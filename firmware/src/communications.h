#pragma once
#include "types.h"

Command* receiveNextCommand();
void sendResponse(const Response* response);