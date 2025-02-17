#define ADDRESS_BUS_O PORTF
#define ADDRESS_BUS_D DDRF

#define PAGE_BUS_O PORTA
#define PAGE_BUS_D DDRA

#define DATA_BUS_O PORTK
#define DATA_BUS_I PINK
#define DATA_BUS_D DDRK

#define CHIP_ENABLE_PIN 10
#define OUTPUT_ENABLE_PIN 11
#define WRITE_ENABLE_PIN 12

#define CHIP_ENABLE_LED 32
#define ERROR_LED 33
#define WRITE_LED 31

void setup() {
  Serial.begin(230400);
  while(!Serial);

  PAGE_BUS_D = 0x1f;
  ADDRESS_BUS_D = 0xff;

  digitalWrite(CHIP_ENABLE_PIN, true);
  digitalWrite(OUTPUT_ENABLE_PIN, true);
  digitalWrite(WRITE_ENABLE_PIN, true);
  digitalWrite(CHIP_ENABLE_LED, true);
  digitalWrite(ERROR_LED, true);
  digitalWrite(WRITE_LED, true);

  pinMode(CHIP_ENABLE_PIN, OUTPUT);
  pinMode(OUTPUT_ENABLE_PIN, OUTPUT);
  pinMode(WRITE_ENABLE_PIN, OUTPUT);
  pinMode(CHIP_ENABLE_LED, OUTPUT);
  pinMode(ERROR_LED, OUTPUT);
  pinMode(WRITE_LED, OUTPUT);
  delay(100);
  digitalWrite(CHIP_ENABLE_LED, false);
  digitalWrite(ERROR_LED, false);
  digitalWrite(WRITE_LED, false);
}

static char buffer[256];
static int buffer_length = 0;

void loop() {
  if (Serial.available() == 0) {
    return;
  }

  // we have data

  char c = Serial.read();
  if (c == 0 || c == '\r') {
    return;
  }

  // process char

  if (c == '\n') {
    // end of command -> process
    processBuffer();
    buffer_length = 0;
    return;
  }

  // fill buffer

  if (buffer_length >= 256) {
    // buffer overflow -> complain and filter
    buffer_length = 0;
    return;
  }

  buffer[buffer_length++] = c;
}

void processBuffer() {
  // TODO
  if (buffer[0] == 'S' && buffer[1] == 'Y' && buffer[2] == 'N' && buffer[3] == '_' && buffer_length == 6) {
    processSync();
  } else if (buffer[0] == 'r') {
    processReadCommand();
  } else if (buffer[0] == 'w') {
    processWriteCommand();
  } else if (buffer[0] == 'p') {
    processPageReadCommand();
  } else if (buffer[0] == 'x') {
    processPageWriteCommand();
  } else {
    digitalWrite(ERROR_LED, true);
    Serial.print("-SYNTAX ERROR: INVALID COMMAND: ");
    for (int i = 0; i < buffer_length; i++) {
      Serial.print(buffer[i]);
    }
    Serial.println();
  }
}

void processSync() {
  for (int i = 0; i < 6; i++) {
    Serial.print(buffer[i]);
  }
  Serial.println();
  digitalWrite(ERROR_LED, false);
}

void processReadCommand() {
  if (buffer_length != 5) {
    digitalWrite(ERROR_LED, true);
    Serial.println("-SYNTAX ERROR: READ COMMAND REQUIRES EXACTLY 4 ADDRESS CHARS");
    return;
  }

  // extract address
  int adrHigh = hexToByte(buffer + 1);
  int adrLow = hexToByte(buffer + 3);

  if (adrHigh == -1 || adrLow == -1) {
    digitalWrite(ERROR_LED, true);
    Serial.println("-SYNTAX ERROR: ADDRESS REQUIRES 4 HEX DIGITS");
    return;
  }

  // assemble address
  int adr = ((adrHigh << 8) & 0xff00) | (adrLow & 0x00ff);

  if (adr < 0 || adr >= 0x2000) {
    digitalWrite(ERROR_LED, true);
    Serial.println("-SYNTAX ERROR: ADDRESS MUST BE IN RANGE 0000..1FFF");
    return;
  }

  // address OK, execute read
  read(adr);
}

void processPageReadCommand() {
  if (buffer_length != 5) {
    digitalWrite(ERROR_LED, true);
    Serial.println("-SYNTAX ERROR: READ COMMAND REQUIRES EXACTLY 4 ADDRESS CHARS");
    return;
  }

  // extract address
  int adrHigh = hexToByte(buffer + 1);
  int adrLow = hexToByte(buffer + 3);

  if (adrHigh == -1 || adrLow == -1) {
    digitalWrite(ERROR_LED, true);
    Serial.println("-SYNTAX ERROR: ADDRESS REQUIRES 4 HEX DIGITS");
    return;
  }

  // assemble address
  int adr = ((adrHigh << 8) & 0xff00) | (adrLow & 0x00ff);

  if (adr < 0 || adr >= 0x2000) {
    digitalWrite(ERROR_LED, true);
    Serial.println("-SYNTAX ERROR: ADDRESS MUST BE IN RANGE 0000..1FFF");
    return;
  }

  if ((adr & 0x003f) > 0) {
    digitalWrite(ERROR_LED, true);
    Serial.println("-SYNTAX ERROR: ADDRESS MUST BE A PAGE START");
    return;
  }

  // address OK, execute read
  pageRead(adr);
}

void processWriteCommand() {
   if (buffer_length != 8) {
    digitalWrite(ERROR_LED, true);
    Serial.println("-SYNTAX ERROR: WRITE COMMAND REQUIRES EXACTLY 4 ADDRESS CHARS, 1 SEPARATOR AND 2 DATA CHARS");
    return;
  }

  // extract address
  int adrHigh = hexToByte(buffer + 1);
  int adrLow = hexToByte(buffer + 3);

  if (adrHigh == -1 || adrLow == -1) {
    digitalWrite(ERROR_LED, true);
    Serial.println("-SYNTAX ERROR: ADDRESS REQUIRES 4 HEX DIGITS");
    return;
  }

  // assemble address
  int adr = ((adrHigh << 8) & 0xff00) | (adrLow & 0x00ff);

  if (adr < 0 || adr >= 0x2000) {
    digitalWrite(ERROR_LED, true);
    Serial.println("-SYNTAX ERROR: ADDRESS MUST BE IN RANGE 0000..1FFF");
    return;
  }

  // address OK
  if (buffer[5] != ':') {
    digitalWrite(ERROR_LED, true);
    Serial.println("-SYNTAX ERROR: WRITE COMMAND NEEDS SEPARATOR AS SIXTH CHAR");
    return;
  }

  int data = hexToByte(buffer + 6);
  if (data == -1) {
    digitalWrite(ERROR_LED, true);
    Serial.println("-SYNTAX ERROR: DATA REQUIRES 2 HEX DIGITS");
    return;
  }

  write(adr, data & 0x00ff);
}

void processPageWriteCommand() {
  if (buffer_length != 134) {
    digitalWrite(ERROR_LED, true);
    Serial.println("-SYNTAX ERROR: WRITE COMMAND REQUIRES EXACTLY 4 ADDRESS CHARS, 1 SEPARATOR AND 128 DATA CHARS");
    return;
  }

  // extract address
  int adrHigh = hexToByte(buffer + 1);
  int adrLow = hexToByte(buffer + 3);

  if (adrHigh == -1 || adrLow == -1) {
    digitalWrite(ERROR_LED, true);
    Serial.println("-SYNTAX ERROR: ADDRESS REQUIRES 4 HEX DIGITS");
    return;
  }

  // assemble address
  int adr = ((adrHigh << 8) & 0xff00) | (adrLow & 0x00ff);

  if (adr < 0 || adr >= 0x2000) {
    digitalWrite(ERROR_LED, true);
    Serial.println("-SYNTAX ERROR: ADDRESS MUST BE IN RANGE 0000..1FFF");
    return;
  }

  if ((adr & 0x003f) > 0) {
    digitalWrite(ERROR_LED, true);
    Serial.println("-SYNTAX ERROR: ADDRESS MUST BE A PAGE START");
    return;
  }

  // address OK
  if (buffer[5] != ':') {
    digitalWrite(ERROR_LED, true);
    Serial.println("-SYNTAX ERROR: WRITE COMMAND NEEDS SEPARATOR AS SIXTH CHAR");
    return;
  }

  // build data buffer
  byte data[128];
  for(int i = 0; i < 64; i++) {
    data[i] = hexToByte(buffer + 6 + (2 * i));
  }

  pageWrite(adr, data);

}

// -------------------------------------------------------
// SERIAL PARSING LOGIC
int hexToByte(char* input) {
  byte high = hexCharToHalfByte(*(input + 0));
  byte low = hexCharToHalfByte(*(input + 1));

  if (high == 255 || low == 255) {
    return -1;
  }

  return ((high << 4) & 0x00f0) | (low & 0x000f);
}

byte hexCharToHalfByte(char input) {
  switch (input) {
    case '0': return 0;
    case '1': return 1;
    case '2': return 2;
    case '3': return 3;
    case '4': return 4;
    case '5': return 5;
    case '6': return 6;
    case '7': return 7;
    case '8': return 8;
    case '9': return 9;
    case 'A': return 10;
    case 'B': return 11;
    case 'C': return 12;
    case 'D': return 13;
    case 'E': return 14;
    case 'F': return 15;
    default: return 255;
  }
}


// -------------------------------------------------------
// CHIP LOGIC

void read(int address) {
  chipEnable(true);
  selectAddress(address);
  digitalWrite(OUTPUT_ENABLE_PIN, false);
  byte readData = DATA_BUS_I;
  digitalWrite(OUTPUT_ENABLE_PIN, true);
  chipEnable(false);

  Serial.write("+");
  if (readData < 16) {
    Serial.print('0');
  }
  Serial.println(readData, HEX);
  digitalWrite(ERROR_LED, false);
}

void pageRead(int address) {
  Serial.write("+");

  chipEnable(true);
  digitalWrite(OUTPUT_ENABLE_PIN, false);
  for (int i = 0; i < 64; i++) {
    selectAddress(address + i);
    delay(1);
    byte readData = DATA_BUS_I;
    
    if (readData < 16) {
      Serial.print('0');
    }
    Serial.print(readData, HEX);
  }
  digitalWrite(OUTPUT_ENABLE_PIN, true);
  chipEnable(false);

  Serial.println();
  digitalWrite(ERROR_LED, false);
}

void write(int address, byte data) {
  chipEnable(true);
  selectAddress(address);
  digitalWrite(WRITE_LED, true);

  DATA_BUS_D = 0xff;
  DATA_BUS_O = data;

  digitalWrite(WRITE_ENABLE_PIN, false);
  digitalWrite(WRITE_ENABLE_PIN, true);
  delay(10);

  DATA_BUS_D = 0x00;
  DATA_BUS_O = 0x00;
  
  digitalWrite(WRITE_LED, false);
  
  digitalWrite(OUTPUT_ENABLE_PIN, false);
  byte readData = DATA_BUS_I;
  digitalWrite(OUTPUT_ENABLE_PIN, true);
  chipEnable(false);

  if (readData == data) {
    digitalWrite(ERROR_LED, false);
    Serial.println("+");
  } else {
    digitalWrite(ERROR_LED, true);
    Serial.print("-WRITE CHECK ERROR: ADDRESS ");
    Serial.print(address, HEX);
    Serial.print(" EXPECTED ");
    Serial.print(data, HEX);
    Serial.print(" BUT READ ");
    Serial.println(readData, HEX);
  }
}

void pageWrite(int address, byte* data) {
  chipEnable(true);
  digitalWrite(WRITE_LED, true);

  DATA_BUS_D = 0xff;
  for (int i = 0; i < 64; i++) {
    selectAddress(address + i);
    DATA_BUS_O = *(data + i);
    digitalWrite(WRITE_ENABLE_PIN, false);
    digitalWrite(WRITE_ENABLE_PIN, true);
  }
  delay(10);

  DATA_BUS_O = 0x00;
  
  digitalWrite(WRITE_LED, false);
  DATA_BUS_D = 0x00;

  // verify data
  digitalWrite(OUTPUT_ENABLE_PIN, false);

  for (int i = 0; i < 64; i++) {
    selectAddress(address + i);
    delay(1);
    byte readData = DATA_BUS_I;
    
    if (readData != *(data + i)) {
      digitalWrite(ERROR_LED, true);
      Serial.print("-WRITE CHECK ERROR: ADDRESS ");
      Serial.print(address + i, HEX);
      Serial.print(" EXPECTED ");
      Serial.print(*(data + i), HEX);
      Serial.print(" BUT READ ");
      Serial.println(readData, HEX);
      digitalWrite(OUTPUT_ENABLE_PIN, true);
      chipEnable(false);
      return;
    }
  }
  Serial.println("+");
  digitalWrite(OUTPUT_ENABLE_PIN, true);
  chipEnable(false);  
}

void selectAddress(int addr) {
  PAGE_BUS_O = (addr >> 8) & 0x001f;
  ADDRESS_BUS_O = addr & 0x00ff;
}

void chipEnable(bool state) {
  digitalWrite(CHIP_ENABLE_PIN, !state);
  digitalWrite(CHIP_ENABLE_LED, state);
}

