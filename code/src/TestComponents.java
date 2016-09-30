/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package champ2011client;

import net.java.games.input.Component;
import net.java.games.input.Controller;
import net.java.games.input.ControllerEnvironment;
import java.lang.NumberFormatException;
import java.lang.ArrayIndexOutOfBoundsException;

/**
 *
 * @author hsatizab
 */
public class TestComponents {

    public TestComponents() {
    }

    public static void main(String[] args) {
        ControllerEnvironment ce = ControllerEnvironment.getDefaultEnvironment();
        Controller[] cs = ce.getControllers();

		gamepadFound = false;
		if (cs.length == 0) {
			System.out.println("No controllers found");
		}
		else {
			gamepadFound = true;
			try {
				int controllerNumber = Integer.valueOf(args[0]).intValue();
				gamepad = cs[controllerNumber];
				
				components = new Component[gamepad.getComponents().length];
				System.out.println("Number of components:" + String.valueOf(components.length));
				for (int i = 0; i < components.length; i++) {
					components[i] = gamepad.getComponents()[i];
				}
				while (gamepadFound) {
					gamepad.poll();
					
					for (int i = 0; i < components.length; i++) {
						System.out.print("  C" + String.valueOf(i) + ":" + String.format("%.2f", components[i].getPollData()));
					}
					System.out.println();
				}
			}
			catch(NumberFormatException nfe) {
				System.out.println("Not a valid controller number!");
			}
			catch(ArrayIndexOutOfBoundsException aiobe) {
				System.out.println("Not a valid controller number!");
			}
		}

    }
    
        
    private static boolean gamepadFound;
    private static Controller gamepad;
    private static Component[] components;

    
}
