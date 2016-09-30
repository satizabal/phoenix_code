//==============================================================================
// ServoDriver.h  This header file defines the ServoDriver class.
// 
// This class is used by the main Phoenix code to talk to the servos, without having
// to know details about how the servos are connected.  There may be several implementations
// of this class, such as one will use the SSC-32 to control the servos.  There may be 
// additional versions of this class that allows the main processor to control the servos.
//==============================================================================
#ifndef _Servo_Driver_h_
#define _Servo_Driver_h_

#include "Hex_Cfg.h"  // make sure we know what options are enabled...
#if ARDUINO>99
#include <Arduino.h> // Arduino 1.0
#else
#include <Wprogram.h> // Arduino 0022
#endif

class ServoDriver {
  public:
    void Init(void);

    void BeginServoUpdate(void);    // Start the update 
    void OutputServoInfoForLeg(byte LegIndex, short sCoxaAngle1, short sFemurAngle1, short sTibiaAngle1);
    void CommitServoDriver(word wMoveTime);
    void FreeServos(void);
    
  private:
  
} ;   

#endif //_Servo_Driver_h_
