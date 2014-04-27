package edu.baylor.hci.Experiment;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.ArrayList;
import java.util.HashMap;

import javax.swing.SwingConstants;
import javax.swing.Timer;
import javax.swing.JLabel;

import edu.baylor.hci.Experiment.PictureHolder.ImageType;
import edu.baylor.hci.LeapOMatic.Settings;

public class DndGameplay implements ActionListener {

	/** Class Constants **/
	// tick every 1000 ms, aka 1 second
	private static final int TIMER_INTERVAL = 1000;
	// delay before timer begins
	private static final int INITIAL_DELAY = 0;
	
	// holds all pairs in the game
	private ArrayList<DndPair> dndPairs = new ArrayList<DndPair>();
	// holds the different picture holders based on clusters. rather dynamic, although we only have 2 clusters 
	private HashMap<Integer, PictureHolder[]> pictureHolders = new HashMap<Integer, PictureHolder[]>();
	// holds the index of the which item on the pair list we should goto next
	private int pairIndex = Integer.MIN_VALUE;
	// True Positive - actual DnD which are correctly dragged and dropped
	private int truePositive = 0;
	// False Positive - Mistakes made
	private int falsePositive = 0;
	// and the timer value, starts from 3 and we count down to 0
	private int time = 5;
	// add the Timer
	Timer timer;
	// Jcomponent to show the countdown
	JLabel lblTimer;
	// and another to show the score
	JLabel lblScore;
	// just a flag to check if the game has started 
	boolean startgame = false;
	// just a flag to mark that the game has ended
	boolean endgame = false;

	/**
	 * Constructor. Currently only sets up the data points for matching. 
	 */
	public DndGameplay() {
		this.initDndPairs();
	}

	/**
	 * starts the game, all game elements will be handled by this class
	 */
	public void start(){
		
		// init the timer
	    this.timer= new Timer(TIMER_INTERVAL, this);
	    this.timer.setInitialDelay(INITIAL_DELAY);
	    this.timer.start();

		
		// Start from -1 because the NEXT will iterate from 0
		// = mandatory =
		this.pairIndex = -1;
		this.updateScore();
		
		//Don't do this here, because it will be done in the timer. 
		//this.next();
	}
	
	/**
	 * triggered when the game is done, 
	 * just clean everything up, but don't exit. 
	 */
	public void finish() {
		this.endgame = true;
		this.updateScore();
		this.timer.stop();
	}
	
	/**
	 * Show the next pair on this list
	 * caveat: there has to be something else on this list
	 * @return true if has next, false if none. 
	 * 
	 */
	public boolean next() {
		// if we will exceed bounds then just return false
		if(this.pairIndex + 1 > this.dndPairs.size() - 1) {
			DndPair oldPair = this.dndPairs.get(this.pairIndex);
			PictureHolder[] oldDragPicHolder = this.pictureHolders.get(oldPair.getDragClusterId());
			PictureHolder[] oldDropPicHolder = this.pictureHolders.get(oldPair.getDropClusterId());
			PictureHolder oldDragPic = oldDragPicHolder[oldPair.getDragPicId()];
			PictureHolder oldDropPic = oldDropPicHolder[oldPair.getDropPicId()];
			oldDragPic.setImageType(ImageType.BLANK);
			oldDropPic.setImageType(ImageType.BLANK);
			return false;
		}
		
		/* nope, we still have stuff.
		 * So, first hide the current pair. 
		 * then show the new pair 
		 */
		
		// set the old pair to blank ONLY if this is not the first run
		if(this.pairIndex >= 0) {
			DndPair oldPair = this.dndPairs.get(this.pairIndex);
			PictureHolder[] oldDragPicHolder = this.pictureHolders.get(oldPair.getDragClusterId());
			PictureHolder[] oldDropPicHolder = this.pictureHolders.get(oldPair.getDropClusterId());
			PictureHolder oldDragPic = oldDragPicHolder[oldPair.getDragPicId()];
			PictureHolder oldDropPic = oldDropPicHolder[oldPair.getDropPicId()];
			oldDragPic.setImageType(ImageType.BLANK);
			oldDropPic.setImageType(ImageType.BLANK);
		}
		
		// Now we go to the new ones, 
		this.pairIndex++;
		// set the new pair to.. whatever
		DndPair pair = this.dndPairs.get(this.pairIndex);
		PictureHolder[] dragPicHolder = this.pictureHolders.get(pair.getDragClusterId());
		PictureHolder[] dropPicHolder = this.pictureHolders.get(pair.getDropClusterId());
		PictureHolder dragPic = dragPicHolder[pair.getDragPicId()];
		PictureHolder dropPic = dropPicHolder[pair.getDropPicId()];
		dragPic.setImageType(ImageType.SOURCE);
		dropPic.setImageType(ImageType.DESTINATION);
		
		// and we say that we're done
		return true;
	}
	
	/**
	 * store our array of picture holders based on the cluster Id
	 * @param clusterId
	 * @param picHolder
	 */
	public void addPictureHolders(int clusterId, PictureHolder[] picHolder) {
		this.pictureHolders.put(clusterId, picHolder);
	}
	
	/**
	 * Checks if this element is dragged from and dropped to the right place
	 * If it's correct, then increase the true positive
	 * If it's incorrect, then increase the false positive
	 * @param index
	 * @param dragClusterId
	 * @param dragPicId
	 * @param dropClusterid
	 * @param dropPicId
	 * @return
	 */
	public boolean checkDnd(int dragClusterId, int dragPicId, int dropClusterid, int dropPicId) {
		
		DndPair dndPair = dndPairs.get(pairIndex);

		// correctness checking, if correct then update the TP count
		if (dndPair.getDragClusterId() == dragClusterId && 
		    dndPair.getDragPicId() 	   == dragPicId     &&
		    dndPair.getDropClusterId() == dropClusterid &&
		    dndPair.getDropPicId()	   == dropPicId) {
			this.truePositive++;
			this.updateScore();
			return true;
		} else { // if wrong then update FP count
			this.falsePositive++;
			this.updateScore();
			return false;	
		}
	}
	
	/**
	 * Hook some JLabel which will be used to display and update the timer 
	 * @param timer
	 */
	public void hookTimer(JLabel timer) {
		this.lblTimer = timer;
		this.lblTimer.setHorizontalAlignment(SwingConstants.CENTER);
	}
	
	/**
	 * Hook some JLabel which will be used to display and update the score
	 * @param score
	 */
	public void hookScore(JLabel score) {
		this.lblScore = score;
		this.lblScore.setHorizontalAlignment(SwingConstants.CENTER);
	}
	
	/**
	 * increment the score and show it if the widget is available
	 */
	private void updateScore() {
		if(this.lblScore != null) {
			String displayText = String.format("<html><h1>Correct: %d <br>Incorrect: %d</h1></html>",this.truePositive, this.falsePositive);
			if(endgame) {
				displayText = displayText + ". Game Over";
			}
			this.lblScore.setText(displayText);
		}
	}
	
	/**
	 * currently the only ActionEvent we listens for is clock ticks. 
	 * First countdown to 0, 
	 * then start game. 
	 * then start counting upwards 
	 */
	@Override
	public void actionPerformed(ActionEvent e) {
		
		// if the game has already started then increment the clock, else countdown
		if(this.startgame) {
			this.time++;
		} else {
			this.time--;
		}
		
		// clock is at 0, this is where the game actually starts
		if(this.time == 0) {
			this.startgame = true;
			this.lblScore.setText("<html><h1><font color=\"green\">Start!</h1><html>");
			this.next();
		}
		
		// should be be displaying
		if(this.lblTimer != null) {
			if(this.startgame) { // now we're counting up.
				this.lblTimer.setText(String.format("<html><h1>Time Elapsed: %d seconds</h1></html>", time));	
			} else { // countdown beforee the game starts
				this.lblTimer.setText(String.format("<html><div style=\"text-align:right; font-size:15px; color:#AA0000;\">Game Starts in: %d seconds</div></html>", time));
			}
		}
	}

	/**
	 * Initializes the pairs of objects used in the gameplay
	 * Could actually read this from a file. But. Well.
	 */
	private void initDndPairs() {
		
		if(Settings.EXPERIMENT_DEBUG) {
			DndPair sPoint1 = new DndPair(DragAndDrop.LEFT_PANEL_ID, 0, DragAndDrop.RIGHT_PANEL_ID, 10);
			DndPair sPoint2 = new DndPair(DragAndDrop.LEFT_PANEL_ID, 1, DragAndDrop.RIGHT_PANEL_ID, 21);
			DndPair sPoint3 = new DndPair(DragAndDrop.LEFT_PANEL_ID, 3, DragAndDrop.RIGHT_PANEL_ID, 9);
		
			this.dndPairs.add(sPoint1);
			this.dndPairs.add(sPoint2);
			this.dndPairs.add(sPoint3);
		} else {
			// 1
			this.dndPairs.add(new DndPair(
					DragAndDrop.LEFT_PANEL_ID, this.getPictureUid(0, 0), 
					DragAndDrop.LEFT_PANEL_ID, this.getPictureUid(0, 1)));
			this.dndPairs.add(new DndPair(
					DragAndDrop.LEFT_PANEL_ID, this.getPictureUid(3, 0), 
					DragAndDrop.LEFT_PANEL_ID, this.getPictureUid(3, 1)));
			this.dndPairs.add(new DndPair(
					DragAndDrop.LEFT_PANEL_ID, this.getPictureUid(2, 0), 
					DragAndDrop.LEFT_PANEL_ID, this.getPictureUid(2, 1)));
			this.dndPairs.add(new DndPair(
					DragAndDrop.LEFT_PANEL_ID, this.getPictureUid(6, 0), 
				    DragAndDrop.LEFT_PANEL_ID, this.getPictureUid(3, 2)));
			this.dndPairs.add(new DndPair(
					DragAndDrop.RIGHT_PANEL_ID, this.getPictureUid(0, 3), 
				    DragAndDrop.RIGHT_PANEL_ID, this.getPictureUid(0, 2)));
			// 6
			this.dndPairs.add(new DndPair(
					DragAndDrop.RIGHT_PANEL_ID, this.getPictureUid(3, 3), 
				    DragAndDrop.RIGHT_PANEL_ID, this.getPictureUid(3, 2)));
			this.dndPairs.add(new DndPair(
					DragAndDrop.RIGHT_PANEL_ID, this.getPictureUid(2, 1), 
				    DragAndDrop.RIGHT_PANEL_ID, this.getPictureUid(2, 3)));
			this.dndPairs.add(new DndPair(
					DragAndDrop.RIGHT_PANEL_ID, this.getPictureUid(0, 0), 
				    DragAndDrop.RIGHT_PANEL_ID, this.getPictureUid(6, 3)));
			this.dndPairs.add(new DndPair(
					DragAndDrop.LEFT_PANEL_ID, this.getPictureUid(0, 1), 
				    DragAndDrop.RIGHT_PANEL_ID, this.getPictureUid(3, 3)));
			this.dndPairs.add(new DndPair(
					DragAndDrop.LEFT_PANEL_ID, this.getPictureUid(3, 3), 
				    DragAndDrop.RIGHT_PANEL_ID, this.getPictureUid(6, 3)));
			// 11
			this.dndPairs.add(new DndPair(
					DragAndDrop.RIGHT_PANEL_ID, this.getPictureUid(1, 1), 
				    DragAndDrop.LEFT_PANEL_ID, this.getPictureUid(1, 1)));
			this.dndPairs.add(new DndPair(
					DragAndDrop.LEFT_PANEL_ID, this.getPictureUid(6, 2), 
				    DragAndDrop.RIGHT_PANEL_ID, this.getPictureUid(2, 2)));
			this.dndPairs.add(new DndPair(
					DragAndDrop.LEFT_PANEL_ID, this.getPictureUid(1, 0), 
				    DragAndDrop.LEFT_PANEL_ID, this.getPictureUid(5, 2)));
			this.dndPairs.add(new DndPair(
					DragAndDrop.RIGHT_PANEL_ID, this.getPictureUid(2, 2), 
				    DragAndDrop.RIGHT_PANEL_ID, this.getPictureUid(5, 1)));
			this.dndPairs.add(new DndPair(
					DragAndDrop.RIGHT_PANEL_ID, this.getPictureUid(6, 1), 
				    DragAndDrop.LEFT_PANEL_ID, this.getPictureUid(5, 0)));

			
			
			
			
			
			
			
		}
	}
	
	/**
	 * Helper function to get the UID based on row and col. 
	 * because the actual UID is in 1-dimensional space
	 * @param row
	 * @param col
	 */
	private int getPictureUid(int row, int col) {
		return row * DragAndDrop.PICTURE_COLS + col;
	}
	
	/** getters **/
	public int getTruePositive() {
		return this.truePositive;
	}
	
	public int getFalsePositive() {
		return this.falsePositive;
	}
	
	/**
	 * somewhat different, we do validation here to make sure this is ONLY returned at the end of the game
	 * So, not _exactly_ a getter but close
	 * @return
	 */
	public int getTotalRuntime() {
		if(this.endgame) {
			return this.time;
		} else return -1;
	}
}
