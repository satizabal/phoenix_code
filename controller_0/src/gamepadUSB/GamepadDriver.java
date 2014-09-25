/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package gamepadUSB;

import net.java.games.input.Component;
import net.java.games.input.Controller;
import net.java.games.input.ControllerEnvironment;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.IOException;

import gnu.io.CommPortIdentifier; 
import gnu.io.SerialPort;
import gnu.io.SerialPortEvent; 
import gnu.io.SerialPortEventListener; 
import java.util.Enumeration;
import java.util.Arrays;

/**
 *
 * @author hsatizab
 */
public class GamepadDriver implements SerialPortEventListener {

    public GamepadDriver() {
        ControllerEnvironment ce = ControllerEnvironment.getDefaultEnvironment();
        Controller[] cs = ce.getControllers();
        controllerFound = false;
        robotFound = false;
        int controllerNumber = -1;
        
        if (cs.length == 0) {
            System.out.println("No controllers found");
        }
        else {
//----------Find the controller number-------------------------------------------------------------
			for (int i = 0; i < cs.length; i++) {
				System.out.println("Controller " + String.valueOf(i) + ": ");
				System.out.println("\tName:" + cs[i].getName());
				System.out.println("\tType:" + cs[i].getType());
				System.out.println("\tNumber of components:" + String.valueOf(cs[i].getComponents().length));
				if (String.valueOf(cs[i].getType()) == "Gamepad") {
				    controllerNumber = i;
				    break;
				}
			}
//-------------------------------------------------------------------------------------------------
            if (controllerNumber >= 0) {
                controllerFound = true;
                
//----------Find the components number-------------------------------------------------------------
//			    try {
//				    gamepad = cs[controllerNumber];
//				    Component[] components = new Component[gamepad.getComponents().length];
//				    System.out.println("Number of components:" + String.valueOf(components.length));
//				    for (int i = 0; i < components.length; i++) {
//					    components[i] = gamepad.getComponents()[i];
//				    }
//				    while (true) {
//					    gamepad.poll();
//					
//					    for (int i = 0; i < components.length; i++) {
//						    System.out.print("  C" + String.valueOf(i) + ":" + String.format("%.2f", components[i].getPollData()));
//					    }
//					    System.out.println();
//				    }
//			    }
//			    catch(NumberFormatException nfe) {
//				    System.out.println("Not a valid controller number!");
//                    controllerFound = false;
//			    }
//			    catch(ArrayIndexOutOfBoundsException aiobe) {
//				    System.out.println("Not a valid controller number!");
//                    controllerFound = false;
//			    }
//-------------------------------------------------------------------------------------------------
			
                gamepad = cs[controllerNumber];
                analogLeftX = gamepad.getComponents()[analogLeftXAxis];
                analogLeftY = gamepad.getComponents()[analogLeftYAxis];
                analogRightX = gamepad.getComponents()[analogRightXAxis];
                analogRightY = gamepad.getComponents()[analogRightYAxis];
                digitalLeft = gamepad.getComponents()[digitalLeftCross];
                button1 = gamepad.getComponents()[digital1];
                button2 = gamepad.getComponents()[digital2];
                button3 = gamepad.getComponents()[digital3];
                button4 = gamepad.getComponents()[digital4];
                button5 = gamepad.getComponents()[digital5];
                button6 = gamepad.getComponents()[digital6];
                button7 = gamepad.getComponents()[digital7];
                button8 = gamepad.getComponents()[digital8];
                button9 = gamepad.getComponents()[digital9];
                button10 = gamepad.getComponents()[digital10];
                button11 = gamepad.getComponents()[digital11];
                button12 = gamepad.getComponents()[digital12];
            }
        }
        
//        keyboard = new InputStreamReader(System.in);
    }
    
   
	public void initialize() {

		CommPortIdentifier portId = null;
		Enumeration portEnum = CommPortIdentifier.getPortIdentifiers();

		//First, Find an instance of serial port as set in PORT_NAMES.
		while (portEnum.hasMoreElements()) {
			CommPortIdentifier currPortId = (CommPortIdentifier) portEnum.nextElement();
			for (String portName : PORT_NAMES) {
				if (currPortId.getName().equals(portName)) {
					portId = currPortId;
					break;
				}
			}
		}
		if (portId == null) {
		    robotFound = false;
			System.out.println("Could not find COM port.");
			return;
		}

		try {
			// open serial port, and use class name for the appName.
			serialPort = (SerialPort) portId.open(this.getClass().getName(), TIME_OUT);

			// set port parameters
			serialPort.setSerialPortParams(DATA_RATE, SerialPort.DATABITS_8, SerialPort.STOPBITS_1, SerialPort.PARITY_NONE);

			// open the streams
			input = new BufferedReader(new InputStreamReader(serialPort.getInputStream()));
			output = serialPort.getOutputStream();

			// add event listeners
			serialPort.addEventListener(this);
			serialPort.notifyOnDataAvailable(true);
			
			robotFound = true;
			
		}
		catch (Exception e) {
    		robotFound = false;
			System.err.println(e.toString());
		}
	}

	public synchronized void close() {
		if (serialPort != null) {
			serialPort.removeEventListener();
			serialPort.close();
		}
	}

	public synchronized void serialEvent(SerialPortEvent oEvent) {
		if (oEvent.getEventType() == SerialPortEvent.DATA_AVAILABLE) {
			try {
				String inputLine=input.readLine();
				System.out.println(inputLine);
//				byte[] temp = inputLine.getBytes("US-ASCII");
//				System.out.println("Receiving..." + temp.length + ":" + Arrays.toString(temp));
				
				if (inputLine.equals("*")) {
				    byte[] toSend = new byte[6];
                    gamepad.poll();

                    for (int i = 0; i < toSend.length; i++) {
                        switch (i) {
                            case 0:
                                toSend[0] = (byte)(Math.round(127 * (1+analogRightX.getPollData())));
                                break;
                            case 1:
                                toSend[1] = (byte)(Math.round(127 * (1+analogRightY.getPollData())));
                                break;
                            case 2:
                                toSend[2] = (byte)(Math.round(127 * (1+analogLeftX.getPollData())));
                                break;
                            case 3:
                                toSend[3] = (byte)(Math.round(127 * (1+analogLeftY.getPollData())));
                                break;
                            case 4:
                                int cross = (int)(Math.round(8 * digitalLeft.getPollData()));
                                byte button1234 = (byte)( ((byte)(button1.getPollData()) << 3) |
                                                          ((byte)(button2.getPollData()) << 2) |
                                                          ((byte)(button3.getPollData()) << 1) |
                                                          ((byte)(button4.getPollData())) );
                                switch (cross) {
                                    case 0:
                                        toSend[4] = (byte)( (byte)(0) | button1234 );         //0b00000000 | button1234;
                                        break;
                                    case 1:
                                        toSend[4] = (byte)( (byte)(144) | button1234 );       //0b10010000 | button1234;
                                        break;
                                    case 2:
                                        toSend[4] = (byte)( (byte)(16) | button1234 );        //0b00010000 | button1234;
                                        break;
                                    case 3:
                                        toSend[4] = (byte)( (byte)(48) | button1234 );        //0b00110000 | button1234;
                                        break;
                                    case 4:
                                        toSend[4] = (byte)( (byte)(32) | button1234 );        //0b00100000 | button1234;
                                        break;
                                    case 5:
                                        toSend[4] = (byte)( (byte)(96) | button1234 );        //0b01100000 | button1234;
                                        break;
                                    case 6:
                                        toSend[4] = (byte)( (byte)(64) | button1234 );        //0b01000000 | button1234;
                                        break;
                                    case 7:
                                        toSend[4] = (byte)( (byte)(192) | button1234 );        //0b11000000 | button1234;
                                        break;
                                    case 8:
                                        toSend[4] = (byte)( (byte)(128) | button1234 );        //0b10000000 | button1234;
                                        break;
                                }
                                break;
                            case 5:
                                toSend[5] = (byte)( ((byte)(button5.getPollData()) << 7) |
                                                    ((byte)(button6.getPollData()) << 6) |
                                                    ((byte)(button7.getPollData()) << 5) |
                                                    ((byte)(button8.getPollData()) << 4) |
                                                    ((byte)(button9.getPollData()) << 3) |
                                                    ((byte)(button10.getPollData()) << 2) |
                                                    ((byte)(button11.getPollData()) << 1) |
                                                    (byte)(button12.getPollData()) );
                                break;
                        }
                    }
//                    System.out.printf("Sending.... %d:%s - AL: %4d,%4d - AR: %4d,%4d - B:%s,%s\n",
//                                      toSend.length, Arrays.toString(toSend),
//                                      toSend[2] ,toSend[3],
//                                      toSend[0] ,toSend[1],
//                                      String.format("%8s", Integer.toBinaryString(toSend[4] & 0x00FF)).replace(' ', '0'),
//                                      String.format("%8s", Integer.toBinaryString(toSend[5] & 0x00FF)).replace(' ', '0'));
                    output.write(toSend);
                }
			}
			catch (Exception e) {
				System.err.println(e.toString());
			}
		}
		// Ignore all the other eventTypes, but you should consider the other ones.
	}


    public void forwardCharacter() {
        boolean working = true;
        int key = 0;
        int lastKey = 0;
        
        while(working) {
//------------------------------------------------------- send one character-----------------------
            System.out.println("Enter a key");
            try {
                while (key != 10) {
                    lastKey = key;
                    key = keyboard.read();
                }
                if (lastKey == 27) {
                   working = false;
                }
                else {
                    System.out.println("Sending: " + String.valueOf(lastKey));
                    output.write(0x000000FF & lastKey);
                    key = 0;
                }
            }
            catch (IOException ioe) {
            }
//-------------------------------------------------------------------------------------------------
        }
    }
    


    public static void main(String[] args) {
        GamepadDriver driver = new GamepadDriver();
        
        if (driver.controllerFound) {
            driver.initialize();
//            driver.forwardCharacter();
            while(true);
        }
        driver.close();
   }

    private boolean controllerFound;
    private Controller gamepad;
    private Component analogLeftX;
    private Component analogLeftY;
    private Component analogRightX;
    private Component analogRightY;
    private Component digitalLeft;
    private Component button1;
    private Component button2;
    private Component button3;
    private Component button4;
    private Component button5;
    private Component button6;
    private Component button7;
    private Component button8;
    private Component button9;
    private Component button10;
    private Component button11;
    private Component button12;
    
    private static final int digital1 = 0;
    private static final int digital2 = 1;
    private static final int digital3 = 2;
    private static final int digital4 = 3;
    private static final int digital5 = 4;
    private static final int digital6 = 5;
    private static final int digital7 = 6;
    private static final int digital8 = 7;
    private static final int digital9 = 8;
    private static final int digital10 = 9;
    private static final int digital11 = 10;
    private static final int digital12 = 11;
    private static final int analogLeftXAxis = 12;
    private static final int analogLeftYAxis = 13;
    private static final int analogRightXAxis = 14;
    private static final int analogRightYAxis = 15;
    private static final int digitalLeftCross = 16;

    private SerialPort serialPort;
	private static final String PORT_NAMES[] = { 
			"/dev/tty.usbserial-A9007UX1", // Mac OS X
            "/dev/ttyACM0", // Raspberry Pi
			"/dev/ttyUSB0", // Linux
			"COM3", // Windows
	};
	private BufferedReader input;
	private OutputStream output;
	private static final int TIME_OUT = 2000;
	private static final int DATA_RATE = 57600;
	private boolean robotFound;

    private InputStreamReader keyboard;
    
}
