#pragma once
#include "common.h"

class CommandExecutor {
   private:
    Print& output;

   public:
    CommandExecutor(Print& output);

    void read(const char* args, int len);
    void write(const char* args, int len);

    void pageRead(const char* args, int len);
    void pageWrite(const char* args, int len);

    void lock(const char* args, int len);
    void unlock(const char* args, int len);

    void sizeTest(const char* args, int len);
};
