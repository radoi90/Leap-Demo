/******************************************************************************\
* Copyright (C) 2012-2013 Leap Motion, Inc. All rights reserved.               *
* Leap Motion proprietary and confidential. Not for distribution.              *
* Use subject to the terms of the Leap Motion SDK Agreement available at       *
* https://developer.leapmotion.com/sdk_agreement, or another agreement         *
* between Leap Motion and you, your company or other organization.             *
\******************************************************************************/

import java.io.IOException;
import java.math.BigDecimal;

import com.leapmotion.leap.*;

class SampleListener extends Listener {
	private static boolean moving = false;
	
    public void onInit(Controller controller) {
        System.out.println("Initialized");
    }

    public void onConnect(Controller controller) {
        System.out.println("Connected");
    }

    public void onDisconnect(Controller controller) {
        //Note: not dispatched when running in a debugger.
        System.out.println("Disconnected");
    }

    public void onExit(Controller controller) {
        System.out.println("Exited");
    }

    public void onFrame(Controller controller) {
        // Get the most recent frame and report some basic information
        Frame frame = controller.frame();

        if (!frame.hands().isEmpty()) {
        	float av = avgVelocity(controller, 10);
        	float at = avgTurn(controller, 10);
        	//System.out.println(av + " " + at);
            
        	boolean newMoving = (av > 9 || at > 1.8);
            
        	if(moving != newMoving) {
            	moving = newMoving;
            	System.out.println(moving);
            }
        }
    }
    
    private float avgVelocity(Controller controller, int n) {
    	float avgV = 0;
    	
    	for(int i = 0; i < n; i++) {
    		Frame frame = controller.frame(i);
    		
    		if (frame.isValid() && !frame.hands().isEmpty()) {
                // Get the first hand
                Hand hand = frame.hands().get(0);
                avgV += hand.palmVelocity().magnitude();
    		}
    	}
    	
    	avgV /= n;
    	return avgV;
    }
    
    private float avgTurn(Controller controller, int n) {
    	float sum = 0;

    	for(int i = 0; i < n; i++) {
    		Frame frame = controller.frame(i);
    		
    		if (frame.isValid() && !frame.hands().isEmpty()) {
                // Get the first hand
                Hand hand = frame.hands().get(0);
                
                sum += hand.translation(controller.frame(i-1)).magnitude();
    		}
    	}
    	
    	sum /= n;
    	return sum;
    }
    
    public static float round(float d, int decimalPlace) {
        BigDecimal bd = new BigDecimal(Float.toString(d));
        bd = bd.setScale(decimalPlace, BigDecimal.ROUND_HALF_UP);
        return bd.floatValue();
    }
}

class Sample {
    public static void main(String[] args) {
        // Create a sample listener and controller
        SampleListener listener = new SampleListener();
        Controller controller = new Controller();

        // Have the sample listener receive events from the controller
        controller.addListener(listener);

        // Keep this process running until Enter is pressed
        System.out.println("Press Enter to quit...");
        try {
            System.in.read();
        } catch (IOException e) {
            e.printStackTrace();
        }

        // Remove the sample listener when done
        controller.removeListener(listener);
    }
}
