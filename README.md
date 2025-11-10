# eeprom-programmer
Arduino-based EEPROM programmer for the AT28C64B (8 KiB) and AT28C256 (32 KiB) EEPROMs.

## Design
The EEPROM programmer consists of 2 parts:
A _Programmer Device_ based consiting of an Arduino Mega 2560 with a daughter-board containing an IC socket for the EEPROM chips (DIP-28P6).
This _Programmer Device_ interacts with a _Client Program_ on the PC.

## Features
### Software Data Protection
The programmer supports enabling and disabling of the _Software Data Protection_ (Write Lockout) mode on the EEPROM.
This feature can be used as a standalone command (`<cmd> lock` and `<cmd> unlock`), and in combination with other _write operations_.
It is even possible to unlock an EEPROM, write to it, and lock it again in a single operation.

### Automatic EEPROM size determination
Since the programmer supports 2 different sizes of EEPROMs, the user would have to indicate what type is connected to the programmer for any given command.
The _Client Program_ is however capable of "guessing" the safe EEPROM size for most operations.
In some cases this may lead to additional unnecessary write cycles on smaller chips (which we consider to be a negligible downside) and potentially increased operation durations.

The following pseudo-C shows the test routine to definitely determine the EEPROM size.
This test can be performed both destructively and non-desctructively.
In all cases the EEPROM *must* be unlocked beforehand.

```c
bool isLargeChip(bool preserveData) {
  address adr = random(0x0000, 0x2000);
  byte data = read(adr);

  // if there's different data in the higher "blocks", it obviously is a large chip
  if (data != read(adr + 0x2000)) {
    return true;
  }

  // modify low byte, check if high byte changes too
  write(adr, ~data);
  //high byte unchanged means large chip
  bool result = (data == read(adr + 0x2000));

  if (preserveData) {
    write(adr, data);
  }
  return result;
}
```

## Operations
### Live Mode
The live mode creates an interactive terminal between the user and the _Programmer Device_ for interactive manipulation of the EEPROM.
This is primarily used for testing or analysis purposes.
Lock- and Unlock-Flags are not respected in this mode (there are manual unlock and lock commands), neither are size flags as they are irrelevant in this mode.

### Erase
The erase opration writes `0xFF` to every byte of the EEPROM memory.
When an explicit size flag is set, the corresponding delete logic is performed.
Otherwise, we perform a destructive _size test_ before flashing the EEPROM.

### Dump
The dump command reads the entire contents of the EEPROM.
If a size is specified, the output file has the matching size.
If no size is specified, the EEPROM is read as if it were a 32 KiB one.
After that the data is checked.
If all 4 8 KiB blocks match _exactly_, we assume to have read an 8 KiB EEPROM and write the output file with the matching size.
If the upper 16 KiB are identical to the lower 16 KiB, we write a 16 KiB file.
If there are any differences in the blocks, we save the entire 32 KiB.

### Flash
The flash command writes a binary file to the EEPROM.
The logic for how different EEPROM sizes are handled is a bit more complex.
In cases where no chip size is provided, we perform a non-desctructive _size test_ before flashing the EEPROM.

| File Size | Chip Size | Behaviour                       |
|----------:|----------:|:--------------------------------|
|     8 KiB |     8 KiB | write file to EEPROM            |
|     8 KiB |    32 KiB | write files 4 times to EEPROM   |
|    16 KiB |    32 KiB | write file 2 times to EEPROM    |
|    32 KiB |    32 KiB | write file to EEPROM            |

All other cases lead to errors.

### Unlock
While the EEPROMs can be unlocked during write operations (erase / flash), a dedicated unlock command is also provided.
Size information is not required for this operation.

### Lock
While the EEPROMs can be locked during write operations (erase / flash), a dedicated lock command is also provided.
Size information is not required for this operation.
