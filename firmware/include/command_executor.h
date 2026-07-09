#pragma once
#include "common.h"

class CommandExecutor {
    Print &output;

public:
    explicit CommandExecutor(Print &output);

    void read(const char *args, int len);

    void write(const char *args, int len);

    void pageRead(const char *args, int len);

    void pageWrite(const char *args, int len);

    void pageSparseWrite(const char *args, int len);

    void lock(int len);

    void unlock(int len);

    void identifyType(const char *args, int len);
};
