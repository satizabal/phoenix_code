/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package champ2011client;

import net.java.games.input.Component;
import net.java.games.input.Controller;
import net.java.games.input.ControllerEnvironment;

/**
 *
 * @author hsatizab
 */
public class TestControllers {

    public TestControllers() {
    }

    public static void main(String[] args) {
		int i;
        ControllerEnvironment ce = ControllerEnvironment.getDefaultEnvironment();
        Controller[] cs = ce.getControllers();

        if (cs.length == 0) {
            System.out.println("No controllers found");
        }
        else {
			for (i = 0; i < cs.length; i++) {
				System.out.println("Controller " + String.valueOf(i) + ": ");
				System.out.println("\tName:" + cs[i].getName());
				System.out.println("\tType:" + cs[i].getType());
				System.out.println("\tNumber of components:" + String.valueOf(cs[i].getComponents().length));
			}
		}
    }

}
