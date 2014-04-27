package edu.baylor.hci.LeapOMatic;
import java.io.File;

import javax.swing.JLabel;
import javax.swing.JTextField;

import org.apache.log4j.Logger;

import com.leapmotion.leap.CircleGesture;
import com.leapmotion.leap.Controller;
import com.leapmotion.leap.Finger;
import com.leapmotion.leap.FingerList;
import com.leapmotion.leap.Frame;
import com.leapmotion.leap.Gesture;
import com.leapmotion.leap.Gesture.State;
import com.leapmotion.leap.GestureList;
import com.leapmotion.leap.Hand;
import com.leapmotion.leap.KeyTapGesture;
import com.leapmotion.leap.Listener;
import com.leapmotion.leap.ScreenTapGesture;
import com.leapmotion.leap.SwipeGesture;
import com.leapmotion.leap.Vector;

import edu.baylor.hci.Logger.PositionLog;

    /**
     * This class extends the base Listener. 
     * Since this is meant for GUI stuff, a JLabel is required
     * @author Alvin
     *
     * TODO:
     * 	hold titles fixed / JLabel autoscroll
     * 	get x/y/z positions
     *  draw positions / gestures / blah onto a graphwi
     */
    public class GuiListener extends Listener {
    	/** Class Constants **/
    	private static final int ROUNDED_TO = 1;

    	/* used to store a running min / max values */
    	/* FIXME accessors shouldn't be public this way. Use getters 
    	 * FIXME set a X-max, Y-max, Z-max, X-min, etc 
    	 */
    	public static Float max=Float.MIN_NORMAL;
    	public static Float min=Float.MAX_VALUE;

		private JLabel jlPitch, jlRoll, jlYaw;
		private JLabel jlFingers; // finger count, low pri
		private JLabel jlGestures;
		private JLabel jlPalm;
		private JLabel jlPalmNormal;
		private JLabel jlPalmVelocity;
		private JLabel jlAvgFingerPos;
		private JLabel jlLogInfo;
		private PositionLog positionLog;
		
		
		private JTextField  jlCircleMinRadius;
		private JTextField  jlCircleMinArc;
		private JTextField  jlSwipeMinLength;
		private JTextField  jlSwipeMinVelocity;
		private JTextField  jlKeyTapMinDownVelocity;
		private JTextField  jlKeyTapHistorySeconds;
		private JTextField  jlKeyTapMinDistance;
		private JTextField  jlScreenTapMinForwardVelocity;
		private JTextField  jlScreenTapHistorySeconds;
		private JTextField  jlScreenTapMinDistance;

		// finger stuff set in arrays
		private JLabel[] jlTipPositions;
		private JLabel[] jlFingerWidth;
		private JLabel[] jlFingerDirection;
		private JLabel[] jlFingerVelocity;
		private JLabel[] jlFingerLength;

		private JLabel jlPalmNormAngleToExpected;
    	
		// declare the logger
	    private static final Logger logger = Logger.getLogger(GuiListener.class);
	    // fine grained control if we want the really noisy logging
	    private static final boolean TRACE_LOG = false; 

    	/**
    	 * Construct the Leap Listener, with the following intentions:
    	 *   1. All GUI elements should be optional. if they exist, set the value, and if they don't exist, then don't set
    	 *   2. PositionLog is mandatory because it has high dependency with the system as a whole. Not a great idea but will let it be for now. 
    	 * @param positionLog
    	 */
    	public GuiListener(PositionLog positionLog) {
        	this.positionLog = positionLog;
    	}
    	
        public void onInit(Controller controller) {
    		// if the directory does not exist, create it or die trying. 
    		File dir = new File(Settings.LOGFILE_DIR); 
    		if (!dir.exists()) {
    			logger.debug("creating directory: " + dir);
    			if(dir.mkdir()) {    
    				logger.debug("dir created");  
    			} else {
    				logger.fatal("Could not create directory " + dir);
    				System.exit(1);
    			}
    		}
        }

        public void onConnect(Controller controller) {
            logger.debug("Connected");
            controller.enableGesture(Gesture.Type.TYPE_SWIPE);
            controller.enableGesture(Gesture.Type.TYPE_CIRCLE);
            controller.enableGesture(Gesture.Type.TYPE_SCREEN_TAP);
            controller.enableGesture(Gesture.Type.TYPE_KEY_TAP);
            this.setConfigPanelText(controller);
            
           // Gesture
        }
        
        public void setConfigPanelText(Controller controller)
        {
        	this.setTextCircleMinRadius(Float.toString(controller.config().getFloat("Gesture.Circle.MinRadius")));
        	this.setTextCircleMinArc(Float.toString(controller.config().getFloat("Gesture.Circle.MinArc")));
        	this.setTextSwipeMinLength(Float.toString(controller.config().getFloat("Gesture.Swipe.MinLength")));
        	this.setTextSwipeMinVelocity(Float.toString(controller.config().getFloat("Gesture.Swipe.MinVelocity")));
        	this.setTextKeyTapMinDownVelocity(Float.toString(controller.config().getFloat("Gesture.KeyTap.MinDownVelocity")));
        	this.setTextKeyTapHistorySeconds(Float.toString(controller.config().getFloat("Gesture.KeyTap.HistorySeconds")));
        	this.setTextKeyTapMinDistance(Float.toString(controller.config().getFloat("Gesture.KeyTap.MinDistance")));
        	this.setTextScreenTapMinForwardVelocity(Float.toString(controller.config().getFloat("Gesture.ScreenTap.MinForwardVelocity")));
        	this.setTextScreenTapHistorySeconds(Float.toString(controller.config().getFloat("Gesture.ScreenTap.HistorySeconds")));
        	this.setTextScreenTapMinDistance(Float.toString(controller.config().getFloat("Gesture.ScreenTap.MinDistance")));
        }
       

        public void onDisconnect(Controller controller) {
            //Note: not dispatched when running in a debugger.
        	logger.debug("Disconnected");
        }

        public void onExit(Controller controller) {
        	logger.debug("Exited");
        }
        
        public void runningMaxAndMinx(Float x, Float y, Float z)
        {
        	Float tempMax=Math.max(Math.max(x, y), z);
        	Float tempMin=Math.min(Math.min(x, y), z);
        	if(tempMax>max)
        		max=tempMax;
        	if(tempMin<min)
        		min=tempMin;
        }
        public void onFrame(Controller controller) {
            // Get the most recent frame and report some basic information
            Frame frame = controller.frame();
            if(TRACE_LOG) {
	            logger.trace("Frame id: " + frame.id()
	                             + ", timestamp: " + frame.timestamp()
	                             + ", hands: " + frame.hands().count()
	                             + ", fingers: " + frame.fingers().count()
	                             + ", tools: " + frame.tools().count()
	                             + ", gestures " + frame.gestures().count()
	                             + ", FingerWidth" + frame.fingers().frontmost().width());
            }

            if (!frame.hands().isEmpty()) {
                // Get the first hand
                Hand hand = frame.hands().get(0);

                // Check if the hand has any fingers
                FingerList fingers = hand.fingers();
                if (!fingers.isEmpty()) {
                    this.setTextFingers(fingers);
                    this.setTextAvgFingerPos(fingers);
                    // set the finger details
                    int fingerCounter = 0;
                    for(Finger finger : frame.fingers())
                    {
                    	/** this part populates all the arrays **/
                    	// finger Width
                    	this.setTextFingerWidth(finger.width(), fingerCounter);
                    	// finger position
                    	Vector position = finger.tipPosition();
                    	// Vector position = finger.stabilizedTipPosition();
                    	this.setTextTipPos(position.getX(), position.getY(), position.getZ(), fingerCounter);
                    	// finger direction
                    	this.setTextFingerDirection(finger.direction().getX(), finger.direction().getY(), finger.direction().getZ(), fingerCounter);
                    	// finger velocity
                    	this.setTextFingerVelocity(finger.tipVelocity().getX(), finger.tipVelocity().getY(), finger.tipVelocity().getZ(), fingerCounter);
                    	// finger length
                    	this.setTextFingerLength(finger.length(), fingerCounter);
                    	
                    	/* DEBUG. Check the difference in speed between finger and palm when the fist gesture is made */
                    	
                    	
                    	fingerCounter++;
                    }
                    
                }

                // Get the hand's sphere radius and palm position
                if(TRACE_LOG) {
                	logger.trace("Hand sphere radius: " + hand.sphereRadius()+ " mm, palm position: " + hand.palmPosition());
                }

                // Get the hand's normal vector and direction
                Vector normal = hand.palmNormal();
                Vector direction = hand.direction();

                // set the pitch, roll, yaw
                this.setTextPitch(direction.pitch());
                this.setTextRoll(normal.roll());
                this.setTextYaw(direction.yaw());
                
                // Set the palms position
                Vector palm = hand.palmPosition();
                Vector palmNormal= hand.palmNormal();
                Vector palmStable = hand.stabilizedPalmPosition(); //
                
                // for the Gui, just set one of the values. For now that means the regular palm. 
                this.setTextPalm(palm);
                this.setTextPalmNormal(palmNormal);
                this.setTextPalmVelocity(hand.palmVelocity(), hand.fingers());
                Vector expectedNormalVector;
    			try {
    				if(TouchPoints.getPalmNormalTransformer().sourceMatrixIsSet())
    				{
    					expectedNormalVector = TouchPoints.getPalmNormalTransformer().getOutputCoordinates(palm.getX(), palm.getY(), palm.getZ());
    					this.setTextPalmNormAngleToExpected(hand.palmNormal().angleTo(expectedNormalVector));
    				}
				} catch (Exception e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}

                if(this.positionLog != null) {
                	runningMaxAndMinx(palm.getX(), palm.getY(), palm.getZ());
                	this.positionLog.addPalmX(palm.getX());
                	this.positionLog.addPalmY(palm.getY());
                	this.positionLog.addPalmZ(palm.getZ());
                	this.positionLog.addStablePalmX(palmStable.getX());
                	this.positionLog.addStablePalmY(palmStable.getY());
                	this.positionLog.addStablePalmZ(palmStable.getZ());
                	this.positionLog.addPalmNormal(palmNormal);
                	this.positionLog.addSphereRadius(hand.sphereRadius());
                }
            }

            // There can be more than one gesture.
            GestureList gestures = frame.gestures();
            for (int i = 0; i < gestures.count(); i++) {
                Gesture gesture = gestures.get(i);

                switch (gesture.type()) {
                    case TYPE_CIRCLE:
                        CircleGesture circle = new CircleGesture(gesture);
                        // Calculate clock direction using the angle between circle normal and pointable
                        String clockwiseness;
                        if (circle.pointable().direction().angleTo(circle.normal()) <= Math.PI/4) {
                            // Clockwise if angle is less than 90 degrees
                            clockwiseness = "clockwise";
                        } else {
                            clockwiseness = "counterclockwise";
                        }

                        // Calculate angle swept since last frame
                        double sweptAngle = 0;
                        if (circle.state() != State.STATE_START) {
                            CircleGesture previousUpdate = new CircleGesture(controller.frame(1).gesture(circle.id()));
                            sweptAngle = (circle.progress() - previousUpdate.progress()) * 2 * Math.PI;
                        }

                        if(TRACE_LOG) {
	                        logger.trace("Circle id: " + circle.id()
	                                   + ", " + circle.state()
	                                   + ", progress: " + circle.progress()
	                                   + ", radius: " + circle.radius()
	                                   + ", angle: " + Math.toDegrees(sweptAngle)
	                                   + ", " + clockwiseness);
                        }
                        
                        this.setTextGestures("<HTML>" 
                                   		   + clockwiseness + " circle.<br>"
                        				   + "Radius: " + circle.radius() 
                        				   + "<br>angle: " + Math.toDegrees(sweptAngle) 
                        				   + "</HTML>");
                        break;
                    case TYPE_SWIPE:
                    	/** 
                    	 * TODO: 
                    	 * 	Find (primary) directions 
                    	 *	adjust sensitivity 
                    	 **/                    	
                        SwipeGesture swipe = new SwipeGesture(gesture);
                        // get direction
                        String swipeDirection; 
                        if(Math.abs(swipe.direction().getX()) >= Math.abs(swipe.direction().getY())) {
                        	if(swipe.direction().getX() > 0) {
                        		swipeDirection = "right";
                        	} else swipeDirection = "left";
                        } else {
                        	if(swipe.direction().getY() > 0) {
                        		swipeDirection = "up";
                        	} else swipeDirection = "down";
                        }
                        if(TRACE_LOG) {
	                        logger.trace("Swipe id: " + swipe.id()
	                                   + ", " + swipe.state()
	                                   + ", position: " + swipe.position()
	                                   + ", direction: " + swipe.direction()
	                                   + ", speed: " + swipe.speed());
                        }
                        this.setTextGestures("<HTML>Swipe: " + swipe.direction() + "<br>"
                        					+ "Direction : " + swipeDirection) ;
                        break;
                    case TYPE_SCREEN_TAP:
                        ScreenTapGesture screenTap = new ScreenTapGesture(gesture);
                        if(TRACE_LOG) {
	                        logger.trace("Screen Tap id: " + screenTap.id()
	                                   + ", " + screenTap.state()
	                                   + ", position: " + screenTap.position()
	                                   + ", direction: " + screenTap.direction());
                        }
                        this.setTextGestures("<HTML>Screen Tap id: " + screenTap.id()
                                   + ", " + screenTap.state()
                                   + "<br>  position: " + screenTap.position()
                                   + ", direction: " + screenTap.direction());
                        break;
                    case TYPE_KEY_TAP:
                        KeyTapGesture keyTap = new KeyTapGesture(gesture);
                        if(TRACE_LOG) {
	                        logger.trace("Key Tap id: " + keyTap.id()
	                                   + ", " + keyTap.state()
	                                   + ", position: " + keyTap.position()
	                                   + ", direction: " + keyTap.direction());
                        }
                        this.setTextGestures("<HTML>Key Tap id: " + keyTap.id()
                        + ", " + keyTap.state()
                        + "<br> position: " + keyTap.position()
                        + ", direction: " + keyTap.direction());
                        break;
                    default:
                    	logger.error("Unknown gesture type detected.");
                        break;
                }
            }

            this.setTextLogInfo(this.positionLog.getLogStatus().toString());
        }
        
        
        public void onFocusLost() {
        	logger.debug("Focus Lost");
        	this.positionLog.writeLog();
        }
        
        


        // == Setters == //
		public void hookLabelPitch(JLabel jlPitch) {
			this.jlPitch = jlPitch;
		}

		public void hookLabelRoll(JLabel jlRoll) {
			this.jlRoll = jlRoll;
		}

		public void hookLabelYaw(JLabel jlYaw) {
			this.jlYaw = jlYaw;
		}

		public void hookLabelFingers(JLabel jlFingers) {
			this.jlFingers = jlFingers;
		}

		public void hookLabelGestures(JLabel jlGestures) {
			this.jlGestures = jlGestures;
		}

		public void hookLabelPalm(JLabel jlPalm) {
			this.jlPalm = jlPalm;
		}
		
		
		public void hookLabelPalmNormAngleToExpected(JLabel jlPalmNormAngleToExpected) {
			this.jlPalmNormAngleToExpected = jlPalmNormAngleToExpected;
		}
		public void hookLabelPalmNormal(JLabel jlPalmNormal) {
			this.jlPalmNormal = jlPalmNormal;
		}
		
		public void hookLabelPalmVelocity(JLabel jlPalmVelocity) {
			this.jlPalmVelocity = jlPalmVelocity;
		}
		
		public void hookLabelAvgFingerPos(JLabel jlAvgFingerPos) {
			this.jlAvgFingerPos = jlAvgFingerPos;
		}

		public void hookLabelLogInfo(JLabel jlLogInfo) {
			this.jlLogInfo = jlLogInfo;
		}

		public void hookLabelFingerWidth(JLabel[] jlFingerWidth) { 
			this.jlFingerWidth = jlFingerWidth;
    	}
		
		public void hookLabelTipPositions(JLabel[] jlTipPositions) {
			this.jlTipPositions = jlTipPositions;
		}

		public void hookLabelFingerDirection(JLabel[] jlFingerDirection) {
			this.jlFingerDirection = jlFingerDirection;
		}
		
		public void hookLabelFingerVelocity(JLabel[] jlFingerVelocity) {
			this.jlFingerVelocity = jlFingerVelocity;
		}
		
		public void hookLabelFingerLength(JLabel[] jlFingerLength) {
			this.jlFingerLength = jlFingerLength;
		}
		public void hookLabelCircleMinRadius(JTextField  jlCircleMinRadius) {
			this.jlCircleMinRadius = jlCircleMinRadius;
		}

		public void hookLabelCircleMinArc(JTextField  jlCircleMinArc) {
			this.jlCircleMinArc = jlCircleMinArc;
		}

		public void hookLabelSwipeMinLength(JTextField  jlSwipeMinLength) {
			this.jlSwipeMinLength = jlSwipeMinLength;
		}

		public void hookLabelSwipeMinVelocity(JTextField  jlSwipeMinVelocity) {
			this.jlSwipeMinVelocity = jlSwipeMinVelocity;
		}

		public void hookLabelKeyTapMinDownVelocity(JTextField  jlKeyTapMinDownVelocity) {
			this.jlKeyTapMinDownVelocity = jlKeyTapMinDownVelocity;
		}

		public void hookLabelKeyTapHistorySeconds(JTextField  jlKeyTapHistorySeconds) {
			this.jlKeyTapHistorySeconds = jlKeyTapHistorySeconds;
		}

		public void hookLabelKeyTapMinDistance(JTextField  jlKeyTapMinDistance) {
			this.jlKeyTapMinDistance = jlKeyTapMinDistance;
		}

		public void hookLabelScreenTapMinForwardVelocity(
				JTextField  jlScreenTapMinForwardVelocity) {
			this.jlScreenTapMinForwardVelocity = jlScreenTapMinForwardVelocity;
		}

		public void hookLabelScreenTapHistorySeconds(JTextField  jlScreenTapHistorySeconds) {
			this.jlScreenTapHistorySeconds = jlScreenTapHistorySeconds;
		}
		public void hookLabelScreenTapMinDistance(JTextField  jlScreenTapMinDistance)
		{
			this.jlScreenTapMinDistance=jlScreenTapMinDistance;
		}
		
        // == Set the text iff the UI element exists == //
		/* This portion is done in a somewhat abstract manner for 2 reasons:
		 * 1. Separation between the element methods
		 * 		so that there is a separation between the UI element and the text setting method and / or formats. 
		 * 		IOW: we are therefore not bound to one specific type of UI element (eg: JLabel)
		 * 2. None of these UI elements are therefore strictly necessary. 
		 * 		The View that is connected to this listener can hook any UI element it chooses to
		 * 		And likewise can choose to not hook any UI element it chooses not to  
		 */
		private void setTextPitch(float pitchRadians) {
			if(this.jlPitch != null) {
				this.jlPitch.setText(String.format("%03d degs", roundDegrees(pitchRadians)));
			}
		}

		private void setTextRoll(float rollRadians) {
			if(this.jlRoll != null) {
				this.jlRoll.setText(String.format("%03d degs", roundDegrees(rollRadians)));
			}
		}

		private void setTextYaw(float yawRadians) {
			if(this.jlYaw != null) {
				this.jlYaw.setText(String.format("%03d degs", roundDegrees(yawRadians)));;
			}
		}

		private void setTextFingers(FingerList fingers) {
			if(this.jlFingers != null) {
				this.jlFingers.setText(String.format("%d ", fingers.count()));
			}
		}

		private void setTextGestures(String text) {
			if(this.jlGestures != null) this.jlGestures.setText(text);
		}

		private void setTextPalm(Vector palm) {
			if(this.jlPalm != null) this.jlPalm.setText(String.format("%.0f, %.0f, %.0f",palm.getX(), palm.getY(), palm.getZ()));
		}
		private void setTextPalmNormAngleToExpected(float angleTo)
		{
			if(this.jlPalmNormAngleToExpected!= null)
			{
				this.jlPalmNormAngleToExpected.setText(Float.toString(angleTo));
			}
		}
		
		private void setTextPalmNormal(Vector palmNormal) {
			if(this.jlPalmNormal != null) this.jlPalmNormal.setText(String.format("%.4f, %.4f, %.4f", palmNormal.getX(), palmNormal.getY(), palmNormal.getZ()));
			//if(this.jlPalmNormal != null) this.jlPalmNormal.setText(palmNormal.toString()); //String.format("%.0f, %.0f, %.0f", palmNormal.getX(), palmNormal.getY(), palmNormal.getZ()));
		}
		
 		private void setTextPalmVelocity(Vector palmSpeed, FingerList fingers) {
 			
 			float maxFingerTipVelocity = 0;
 			// Calculate the Max finger position
 			for(Finger finger : fingers) {
 				if(finger.tipVelocity().magnitude() > maxFingerTipVelocity) {
 					maxFingerTipVelocity += finger.tipVelocity().magnitude();
 				}
 			}
 			
 			float avgFingerTipVelocity = 0;
 			float totalFingerTipVelocity = 0;
 			for(Finger finger : fingers) {
 				totalFingerTipVelocity += finger.tipVelocity().magnitude();
 			}
 			avgFingerTipVelocity = totalFingerTipVelocity / fingers.count();

 			
 			// set the TOTAL speed of the palm, based on absolute X, Y and Z
 			// palmSpeed.magnitude is equivalent to Math.abs(palmSpeed.getX()) + Math.abs(palmSpeed.getY()) + Math.abs(palmSpeed.getZ())
 			
 			// if(this.jlPalmVelocity != null) this.jlPalmVelocity.setText(String.format("%.0f | %.0f | %.0f", palmSpeed.magnitude(), maxFingerTipVelocity, maxFingerTipVelocity / palmSpeed.magnitude() ));
 			
 			int speedRatio = (int) (avgFingerTipVelocity / palmSpeed.magnitude());
 			String outtext;
 			if(speedRatio > 8) outtext = "NO";
 			else outtext = "YES";
 			
 			if(this.jlPalmVelocity != null) this.jlPalmVelocity.setText(outtext);
 		}
		
		private void setTextAvgFingerPos(FingerList fingers) {

			if(this.jlAvgFingerPos != null) {
	            // Calculate the hand's average finger tip position
	            Vector avgPos = Vector.zero();
	            for (Finger finger : fingers) {
	                avgPos = avgPos.plus(finger.tipPosition());
	            }
	            avgPos = avgPos.divide(fingers.count());
	            this.jlAvgFingerPos.setText(String.format("%.0f, %.0f, %.0f", avgPos.getX(), avgPos.getY(), avgPos.getZ()));
	            // Also add the finger positions to the position log
	            this.positionLog.addAvgFingerX(avgPos.getX());
	            this.positionLog.addAvgFingerY(avgPos.getY());
	            this.positionLog.addAvgFingerZ(avgPos.getZ());
			}
		}

		private void setTextLogInfo(String text) {
			if(this.jlLogInfo != null) this.jlLogInfo.setText(text);;
		}

		private void setTextFingerWidth(float width, int fingerId) {
			if(this.jlFingerWidth != null && this.jlFingerWidth.length > fingerId) {
				this.jlFingerWidth[fingerId].setText(String.format("%.0f", width));
			}
		}
		
		private void setTextTipPos(float xCoord, float yCoord, float zCoord, int fingerId) {
			if(this.jlTipPositions != null && this.jlTipPositions.length > fingerId) {
				jlTipPositions[fingerId].setText(String.format("%.0f, %.0f, %.0f", xCoord, yCoord, zCoord));
			}
		}
		
		private void setTextFingerDirection(float xDir, float yDir, float zDir, int fingerId) {
			if(this.jlFingerDirection != null && this.jlTipPositions.length > fingerId) {
				jlFingerDirection[fingerId].setText(String.format("%.2f, %.2f, %.2f", xDir, yDir, zDir));
			}
		}
		
		private void setTextFingerVelocity(float xCoord, float yCoord, float zCoord, int fingerId) {
			if(this.jlFingerVelocity != null && this.jlFingerVelocity.length > fingerId) {
				// the string format includes the negative symbol. and the numbers here range from -1000 to 1000, so
				// we need to consider numbers like -1000 which require 5 digits
				this.jlFingerVelocity[fingerId].setText(String.format("%05.0f, %05.0f, %05.0f", xCoord, yCoord, zCoord));
			}
		}
		
		private void setTextFingerLength(float length, int fingerId) {
			if(this.jlFingerLength != null && this.jlFingerLength.length > fingerId) {
				this.jlFingerLength[fingerId].setText(String.format("%.0f", length));
			}
		}
		
		public void setTextCircleMinRadius(String jlCircleMinRadius) {
			if(this.jlCircleMinRadius!=null){
				this.jlCircleMinRadius.setText(jlCircleMinRadius);
			}
		}

		public void setTextCircleMinArc(String jlCircleMinArc) {
			if(this.jlCircleMinArc != null) {
				this.jlCircleMinArc.setText(jlCircleMinArc);
			}
		}

		public void setTextSwipeMinLength(String jlSwipeMinLength) {
			if(this.jlSwipeMinLength != null){
				this.jlSwipeMinLength.setText(jlSwipeMinLength);
			}
		}

		public void setTextSwipeMinVelocity(String jlSwipeMinVelocity) {
			if(this.jlSwipeMinVelocity!=null){
				this.jlSwipeMinVelocity.setText(jlSwipeMinVelocity);
			}
		}

		public void setTextKeyTapMinDownVelocity(String jlKeyTapMinDownVelocity) {
			if(this.jlKeyTapMinDownVelocity != null){
				this.jlKeyTapMinDownVelocity.setText(jlKeyTapMinDownVelocity);
			}
		}

		public void setTextKeyTapHistorySeconds(String jlKeyTapHistorySeconds) {
			if(this.jlKeyTapHistorySeconds != null){
				this.jlKeyTapHistorySeconds.setText(jlKeyTapHistorySeconds);
			}
		}

		public void setTextKeyTapMinDistance(String jlKeyTapMinDistance) {
			if(this.jlKeyTapMinDistance!=null){
				this.jlKeyTapMinDistance.setText(jlKeyTapMinDistance);
			}
		}

		public void setTextScreenTapMinForwardVelocity(String jlScreenTapMinForwardVelocity) {
			if(this.jlScreenTapMinForwardVelocity != null) {
				this.jlScreenTapMinForwardVelocity.setText(jlScreenTapMinForwardVelocity);
			}
		}

		public void setTextScreenTapHistorySeconds(String jlScreenTapHistorySeconds) {
			if(this.jlScreenTapHistorySeconds != null){
				this.jlScreenTapHistorySeconds.setText(jlScreenTapHistorySeconds);
			}
		}
		public void setTextScreenTapMinDistance(String jlScreenTapMinDistance)
		{
			if(this.jlScreenTapMinDistance != null) {
				this.jlScreenTapMinDistance.setText(jlScreenTapMinDistance);
			}
		}
		
		
		//public
		// == HELPERS == //
        /**
         * Take in the radians and return some standard return type in degrees
         * @param radians
         * @return int degrees	
         */
        private static int roundDegrees(double radians) {
        	return (int) Math.round(Math.toDegrees(radians)) / ROUNDED_TO * ROUNDED_TO;
        }


    }
