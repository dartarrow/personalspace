package edu.baylor.hci.Experiment;

import java.awt.Color;
import java.awt.Graphics;
import java.awt.Image;
import java.awt.event.FocusEvent;
import java.awt.event.FocusListener;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.awt.event.MouseMotionListener;
import java.io.InputStream;

import javax.accessibility.Accessible;
import javax.imageio.ImageIO;
import javax.swing.BorderFactory;
import javax.swing.JComponent;
import javax.swing.TransferHandler;

import org.apache.log4j.Logger;

/**
 * This class is for the widgets (UI element) that we use to drag around. 
 * 
 * @author Alvin
 *
 */
public class PictureHolder extends JComponent implements MouseListener, FocusListener, Accessible, MouseMotionListener  {

	public enum ImageType { BLANK, SOURCE, DESTINATION;	}
	/** Class Constants **/
	// source and dest pics
	private static final String SOURCE_PIC = "source_200.jpg";
	private static final String DEST_PIC   = "dest_200.jpg";
	private static final String BLANK_PIC  = "blank_200.jpg";
	// hardcode the size. Square. Refers to the both the height and width
	private static final int IMAGE_SIZE = 200;

	
	private static final long serialVersionUID = 1701562082304155547L;
	// this Picture's picture
	private Image image;
	// size. this is a square, so height = width. 
	// cluster, eg: Left / right / top / whatever
	private int clusterId;
	// pictureId because each picture will be in 2 different clusters for matching
	private int id;
	// precreate the two types of pictures we use, so the creation will not be redundant. 
	private Image sourceImage;
	private Image destImage;
	private Image blankImage;
	// set the ImageType, will be useful for logging
	private ImageType imageType;
	// the HoverTime is basically the time between enter and select. 
	private long mouseEnterTime; // timestamp 
	private int mouseHoverTimeMS; // difference between Enter and Now

    private static final Logger logger = Logger.getLogger(PictureHolder.class);

	
	/**
	 * Create a square picture holder with the given params
	 * @param clusterId - Logical cluster, generally refers to the panel where we attach it. 
	 * @param id - all pics need an ID for correctness checking 
	 * @throws Exception
	 */
	public PictureHolder(int clusterId, int id) {

		this.clusterId = clusterId;
		this.id = id;
		
		// default it to a blank image
		this.setImageType(ImageType.BLANK);
		// add all our listeners
		setFocusable(true);
		addMouseListener(this);
		addFocusListener(this);
		addMouseMotionListener(this);
		
	}

	/**
	 * set the image for this PictureHolder based on the types available here. 
	 * Images cannot be set arbitrarily. No reason to do that. This method makes everything easier. 
	 * @param imageType
	 */
	public void setImageType(ImageType imageType) {
		this.imageType = imageType;
		
		// this loads from a static, usual location. In eclipse, its a Source folder
		ClassLoader classLoader = Thread.currentThread().getContextClassLoader();

		try{
			switch(imageType){
			case BLANK:
				InputStream blankInput   = classLoader.getResourceAsStream(BLANK_PIC);
				this.blankImage 	= ImageIO.read(blankInput);
				this.image = this.blankImage;
				break;
			case DESTINATION:
				InputStream destInput    = classLoader.getResourceAsStream(DEST_PIC);
				this.destImage 		= ImageIO.read(destInput);
				this.image = this.destImage;
				break;
			case SOURCE:
				InputStream sourceInput  = classLoader.getResourceAsStream(SOURCE_PIC);
				this.sourceImage	= ImageIO.read(sourceInput);
				this.image = this.sourceImage;
				break;
			default:
				break;
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
		this.repaint();
	}
	
	public void mouseClicked(MouseEvent e) {
		logger.debug("mouseClick on Jpanel " + clusterId + " picture " + id);
	}


	/* Set the border to red to show that this is on focus
	 */
	public void mouseEntered(MouseEvent e) {
		this.setBorder(BorderFactory.createLineBorder(Color.RED));
		
		/* we also calculate the hover time here.
		 * we will need the hover time for both the Drag and the Drop. 
		 * But in this implementation we'll just focus on the Selection (ie the select/drag/click time)
		 * !! SO we only do this if this picture is a source image AND if the cursor has not yet entered 
		 * (ie, the time is 0)
		 */
	
		if(this.imageType == ImageType.SOURCE && this.mouseEnterTime == 0) {
			this.mouseEnterTime = System.currentTimeMillis();
		}
	}

	public void mouseExited(MouseEvent e) {
		this.setBorder(BorderFactory.createEmptyBorder());
	}

	public void mousePressed(MouseEvent e) {
		/* this is ONLY done if this is a source picture,
		 * we calculate the hover time in milliseconds, 
		 * then we reset the EnterTime. Important because enterTime==0 is a check in MouseEntered 
		 */
		if(this.imageType == ImageType.SOURCE) {
			this.mouseHoverTimeMS = (int)(System.currentTimeMillis() - this.mouseEnterTime);
			this.mouseEnterTime = 0;
		}
		
	}
	
	/** CHECK FOR CORRECTNESS **/
	public void mouseReleased(MouseEvent e) {
		this.setBorder(BorderFactory.createLineBorder(Color.WHITE));
	}

	@Override
	public void mouseDragged(MouseEvent e) {
		JComponent c = (JComponent) e.getSource();
		TransferHandler handler = c.getTransferHandler();
		//Tell the transfer handler to initiate the drag.
		handler.exportAsDrag(c, e, TransferHandler.MOVE);
	}

	@Override
	public void mouseMoved(MouseEvent e) {
		
	}
	
	public void focusGained(FocusEvent e) {
		//Draw the component with a red border
		//indicating that it has focus.
		this.repaint();
	}

	public void focusLost(FocusEvent e) {
		//Draw the component with no border
		//indicating that it doesn't have focus.
		this.repaint();
	}

	public void paintComponent(Graphics graphics) {
		
		super.paintComponent(graphics);
		
		Graphics g = graphics.create();

		//Draw in our entire space, even if isOpaque is false.
		g.setColor(Color.WHITE);
		g.fillRect(0, 0, IMAGE_SIZE, IMAGE_SIZE);

		if (image != null) {
			//Draw image at its natural size of (size X size).
			g.drawImage(image, 0, 0, this);
		}

		g.drawRect(0, 0, IMAGE_SIZE, IMAGE_SIZE);
		g.dispose();
	}
	
	// overright toString, returns only clusterId and uID
	public String toString() {
		return "[ " + this.clusterId + ", " + this.id + " ]";
	}
	
	//** getters **//
	public Image getImage() {
		return this.image;
	}
	
	public int getClusterId() {
		return this.clusterId;
	}
	
	public int getId() {
		return this.id;
	}
	
	/**
	 * Get the HoverTime in MS. Only happens if this is a Source Image. 
	 * If its not a source, returns -1
	 * @return Hover Time in MilliSeconds
	 */
	public int getHoverTime() {
		return this.mouseHoverTimeMS;
	}

	
}
