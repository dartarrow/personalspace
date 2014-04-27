package edu.baylor.hci.LeapOMatic;

import java.awt.Dimension;
import java.awt.Toolkit;
import java.io.IOException;

import javax.swing.JFrame;

import org.apache.log4j.Logger;

import com.leapmotion.leap.*;

import edu.baylor.hci.Calibration.MouseListener;
import edu.baylor.hci.Calibration.TransformationMatrix;
import edu.baylor.hci.Calibration.TransformationMatrix.MATRIX_TYPE;
import edu.baylor.hci.Graphics.GestureGraph;
import edu.baylor.hci.Logger.PositionLog;

/**
 * This is _THE_ bootstrap class. 
 * The main() starts here
 * All major components of the system is initialized here, and parsed around the system with getter functions.
 * Should keep it that way. 
 * @author Alvin
 *
 */
public class TouchPoints extends JFrame {
	
	/** Class Constants **/
	private static final int WINDOW_HEIGHT = 1200;
	private static final int WINDOW_WIDTH = 1200;
	private static final long serialVersionUID = 1211493067872314884L;
	
	/** Object Vars **/
	/* mandatory  Leap controller */
	private static Controller controller;
	/* our guiListener, connected to the controller */
	private static GuiListener guiListener;
	/* our mouse Listener connected to the controller */
	private static MouseListener mouseListener;
	/* logger, for logging in matlab format AND graphics population */ 
	private static PositionLog positionLog;
	/* mouse position logger, logs in matlab readable format as well */
	/*** REMOVED this won't work on a global level. there's no reason for it to be here.
	 *** If you need to log the mouse movements, then create one MouseLogger object and just use that wherever it's needed 
	 ***/
	/*** private static MouseLogger mouseLog; ***/
	private static GestureGraph gestureGraph;
	// initialize the transformation matrix algo we want to use
	//-- Coordinate Transformer is the regular S-T-D transformation matrix for mouse position 
	private static TransformationMatrix coordinateTransformer;
	//-- Here we get the expected NORMAL (pitch/yaw/roll) of the palm. Used in palm-flip gesture.   
	private static TransformationMatrix palmNormalTransformer;
	private static boolean leftClickEnabled;
	private static boolean rightClickEnabled;
	private static boolean stopGestureEnabled;
	private static String participantName;

    

	final static Logger logger = Logger.getLogger(TouchPoints.class);
    
	TouchPoints() {
		
		// First things first: enter the matrix //
		initTransformMatrices();		
		
		// == RANDOM NOISE == //
		logger.fatal("Starting up");
		logger.error("Starting up");
		logger.warn("Starting up");
		logger.info("Starting up");
		logger.debug("Starting up");
		logger.trace("Starting up");
		
		
		// self's jFrame setup
		this.setTitle("Personal Space");
        this.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        this.setSize(WINDOW_WIDTH, WINDOW_HEIGHT);
        this.setVisible(true);
        // Start the screen at the Horizontal Center, Vertical top
        Dimension dim = Toolkit.getDefaultToolkit().getScreenSize();
        this.setLocation(dim.width/2-this.getSize().width/2, 10);

        // Create a sample listener and controller, add the logger to it as well. 
      
        positionLog = new PositionLog("positionLog");
        /*** mouseLog= new MouseLogger("mouseLog"); ***/
        // create my UI
        TouchPointsView view = new TouchPointsView();
        guiListener = new GuiListener(positionLog);
        // setup the callback; hook the GUI elements to the listener
        guiListener.hookLabelFingers(view.jlFingers);
        guiListener.hookLabelGestures(view.jlGestures);
        guiListener.hookLabelLogInfo(view.jlLogInfo);
        guiListener.hookLabelPalm(view.jlPalm);
        guiListener.hookLabelPalmNormal(view.jlPalmNormal);
        guiListener.hookLabelPalmVelocity(view.jlPalmVelocity);
        guiListener.hookLabelAvgFingerPos(view.jlAvgFinger);
        guiListener.hookLabelPitch(view.jlPitch);
        guiListener.hookLabelRoll(view.jlRoll);
        guiListener.hookLabelYaw(view.jlYaw);
        guiListener.hookLabelTipPositions(view.jlTipPos);
        guiListener.hookLabelFingerWidth(view.jlFingerWidth);
        guiListener.hookLabelFingerDirection(view.jlFingerDirection);
        guiListener.hookLabelFingerLength(view.jlFingerLength);
        guiListener.hookLabelFingerVelocity(view.jlFingerVelocity);
        guiListener.hookLabelPalmNormAngleToExpected(view.jlPalmNormAngleToExpected);
        guiListener.hookLabelCircleMinRadius(view.jlCircleMinRadius);
        guiListener.hookLabelCircleMinArc(view.jlCircleMinArc);
        guiListener.hookLabelSwipeMinLength(view.jlSwipeMinLength);
        guiListener.hookLabelSwipeMinVelocity(view.jlSwipeMinVelocity);
        guiListener.hookLabelKeyTapMinDownVelocity(view.jlKeyTapMinDownVelocity);
        guiListener.hookLabelKeyTapHistorySeconds(view.jlKeyTapHistorySeconds);
        guiListener.hookLabelKeyTapMinDistance(view.jlKeyTapMinDistance);
        guiListener.hookLabelScreenTapMinForwardVelocity(view.jlScreenTapMinForwardVelocity);
        guiListener.hookLabelScreenTapHistorySeconds(view.jlScreenTapHistorySeconds);
        guiListener.hookLabelScreenTapMinDistance(view.jlScreenTapMinDistance);
        
        // Have the sample listener receive events from the controller
        controller = new Controller();
        controller.addListener(guiListener);
        controller.setPolicyFlags(Controller.PolicyFlag.POLICY_BACKGROUND_FRAMES); //(Leap::Controller::POLICY_BACKGROUND_FRAMES);
        controller.config().setFloat("Gesture.KeyTap.MinDistance", 3.0f); //sets the KeyTap.minDistance to 3.0
        mouseListener = new MouseListener();
		controller.addListener(mouseListener);
		
        add(view);
        //view.

	}
	
	/**
	 * constructs the description to append to the fileName
	 * participantName+_CursorType => Darren_LOM, Darren_leapVanilla, Darren_mouse could all be constructed given the name Darren
	 * @return
	 */
	public static String getTypetoAppend()
	{
		String type="";
		switch(mouseListener.status)
		{
		case NONE:
			type=""; //this will be added when user selects either mouse or touchpad option in the tpView
		break;
		case NORMAL_CURSOR:
			type="_leapVanilla";
		break;
		case MATRIX_CURSOR:
			type="_LOM";
			break;
		}
		
	    return type;
	}
	public static String getParticipantName() {
		return participantName;
	}
	public static void setParticipantName(String participantName) {
		TouchPoints.participantName = participantName;
	}
	public static boolean isLeftClickEnabled() {
		return leftClickEnabled;
	}

	public static void setLeftClickEnabled(boolean leftClickEnabled) {
		TouchPoints.leftClickEnabled = leftClickEnabled;
	}

	public static boolean isRightClickEnabled() {
		return rightClickEnabled;
	}

	public static void setRightClickEnabled(boolean rightClickEnabled) {
		TouchPoints.rightClickEnabled = rightClickEnabled;
	}

	public static boolean isStopGestureEnabled() {
		return stopGestureEnabled;
	}

	public static void setStopGestureEnabled(boolean stopGestureEnabled) {
		TouchPoints.stopGestureEnabled = stopGestureEnabled;
	}
	public static MouseListener getMouseListener() {
		return mouseListener;
	}
	
	public static GuiListener getGuiListener() {
		return guiListener;
	}
	
	public static TransformationMatrix getCoordinateTransformer() {
		return coordinateTransformer;
	}
	
	public static void clearTransformMatrices()
	{
		coordinateTransformer = null;
		palmNormalTransformer = null;
		initTransformMatrices();
	}
	
	private static void initTransformMatrices() {
		coordinateTransformer = new TransformationMatrix(MATRIX_TYPE.NORMALIZED_Z);
		palmNormalTransformer = new TransformationMatrix(MATRIX_TYPE.REGULAR);
	}
	
	public static TransformationMatrix getPalmNormalTransformer() {
		return palmNormalTransformer;
	}
	
	public static PositionLog getPositionLog() {
		return positionLog;
	}
	
	/***
	public static MouseLogger getMouseLogger() {
		return mouseLog;
	}
	***/
	
	public static GestureGraph getGraph()
	{
		return gestureGraph;
	}
	
	public static void startGraph()
	{
		gestureGraph.start();
	}
	 public static void setConfigValues(float circleMinRadius, 
			 							float circleMinArc, 
			 							float swipeMinLength, 
			 							float swipeMinVelocity, 
			 							float keyTapMinDownVelocity, 
			 							float keyTapHistorySeconds, 
			 							float keyTapMinDistance, 
			 							float screenTapMinForwardVelocity, 
			 							float screenTapHistorySeconds, 
			 							float screenTapMinDistance)
	 {
	        	controller.config().setFloat("Gesture.Circle.MinRadius", circleMinRadius);
	        	controller.config().setFloat("Gesture.Circle.MinArc", circleMinArc);
	        	controller.config().setFloat("Gesture.Swipe.MinLength", swipeMinLength);
	        	controller.config().setFloat("Gesture.Swipe.MinVelocity", swipeMinVelocity);
	        	controller.config().setFloat("Gesture.KeyTap.MinDownVelocity", keyTapMinDownVelocity);
	        	controller.config().setFloat("Gesture.KeyTap.HistorySeconds", keyTapHistorySeconds);
	        	controller.config().setFloat("Gesture.KeyTap.MinDistance", keyTapMinDistance);
	        	controller.config().setFloat("Gesture.ScreenTap.MinForwardVelocity", screenTapMinForwardVelocity);
	        	controller.config().setFloat("Gesture.ScreenTap.HistorySeconds", screenTapHistorySeconds);
	        	controller.config().setFloat("Gesture.ScreenTap.MinDistance", screenTapMinDistance);
	        	controller.config().save();
	 }

	 public static void main(String[] args) {
		 // init myself
		 new TouchPoints();
		 // create a new graph object because we have to. But don't start it. 
		 gestureGraph = new GestureGraph();
		
		 // Keep this process running until Enter is pressed
		 System.out.println("Press Enter to quit...");
		 try {
			 System.in.read();
		 } catch (IOException e) {
			 e.printStackTrace();
		 }
		 controller.removeListener(guiListener);
    }
}
