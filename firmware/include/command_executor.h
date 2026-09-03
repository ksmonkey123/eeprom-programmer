#pragma once
#include "common.h"

class CommandExecutor {
    Print &output;

public:
    explicit CommandExecutor(Print &output);

    void pageRead(const char *args, int len);

    void pageWrite(const char *args, int len);

    void lock(int len);

    void unlock(int len);

    void identifyType(int len);
};
