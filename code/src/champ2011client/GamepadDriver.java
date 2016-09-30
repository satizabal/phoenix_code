/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package champ2011client;

import java.io.BufferedWriter;
import java.io.FileWriter;
import java.io.IOException;
import java.util.Random;
import net.java.games.input.Component;
import net.java.games.input.Controller;
import net.java.games.input.ControllerEnvironment;

/**
 *
 * @author hsatizab
 */
public class GamepadDriver extends CarController {

    public GamepadDriver() {
        ControllerEnvironment ce = ControllerEnvironment.getDefaultEnvironment();
        Controller[] cs = ce.getControllers();

        gamepadFound = false;
        if (cs.length == 0) {
            System.out.println("No controllers found");
        }
        else {
            gamepad = cs[0];
            steer = gamepad.getComponents()[analogRightXAxis];
            throtleBrake = gamepad.getComponents()[analogLeftYAxis];
            enableLog = gamepad.getComponents()[digital6];
            disableLog = gamepad.getComponents()[digital8];
            enableManual = gamepad.getComponents()[digital5];
            enableAutomatic = gamepad.getComponents()[digital7];
            gearBox = gamepad.getComponents()[digitalLeftCross];
            gamepadFound = true;
        }
        
        logOpen = false;
        logEnabled = false;
        manualAutomatic = false;
        gearBoxBefore = 0;
        try {
            log = new BufferedWriter(new FileWriter("log.txt"));
            logOpen = true;
        }
        catch (IOException ioe) {
            System.out.println("Impossible to open log file");
        }
        
        random = new Random();
    }
    
    public void reset() {
        System.out.println("Restarting the race!");
    }

    public void shutdown() {
        if (logOpen) {
            try {
                log.close();
                logOpen = false;
            }
            catch (IOException ioe) {
                System.out.println("Impossible to close log file");
            }
        }
        
        System.out.println("Bye bye!");
    }

    public Action control(SensorModel sensors) {
        Action action = new Action ();
        float temp = 0;
        
        if (gamepadFound) {
            gamepad.poll();
//            action.steering = -steer.getPollData()/2;
            action.steering = Math.pow(-steer.getPollData(), 3);
            temp = throtleBrake.getPollData();
            if (temp < 0.0) {
                action.accelerate = -temp;
            }
            else {
                action.brake = temp;
            }
            
            if (manualAutomatic) {
                action.gear = sensors.getGear();
                if ((gearBoxBefore != 0.25) && (gearBox.getPollData() == 0.25)) {
                    action.gear = Math.min(action.gear + 1, 6);
                    System.out.println("Gear box at:" + String.valueOf(action.gear));
                }
                else {
                    if ((gearBoxBefore != 0.75) && (gearBox.getPollData() == 0.75)) {
                        action.gear = Math.max(action.gear - 1, -1);
                        System.out.println("Gear box at:" + String.valueOf(action.gear));
                    }
                }
            }
            else {
                action.gear = getGear(sensors);
            }
            gearBoxBefore = gearBox.getPollData();
        
            if ((!manualAutomatic) && (enableManual.getPollData() > 0)) {
                manualAutomatic = true;
                System.out.println("Manual gear box enabled.");
            }
            if (manualAutomatic && (enableAutomatic.getPollData() > 0)) {
                manualAutomatic = false;
                System.out.println("Automatic gear box enabled.");
            }
            
            if ((!logEnabled) && (enableLog.getPollData() > 0)) {
                logEnabled = true;
                System.out.println("Log enabled.");
            }
            if (logEnabled && (disableLog.getPollData() > 0)) {
                logEnabled = false;
                System.out.println("Log disabled.");
            }
        }
        
        if (logOpen && logEnabled) {
            String toSave = String.valueOf((sensors.getSpeed() / 100) + (random.nextGaussian()/100)) + "\t";
            toSave += String.valueOf(sensors.getAngleToTrackAxis() + (random.nextGaussian()/100)) + "\t";
            double[] dist = sensors.getTrackEdgeSensors();
            for (int i = 0; i < dist.length; i++) {
                toSave += String.valueOf((dist[i] / 100) + (random.nextGaussian()/100)) + "\t";
            }
            toSave += String.valueOf(-temp) + "\t";
            toSave += String.valueOf(action.steering);
            
            try {
                log.write(toSave);
                log.newLine();
            }
            catch (IOException ioe) {
                System.out.println("Impossible to write in log file");
            }
        }
        
        return action;
    }
    
    private int getGear(SensorModel sensors){
        int gear = sensors.getGear();
        double rpm  = sensors.getRPM();
        
        if (rpm < 0) {
            gear = 0;
        }
        else {
            if ( (rpm > gearUp[gear]) && (gear < 6) ){
                gear = Math.min(gear + 1, 6);
            }
            else if ( (rpm < gearDown[gear]) && (gear > 0) ){
                gear = Math.max(gear - 1, 0);
            }
        }
        return gear;
    }
        
    private boolean gamepadFound;
    private boolean logOpen;
    private boolean logEnabled;
    private boolean manualAutomatic;
    private double gearBoxBefore;
    private Controller gamepad;
    private Component steer;
    private Component throtleBrake;
    private Component enableLog;
    private Component disableLog;
    private Component enableManual;
    private Component enableAutomatic;
    private Component gearBox;
    
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
    private static final int analogLeftXAxis = 12;
    private static final int analogLeftYAxis = 13;
    private static final int analogRightXAxis = 14;
    private static final int analogRightYAxis = 15;
    private static final int digitalLeftCross = 16;
    
    /* Gear Changing Constants*/
    private static final int[] gearUp = {  5000, 6000, 6000, 6000, 6500, 7000, 7000};
    private static final int[] gearDown = {0,    940,  2500, 3000, 3500, 3500, 4000};
    
    private BufferedWriter log;
    private Random random;
}
