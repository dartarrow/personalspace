package edu.baylor.hci.Calibration;
import java.awt.AWTException;
import java.awt.Robot;

import org.apache.log4j.Logger;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.InputEvent;

import javax.swing.Timer;

import com.leapmotion.leap.Controller;
import com.leapmotion.leap.FingerList;
import com.leapmotion.leap.Frame;
import com.leapmotion.leap.Gesture;
import com.leapmotion.leap.GestureList;
import com.leapmotion.leap.Hand;
import com.leapmotion.leap.Listener;
import com.leapmotion.leap.Vector;

import edu.baylor.hci.LeapOMatic.TouchPoints;

    public class MouseListener extends Listener implements ActionListener {

    	/* == Class Constants == */
    	/* Scaling factor. Will (should?) be a different value on different resolutions. remember millimeters */
    	private static final int SCALING_FACTOR = 15;
    	/* we need some value to offset the origin, this is because there is NO WAY to get y-0 on the leap */ 
    	private static final int _Y_ORIGIN = 100;
    	/* screen origin starts top left, leap origin starts bottom, we need to invert this somehow
    	 * this is quite arbitrary. But we'll set the size to 200mm */
    	private static final int _Y_HEIGHT = 200;
    	/*  We need to shift the X-origin. So, the X-0 is the center of the screen. In millimeters */
    	private static final int X_ORIGIN = 0;
    	
    	private static final Double TRACKING_ANGLE_DISABLE_THRESHOLD = 2.0;
    	private static final int INITIAL_DELAY = 5000;
    	private static final int TIMER_INTERVAL = 2500;
    	/* Smoothen Grasp / Pinch action 
    	 * This is a number between 0 to infinity. 
    	 * indicating the ratio between the finger and palm speeds that we consider to be the action triggered. 
    	 * if set to 0, then turned off
    	 */ 
    	private static final int GRASP_DETECT_THRESHOLD = 5;
    	private enum GRASP_DETECT_ALGO {
    		ABSOLUTE_DIFFERENCE, 
    		SQUARED_DIFFERENCE,
    		ABSOLUTE_RATIO, 
    		SQUARED_RATIO
    	}
    	/*
    	 * Current Status of mouseListener meaning none=>normal mouse input, NORMAL_CURSOR=>De facto Standard, MATRIX_CURSOR=>Our Bear Hands approach using the matrix constructed to move cursor
    	 */
    	public enum Status {
    		NONE,
    		NORMAL_CURSOR,
    		MATRIX_CURSOR
    	}
    	//Used to determine what the mouseListener is doing
    	public Status status = Status.NONE;
    	private static final GRASP_DETECT_ALGO GRASP_ALGO = GRASP_DETECT_ALGO.ABSOLUTE_RATIO;  
    	
    	/** Object Vars **/
    	// the robot to move the mouse
    	private Robot mousebot;
    	// To log experiment data(mouseClick and mouseMovements with times)
    	
    	// a settable value for y's origin point, (distance from the ground/leap)
    	private int yOrigin = _Y_ORIGIN;
    	// a settable value for y's max value. needed for inversion
    	private int yMaxHeight = _Y_HEIGHT;
    	// used to simulate a mouse button down. basically a toggle
    	private boolean mouseLeftButtonDown = false;
    	//used to check if we should be tracking user's movement. If the hand is not inverted(track) else disable tracking
    	/** private boolean handInverted = false; // FIXME: isn't used. Remove all reference (currently commented out) **/
    	private Timer timer;
    	private boolean timerHasExpired=true;
    	private static final Logger logger = Logger.getLogger(MouseListener.class);
    	
    	/**
    	 * Constructor - only needs to instantiate the Robot to move the mouse
    	 */
    	public MouseListener() {
    		try {
				this.mousebot = new Robot();
			} catch (AWTException e) {
				logger.error(e.getMessage());
				e.printStackTrace();
			}
    	}
    	
    	
    	/**
    	 * onConnect we register all the gestures we want to process here.
    	 * The only thing we will be looking out for is the  
    	 */
    	public void onConnect(Controller controller) {
    		controller.enableGesture(Gesture.Type.TYPE_KEY_TAP);
    		controller.enableGesture(Gesture.Type.TYPE_SWIPE, false);
    		controller.enableGesture(Gesture.Type.TYPE_CIRCLE , false);
    		controller.enableGesture(Gesture.Type.TYPE_SCREEN_TAP, false);
    	}
    	
    	/**
    	 * This function is called when we are using the MATRIX_CURSOR Status, calls checkHandInverted and returns a Vector with X, Y, and Z
    	 * Also returns a Vector with x and y in it.
    	 * @param frame
    	 * @return
    	 */
    	private Vector getCoords(Frame frame, Vector palm)
    	{
    		Hand hand= frame.hands().get(0);
    		Vector outputCoords = null;
    		try {
				outputCoords = TouchPoints.getCoordinateTransformer().getOutputCoordinates(palm.getX(), palm.getY(), palm.getZ());
				checkHandInverted(palm, hand);
			} catch (Exception e) {
				logger.error(e.getMessage());
				e.printStackTrace();
			}
    		return outputCoords;
    	}

    	/**
    	 * Checks if the hand has been inverted from our expected palmNormal, if this has occurred we start our timer for disable tracking
    	 * @param palm
    	 * @param hand
    	 * @throws Exception
    	 */
		private void checkHandInverted(Vector palm, Hand hand) throws Exception {
			Vector expectedNormalVector;
			expectedNormalVector = TouchPoints.getPalmNormalTransformer().getOutputCoordinates(palm.getX(), palm.getY(), palm.getZ());
			if(Math.abs(hand.palmNormal().angleTo(expectedNormalVector))>=TRACKING_ANGLE_DISABLE_THRESHOLD && timerHasExpired && !mouseLeftButtonDown) 
			{ //if we are inverting our hand, and not grasping(leftClick) and the stop trackingTimer is not running we enter here
				if(TouchPoints.isStopGestureEnabled())
				{
					timer= new Timer(TIMER_INTERVAL, this);
					timer.setInitialDelay(INITIAL_DELAY);
				    timer.start();
					timerHasExpired=false; //will be toggled when timer actionPerformed occurs.
				}
			}
		}
    	/**
    	 * On leap tick, move the mouse
    	 */
        public void onFrame(Controller controller) {
        	if(this.status==Status.NORMAL_CURSOR || this.status==Status.MATRIX_CURSOR) {
	            // Get the most recent frame and report some basic information
	            Frame frame = controller.frame();
                
	            if (!frame.hands().isEmpty()) {
	                /* Get the hand's sphere radius and palm position
	                 * Move the cursor to the onscreen position based on the position of the palm
	                 */
	            	
	            	
	                Vector palm = frame.hands().get(0).stabilizedPalmPosition();
	                int newX = Integer.MIN_VALUE, newY = Integer.MIN_VALUE;
            		logger.trace("Leap Coordinates: " + palm.getX() + " " + palm.getY() + " " + palm.getZ());
            		
	                if(this.status==Status.MATRIX_CURSOR)
	                {
	                	Vector temp=getCoords(frame, palm); //feeds palm into our getCoords function which uses the transformationMatrix to generate proper screen coordinates based on calibration
	                	newX=(int)temp.getX();
	                	newY=(int)temp.getY();
	                	logger.trace("Matrix translated:" + newX + " " + newY);
	                	//call function
	                }
	                else
	                {
		                // get the translated coordinates
		                newX = this.translateX(palm.getX());
		                newY = this.translateY(palm.getY());
		                logger.trace("Nomatrix translated: " + newX + " " + newY);
	                }
	                
	            	// check the grasp detector. if we're making a gesture then no need to do any movements
	            	if(this.isGraspGesture(frame.hand(0).fingers(), frame.hand(0).palmVelocity())) {
	            		logger.debug("Grasping");
	            	} else {
	            		if(timerHasExpired)
	            		{
	            			this.mousebot.mouseMove(newX, newY);
	            			/*** TouchPoints.getMouseLogger().addMouseXY(newX, newY); //log x and y in experimentLog ***/
	            		}
	            	}
	                
	                /* If the palm is detected, BUT there are no fingers detected, 
	                 * it is to be assumed that the user is making a fist.  
	                 * We will interpret this to mean a mouse left button down
	                 */
	                // if user made a fist, and button is not currently held down, and there is no stopTracking timer running, then hold it down
	                if(frame.hands().get(0).fingers().count() == 0 && !this.mouseLeftButtonDown && timerHasExpired && TouchPoints.isLeftClickEnabled())
	                {
	                	logger.debug("Detected palm but no fingers, triggering mouse keypress");
	                	this.mousebot.mousePress(InputEvent.BUTTON1_DOWN_MASK);
	                	this.mouseLeftButtonDown = true;
	                	/*** TouchPoints.getMouseLogger().addMousePress(newX, newY); ***/
	                }
	                /* if button is currently held down, AND user is not making a fist, then release
	                 * Now.. SOMETIMES when we use.. not a fist but a lizard (RockPaperScissorsLizardSpock) 
	                 * to map to this gesture, it will then almost immediately find ONE finger. 
	                 * So. the release will look for MORE than one finger. 
	                 * IF this is not sufficient we can set it to.. say.. 3 fingers minimum
	                 */
	                if(frame.hands().get(0).fingers().count() > 1 && this.mouseLeftButtonDown && TouchPoints.isLeftClickEnabled())
	                {
	                	logger.debug("Releasing mouse keypress");
	                	this.mousebot.mouseRelease(InputEvent.BUTTON1_DOWN_MASK);
	                	this.mouseLeftButtonDown = false;
	                	/*** TouchPoints.getMouseLogger().addMouseRelease(newX, newY); ***/
	                }
	                
	                
	                /* Process the gestures to indicate some form of action
	                 * The only action we actually listen for is the keytap
	                 * which maps to a left mouse click. 
	                 * UPDATE:In order for the StarCraft demonstration we added right click which corresponds to a circle gesture
	                 */
	                GestureList gestures = frame.gestures();
	                for (int i = 0; i < gestures.count(); i++) {
	                    Gesture gesture = gestures.get(i);
	                    //gesture.type().
	                    switch (gesture.type()) {
	                    case TYPE_KEY_TAP:
	                    	if(timerHasExpired && TouchPoints.isLeftClickEnabled())
	                    	{
		                    	logger.debug("Detected keytap, triggering mouse click");
		                    	this.mousebot.mousePress(InputEvent.BUTTON1_DOWN_MASK);
		                    	this.mousebot.mouseRelease(InputEvent.BUTTON1_DOWN_MASK);
		                    	/*** TouchPoints.getMouseLogger().addMouseClick(newX, newY); ***/
	                    	}
	                    	break;
	                    case TYPE_CIRCLE:
	                    {
	                    	if(TouchPoints.isRightClickEnabled())
	                    	{	
	                    		this.mousebot.mousePress(InputEvent.BUTTON3_DOWN_MASK);
	                    		this.mousebot.mouseRelease(InputEvent.BUTTON3_DOWN_MASK);
	                    	}
	                    }
						default:
							// catch anything else. Technically should never happen... but we'll see. 
							logger.warn("Detected a strange gesture: " + gesture.type().toString());
							break;
	                    }
	                }

	            }
        	}
        }
        
        
        /**
         * Check if the grasp gesture is being done
         * @param fingers
         * @param palmSpeed
         * @return
         */
        @SuppressWarnings("unused") // becase the grasp_detect is static. but Alvin wants this. 
		private boolean isGraspGesture(FingerList fingers, Vector palmSpeed) {
        	// if the threshold is set to 0, then there's no need to check, just say no. 
        	if(GRASP_DETECT_THRESHOLD <= 0) return false;
        		
        	
        	/* Check the palm velocity against the finger velocity. 
        	 * BUT there's quite a few ways to do this. So 4 algos are implemented here.  
        	 * This implementation takes the finger speed of the frontmost finger, 
        	 * Alternatives:
        	 * 	average speed of all fingers
        	 *  maximum speed of any one finger 
        	 * But intuitively Alvin thinks frontmost > max > average. 
        	 * ***Of course needs testing***
        	 */
        	switch(GRASP_ALGO) {
        	case ABSOLUTE_DIFFERENCE:
        		if(Math.abs(fingers.frontmost().tipVelocity().magnitude() - palmSpeed.magnitude()) > GRASP_DETECT_THRESHOLD) {
        			return true;
        		} else return false;        		
        	case ABSOLUTE_RATIO:
        		if(Math.abs(fingers.frontmost().tipVelocity().magnitude() / palmSpeed.magnitude()) > GRASP_DETECT_THRESHOLD) {
        			return true;
        		} else return false;
        	case SQUARED_DIFFERENCE:
        		if(Math.abs(fingers.frontmost().tipVelocity().magnitudeSquared() - palmSpeed.magnitudeSquared()) > GRASP_DETECT_THRESHOLD) {
        			return true;
        		} else return false;
        	case SQUARED_RATIO:
        		if(Math.abs(fingers.frontmost().tipVelocity().magnitudeSquared() / palmSpeed.magnitudeSquared()) > GRASP_DETECT_THRESHOLD) {
        			return true;
        		} else return false;
			default:
				return false;
        	}
        }
        
        public void setStatus(Status newStatus)
        {
        	this.status= newStatus;
        }
        
        /**
         * Pretty simple translation from an X offset. The problem here is that the left side is hard limited, 
         * while the right side is pretty much to infinity. Or until leap stops detecting. Whichever comes first. 
         * @param leapXCoord
         * @return screen coordinates for the X axis
         */
        private int translateX(float leapXCoord) {
        	return (int) (X_ORIGIN + leapXCoord) * SCALING_FACTOR;
        }
        
        /**
         * Returns the new Y coordinates. The new Y coordinates are based on some offset from the bottom (because the bottom is never 0)
         * And the TOP value needs to be recalculated. and the top for leap is unbounded and positive. While the screen is Y-positive at the bottom 
         * @param leapYCoord
         * @return screen coordinates for the Y axis
         */
        private int translateY(float leapYCoord) {
        	// first employ the offset
        	int offsetY = (int)leapYCoord - yOrigin;
        	// if its less than 0, then just reset the value to 0
        	if(offsetY < 0) offsetY = 0;
        	// flip it upside down, and employ the scaling factor
        	int newHeight = ((yOrigin + yMaxHeight) - (int)leapYCoord) * SCALING_FACTOR ;
        	return newHeight;

        }


		@Override
		public void actionPerformed(ActionEvent arg0) {
			// TODO Auto-generated method stub
			timer.stop();
			timerHasExpired=true;
		}
        
        
    }
