//====================================================================
//Project Lynxmotion Phoenix
//
// Servo Driver - This version is setup to use the SSC-32 to control
// the servos.
//====================================================================
#if ARDUINO>99
#include <Arduino.h> // Arduino 1.0
#else
#include <Wprogram.h> // Arduino 0022
#endif
#include "Hex_Globals.h"
#include "ServoDriver.h"
#define NUMSERVOSPERLEG 3

#ifdef USE_SSC32

//Servo Pin numbers - May be SSC-32 or actual pins on main controller, depending on configuration.
const byte cCoxaPin[] PROGMEM = {cRRCoxaPin,  cRMCoxaPin,  cRFCoxaPin,  cLRCoxaPin,  cLMCoxaPin,  cLFCoxaPin};
const byte cFemurPin[] PROGMEM = {cRRFemurPin, cRMFemurPin, cRFFemurPin, cLRFemurPin, cLMFemurPin, cLFFemurPin};
const byte cTibiaPin[] PROGMEM = {cRRTibiaPin, cRMTibiaPin, cRFTibiaPin, cLRTibiaPin, cLMTibiaPin, cLFTibiaPin};



// Add support for running on non-mega Arduino boards as well.
#ifdef __AVR__
#if not defined(UBRR1H)
#if cSSC_IN == 0
#define SSCSerial Serial
#else
    SoftwareSerial SSCSerial(cSSC_IN, cSSC_OUT);
#endif    
#endif
#endif

//=============================================================================
// Global - Local to this file only...
//=============================================================================

// definition of some helper functions
extern int SSCRead (byte* pb, int cb, word wTimeout, word wEOL);


//--------------------------------------------------------------------
//Init
//--------------------------------------------------------------------
void ServoDriver::Init(void) {
    SSCSerial.begin(cSSC_BAUD);
}

//------------------------------------------------------------------------------------------
//[BeginServoUpdate] Does whatever preperation that is needed to starrt a move of our servos
//------------------------------------------------------------------------------------------
void ServoDriver::BeginServoUpdate(void)    // Start the update 
{
}

//------------------------------------------------------------------------------------------
//[OutputServoInfoForLeg] Do the output to the SSC-32 for the servos associated with
//         the Leg number passed in.
//------------------------------------------------------------------------------------------
#define cPwmDiv       991  //old 1059;
#define cPFConst      592  //old 650 ; 900*(1000/cPwmDiv)+cPFConst must always be 1500
                           // A PWM/deg factor of 10,09 give cPwmDiv = 991 and cPFConst = 592
                           // For a modified 5645 (to 180 deg travel): cPwmDiv = 1500 and cPFConst = 900.
void ServoDriver::OutputServoInfoForLeg(byte LegIndex, short sCoxaAngle1, short sFemurAngle1, short sTibiaAngle1)
{        
    word    wCoxaSSCV;        // Coxa value in SSC units
    word    wFemurSSCV;        //
    word    wTibiaSSCV;        //

    //Update Right Legs
    g_InputController.AllowControllerInterrupts(false);    // If on xbee on hserial tell hserial to not processess...
    if (LegIndex < 3) {
        wCoxaSSCV = ((long)(-sCoxaAngle1 +900))*1000/cPwmDiv+cPFConst;
        wFemurSSCV = ((long)(-sFemurAngle1+900))*1000/cPwmDiv+cPFConst;
        wTibiaSSCV = ((long)(-sTibiaAngle1+900))*1000/cPwmDiv+cPFConst;
    } else {
        wCoxaSSCV = ((long)(sCoxaAngle1 +900))*1000/cPwmDiv+cPFConst;
        wFemurSSCV = ((long)((long)(sFemurAngle1+900))*1000/cPwmDiv+cPFConst);
        wTibiaSSCV = ((long)(sTibiaAngle1+900))*1000/cPwmDiv+cPFConst;
    }

#ifdef cSSC_BINARYMODE
    SSCSerial.write(pgm_read_byte(&cCoxaPin[LegIndex])  + 0x80);
    SSCSerial.write(wCoxaSSCV >> 8);
    SSCSerial.write(wCoxaSSCV & 0xff);
    SSCSerial.write(pgm_read_byte(&cFemurPin[LegIndex]) + 0x80);
    SSCSerial.write(wFemurSSCV >> 8);
    SSCSerial.write(wFemurSSCV & 0xff);
    SSCSerial.write(pgm_read_byte(&cTibiaPin[LegIndex]) + 0x80);
    SSCSerial.write(wTibiaSSCV >> 8);
    SSCSerial.write(wTibiaSSCV & 0xff);
#else
    SSCSerial.print("#");
    SSCSerial.print(pgm_read_byte(&cCoxaPin[LegIndex]), DEC);
    SSCSerial.print("P");
    SSCSerial.print(wCoxaSSCV, DEC);
    SSCSerial.print("#");
    SSCSerial.print(pgm_read_byte(&cFemurPin[LegIndex]), DEC);
    SSCSerial.print("P");
    SSCSerial.print(wFemurSSCV, DEC);
    SSCSerial.print("#");
    SSCSerial.print(pgm_read_byte(&cTibiaPin[LegIndex]), DEC);
    SSCSerial.print("P");
    SSCSerial.print(wTibiaSSCV, DEC);
#endif        
    g_InputController.AllowControllerInterrupts(true);    // Ok for hserial again...
}


//--------------------------------------------------------------------
//[CommitServoDriver Updates the positions of the servos - This outputs
//         as much of the command as we can without committing it.  This
//         allows us to once the previous update was completed to quickly 
//        get the next command to start
//--------------------------------------------------------------------
void ServoDriver::CommitServoDriver(word wMoveTime)
{
#ifdef cSSC_BINARYMODE
    byte    abOut[3];
#endif
    
    g_InputController.AllowControllerInterrupts(false);    // If on xbee on hserial tell hserial to not processess...

#ifdef cSSC_BINARYMODE
    abOut[0] = 0xA1;
    abOut[1] = wMoveTime >> 8;
    abOut[2] = wMoveTime & 0xff;
    SSCSerial.write(abOut, 3);
#else
      //Send <CR>
    SSCSerial.print("T");
    SSCSerial.println(wMoveTime, DEC);
#endif

    g_InputController.AllowControllerInterrupts(true);    

}

//--------------------------------------------------------------------
//[FREE SERVOS] Frees all the servos
//--------------------------------------------------------------------
void ServoDriver::FreeServos(void)
{
    g_InputController.AllowControllerInterrupts(false);    // If on xbee on hserial tell hserial to not processess...
    for (byte LegIndex = 0; LegIndex < 32; LegIndex++) {
        SSCSerial.print("#");
        SSCSerial.print(LegIndex, DEC);
        SSCSerial.print("P0");
    }
    SSCSerial.print("T200\r");
    g_InputController.AllowControllerInterrupts(true);    
}



//==============================================================================
// Quick and dirty helper function to read so many bytes in from the SSC with a timeout and an end of character marker...
//==============================================================================
int SSCRead (byte* pb, int cb, word wTimeout, word wEOL)
{
    int ich;
    byte* pbIn = pb;
    unsigned long ulTimeLastChar = micros();
    while (cb) {
        while (!SSCSerial.available()) {
            // check for timeout
            if ((word)(micros()-ulTimeLastChar) > wTimeout) {
                return (int)(pb-pbIn);
            }    
        }
        ich = SSCSerial.read();
        *pb++ = (byte)ich;
        cb--;

        if ((word)ich == wEOL)
            break;    // we matched so get out of here.
        ulTimeLastChar = micros();    // update to say we received something
    }

    return (int)(pb-pbIn);
}

#endif
