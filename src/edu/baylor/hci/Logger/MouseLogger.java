package edu.baylor.hci.Logger;

import java.util.ArrayList;
import org.apache.log4j.Logger;


/**
 * this Class simply logs the position of the pointer device actions and writes it to a file.
 * The actions are NOT hooked to the mouse or pointer. The actions need to be called manually. 
 * 
 * @author Alvin
 *
 */

public class MouseLogger extends MFileLogger 
{
	/** Class Constants **/
	/* what distance (in pixels) needed to exceed before its actually added to the list. Reduces noise. */ 
	private static final int DISTANCE_THRESHOLD = 25;

	/** Object Vars **/
	// position and time
	private ArrayList<Integer> mouseX = new ArrayList<Integer>();
	private ArrayList<Integer> mouseY = new ArrayList<Integer>();
	private ArrayList<Long> mouseTime = new ArrayList<Long>();
	// the distance here indicates the full path traversed between and X and Y and not the simple Eucledian distance
	private ArrayList<Integer> distanceMoved = new ArrayList<Integer>();
	private ArrayList<Long> distanceMovedStartTime = new ArrayList<Long>();
	private ArrayList<Long> distanceMovedEndTime = new ArrayList<Long>();
	
	/**
	 * these are containers for the vectors(points) per trial
	 */
	private ArrayList<ArrayList<Integer>> mouseXPerTrial=new ArrayList<ArrayList<Integer>>();
	private ArrayList<ArrayList<Integer>> mouseYPerTrial=new ArrayList<ArrayList<Integer>>();
	private ArrayList<ArrayList<Long>> mouseTimeStampPerTrial=new ArrayList<ArrayList<Long>>();
	
	/**
	 * the vectors that will be tallied(running total) and added to the containers above 
	 */
	private ArrayList<Integer> xVector;
	private ArrayList<Integer> yVector;
	private ArrayList<Long> timeVector;
	
	private int distance=0;
	// Logger as usual. 
	final static Logger logger = Logger.getLogger(MouseLogger.class);
	
	public MouseLogger(String logfilePrefix) 
	{
		setFilePrefix(logfilePrefix);
	}

	/**
	 * Frontend to the log writing function. 
	 * Initializes, writes, closes file
	 */
	public void writeLog() 
	{

		// write my comments at the top of the file
		writeComments();
		// write my vars
		writeIntegerVars("mouseX", mouseX);
		writeIntegerVars("mouseY", mouseY);
		writeLongVars("mouseTime", mouseTime);
		
		writeIntegerVars("distanceMoved", distanceMoved);
		writeLongVars("distanceMovedStart", distanceMovedStartTime);
		writeLongVars("distanceMovedEnd", distanceMovedEndTime);
		
		writeIntegerVectors("mouseXPerTrial", mouseXPerTrial);
		writeIntegerVectors("mouseYPerTrial", mouseYPerTrial);
		writeLongVectors("mouseTimeStampPerTrial", mouseTimeStampPerTrial);
		// clear arrays
		clearArrays();
		logger.info("Log successfully written");

	}

	/**
	 * when called, we will start summing the entire distance covered by the mouse cursor 
	 * sets the distance=0(used to sum the distance), isSummingDistance=true indicating that we are ready to start summing, and adds the current time to distanceMovedStart
	 */
	public void startCalcDistance()
	{
		// reset the distance
		distance=0;
		distanceMovedStartTime.add(System.currentTimeMillis());
		
		xVector=new ArrayList<Integer>();
		yVector = new ArrayList<Integer>();
		timeVector = new ArrayList<Long>();
		
	
	}
	
	/**
	 * sets isCalcDistance to false meaning that we do not wish to sum for a moment, adds the distance that was summed to distanceMoved and adds the timeStamp to distanceMovedEnd
	 */
	public void endCalcDistance()
	{
		distanceMovedEndTime.add(System.currentTimeMillis());
		distanceMoved.add(distance);
		
		mouseXPerTrial.add(xVector);
		mouseYPerTrial.add(yVector);
		mouseTimeStampPerTrial.add(timeVector);
		
	}
	
	/**
	 * Clearing the arrays means that this object can be "reused"
	 * which is not impossible, but a bad idea because the filename does not change.
	 * To force this to fail hard, we just set all these vars to null so that they cannot be reused. 
	 */
	public void clearArrays()
	{
		mouseX.clear();
		mouseY.clear();
		mouseTime.clear();
		
		distanceMoved.clear();
		distanceMovedEndTime.clear();
		distanceMovedStartTime.clear();
		
	}

	/**
	 * Adds the X and Y locations in Pixels. 
	 * But these locations are only recorded if there's a Euclidean distance of DISTANCE_THRESHOLD (probably 25) pixels or more between them
	 * @param x
	 * @param y
	 */
	public void addMouseXY(int x, int  y)
	{
	
		if(getLogStatus() ==LogStatus.START)
		{ 	
			/* if this is the first entry, then just add and move on.
			 * We don't want to perform the distance check, because there's nothing to compare it to  
			 */
			if(mouseX.size() == 0) 
			{
				mouseX.add(x);
				mouseY.add(y);
				mouseTime.add(System.currentTimeMillis());	

				// per trial
				xVector.add(x);
				yVector.add(y);
				timeVector.add(System.currentTimeMillis());

			} 
			else 
			{
				// so its not the first entry, we'll just store the previous numbers for later.. 
				int prevX = mouseX.get(mouseX.size()-1);
				int prevY = mouseY.get(mouseY.size()-1);
				int distanceTravelled = getDistance(x, y, prevX, prevY); 
				
				
				/* this is critical : 
				 * We ONLY perform the logging if the distance is more than the threshold.  
				 */
				if(distanceTravelled >= DISTANCE_THRESHOLD) 
				{
					// do the usual logging: X, Y, Time. 
					mouseX.add(x);
					mouseY.add(y);
					mouseTime.add(System.currentTimeMillis());
					// if I'm supposed to, then I'll log the distance
					//if(this.isCalcDistance) 
					this.distance+=distanceTravelled;

					// per trial
					xVector.add(x);
					yVector.add(y);
					timeVector.add(System.currentTimeMillis());

				}
			}
		}
	}
}
