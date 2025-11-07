#pragma once
#include <HardwareSerial.h>

class Communications {
   private:
    HardwareSerial& channel;

   public:
    Communications(HardwareSerial& channel);

    /**
     * Receive the next command from the serial port.
     *
     * SYN commands and buffer errors are handled internally.
     *
     * @param buffer: input is written here
     * @param buffer_size: the size of the buffer. This limits the max. command
     * length.
     * @return length of received command.
     */
    int receiveNextCommand(char* buffer, int limit);
    /**
     * Get a printer for sending responses to the host
     */
    Print& getOutput();
};
