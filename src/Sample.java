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

class HandSampler extends Listener {
	private boolean moving = false;
	private boolean viable = false;
	private Hand currSample;
	private static final int HISTORY = 10;
	private static final float MOVING_VELOCITY = 25;
	private static final float MOVING_TURN = 0.3f;
	
	public Hand getSample() {
		if (!viable) return null;
		else return currSample;
	}
	
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
        	boolean newMoving = isMoving(controller);
            
        	if (moving != newMoving) {
            	moving = newMoving;
            }
        	
        	if (moving) {
        		viable = false;
        	} else {
        		viable = true;
        		currSample = frame.hands().get(0);
        	}
        } else {
        	viable = false;
        }
    }
    
    private float avgVelocity(Controller controller, int n) {
    	Vector sum = Vector.zero();
    	
    	for(int i = 0; i < n; i++) {
    		Frame frame = controller.frame(i);
    		
    		if (frame.isValid() && !frame.hands().isEmpty()) {
                // Get the first hand
                Hand hand = frame.hands().get(0);
                sum = sum.plus(hand.palmVelocity());
    		}
    	}
    	
    	sum = sum.divide(n);
    	return sum.magnitude();
    }
    
    private float avgTurn(Controller controller, int n) {
    	Vector sum = Vector.zero();

    	for(int i = 0; i < n; i++) {
    		Frame frame = controller.frame(i);
    		
    		if (frame.isValid() && !frame.hands().isEmpty()) {
                // Get the first hand
                Hand hand = frame.hands().get(0);
                
                sum = sum.plus(hand.translation(controller.frame(i-1)));
    		}
    	}
    	
    	sum = sum.divide(n);
    	return sum.magnitude();
    }
    
    private boolean isMoving(Controller controller) {
    	return (avgVelocity(controller, HISTORY) > MOVING_VELOCITY || avgTurn(controller, HISTORY) > MOVING_TURN);
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
        HandSampler sampler = new HandSampler();
        Controller controller = new Controller();

        // Have the sample listener receive events from the controller
        controller.addListener(sampler);
        
        Hand control, test;
        
        // Keep this process running until there is a sample and Enter is pressed 
        while(true) {
        	System.out.println("Keep hand still and press Enter to record control hand gesture...");
	        try {
	            System.in.read();
	        } catch (IOException e) {
	            e.printStackTrace();
	        }
	        control = sampler.getSample();
	        if (control != null) break;
        }

        // Remove the sample listener when done
        controller.removeListener(sampler);
        
        controller.addListener(sampler);
        
        // Keep this process running until there is a sample and Enter is pressed 
        while(true) {
        	System.out.println("Keep hand still and press Enter to record test hand gesture...");
	        try {
	            System.in.read();
	        } catch (IOException e) {
	            e.printStackTrace();
	        }
	        test = sampler.getSample();
	        if (test != null) break;
        }
     
        // Remove the sample listener when done
        controller.removeListener(sampler);
        
        System.out.println(Math.abs(control.fingers().count() - test.fingers().count()));
    }
}
