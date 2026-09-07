#pragma once
#include "common.h"

class CommandExecutor {
    Print &output;

public:
    explicit CommandExecutor(Print &output);

    void pageRead(const char *args, int len) const;

    void pageWrite(const char *args, int len) const;

    void lock(int len) const;

    void unlock(int len) const;

    void identifyType(int len) const;
};
