package edu.baylor.hci.Experiment;

import java.awt.Image;
import java.awt.MouseInfo;
import java.awt.Point;
import java.awt.datatransfer.DataFlavor;
import java.awt.datatransfer.Transferable;
import java.awt.datatransfer.UnsupportedFlavorException;

import javax.swing.BorderFactory;
import javax.swing.JComponent;
import javax.swing.TransferHandler;

import edu.baylor.hci.LeapOMatic.TouchPoints;
import edu.baylor.hci.Logger.ExperimentLogger;
import edu.baylor.hci.Logger.MFileLogger.LogStatus;


/**
 * This class does the processing of if the classes picturescan be dragged and dropped. 
 * Extends the TransferHandler because its the _right_way_ but doesn't actually do anything relevant
 *  
 * @author Alvin
 * 	
 */
public class DndHandler extends TransferHandler{

	private static final long serialVersionUID = 9171459555183867826L;
	/** Class Constants **/
	private static final int INVALID_CLUSTER_ID = -1;
	private static final int INVALID_IMAGE_ID   = -1;
	
	// for logging purposes. 
	long dragTime;
	long dropTime;
	Point dragPoint;
	Point dropPoint;
	
	// operational Stuff, where was the mouse dragged from and where was it released
	private int selectedClusterDragId = INVALID_CLUSTER_ID;
	private int selectedClusterDropId = INVALID_CLUSTER_ID;
	private int selectedImageDragId   = INVALID_IMAGE_ID;
	private int selectedImageDropId   = INVALID_IMAGE_ID;
	// holds the current dargged and dropped Pictures
	PictureHolder draggedPicture;
	PictureHolder droppedPicture;
	DataFlavor pictureFlavor = DataFlavor.imageFlavor;

	// gameplay mechanic. really required
	DndGameplay gameplay;
	// Logs our experiment
	
	ExperimentLogger experimentLogger;

	/**
	 * Constructor. Needs the gameplay mechanic for checking correctness
	 * 
	 */
	public DndHandler(DndGameplay gameplay){
		this.gameplay = gameplay;
		//enable the experiment logger
		 experimentLogger = new ExperimentLogger("dragAndDrop"+TouchPoints.getTypetoAppend());
		this.experimentLogger.setLogStatus(LogStatus.START);
		
	   
	}
	
	/**
	 * Drop action
	 */
	public boolean importData(JComponent component, Transferable transferable) {
		this.dropTime = System.currentTimeMillis();
		this.dropPoint = MouseInfo.getPointerInfo().getLocation();
		
		if (canImport(component, transferable.getTransferDataFlavors())) {
			this.droppedPicture = (PictureHolder) component;
			return true;
		} else {
			return false;
		}
	}

	/**
	 * triggered on Drag action
	 */
	protected Transferable createTransferable(JComponent component) {
		this.dragTime = System.currentTimeMillis();
		this.dragPoint = MouseInfo.getPointerInfo().getLocation();
		
		this.draggedPicture = (PictureHolder) component;
		return new PictureTransferable(this.draggedPicture);
	}
	
	/**
	 * TODO: find out what this actually does
	 */
	public int getSourceActions(JComponent c) {
		return COPY_OR_MOVE;
	}

	/**
	 * No idea when this is done.
	 * TODO: trace this!! check correctness is done here. But. May be incorrect
	 */
	protected void exportDone(JComponent c, Transferable data, int action) {
		this.draggedPicture = (PictureHolder) c;
		this.checkCorectness();
	}

	public boolean canImport(JComponent c, DataFlavor[] flavors) {
		// do something
		return true;
	}
	
	/**
	 * check if the correct _source_ image has been dragged onto the correct _destination_ image
	 * @return
	 */
	private void checkCorectness() {
		// 0. first reset all the gui actions
		this.draggedPicture.setBorder(BorderFactory.createEmptyBorder());
		
		
		// 1. do checking
		boolean isCorrect; // use for switching later on 
		if(this.draggedPicture == null || this.droppedPicture == null) {
			// in the first try, there is nothing in thse objects, so 
			// there will be an error if we try getting something that doesn't exist
			isCorrect = false;
		} else if(this.gameplay.checkDnd(this.draggedPicture.getClusterId(), 
								  this.draggedPicture.getId(), 
								  this.droppedPicture.getClusterId(), 
								  this.droppedPicture.getId())) {
			isCorrect = true;
		} else {
			isCorrect = false;
		}
		this.resetDragSource();
		this.resetDropDestination();

		// different actions triggered based on if the last action was a correct or incorrect one. 
		if(isCorrect) {
			// add stuff to logs.. 
			this.experimentLogger.addDndTruePos(dragPoint, dropPoint, dragTime, dropTime);
			this.experimentLogger.addDragHoverTime(this.draggedPicture.getHoverTime());

			// if there's nothing else to do, then we end the game
			if(!this.gameplay.next()) {
				// ** GAME OVER ** //
				this.gameplay.finish();
				// add comments for number of TP / FP
				this.experimentLogger.addComment("True Positives: " + this.gameplay.getTruePositive());
				this.experimentLogger.addComment("False Positives: " + this.gameplay.getFalsePositive());
				// and the total time
				this.experimentLogger.addComment("Total Time: " + this.gameplay.getTotalRuntime());
				// first write out the log
				this.experimentLogger.writeLog();
			}
		} else {
			/* This is an incorrect match. We will be making some assumptions here.  
			 * Because what exactly is an incorrect match? 
			 * Is it because it's incorrectly selected? Or incorrectly dropped? Or accidentally selected?
			 * -- At this point we assume that there is no way the user makes a gaming mistake. 
			 * So they will always and only drag the correct widget. 
			 */
			this.experimentLogger.addDndFalsePos(dragPoint, dropPoint, dragTime, dropTime);
		}
	}
	
	private void resetDragSource() {
		this.selectedClusterDragId = INVALID_CLUSTER_ID;
		this.selectedImageDragId   = INVALID_IMAGE_ID;
 	}
	
	private void resetDropDestination() {
		this.selectedClusterDropId = INVALID_CLUSTER_ID;
		this.selectedImageDropId   = INVALID_IMAGE_ID;		
	}

	/** Setters / getters **/
	public int getClusterDragId() {
		return selectedClusterDragId;
	}
	public void setClusterDragId(int clusterDragId) {
		this.selectedClusterDragId = clusterDragId;
	}
	public int getClusterDropId() {
		return selectedClusterDropId;
	}
	public void setClusterDropId(int clusterDropId) {
		this.selectedClusterDropId = clusterDropId;
	}
	public int getImageDragId() {
		return selectedImageDragId;
	}
	public void setImageDragId(int imageDragId) {
		this.selectedImageDragId = imageDragId;
	}
	public int getImageDropId() {
		return selectedImageDropId;
	}
	public void setImageDropId(int imageDropId) {
		this.selectedImageDropId = imageDropId;
	}

	/**
	 * Some mandatory transferable class thing that is needed for something, somewhere. 
	 * I don't even know.. 
	 * @author Alvin
	 *
	 */
	class PictureTransferable implements Transferable {
		private Image image;

		PictureTransferable(PictureHolder pic) {
			image = pic.getImage();
		}

		public Object getTransferData(DataFlavor flavor) throws UnsupportedFlavorException {
			if (!isDataFlavorSupported(flavor)) {
				throw new UnsupportedFlavorException(flavor);
			}
			return image;
		}

		public DataFlavor[] getTransferDataFlavors() {
			return new DataFlavor[] { pictureFlavor };
		}

		public boolean isDataFlavorSupported(DataFlavor flavor) {
			return pictureFlavor.equals(flavor);
		}
	}
}
