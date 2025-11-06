#pragma once
#include <Arduino.h>

struct Command {
    char operation;
    char* params;
    int params_size;
};

struct Response {
    bool success;
    char* payload;
    int payload_size;
};