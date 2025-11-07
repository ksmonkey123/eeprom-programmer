#pragma once

namespace comms {
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
 * send a positive response with data
 *
 * @param buffer: NULL terminated buffer holding the error message.
 * If no data should be sent along, pass nullptr.
 */
void sendResponse(const char* buffer);
/**
 * send an error
 *
 * @param buffer: NULL terminated buffer holding the error message
 * If no data should be sent along, pass nullptr.
 */
void sendError(const char* buffer);
/**
 * send an error
 *
 * @param buffers: collection of the buffers to append together. every
 * referenced buffer must be null terminated.
 * @param buffer_count: number of items contained in buffers
 */
void sendError(const char** buffers, int buffer_count);
}  // namespace comms
