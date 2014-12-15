#include "serialGamepad.h"

SerialGamepad::SerialGamepad() {
  int i = 0;
  for (i = 0; i < 4; i++) {
    analogs[i] = 0;
  }
  for (i = 0; i < 16; i++) {
    buttons[i] = false;
  }
  dataAvailable = false;
  debug = false;
}

void SerialGamepad::read_gamepad() {
  int i, temp;
  byte ch[6];

  Serial.println('*');                                  //asking for gamapad data
  
  dataAvailable = true;
  for (i = 0; i < 6; i++) {
    temp = Serial.read();
    if (temp == -1) {
      dataAvailable = false;
      break;
    }
    ch[i] = temp & 0x00FF;
  }
  //Serial.write(ch, 6);
  //Serial.println();

  if (dataAvailable) {
    analogs[0]  = ch[0];
    analogs[1]  = ch[1];
    analogs[2]  = ch[2];
    analogs[3]  = ch[3];
    buttons[0]  = (!buttons[0])  && ((ch[4] & B00001000) != 0);
    buttons[1]  = (!buttons[1])  && ((ch[4] & B00000100) != 0);
    buttons[2]  = (!buttons[2])  && ((ch[4] & B00000010) != 0);
    buttons[3]  = (!buttons[3])  && ((ch[4] & B00000001) != 0);
    buttons[4]  = (!buttons[4])  && ((ch[5] & B10000000) != 0);
    buttons[5]  = (!buttons[5])  && ((ch[5] & B01000000) != 0);
    buttons[6]  = (!buttons[6])  && ((ch[5] & B00100000) != 0);
    buttons[7]  = (!buttons[7])  && ((ch[5] & B00010000) != 0);
    buttons[8]  = (!buttons[8])  && ((ch[5] & B00001000) != 0);
    buttons[9]  = (!buttons[9])  && ((ch[5] & B00000100) != 0);
    buttons[10] = (!buttons[10]) && ((ch[5] & B00000010) != 0);
    buttons[11] = (!buttons[11]) && ((ch[5] & B00000001) != 0);
    buttons[12] = (!buttons[12]) && ((ch[4] & B00010000) != 0);
    buttons[13] = (!buttons[13]) && ((ch[4] & B00100000) != 0);
    buttons[14] = (!buttons[14]) && ((ch[4] & B01000000) != 0);
    buttons[15] = (!buttons[15]) && ((ch[4] & B10000000) != 0);
  }
  
}

boolean SerialGamepad::ButtonPressed(unsigned int button) {
  if (dataAvailable) {
    switch (button) {
      case PSB_SELECT: return buttons[8];
      case PSB_L3: return buttons[10];
      case PSB_R3: return buttons[11];
      case PSB_START: return buttons[9];
      case PSB_PAD_UP: return buttons[12];
      case PSB_PAD_RIGHT: return buttons[13];
      case PSB_PAD_DOWN: return buttons[14];
      case PSB_PAD_LEFT: return buttons[15];
      case PSB_L2: return buttons[6];
      case PSB_R2: return buttons[7];
      case PSB_L1: return buttons[4];
      case PSB_R1: return buttons[5];
      case PSB_TRIANGLE: return buttons[3];
      case PSB_CIRCLE: return buttons[2];
      case PSB_CROSS: return buttons[1];
      case PSB_SQUARE: return buttons[0];
      default: return false;
    }
  }
  else {
      return false;
  }
}

byte SerialGamepad::Analog(byte button) {
  if (dataAvailable) {
    switch(button) {
      case PSS_RX: return analogs[0];
      case PSS_RY: return analogs[1];
      case PSS_LX: return analogs[2];
      case PSS_LY: return analogs[3];
      case 1:      return 0x70;        //for the test
      default: return 0;
    }
  }
  else {
    return 0;
  }
}

void SerialGamepad::printMessage(char msg[]) {
  if (debug) {
    Serial.println(msg);
  }
}

