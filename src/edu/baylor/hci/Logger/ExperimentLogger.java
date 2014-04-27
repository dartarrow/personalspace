package edu.baylor.hci.Logger;

import java.awt.Point;
import java.util.ArrayList;

import org.apache.log4j.Logger;


/**
 * Collects points related metrics during the experiment, outs to a matlab file format
 * Post-analysis data can be used to calculate experimental metrics
 * eg: those based on Fitts & Mackenzie, Distance Travelled, DifficultyIndex, Throughput, Accuracy, etc
 * 
 * @author Alvin
 */
public class ExperimentLogger extends MFileLogger{
	private Long startTime= null;
	private Long endTime=null;
	/** Vars used in drag and drop experiment **/
	private ArrayList<Integer> dragTruePosX = new ArrayList<Integer>();
	private ArrayList<Integer> dragTruePosY = new ArrayList<Integer>();
	private ArrayList<Integer> dropTruePosX = new ArrayList<Integer>();
	private ArrayList<Integer> dropTruePosY = new ArrayList<Integer>();
	private ArrayList<Integer> dndTruePosTime = new ArrayList<Integer>(); // duration, not timestamp
	private ArrayList<Integer> dndTruePosDistance = new ArrayList<Integer>();
	private ArrayList<Integer> dragHoverTime = new ArrayList<Integer>();
	
	private ArrayList<Integer> dragFalsePosX = new ArrayList<Integer>();
	private ArrayList<Integer> dragFalsePosY = new ArrayList<Integer>();
	private ArrayList<Integer> dropFalsePosX = new ArrayList<Integer>();
	private ArrayList<Integer> dropFalsePosY = new ArrayList<Integer>();
	private ArrayList<Integer> dndFalsePosTime = new ArrayList<Integer>(); // duration, not timestamp
	private ArrayList<Integer> dndFalsePosDistance = new ArrayList<Integer>();
	
	/** vars used in the Click experiment **/
	private ArrayList<Integer> widgetEnterPosX = new ArrayList<Integer>();
	private ArrayList<Integer> widgetEnterPosY = new ArrayList<Integer>();
	private ArrayList<Long> widgetEnterTime = new ArrayList<Long>();
	
	private ArrayList<Integer> widgetAppearPosX = new ArrayList<Integer>();
	private ArrayList<Integer> widgetAppearPosY = new ArrayList<Integer>();
	private ArrayList<Long> widgetAppearTime = new ArrayList<Long>();
	
	private ArrayList<Integer> widgetDisappearPosX = new ArrayList<Integer>();
	private ArrayList<Integer> widgetDisappearPosY = new ArrayList<Integer>();
	private ArrayList<Long> widgetDisappearTime = new ArrayList<Long>();
	
	private ArrayList<Integer> missclickPosX= new ArrayList<Integer>();
	private ArrayList<Integer> missclickPosY= new ArrayList<Integer>();
	private ArrayList<Long> missclickPosTime= new ArrayList<Long>();

	private ArrayList<Integer> wrongactionPosX= new ArrayList<Integer>();
	private ArrayList<Integer> wrongactionPosY= new ArrayList<Integer>();
	private ArrayList<Long> wrongactionPosTime= new ArrayList<Long>();

	private ArrayList<Long> travelDuration = new ArrayList<Long>();
	private ArrayList<Integer> shortestPath= new ArrayList<Integer>();
	private ArrayList<Long> selectionDuration= new ArrayList<Long>();
	private ArrayList<Long> timeBetweenTrials= new ArrayList<Long>();
	
	/** This starts the version 2 of our logging for MultiHanded and MultiModal Exps **/
	/**
	 * enums for size
	 */
	private ArrayList<String> variableNames = new ArrayList<String>();
	private ArrayList<Integer> variableValues = new ArrayList<Integer>();
	
	private ArrayList<String> sizePerTrial = new ArrayList<String>(); /** size of target per trial */
	private ArrayList<String> typePerTrial = new ArrayList<String>(); /** type of target per trial i.e LEFT_CLICK, Right_CLICK, HOVER */
	
	private ArrayList<Long> moveDurationPerTrial = new ArrayList<Long>(); /** both measured in miliseconds **/
	//private ArrayList<Integer> selectionDurationPerTrial = new ArrayList<Integer>(); this is done with HoverTime

	/** This ends V2 logging **/
	
	final static Logger logger = Logger.getLogger(ExperimentLogger.class); //instantiate Log4j logger

	/**
	 * Standard constructor, just set the prefix for the file
	 * @param logfilePrefix
	 */
	public ExperimentLogger(String logfilePrefix) {	
			setFilePrefix(logfilePrefix);
	}
	
	/**
	 * Frontend to the log writing function. 
	 * Initializes, writes, closes file
	 */
	public void writeLog() {
		setLogStatus(LogStatus.IDLE);
		
		if(startTime!=null && endTime!=null) //only will add these comments if startTime and endTime have been set or only in sequentialClicks as we have it outlined now. 
		{
			addComment("GameTime : "+(endTime-startTime));
			addComment("#missclicks : " + missclickPosTime.size());
			addComment("#wrongActions : " + wrongactionPosTime.size());
			addComment("#truePos : "+widgetEnterTime.size());
			writeComments();
		}
		
		for(int i=0; i<variableNames.size(); i++)
		{
			writeScalar(variableNames.get(i), variableValues.get(i));
		}
		writeVariableVars("SizePerTrial", sizePerTrial);
		writeVariableVars("typePerTrial", typePerTrial);
		
		writeLongVars("moveDurationPerTrial", moveDurationPerTrial);
		//writeIntegerVars("selectionDurationPerTrial", selectionDurationPerTrial); done using hoverTime
		
		writeIntegerVars("widgetEnterPosX", widgetEnterPosX);
		writeIntegerVars("widgetEnterPosY", widgetEnterPosY);
		writeLongVars("widgetEnterPosTime", widgetEnterTime);
		
		writeIntegerVars("widgetAppearPosX", widgetAppearPosX);
		writeIntegerVars("widgetAppearPosY", widgetAppearPosY);
		writeLongVars("widgetAppearPosTime", widgetAppearTime);

		writeIntegerVars("widgetDisappearPosX", widgetDisappearPosX);
		writeIntegerVars("widgetDisappearPosY", widgetDisappearPosY);
		writeLongVars("widgetDisappearPosTime", widgetDisappearTime);

		writeIntegerVars("missclickPosX", missclickPosX);
		writeIntegerVars("missclickPosY", missclickPosY);
		writeLongVars("missclickPosTime", missclickPosTime);
		
		writeIntegerVars("wrongactionPosX", wrongactionPosX);
		writeIntegerVars("wrongactionPosY", wrongactionPosY);
		writeLongVars("wrongactionPosTime", wrongactionPosTime);
		
		writeLongVars("travelDuration", travelDuration);
		writeLongVars("selectionDuration", selectionDuration);
		writeLongVars("timeBetweenTrials", timeBetweenTrials);
		writeIntegerVars("shortestPath", shortestPath);
				
		// == write arrays for DnD ==
		/* Disabled for now, just extra unnecessary noise 
		 * 
		writeIntegerVars("dragTruePosX", dragTruePosX);
		writeIntegerVars("dragTruePosY", dragTruePosY);
		writeIntegerVars("dropTruePosX", dropTruePosX);
		writeIntegerVars("dropTruePosY", dropTruePosY);
		writeIntegerVars("dndTruePosTime", dndTruePosTime);
		writeIntegerVars("dndTruePosDistance", dndTruePosDistance);
		
		writeIntegerVars("dragFalsePosX", dragFalsePosX);
		writeIntegerVars("dragFalsePosY", dragFalsePosY);
		writeIntegerVars("dropFalsePosX", dropFalsePosX);
		writeIntegerVars("dropFalsePosY", dropFalsePosY);
		writeIntegerVars("dndFalsePosTime", dndFalsePosTime);
		writeIntegerVars("dndFalsePosDistance", dndFalsePosDistance);
		
		writeIntegerVars("dragHoverTime", dragHoverTime);
		*/
		
		// clear arrays
		clearArrays();
	}
	
	/**
	 * sets experiment StartTime
	 * @param start
	 */
	public void setStartTime(Long start)
	{
		startTime=start;
	}
	
	/**
	 * sets the experiment endTime
	 * @param end
	 */
	public void setEndTime(Long end)
	{
		endTime=end;
	}
	
	
	/**
	 * Clears all arrays used in the click experiment, called once we have flushed the log.
	 */
	public void clearArrays()
	{
		widgetEnterPosX.clear();
		widgetEnterPosY.clear();
		widgetEnterTime.clear();
		
		widgetAppearPosX.clear();
		widgetAppearPosY.clear();
		widgetAppearTime.clear();
		
		widgetDisappearPosX.clear();
		widgetDisappearPosY.clear();
		widgetDisappearTime.clear();
		
		missclickPosX.clear();
		missclickPosY.clear();
		missclickPosTime.clear();
		
		wrongactionPosX.clear();
		wrongactionPosY.clear();
		wrongactionPosTime.clear();

		selectionDuration.clear();
		
		timeBetweenTrials.clear();
		variableNames.clear();
		variableValues.clear();
		sizePerTrial.clear();
		
		travelDuration.clear();
		shortestPath.clear();
		
	}

	
	/**
	 * Adds the time and place when the widgets appear. 
	 * Used to calculate navigation time (widget enter - widget appear)
	 * @param x
	 * @param y
	 */
	public void addWidgetAppearPosXY(int x, int y)
	{
		if(getLogStatus()==LogStatus.START)
		{
			widgetAppearPosX.add(x);
			widgetAppearPosY.add(y);
			widgetAppearTime.add(System.currentTimeMillis());
		}
	}
	
	/**
	 * adds x and y when the cursor entered the widget
	 * The time has to be manually set from the outside because this function
	 * will infact only be called when the widget disappears. 
	 * ELSE, a re-entry will cause multiple unnecessary log entries  
	 * 
	 * @param x
	 * @param y
	 */
	public void addWidgetEnterPosXY(int x, int y, long enterTime)
	{
		if(getLogStatus()==LogStatus.START)
		{
			widgetEnterPosX.add(x);
			widgetEnterPosY.add(y);
			widgetEnterTime.add(enterTime);
			
			// first get the current index (ie trial-1) 
			int currentIndex = widgetEnterTime.size() - 1;
			// calculate travel time (enterTime - appearTime)
			travelDuration.add(widgetEnterTime.get(currentIndex) - widgetAppearTime.get(currentIndex));
			// calculate shortest path, distance between coordinate when the widget entered the box and when it appeared. 
			shortestPath.add(getDistance(x, y, widgetAppearPosX.get(currentIndex), widgetAppearPosY.get(currentIndex)));
		}
	}
	
	/**
	 * Coordinates of the mouse when the widget disappeared. 
	 * Also sets the time, and the selection duration. 
	 * @param x
	 * @param y
	 */
	public void addWidgetDisappearPosXY(int x, int y)
	{
		if(getLogStatus()==LogStatus.START)
		{
			widgetDisappearPosX.add(x);
			widgetDisappearPosY.add(y);
			widgetDisappearTime.add(System.currentTimeMillis());
			
			
			// first get our current index (or trial-1)
			int currentIndex = widgetDisappearTime.size() - 1;
			// set the selection duration. Basically HoverTime
			selectionDuration.add(widgetDisappearTime.get(currentIndex) - widgetEnterTime.get(currentIndex));
			
			// if this is not the FIRST trial, then we calculate the time between each trial's completion
			// calculated as thisDisappearTime - lastDisappearTime
			if(currentIndex >= 1)
			{
				addTimeBetweenTrials(widgetDisappearTime.get(currentIndex), widgetDisappearTime.get(currentIndex - 1));
			}
		}
		
	}
	
	/**
	 * adds x and y to our missclick arrayLists, implicitly adds a time stamp to arrayList<Long> falsePosTime.
	 * @param x
	 * @param y
	 */
	public void addMissclickPosXY(int x, int  y)
	{
		if(getLogStatus()==LogStatus.START)
		{
			missclickPosX.add(x);
			missclickPosY.add(y);
			missclickPosTime.add(System.currentTimeMillis());
		}
	}
	
	/**
	 * adds x and y to our missclick arrayLists, implicitly adds a time stamp to arrayList<Long> falsePosTime.
	 * @param x
	 * @param y
	 */
	public void addWrongActionPosXY(int x, int  y)
	{
		if(getLogStatus()==LogStatus.START)
		{
			wrongactionPosX.add(x);
			wrongactionPosY.add(y);
			wrongactionPosTime.add(System.currentTimeMillis());
		}
	}
	
	/**
	 * This function is called from addWidgetDisappearXY(), 
	 * simply calculates the time from the last good trial until the current.
	 * it's the difference between the time the last trial completed and the time this one completed
	 * @param lastTrialTime
	 * @param currentTrialTime
	 */
	private void addTimeBetweenTrials(Long lastTrialTime, Long currentTrialTime )
	{
		if(getLogStatus()==LogStatus.START)
		{
			timeBetweenTrials.add(currentTrialTime-lastTrialTime);
		}
	}
			
	/**
	 * Adds the Drag coords and Drop coords in pixels, where this is a True Positive
	 * Also implicitly sets the TP time and relevant distance
	 * @param drag - XY point where the drag started. In absolute screen pixel
	 * @param drop - XY point where the drop occured. In absolute screen pixel
	 * @param startTime - time in milliseconds when the drag started.
	 * @param stopTime - time in milliseconds when the drop finished
	 */
	public void addDndTruePos(Point drag, Point drop, Long startTime, Long stopTime) {
		if(getLogStatus()==LogStatus.START)
		{
			// straightforward - set the values. 
			dragTruePosX.add(drag.x);
			dragTruePosY.add(drag.y);
			dropTruePosX.add(drop.x);
			dropTruePosY.add(drop.y);
			// this logs the time between the drag and the drop.. 
			dndTruePosTime.add((int)(stopTime - startTime));
			// use the distance formula, add the distance between the drag and the drop. 
			dndTruePosDistance.add(getDistance(drag.x, drag.y, drop.x, drop.y));
		}
	}
	
	/**
	 * Adds the Drag coords and Drop coords in pixels, where this is a False Positive
	 * Also implicitly sets the FP time
	 * @param drag - XY point where the drag started. In absolute screen pixel
	 * @param drop - XY point where the drop occured. In absolute screen pixel
	 * @param startTime - time in milliseconds when the drag started.
	 * @param stopTime - time in milliseconds when the drop finished
	 */
	public void addDndFalsePos(Point drag, Point drop, Long startTime, Long stopTime) {
		if(getLogStatus()==LogStatus.START)
		{
			/// add coord values
			dragFalsePosX.add(drag.x);
			dragFalsePosY.add(drag.y);
			dropFalsePosX.add(drop.x);
			dropFalsePosY.add(drop.y);
			// distance between startTime and end time. 
			// NOTE: theoretically could be 0 (or very close to 0) if this is a Click. but the dnd system shouldn't care about the clicks. so. 
			dndFalsePosTime.add((int)(stopTime - startTime));
			// use the distance formula, add the distance between the drag and the drop.
			/* NOTE: why do we need the distance for a false positive? No idea. 
			 *     But the difference between a long distance FP and a short distance FP may indicate the difference between system 
			 *     related issue (eg: accidentally detecting grab / grasp / pinch actions) and user related issue 
			 *     (eg: missed the drop point)
			 */
			dndFalsePosDistance.add(getDistance(drag.x, drag.y, drop.x, drop.y));
		}
	}

	/**
	 * How long did the user pause over the widget before selecting it. 
	 * This should include re-entries, so don't restart the clock when reentries occur. 
	 * @param hovertime
	 */
	public void addDragHoverTime(int hovertime) {
		if(getLogStatus()==LogStatus.START)
		{
			dragHoverTime.add(hovertime);
		}
	}
	
	public void addVariableNames(String var){
		if(getLogStatus()==LogStatus.START) 
		{
			variableNames.add(var);
		}
	}
	public void addVariableValues(Integer val){
		if(getLogStatus()==LogStatus.START) 
		{
			variableValues.add(val);
		}
	}
	public void addSizePerTrial(String var){
		if(getLogStatus()==LogStatus.START) 
		{
			sizePerTrial.add(var);
		}
	}
	public void addTypePerTrial(String var){
		if(getLogStatus()==LogStatus.START) 
		{
			typePerTrial.add(var);
		}
	}
	public void addMoveDurationPerTrial(long duration)
	{
		if(getLogStatus()==LogStatus.START) 
		{
			moveDurationPerTrial.add(duration);
		}
	}
	
	


}
