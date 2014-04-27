package edu.baylor.hci.Experiment;

import java.awt.GraphicsEnvironment;
import java.awt.KeyEventDispatcher;
import java.awt.KeyboardFocusManager;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyEvent;
import java.awt.event.MouseEvent;
import java.awt.event.MouseMotionListener;
import java.io.InputStream;

import javax.sound.sampled.AudioInputStream;
import javax.sound.sampled.AudioSystem;
import javax.sound.sampled.Clip;
import javax.swing.JFrame;
import javax.swing.Timer;
import javax.swing.WindowConstants;

import org.apache.log4j.Logger;

import edu.baylor.hci.Experiment.ExpConfig.ExperimentNum;
import edu.baylor.hci.Logger.ExperimentLogger;
import edu.baylor.hci.Logger.MouseLogger;
import edu.baylor.hci.Logger.MFileLogger.LogStatus;




public class SequentialHover extends JFrame implements ActionListener, KeyEventDispatcher, MouseMotionListener {
	
	/** Class Constants **/
	// timers initial delay
	private static final int INITIAL_DELAY = 0;
	// time per tick. Set to 1 second because we need _Real_ Time. 
	private static final int TIMER_INTERVAL = 1000;
	// how long to countdown to start of game. 
	public static final int COUNTDOWN_TIMER = 2;
	// How many rows and columns - Public because the view will access this
	private static final int ROWS = 9;
	private static final int COLS = 11;
	// the position of the Status message. Remember starts from 0
	private static final int STATUSCELL_ROW = 4;
	private static final int STATUSCELL_COL = 5;
	
	private static final String BEEP_SOUND = "beep.wav";
	

	// required sUID
	private static final long serialVersionUID = 2858671353438527428L;
	
	/** class global vars **/
	private SequentialHoverView view;
	private Timer timer;
	private JFrame fullscreenFrame;
	// the following will be used for every experiment. 
	private ExperimentLogger expLogger;
	private MouseLogger mouseLog;
	// current score. Init to 0
	private int score = 0;
	// missclicks. Clicks outside the target
	private int missclicks = 0;
	// wrongaction. eg: left click on a rightclick button
	private int wrongAction = 0;
	// init the ticks to the negative version of countdown
	public int ticks = -1 * COUNTDOWN_TIMER;
	// view set if user is ready
	public boolean userReady = false;
	
	private boolean hasLogged=false;

	// oblig logger
    private static final Logger logger = Logger.getLogger(SequentialHover.class);
    
	public SequentialHover(ExperimentNum expNum, String logFileNameAndExpNum)
	{
 		fullscreenFrame= new JFrame();
 		fullscreenFrame.setDefaultCloseOperation(WindowConstants.DISPOSE_ON_CLOSE);
 		fullscreenFrame.setUndecorated(true);
 		fullscreenFrame.setResizable(false);
 		fullscreenFrame.validate();
 		GraphicsEnvironment.getLocalGraphicsEnvironment().getDefaultScreenDevice().setFullScreenWindow(fullscreenFrame);
 		
 		addKeyListener();
 		mouseLog = new MouseLogger("mLog_"+logFileNameAndExpNum);//+TouchPoints.getTypetoAppend());
	    expLogger = new ExperimentLogger("expLog_"+logFileNameAndExpNum); //+TouchPoints.getTypetoAppend());
	    expLogger.setLogStatus(LogStatus.START); //starting Logger
	    try
	    {
	    	view= new SequentialHoverView(this, expNum); //right now this integer corresponds to selecting the experimental ordering, though this will be changed to be automatically set based upon the button pressed.
	    }
	    catch(Exception e)
	    {
	    	e.printStackTrace();
	    }
 		fullscreenFrame.add(view);
	    
	    timer= new Timer(TIMER_INTERVAL, this);
	    timer.setInitialDelay(INITIAL_DELAY);
	    timer.start();
	    
	}
	/**
	 * attaches a listener to the keyboard for every key press, release and typed. Any of these will trigger the dispatchKeyEvent(e)
	 */
	private void addKeyListener() {
		KeyboardFocusManager manager = KeyboardFocusManager.getCurrentKeyboardFocusManager();
 		manager.addKeyEventDispatcher(this);
	}
	/**
	 * results from hitting the escape key, exits the panel and if premature stops the view and writes the logs.
	 */
	public void exit()
	{
		view.stopViewAndWriteLogs();
		writeLogs();
		KeyboardFocusManager manager = KeyboardFocusManager.getCurrentKeyboardFocusManager();
		manager.removeKeyEventDispatcher(this);
		this.dispose();
		fullscreenFrame.dispose();
	}
	/**
	 * is called by the view when the experimentOrder is empty meaning that we have exhausted the order. Updates statusCell, stops timer, stops view and writes logs and sets hasLogged to true so that we do not log twice when 
	 * escape is pressed(exit panel)
	 * 
	 */
	public void stopGame()
	{		
		view.setStatusCell(ticks, score, missclicks, wrongAction);
		timer.stop();
		view.stopViewAndWriteLogs();
		playEndgameSound();
		writeLogs();
	}

	private void playEndgameSound()
	{
		
		ClassLoader classLoader = Thread.currentThread().getContextClassLoader();
		InputStream beepString = classLoader.getResourceAsStream(BEEP_SOUND);
		try {
	        AudioInputStream audioInputStream = AudioSystem.getAudioInputStream(beepString);
	        Clip clip = AudioSystem.getClip();
	        clip.open(audioInputStream);
	        clip.start();
	    } catch(Exception ex) {
	        System.out.println("Error with playing sound.");
	        ex.printStackTrace();
	    }

	}
	private void startGame() 
	{
		expLogger.setStartTime(System.currentTimeMillis());
		mouseLog.setLogStatus(LogStatus.START);
		view.toggleExperimentOrderCell();		
	}
	
	public ExperimentLogger getExperimentLogger()
	{
		return expLogger;
	}
	
	public MouseLogger getMouseLogger()
	{
		return mouseLog;
	}
	
	/**
	 * Will write logs iff 
	 * 1. hasn't already written logs
	 * 2. score is more than 0
	 */
	public void writeLogs()
	{
		if(!hasLogged && score > 0)
		{
			expLogger.setEndTime(System.currentTimeMillis());
			expLogger.writeLog();
			expLogger.setLogStatus(LogStatus.STOP);

			mouseLog.writeLog();
			mouseLog.setLogStatus(LogStatus.STOP);

			hasLogged=true;	
		}
		
	}
	
	/**
	 * Correct Selection action
	 */
	public void incrementScore()
	{
		score++;
	}
	
	/**
	 * Incorrect action, eg: Left click on a right click button
	 */
	public void incrementWrongAction() 
	{
		this.wrongAction++;
	}
	
	/**
	 * count the missclicks, eg: left click on anything besides the target 
	 */
	public void incrementMissclick() 
	{
		this.missclicks++;
	}
	
	/**
	 * Used to perform actions on each clock tick
	 */
	@Override
	public void actionPerformed(ActionEvent e) {
		
		// show the score and the timer
//		view.setStatusCell(score, ticks);
		
		if(ticks==0) // Exactly 0: Start game 
		{
			logger.info("Starting Experiment");
			startGame();
		} else if(ticks < 0) // Countdown Mode
		{
			// Only count down if user is ready
			if(userReady) 
			{
				// nothing really
			} else 
			{
				// user's not ready, don't tick
				ticks--;
			}
			view.setStatusCell(ticks);
		} else // Game Mode
		{
			// in the regular game, just show the tick and the 
			view.setStatusCell(ticks, score, missclicks, wrongAction);
		}
		
		// increment the tickcount at every clock tick
		ticks++;
	}
	
	

	@Override
	public boolean dispatchKeyEvent(KeyEvent e) {
		if(e.getKeyCode()==KeyEvent.VK_ESCAPE)
		{
			 if (e.getID() == KeyEvent.KEY_RELEASED){
				 exit();
			 }
		}
		return false;
	}
	@Override
	public void mouseDragged(MouseEvent e) {
	}
	@Override
	public void mouseMoved(MouseEvent e) {
	}

	/** getters that the views will need **/
	public int getRowCount() {
		return ROWS;
	}
	
	public int getColCount() {
		return COLS;
	}

	public int getStatuscellRow() {
		return STATUSCELL_ROW;
	}
	
	public int getStatuscellCol() {
		return STATUSCELL_COL;
	}
}
