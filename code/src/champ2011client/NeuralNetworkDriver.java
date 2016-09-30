/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package champ2011client;

import toolFNNXImportMLP.FNNXImportMLP;
import toolFNNXMLP.FNNXMLP;
import scripting.InterThreadMonitor;


/**
 *
 * @author hsatizab
 */
public class NeuralNetworkDriver extends CarController {
    
    public NeuralNetworkDriver() {
        FNNXImportMLP loader = new FNNXImportMLP("network.fxr");

        network = (FNNXMLP)loader.eval(null);
        InterThreadMonitor monitor = loader.getMonitor();
        
        networkLoaded = true;
        if ((network == null) || (monitor.hasErrorMessage())) {
            networkLoaded = false;
            System.out.println("Error. Impossible to load neural network.");
			System.out.println(monitor.getErrorMessage());
        }
//        else {
//            System.out.println(network.toString());
//        }
    }
    
    public void reset() {
        System.out.println("Restarting the race!");
    }

    public void shutdown() {
        System.out.println("Bye bye!");
    }
    
    public Action control(SensorModel sensors) {
        Action action = new Action ();

        if (networkLoaded) {
            float[] inputs = new float[21];
            inputs[0] = (float)(sensors.getSpeed() / 100);
            inputs[1] = (float)sensors.getAngleToTrackAxis();
            double[] dist = sensors.getTrackEdgeSensors();
            for (int i = 0; i < dist.length; i++) {
                inputs[i+2] = Math.max((float)(dist[i] / 100), 0);
            }

            network.setCurrentInput(inputs);
            network.feedForward();
            
            network.getLastLayer().head();
            double throtle = network.getLastLayer().getOutput();
            if (throtle > 0) {
                action.accelerate = throtle;
                action.brake = 0;
            }
            else {
                action.accelerate = 0;
                action.brake = -throtle;
            }

//            action.accelerate = getAccelerate(sensors.getSpeed());

            network.getLastLayer().tail();
            action.steering = network.getLastLayer().getOutput();
            
            action.gear = getGear(sensors);

//            System.out.print(action.accelerate);
//            System.out.print("\t");
//            System.out.print(action.brake);
//            System.out.print("\t");
//            System.out.println(action.steering);
        }
        return action;
    }
    
    private double getAccelerate(double speed) {
        double a = 1.0 - (speed/100);
        a = Math.max(a, 0.0);
        a = Math.min(a, 1.0);
        
        return a;
    }
    
    private int getGear(SensorModel sensors){
        int gear = sensors.getGear();
        double rpm  = sensors.getRPM();
        
        if (rpm > gearUp[gear]) {
            gear = Math.min(gear + 1, 6);
        }
        else if (rpm < gearDown[gear]) {
            gear = Math.max(gear - 1, 0);
        }
        return gear;
    }
    
    private boolean networkLoaded;
    
    /* Gear Changing Constants*/
    private static final int[] gearUp = {  3000, 5000, 6000, 6000, 6500, 7000};
    private static final int[] gearDown = {0,    940,  2500, 3000, 3500, 3500};
    
    private FNNXMLP network;
}
