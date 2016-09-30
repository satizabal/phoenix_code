#include "serialGamepad.h"

SerialGamepad::SerialGamepad() {
  resetValues();
  askForData = true;
  debug = false;
  timeCounter = 0;
}

void SerialGamepad::resetValues() {
  int i = 0;
  for (i = 0; i < 4; i++) {
    analogs[i] = 0x7F;
  }
  for (i = 0; i < 16; i++) {
    buttons[i] = false;
  }
}

void SerialGamepad::read_gamepad() {
  int i, temp;
  byte ch[6];
  boolean dataComplete;

  if (askForData) {
    Serial.println('*');                                  //asking for gamepad data
    askForData = false;
    resetValues();
  }
  else {
    dataComplete = true;
    for (i = 0; i < 6; i++) {
      temp = Serial.read();
      if (temp == -1) {
        dataComplete = false;
        break;
      }
      ch[i] = temp & 0x00FF;
    }

    if (dataComplete) {
      //Serial.write(ch, 6);
      //Serial.println();
      analogs[0]  = ch[0];
      analogs[1]  = ch[1];
      analogs[2]  = ch[2];
      analogs[3]  = ch[3];

      buttons[0]  = ((ch[4] & B00001000) != 0);
      buttons[1]  = ((ch[4] & B00000100) != 0);
      buttons[2]  = ((ch[4] & B00000010) != 0);
      buttons[3]  = ((ch[4] & B00000001) != 0);
      buttons[4]  = ((ch[5] & B10000000) != 0);
      buttons[5]  = ((ch[5] & B01000000) != 0);
      buttons[6]  = ((ch[5] & B00100000) != 0);
      buttons[7]  = ((ch[5] & B00010000) != 0);
      buttons[8]  = ((ch[5] & B00001000) != 0);
      buttons[9]  = ((ch[5] & B00000100) != 0);
      buttons[10] = ((ch[5] & B00000010) != 0);
      buttons[11] = ((ch[5] & B00000001) != 0);
      buttons[12] = ((ch[4] & B00010000) != 0);
      buttons[13] = ((ch[4] & B00100000) != 0);
      buttons[14] = ((ch[4] & B01000000) != 0);
      buttons[15] = ((ch[4] & B10000000) != 0);

      askForData = true;
    }
    else {
      resetValues(); 
      timeCounter += 1;
      if (timeCounter > TIMEOUT) {
        timeCounter = 0;
        askForData = true;
      }
    }
  }
}

boolean SerialGamepad::ButtonPressed(unsigned int button) {
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

byte SerialGamepad::Analog(byte button) {
  switch(button) {
    case PSS_RX: return analogs[0];
    case PSS_RY: return analogs[1];
    case PSS_LX: return analogs[2];
    case PSS_LY: return analogs[3];
    case 1:      return 0x70;        //for the test
    default: return 0x7F;
  }
}

void SerialGamepad::printMessage(char msg[]) {
  if (debug) {
    Serial.println(msg);
  }
}

