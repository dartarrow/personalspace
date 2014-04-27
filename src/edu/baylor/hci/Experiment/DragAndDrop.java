package edu.baylor.hci.Experiment;

import java.awt.GraphicsEnvironment;
import java.awt.GridLayout;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;

import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.WindowConstants;

import org.apache.log4j.Logger;

/**
 * This is the main class for the DragAndDrop game, 
 * it works as a bootstrap class, Everything relevant is declared and init'd here. 
 * then we let all the individual classes go do their thing.  
 * 
 * @author Alvin
 *
 * TODO: align the Pictures in the center
 */
public class DragAndDrop {
	/** Class Constants **/
	// columns of pictures/rows in each panel
	public static final int PICTURE_COLS = 4;
	// rows of pictures
	public static final int PICTURE_ROWS = 7;
	// random IDs assigned to each label
	public static final int LEFT_PANEL_ID = 1;
	public static final int RIGHT_PANEL_ID = 2;	

	// main frame
	private JFrame fullscreenFrame;
	// create 3 Panels
	private JPanel leftPanel = new JPanel();
	private JPanel centerPanel = new JPanel();
	private JPanel rightPanel = new JPanel();
	
	// create 2 sets of Pictures, one for the right frame and one for the left. 
	PictureHolder leftPics[] = new PictureHolder[PICTURE_ROWS * PICTURE_COLS];
	PictureHolder rightPics[] = new PictureHolder[PICTURE_ROWS * PICTURE_COLS];
	
	// handler, or kinda like a controller
	DndGameplay gameplay = new DndGameplay();
	DndHandler handler = new DndHandler(this.gameplay);

    private static final Logger logger = Logger.getLogger(PictureHolder.class);
	/**
	 * Creates and launches this class
	 */
	public DragAndDrop() {
	    
		// MAGIC: fullscreen stuff
 		fullscreenFrame= new JFrame();
 		fullscreenFrame.setDefaultCloseOperation(WindowConstants.DISPOSE_ON_CLOSE);
 		fullscreenFrame.setUndecorated(true);
 		fullscreenFrame.setResizable(false);
 		fullscreenFrame.validate();
 		fullscreenFrame.setLayout(new GridLayout(1, 3));
 		GraphicsEnvironment.getLocalGraphicsEnvironment().getDefaultScreenDevice().setFullScreenWindow(fullscreenFrame);

	    // set panel layout
	    leftPanel.setLayout(new GridLayout(PICTURE_ROWS, PICTURE_COLS));
	    rightPanel.setLayout(new GridLayout(PICTURE_ROWS, PICTURE_COLS));
	    // the center panel is for the clock and the score, so.. basically nothing
	    centerPanel.setLayout(new GridLayout(3, 1)); // Arbitrary. 
	    


	    /* add timer and score to the center panel.  
	     * 
	     */
	    JLabel	lblTimer = new JLabel();
	    lblTimer.setText("This is a timer");
	    JLabel lblScore = new JLabel();
	    lblScore.setText("This is a score");
	    JPanel feedbackPanel = new JPanel(new GridLayout(2,1));
	    feedbackPanel.add(lblTimer);
	    feedbackPanel.add(lblScore);
	    centerPanel.add(feedbackPanel);
	    
		// add it to this Frame
		fullscreenFrame.add(leftPanel, 0);
		fullscreenFrame.add(centerPanel, 1);
		fullscreenFrame.add(rightPanel, 2);

		// set up the pictures
	    populatePanelWithPictures(leftPanel, leftPics, LEFT_PANEL_ID);
		populatePanelWithPictures(rightPanel, rightPics, RIGHT_PANEL_ID);

		// add the pictures to the gameplay mechanic
		this.gameplay.addPictureHolders(LEFT_PANEL_ID, leftPics);
		this.gameplay.addPictureHolders(RIGHT_PANEL_ID, rightPics);
		// and the score / timer labels
		this.gameplay.hookScore(lblScore);
		this.gameplay.hookTimer(lblTimer);
		
		// the center panel is a deadzone as far as touches are concerned
		// but we need to look for false positives that start here as well
		this.setCenterPanelClickable();
		
		// MAY THE GAMES.. BEGIN!
		this.gameplay.start();
	}

	/**
	 * add the relevant widgets to this frame
	 * @param jpanel
	 */
	private void populatePanelWithPictures(JPanel jpanel, PictureHolder[] pic, int panelId) {
		
		// get the images first
		for(int picCount = 0; picCount < pic.length; picCount++) {
			try {
				PictureHolder ph = new PictureHolder(panelId, picCount);
				ph.setTransferHandler(this.handler);
				pic[picCount] = ph;
			} catch (Exception e) {
				e.printStackTrace();
				System.exit(1);
			}
			jpanel.add(pic[picCount], picCount);
			pic[picCount].paintComponent(fullscreenFrame.getGraphics());
		}
	}
	
	private void setCenterPanelClickable() {
		this.centerPanel.addMouseListener(new MouseListener() {
			
			@Override
			public void mouseReleased(MouseEvent arg0) {
				// TODO Auto-generated method stub
				
			}
			
			@Override
			public void mousePressed(MouseEvent arg0) {
				logger.warn("Clicked on center panel");
			}
			
			@Override
			public void mouseExited(MouseEvent arg0) {
				// TODO Auto-generated method stub
				
			}
			
			@Override
			public void mouseEntered(MouseEvent arg0) {
				// TODO Auto-generated method stub
				
			}
			
			@Override
			public void mouseClicked(MouseEvent arg0) {
				// TODO Auto-generated method stub
				
			}
		});
	}
	
}
