package edu.baylor.hci.LeapOMatic;

//UI prereqs
import java.awt.Checkbox;
import java.awt.Choice;
import java.awt.Color;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;
import java.awt.event.KeyEvent;

import javax.swing.AbstractAction;
import javax.swing.ActionMap;
import javax.swing.BorderFactory;
import javax.swing.ButtonGroup;
import javax.swing.InputMap;
import javax.swing.JButton;
import javax.swing.JComponent;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JRadioButton;
import javax.swing.JTextField;
import javax.swing.KeyStroke;
import javax.swing.SwingUtilities;
import javax.swing.border.Border;

import org.apache.log4j.Logger;

import com.jogamp.newt.event.InputEvent;

import edu.baylor.hci.Calibration.Calibration;
import edu.baylor.hci.Calibration.MouseListener;
import edu.baylor.hci.Calibration.MouseListener.Status;
import edu.baylor.hci.Experiment.DragAndDrop;
import edu.baylor.hci.Experiment.ExpConfig;
import edu.baylor.hci.Experiment.ExpConfig.ExperimentNum;
import edu.baylor.hci.Experiment.SequentialHover;
import edu.baylor.hci.Logger.PositionLog;
import edu.baylor.hci.Logger.MFileLogger.LogStatus;


public class TouchPointsView extends JPanel implements ActionListener, ItemListener {
	/** CONSTANTS **/
	private static final long serialVersionUID = 1211493067872314885L;
	// whats the max number of fingers to cater for? Seems like 7 is a nice number
	private static final int FINGER_COUNT = 7;
	// whats the height of each row. used by both title and content width
	private static final int ROW_HEIGHT = 20;
	// width of the TITLE panel
	private static final int PALM_PANEL_TITLE_WIDTH = 100;
	// width of the CONTENT panel
	private static final int PALM_PANEL_CONTENT_WIDTH = 200; 
	// width of the title panel
	private static final Dimension PALM_PANEL_TITLE_DIMENSION = new Dimension(PALM_PANEL_TITLE_WIDTH, ROW_HEIGHT);
	private static final Dimension CONFIG_PANEL_TITLE_DIMENSION = new Dimension(150, ROW_HEIGHT);
	// width of the content panel
	private static final Dimension PALM_PANEL_CONTENT_DIMENSTION = new Dimension(PALM_PANEL_CONTENT_WIDTH, ROW_HEIGHT);
	// larger content, just make it 4 time higher
	private static final Dimension PALM_PANEL_LARGE_CONTENT_DIMENSTION = new Dimension(PALM_PANEL_CONTENT_WIDTH, ROW_HEIGHT * 4);
	
	// width of the CONTENT panel
	private static final int FINGER_PANEL_CONTENT_WIDTH = 100; 
	// width of the content panel
	private static final Dimension FINGER_PANEL_CONTENT_DIMENSTION = new Dimension(FINGER_PANEL_CONTENT_WIDTH, ROW_HEIGHT);

	// INSETS are used by the gra
	private static final Insets TITLE_INSETS = new Insets(4,0,4,30);
	private static final Insets CONTENT_INSETS= new Insets(4,0,4,200);
	// private static final Insets LARGE_CONTENT_INSETS= new Insets(20, 0, 10, 0);
	
	private static final boolean LEFT_CLICK_DEFAULT = false;
	private static final boolean RIGHT_CLICK_DEFAULT = false;
	private static final boolean STOP_GESTURE_DEFAULT = false;
	
	public static final int LEFT_HAND = 0;
	public static final int RIGHT_HAND = 1;
	/** Object variables **/
    private static final Logger logger = Logger.getLogger(TouchPointsView.class);
	
	/*
	 * This class and the object below are purely to toggle the tracking status in mouseListener
	 * based on the dropDown selection. OnChange we set the MouseListener.status to the new selected status
	 */
	private class ItemListenerUtility implements ItemListener
	{
		
		@Override
		public void itemStateChanged(ItemEvent arg0) {
			for(Status status: MouseListener.Status.values())
			{
				if(status.toString()==trackingType.getSelectedItem())
				{
					TouchPoints.getMouseListener().status=status;
				}
			}
			
			
		}
		
	}
	private ItemListenerUtility itemListener = new ItemListenerUtility();
	// declare the UI stuff - Labels
	private JLabel jlTitlePitch;
	private JLabel jlTitleRoll;
	private JLabel jlTitleYaw;
	private JLabel jlTitleFingerCount;
	private JLabel jlTitleGestures;
	private JLabel jlTitlePalm;
	private JLabel jlTitlePalmNormal;
	private JLabel jlTitlePalmNormAngleToExpected;
	private JLabel jlTitlePalmVelocity;
	private JLabel jlTitleAvgFinger;
	private JLabel jlTitleLogInfo;
	private JLabel jlTitleTipPos;
	private JLabel jlTitleFingerWidth;
	private JLabel jlTitleFingerVelocity;
	private JLabel jlTitleFingerDirection;
	private JLabel jlTitleFingerLength;
	private JLabel[] jlTitleFingers = new JLabel[FINGER_COUNT];
	//Configurable gesture thresholds (Title JLABELS)
	private JLabel jlTitleCircleMinRadius;
	private JLabel jlTitleCircleMinArc;
	private JLabel jlTitleSwipeMinLength;
	private JLabel jlTitleSwipeMinVelocity;
	private JLabel jlTitleKeyTapMinDownVelocity;
	private JLabel jlTitleKeyTapHistorySeconds;
	private JLabel jlTitleKeyTapMinDistance;
	private JLabel jlTitleScreenTapMinForwardVelocity;
	private JLabel jlTitleScreenTapHistorySeconds;
 	 // private JLabel jlTitleScreenTapMinDistance; // unused
	
	public JLabel jlPitch;
	public JLabel jlRoll;
	public JLabel jlYaw;
	public JLabel jlFingers;
	public JLabel jlGestures;
	public JLabel jlPalm;
	public JLabel jlPalmNormal;
	public JLabel jlPalmNormAngleToExpected;
	public JLabel jlPalmVelocity;
	public JLabel jlAvgFinger;
	public JLabel jlLogInfo;
	
	public JTextField  jlCircleMinRadius;
	public JTextField  jlCircleMinArc;
	public JTextField  jlSwipeMinLength;
	public JTextField  jlSwipeMinVelocity;
	public JTextField  jlKeyTapMinDownVelocity;
	public JTextField  jlKeyTapHistorySeconds;
	public JTextField  jlKeyTapMinDistance;
	public JTextField  jlScreenTapMinForwardVelocity;
	public JTextField  jlScreenTapHistorySeconds;
	public JTextField  jlScreenTapMinDistance;
	
	private JButton btnShowCalibration;
	private Calibration calibrationPanel; 
	private JButton btnShowGraph;
	private JButton btnShowDndGame; 
	private Choice trackingType;
	private Choice experimentConfig;
	private int lastTrackingUsed;
	
	private JButton btnLaunchExperiment;
	private SequentialHover sequentialHover;
	private Checkbox leftClick;
	private Checkbox rightClick;
	private Checkbox stopGesture;
	private JLabel jlTitleParticipantName;
	private JTextField jlParticipantName;
	private ButtonGroup handedness;
	private JRadioButton rightHand;
	private JRadioButton leftHand;
	
	private ButtonGroup deviceGroup;
	private JRadioButton mouse;
	private JRadioButton touchpad;
	private JRadioButton LOM;
	
	
	// Fingers need an Array of UI Elements
	public JLabel[] jlTipPos = new JLabel[FINGER_COUNT];
	public JLabel[] jlFingerWidth = new JLabel[FINGER_COUNT];
	public JLabel[] jlFingerVelocity = new JLabel[FINGER_COUNT];
	public JLabel[] jlFingerDirection= new JLabel[FINGER_COUNT];
	public JLabel[] jlFingerLength = new JLabel[FINGER_COUNT];
	
	
	// counter for the first panel
	public int panel1row = 0;

	public boolean enableSystemOut=false;
	
	/**
	 * takes any JComponent title and JComponent content and adds it to the panel with its proper constraints.
	 * @param title
	 * @param content
	 * @param panel
	 * @param c
	 */
	private void addRow(JComponent title, JComponent content, JPanel panel,  GridBagConstraints c)
	{
		this.addRow(title, content, panel, c, false);
	}
	
	/**
	 * Overloads the original. Uses different constraints if this is a largeField
	 * @param title
	 * @param content
	 * @param panel
	 * @param gbConstraints
	 * @param largeContent
	 */
	private void addRow(JComponent title, 
						JComponent content, 
						JPanel panel,  
						GridBagConstraints gbConstraints, 
						boolean largeContent)
	{
	
		this.setDefaultsPalmPanel(title, largeContent, true);
		this.setDefaultsPalmPanel(content, largeContent);
		
		int col=0;
		gbConstraints.gridy=panel1row;
		gbConstraints.gridx=col++;
		gbConstraints.insets=TITLE_INSETS;
		panel.add(title, gbConstraints);
		
		gbConstraints.gridy=panel1row++;
		gbConstraints.gridx=col;
		gbConstraints.insets = CONTENT_INSETS;

		// if this is a large one, just modify from the defaults
		Border blackline = BorderFactory.createLineBorder(Color.black);
		content.setBorder(blackline);
		panel.add(content, gbConstraints);
	}
	
	/**
	 * Adds a row of JComponent to our panel. 
	 * Each Row consists of one title and one content. Eg: `PalmX : 123.93491` 
	 *                                                     [title]  [content]
	 * @param title
	 * @param content
	 * @param panel
	 * @param gbConstraints
	 * @param largeContent
	 * @param largeTitle
	 */
	private void addRow(JComponent title, 
						JComponent content, 
						JPanel panel,  
						GridBagConstraints gbConstraints, 
						boolean largeContent, 
						boolean largeTitle )
	{
	
		this.setDefaultsPalmPanel(title, largeTitle, true);
		this.setDefaultsPalmPanel(content, largeContent);
		
		int col=0;
		gbConstraints.gridy=panel1row;
		gbConstraints.gridx=col++;
		gbConstraints.insets=TITLE_INSETS;
		panel.add(title, gbConstraints);
		
		gbConstraints.gridy=panel1row++;
		gbConstraints.gridx=col;
		gbConstraints.insets = CONTENT_INSETS;
		//gbConstraints.ipadx=50;

		// if this is a large one, just modify from the defaults
		/* FIXME the borders added only for debugging and visualization. Needs to be removed eventually */
		Border blackline = BorderFactory.createLineBorder(Color.black);
		content.setBorder(blackline);
		panel.add(content, gbConstraints);
	}
	
	/**
	 * Gets the default values that we want to apply onto the JComponents here. 
	 * @param component - Any JComponent
	 * @param isLargeField - is this field "Large", i THINK that means the height is bigger than usual
	 * @param isTitleComponent
	 */
	private void setDefaultsPalmPanel(JComponent component, boolean isLargeField, boolean isTitleComponent) 
	{
		
		if(isTitleComponent) {
			// we don't care if titles are big. all sets same size
			if(isLargeField)
			{
				component.setMinimumSize(CONFIG_PANEL_TITLE_DIMENSION);
				component.setMaximumSize(CONFIG_PANEL_TITLE_DIMENSION);
				component.setPreferredSize(CONFIG_PANEL_TITLE_DIMENSION);
			}
			else
			{
				component.setMinimumSize(PALM_PANEL_TITLE_DIMENSION);
				component.setMaximumSize(PALM_PANEL_TITLE_DIMENSION);
				component.setPreferredSize(PALM_PANEL_TITLE_DIMENSION);
			}
		} else {
			// its a content. but is it big?
			if(isLargeField) {
				component.setMinimumSize(PALM_PANEL_LARGE_CONTENT_DIMENSTION);
				component.setMaximumSize(PALM_PANEL_LARGE_CONTENT_DIMENSTION);		
				component.setPreferredSize(PALM_PANEL_LARGE_CONTENT_DIMENSTION);
			} else {
				component.setMinimumSize(PALM_PANEL_CONTENT_DIMENSTION);
				component.setMaximumSize(PALM_PANEL_CONTENT_DIMENSTION);		
				component.setPreferredSize(PALM_PANEL_CONTENT_DIMENSTION);
			}
		}

	}
	
	/**
	 * 
	 * @param component
	 * @param isLargeField
	 */
	private void setDefaultsPalmPanel(JComponent component, boolean isLargeField) 
	{
		this.setDefaultsPalmPanel(component, isLargeField, false);
	}
	
	
	/**
	 * Helper function to set up the default size and any other attribute, 
	 *  for the panel holding the finger matrix
	 * @param component
	 */
	private void setDefaultsFingerPanel(JComponent component) {
		component.setMinimumSize(FINGER_PANEL_CONTENT_DIMENSTION);
		component.setMaximumSize(FINGER_PANEL_CONTENT_DIMENSTION);		
		component.setPreferredSize(FINGER_PANEL_CONTENT_DIMENSTION);
	}
	
	/**
	 * Toggle's the positionLog's logging Status to start or pause
	 */
	private void toggleStartPause()
	{
		if(TouchPoints.getPositionLog().getLogStatus()==PositionLog.LogStatus.PAUSE || 
		   TouchPoints.getPositionLog().getLogStatus()==PositionLog.LogStatus.IDLE)
		{
			TouchPoints.getPositionLog().setLogStatus(LogStatus.START);
			
		}
		else
		{
			TouchPoints.getPositionLog().setLogStatus(LogStatus.PAUSE);			
		}
	}
	
	/*
	 * Called from setupKeyBindings actionPerformed
	 * 
	 * LogStatus Values:
	 * 
	 */
	
	/**
	 * 
	 * @param e = key that the user pressed to toggle the logging
	 * e = VK_P is used to toggle between LogStatus.START and LogStatus.PAUSE
	 * e = VK_O is used to write the log (which sets logStatus to LogStatus.IDLE), must call logStatus.STOP after.
	 */
	private void toggleLogging(int e)
	{
		switch(e)
		{
		case KeyEvent.VK_P:
			logger.info("Toggling START/PAUSE");
			toggleStartPause();
			break;
		case KeyEvent.VK_O:
			if(TouchPoints.getPositionLog().getLogStatus()!=LogStatus.STOP)
			{
				logger.info("WRITING LOG & STOPPED");
				TouchPoints.getPositionLog().writeLog();
				TouchPoints.getPositionLog().setLogStatus(LogStatus.STOP);
			}
			break;
		}
	}
	
	/**
	 * Define all the Keys that we're using here. 
	 * CTRL + P : Toggle [P]osition Log
	 * CTRL + O : Write File contents [O]ut to file
	 * SPACE : Killswitch for the Matrix driven mouse movement
	 * @param panel
	 */
	private void setupKeyBindings(JPanel panel){
		
		InputMap im = panel.getInputMap(JPanel.WHEN_IN_FOCUSED_WINDOW);
	    ActionMap am = panel.getActionMap();
	    im.put(KeyStroke.getKeyStroke(KeyEvent.VK_P, InputEvent.CTRL_MASK), "P");
	    am.put("P", new AbstractAction() {
			private static final long serialVersionUID = 4571122900380440352L;
			public void actionPerformed(ActionEvent e)
	    	{	
	    		toggleLogging(KeyEvent.VK_P);
	    	}	
	    });
	    im.put(KeyStroke.getKeyStroke(KeyEvent.VK_O, InputEvent.CTRL_MASK), "O");
	    am.put("O", new AbstractAction() {
			private static final long serialVersionUID = -4157299925048807633L;
			public void actionPerformed(ActionEvent e)
	    	{	
				int temp=e.getModifiers();
				System.out.println(temp);
				
					
					toggleLogging(KeyEvent.VK_O);
				
	    	}	
	    });
	    im.put(KeyStroke.getKeyStroke(KeyEvent.VK_SPACE, InputEvent.CTRL_MASK), "SPC");
	    am.put("SPC", new AbstractAction() {
			private static final long serialVersionUID = -4157299925048807633L;
			public void actionPerformed(ActionEvent e)
	    	{	
				int temp=trackingType.getSelectedIndex();
				if(trackingType.getSelectedItem()!=MouseListener.Status.NONE.toString())
				{
					trackingType.select(MouseListener.Status.NONE.toString());
					TouchPoints.getMouseListener().status=MouseListener.Status.NONE;
					lastTrackingUsed=temp;
				}
				else
				{
					trackingType.select(lastTrackingUsed);
					lastTrackingUsed=temp;
					for(Status status: MouseListener.Status.values())
					{
						if(status.toString()==trackingType.getSelectedItem())
						{
							TouchPoints.getMouseListener().status=status;
						}
					}
				}
	    	}	
	    });
	}
	
	
	TouchPointsView() {

		/** setup gui  **/
		GridBagLayout gridbag= new GridBagLayout();
		GridBagConstraints gbConstraints = new GridBagConstraints();
		gbConstraints.fill= GridBagConstraints.HORIZONTAL;
		gbConstraints.anchor=GridBagConstraints.NORTHWEST;
		gbConstraints.insets = new Insets(0, 0, 10, 10);
		
		JPanel superPanel= new JPanel(new GridBagLayout());
		JPanel participantPanel = getParticipantFormPanel();
		JPanel palmPanel = getPalmPanel();
		JPanel fingerPanel = getFingerPanel();
		JPanel gestureConfigPanel = getConfigPanel();
		JPanel adminPanel= getAdminPanel();
		JPanel legendPanel = getLegendPanel();
		setupKeyBindings(palmPanel);
		setLayout(gridbag);
		superPanel.add(participantPanel, gbConstraints);
		
		palmPanel.setAlignmentX(Component.LEFT_ALIGNMENT);
		
		gbConstraints.gridy=1;
		gbConstraints.gridx=0;
		superPanel.add(palmPanel, gbConstraints);
		gbConstraints.gridy=2;
		gbConstraints.gridx=0;
		superPanel.add(fingerPanel, gbConstraints);
		gbConstraints.gridy=3;
		gbConstraints.gridx=0;
		superPanel.add(gestureConfigPanel, gbConstraints);
		gbConstraints.gridy=4;
		gbConstraints.gridx=0;
		superPanel.add(adminPanel, gbConstraints);
		gbConstraints.gridy=5;
		gbConstraints.gridx=0;
		superPanel.add(legendPanel, gbConstraints);
		
		
		//superPanel.add((javax.swing.JComponent)chart.getCanvas());
		
		this.add(superPanel);
		this.setVisible(true);

		palmPanel.setBackground(Color.GRAY);
		palmPanel.setOpaque(true);
	}
	
	/**
	 * setup and return the left panel. 
	 * @return
	 */
	private JPanel getPalmPanel() {
		
		GridBagConstraints gbConstraints = new GridBagConstraints();
		gbConstraints.fill= GridBagConstraints.CENTER;
		gbConstraints.anchor=GridBagConstraints.CENTER;

		
		JPanel leftPanel = new JPanel(new GridBagLayout());
		// JLabels - Titles
		jlTitlePitch = new JLabel("Pitch");
		jlTitleRoll = new JLabel("Roll");
		jlTitleYaw = new JLabel("Yaw");	
		jlTitleFingerCount = new JLabel("Fingers");
		jlTitleGestures = new JLabel("Gestures");
		jlTitlePalm = new JLabel("Palm Pos");
		jlTitlePalmNormAngleToExpected = new JLabel("Normal Angle to Expected");
		jlTitlePalmNormal = new JLabel("Palm Normal");
		jlTitlePalmVelocity = new JLabel("Palm Velocity (EXPERIMENTAL)");
		jlTitleAvgFinger = new JLabel("Average Finger position");
		jlTitleLogInfo= new JLabel("Logging Status");
		
		// JLabels - Contents
		jlPitch = new JLabel(" - ");
		jlRoll = new JLabel(" - ");
		jlYaw = new JLabel(" - ");
		jlFingers = new JLabel(" - ");
		jlGestures = new JLabel(" - ");
		jlPalm = new JLabel(" - ");
		jlPalmNormAngleToExpected = new JLabel(" - ");
		jlPalmNormal = new JLabel(" - ");
		jlPalmVelocity = new JLabel(" - ");
		jlAvgFinger = new JLabel(" - ");
		jlLogInfo= new JLabel(" - ");				

		addRow(jlTitlePitch, jlPitch, leftPanel, gbConstraints);
		addRow(jlTitleRoll, jlRoll, leftPanel, gbConstraints);
		addRow(jlTitleYaw, jlYaw, leftPanel, gbConstraints);
		addRow(jlTitleFingerCount, jlFingers, leftPanel, gbConstraints);
		addRow(jlTitleGestures, jlGestures, leftPanel, gbConstraints, true);
		addRow(jlTitlePalm, jlPalm, leftPanel, gbConstraints);
		addRow(jlTitlePalmNormAngleToExpected, jlPalmNormAngleToExpected, leftPanel, gbConstraints);
		addRow(jlTitlePalmNormal, jlPalmNormal, leftPanel, gbConstraints);
		addRow(jlTitlePalmVelocity, jlPalmVelocity, leftPanel, gbConstraints);
		addRow(jlTitleAvgFinger, jlAvgFinger, leftPanel, gbConstraints);
		addRow(jlTitleLogInfo, jlLogInfo, leftPanel, gbConstraints);
		
		return leftPanel;
	}
	
	/**
	 * setup and return the right panel. 
	 * @return
	 */	
	private JPanel getFingerPanel() {
		JPanel rightPanel = new JPanel(new GridBagLayout());

		GridBagConstraints gbConstraints = new GridBagConstraints();
		gbConstraints.fill= GridBagConstraints.HORIZONTAL;
		gbConstraints.anchor=GridBagConstraints.WEST;

		// fingers - array
		jlTitleTipPos = new JLabel("Tip");
		jlTitleFingerWidth = new JLabel("Width");
		jlTitleFingerVelocity = new JLabel("Velocity");
		jlTitleFingerDirection = new JLabel("Direction");
		jlTitleFingerLength = new JLabel("Length");


		// fingers - array
		for(int i = 0; i < FINGER_COUNT;i++) {
			//init the title here also
			jlTitleFingers[i] = new JLabel("Finger [" + i+"]");
			jlTipPos[i] = new JLabel("Finger Tip " + i);
			jlFingerWidth[i] = new JLabel("Finger Width " + i);
			jlFingerVelocity[i] = new JLabel("Finger Velocity");
			jlFingerDirection[i] = new JLabel("Finger Direction");
			jlFingerLength[i] = new JLabel("Finger Length");
		}

		/* |-----------------------------------------------------------|
		 * | <empty>  | TipPos | Width | Velocity | Direction | Length |
		 * |-----------------------------------------------------------|
		 * | Finger-0 | xxxxxx | xxxxx | xxxxx    | xxxxxx    | xxxxxx |
		 * | Finger-1 | xxxxxx | xxxxx | xxxxx    | xxxxxx    | xxxxxx |
		 * |   ...    | xxxxx  | xxxxx | xxxxx    | xxxxxx    | xxxxxx |
		 * | Finger-N | xxxxx  | xxxxx | xxxxx    | xxxxxx    | xxxxxx |
		 * |-----------------------------------------------------------|
		 */
		
		gbConstraints.gridx=0;
		gbConstraints.gridy=0;
		rightPanel.add(new JLabel(""), gbConstraints);
		gbConstraints.gridx++;
		rightPanel.add(jlTitleTipPos, gbConstraints);
		gbConstraints.gridx++;
		rightPanel.add(jlTitleFingerWidth, gbConstraints);
		gbConstraints.gridx++;
		rightPanel.add(jlTitleFingerVelocity, gbConstraints);
		gbConstraints.gridx++;
		rightPanel.add(jlTitleFingerDirection, gbConstraints);
		gbConstraints.gridx++;
		rightPanel.add(jlTitleFingerLength, gbConstraints);
		
		
		
		// dynamically add finger stuff;
		for(int i = 0; i < FINGER_COUNT;i++) {
			//init the grid counts 
			gbConstraints.gridy++;
			gbConstraints.gridx=0;
			//add the fingerID
			this.setDefaultsFingerPanel(jlTitleFingers[i]);
			rightPanel.add(jlTitleFingers[i], gbConstraints);
			gbConstraints.gridx++;
			// Tip Position 
			this.setDefaultsFingerPanel(jlTipPos[i]);
			rightPanel.add(jlTipPos[i], gbConstraints);
			gbConstraints.gridx++;
			// Finger Width
			this.setDefaultsFingerPanel(jlFingerWidth[i]);
			rightPanel.add(jlFingerWidth[i], gbConstraints);
			gbConstraints.gridx++;
			// finger Velocity
			this.setDefaultsFingerPanel(jlFingerVelocity[i]);
			rightPanel.add(jlFingerVelocity[i], gbConstraints);
			gbConstraints.gridx++;
			// Finger Direction
			this.setDefaultsFingerPanel(jlFingerDirection[i]);
			rightPanel.add(jlFingerDirection[i], gbConstraints);
			gbConstraints.gridx++;
			// Finger Length
			this.setDefaultsFingerPanel(jlFingerLength[i]);
			rightPanel.add(jlFingerLength[i], gbConstraints);
		}

		return rightPanel;
	}
	
	/* TODO Create Panel for sliders to append to the superPanel
	 * FIXME: add comments 
	 * 
	 */
	private JPanel getConfigPanel()
	{
		JPanel config= new JPanel(new GridBagLayout());
		
		GridBagConstraints gbConstraints = new GridBagConstraints();
		gbConstraints.fill= GridBagConstraints.CENTER;
		gbConstraints.anchor=GridBagConstraints.CENTER;
		
		
		//JPanel leftPanel = new JPanel(new GridBagLayout());
		// JLabels - Titles
		jlTitleCircleMinRadius = new JLabel("Circle Min Radius");
		jlTitleCircleMinArc = new JLabel("Circle Min Arc");
		jlTitleSwipeMinLength = new JLabel("Swipe Min Length");	
		jlTitleSwipeMinVelocity = new JLabel("Swipe Min Velocity");
		jlTitleKeyTapMinDownVelocity = new JLabel("KeyTap Min DownVelocity");
		jlTitleKeyTapHistorySeconds = new JLabel("KeyTap HistorySeconds");
		jlTitleKeyTapMinDistance = new JLabel("KeyTap Min Distance");
		jlTitleScreenTapMinForwardVelocity= new JLabel("ScreenTap Min ForwardVelocity");
		jlTitleScreenTapHistorySeconds= new JLabel("ScreenTap HistorySeconds");
		 // jlTitleScreenTapMinDistance= new JLabel("ScreenTap MinDistance"); // unused
		// JLabels - Contents
		jlCircleMinRadius = new JTextField(" - ");
		jlCircleMinRadius.addActionListener(this); //
		jlCircleMinArc = new JTextField(" - ");
		jlCircleMinArc.addActionListener(this);
		jlSwipeMinLength = new JTextField(" - ");
		jlSwipeMinLength.addActionListener(this);
		jlSwipeMinVelocity = new JTextField(" - ");
		jlSwipeMinVelocity.addActionListener(this);
		jlKeyTapMinDownVelocity = new JTextField(" - ");
		jlKeyTapMinDownVelocity.addActionListener(this);
		jlKeyTapHistorySeconds = new JTextField(" - ");
		jlKeyTapHistorySeconds.addActionListener(this);
		jlKeyTapMinDistance = new JTextField(" - ");
		jlKeyTapMinDistance.addActionListener(this);
		jlScreenTapMinForwardVelocity= new JTextField(" - ");
		jlScreenTapMinForwardVelocity.addActionListener(this);
		jlScreenTapHistorySeconds= new JTextField(" - ");
		jlScreenTapHistorySeconds.addActionListener(this);
		jlScreenTapMinDistance= new JTextField(" - ");
		jlScreenTapMinDistance.addActionListener(this);
		
		addRow(jlTitleCircleMinRadius, jlCircleMinRadius, config, gbConstraints, false, true);
		addRow(jlTitleCircleMinArc, jlCircleMinArc, config, gbConstraints, false, true);
		addRow(jlTitleSwipeMinLength, jlSwipeMinLength, config, gbConstraints, false, true);
		addRow(jlTitleSwipeMinVelocity, jlSwipeMinVelocity, config, gbConstraints, false, true);
		addRow(jlTitleKeyTapMinDownVelocity, jlKeyTapMinDownVelocity, config, gbConstraints, false, true);
		addRow(jlTitleKeyTapHistorySeconds, jlKeyTapHistorySeconds, config, gbConstraints, false, true);
		addRow(jlTitleKeyTapMinDistance, jlKeyTapMinDistance, config, gbConstraints, false, true);
		addRow(jlTitleScreenTapMinForwardVelocity, jlScreenTapMinForwardVelocity, config, gbConstraints, false, true);
		addRow(jlTitleScreenTapHistorySeconds, jlScreenTapHistorySeconds, config, gbConstraints, false, true);
		return config;
	}
	
	/**
	 * The admin panel reflects the panel with calibration, mouseCursor trigger, graph button, and each game launcher, as well as right click and stop gesture toggles
	 * This panel is used to trigger any of the above actions such as starting a calibration event.
	 * @return
	 */
	private JPanel getAdminPanel()
	{
		JPanel adminPanel= new JPanel(new GridBagLayout());
		Border blackline = BorderFactory.createLineBorder(Color.black);
		adminPanel.setBorder(blackline);

		GridBagConstraints gbConstraints = new GridBagConstraints();
		//gbConstraints.fill= GridBagConstraints.HORIZONTAL;
		gbConstraints.anchor = GridBagConstraints.WEST;
		gbConstraints.ipadx = 10;
		// space between widgets
		gbConstraints.insets = new Insets(4, 4, 2, 2);

		// distribute the extra space so the widgets are all equally distributed
		gbConstraints.weightx = 1.0;
		gbConstraints.weighty = 1.0;
		
		//== First Row  -- All the things we need to do BEFORE the Calibration
		// [Which Hand]
		handedness = new ButtonGroup();
		rightHand = new JRadioButton("Right Hand");
		rightHand.setSelected(true);
		leftHand = new JRadioButton("Left Hand");
		handedness.add(rightHand);
		handedness.add(leftHand);
		// [Left hand sign]
		gbConstraints.gridy=0;
		gbConstraints.gridx=0;
		adminPanel.add(leftHand, gbConstraints);
		// [Calibration button]
		btnShowCalibration = new JButton("Calibration");
		btnShowCalibration.addActionListener(this);
		gbConstraints.gridy=0;
		gbConstraints.gridx=1;
		gbConstraints.fill = GridBagConstraints.HORIZONTAL;
		adminPanel.add(btnShowCalibration, gbConstraints);
		gbConstraints.fill = GridBagConstraints.NONE;
		// [Right hand sign]
		gbConstraints.gridy=0;
		gbConstraints.gridx=2;
		adminPanel.add(rightHand, gbConstraints);

		

		//== Second Row: Which Device
		deviceGroup = new ButtonGroup();
		mouse = new JRadioButton("Mouse");
		mouse.setSelected(true);
		touchpad= new JRadioButton("Touchpad");
		LOM = new JRadioButton("LOM");
		deviceGroup.add(mouse);
		deviceGroup.add(touchpad);
		deviceGroup.add(LOM);
		
		gbConstraints.gridy=1;
		gbConstraints.gridx=0;
		adminPanel.add(mouse, gbConstraints);
		gbConstraints.gridy=1;
		gbConstraints.gridx=1;
		adminPanel.add(touchpad, gbConstraints);
		gbConstraints.gridy=1;
		gbConstraints.gridx=2;
		adminPanel.add(LOM, gbConstraints);

		
		//== Thrid Row: experiment related
		
		// [Matrix Type]
		JLabel trackingLabel = new JLabel("Input type:");
		trackingType = new Choice();
		gbConstraints.gridy=2;
		gbConstraints.gridx=0;
		adminPanel.add(trackingLabel, gbConstraints);
		
		int i=0;
		for(Status status: MouseListener.Status.values())
		{
			trackingType.insert(status.toString(), i++);
		}
		trackingType.addItemListener(itemListener);
		gbConstraints.gridy=2;
		gbConstraints.gridx=1;
		adminPanel.add(trackingType, gbConstraints);
		
		// [Experiment Type]
		experimentConfig = new Choice();
		i=0;
		for(ExperimentNum num: ExpConfig.ExperimentNum.values())
		{
			experimentConfig.insert(num.toString(), i++);
		}
		experimentConfig.addItemListener(itemListener);
		gbConstraints.gridy=2;
		gbConstraints.gridx=2;
		adminPanel.add(experimentConfig, gbConstraints);
		
		// This line is all the administrative stuff that only US will use. 
		// Skip one, go to the 5th row
		btnShowGraph = new JButton("Show Graph");
		btnShowGraph.addActionListener(this);
		gbConstraints.gridy=4;
		gbConstraints.gridx=1;
		gbConstraints.gridwidth=4;
		adminPanel.add(btnShowGraph, gbConstraints);
		gbConstraints.gridwidth=1;

		leftClick = new Checkbox("LeftClick", false);
		leftClick.addItemListener(this);
		TouchPoints.setLeftClickEnabled(LEFT_CLICK_DEFAULT);
		rightClick = new Checkbox("RightClick", false);
		TouchPoints.setRightClickEnabled(RIGHT_CLICK_DEFAULT);
		rightClick.addItemListener(this);
		stopGesture = new Checkbox("Stop Gesture", false);
		TouchPoints.setStopGestureEnabled(STOP_GESTURE_DEFAULT);
		stopGesture.addItemListener(this);

		// sixth row: enable selection input stuff
		gbConstraints.gridy=5;
		gbConstraints.gridx=0;
		adminPanel.add(leftClick, gbConstraints);
		gbConstraints.gridy=5;
		gbConstraints.gridx=1;
		adminPanel.add(rightClick, gbConstraints);
		gbConstraints.gridy=5;
		gbConstraints.gridx=2;
		adminPanel.add(stopGesture, gbConstraints);

		//addRow(mouse, touchpad, LOM, selectionPanel, gbConstraints, false, true);

		// [LAUNCH EXPERIMENT]
		// this one spans multiple rows
		btnLaunchExperiment = new JButton("Launch Experiment");
		btnLaunchExperiment.setPreferredSize(new Dimension(200, 200));
		btnLaunchExperiment.addActionListener(this);
		gbConstraints.gridx=4;
		gbConstraints.gridy=0;
		// make the button span 5 rows, 
		gbConstraints.gridheight=6;
		gbConstraints.weightx = 5.0;
		gbConstraints.weighty = 5.0;
		gbConstraints.insets = new Insets(0, 10, 0, 0);

		adminPanel.add(btnLaunchExperiment, gbConstraints);
		//then make everything else back to one row
		gbConstraints.gridheight=1;

		//btnShowDndGame = new JButton("Launch DnD Game");
		//btnShowDndGame.addActionListener(this);
		//adminPanel.add(btnShowDndGame);


		return adminPanel;
		

	}
	
	/**
	 * creates and and returns the experimentForm to the master panel. The only item in this panel as of now is the participant's name which will be used to set a variable in TouchPoints to be appended our logging files.
	 * @return
	 */
	private JPanel getParticipantFormPanel()
	{
		JPanel participantForm = new JPanel(new FlowLayout());
		
		
		this.jlTitleParticipantName = new JLabel("Name");
		jlParticipantName = new JTextField();
		jlParticipantName.setPreferredSize(new Dimension(400, 30));
		jlParticipantName.addActionListener(this);
		
		participantForm.add(jlTitleParticipantName);
		participantForm.add(jlParticipantName);

		return participantForm;
	}
	
	private int getHandSelected()
	{
		if(rightHand.isSelected())
		{
			return RIGHT_HAND;
		}
		else
		{
			return LEFT_HAND;
		}
	}
	private String getHandSelectedString()
	{
		if(rightHand.isSelected())
		{
			return "RIGHT";
		}
		else
		{
			return "LEFT";
		}
	}
	private String getDeviceSelected()
	{
		if(mouse.isSelected())
		{
			return "mouse";
		}
		else if(touchpad.isSelected())
		{
			return "touchpad";
		}
		else
		{
			return "LOM";
		}
	}
	/**
	 * builds a string to be appended to log files
	 * @return
	 */
	private String getParticipantNameAppendString()
	{
		return jlParticipantName.getText().replaceAll("[^A-Za-z0-9]", "");//participantName
	}
	/*
	 * TODO catch exceptions for non float types
	 * FIXME: add comments in this case, what actions are dependent on this part? What actions are done here? 
	 */
	public void actionPerformed(ActionEvent evt) {
	    logger.trace(evt.toString());
	    if(evt.getSource() == btnShowCalibration) //calibrationButton was hit
	    {
	    	String logFileName = getParticipantNameAppendString()+"_"+getHandSelectedString();
	    	int calibrationHand = getHandSelected();
	    	if(calibrationPanel==null)
	    	{
	    		
	    		calibrationPanel= new Calibration(calibrationHand, logFileName);
	    	}
	    	else if(!calibrationPanel.isShowing()) 
	    	{
	    		calibrationPanel= new Calibration(calibrationHand, logFileName);
	    	}
	    }
	    else if(evt.getSource()==btnLaunchExperiment)
	    {
	    	String logFileName = getParticipantNameAppendString();
	    	ExperimentNum temp=null;
	    	for(ExperimentNum number : ExperimentNum.values())
	    	{
	    		if(number.toString()==experimentConfig.getSelectedItem().toString()){
	    			temp=number;
	    		}
	    	}
	    	logFileName+="_"+getHandSelectedString()+"_"+getDeviceSelected()+"_"+temp.toString();
	    	System.out.println(logFileName);
	    	if(sequentialHover==null)
	    	{
	    		sequentialHover = new SequentialHover(temp, logFileName);
	    	}
	    	else if(!sequentialHover.isShowing())
	    	{
	    		sequentialHover.dispose();
	    		sequentialHover = new SequentialHover(temp, logFileName);
	    	}
	    }
	    else if(evt.getSource() == btnShowDndGame)
	    {
		    SwingUtilities.invokeLater(new Runnable() {
		    	public void run() {
		    		new DragAndDrop();
		    	}});

	    }
	    else if(evt.getSource()==btnShowGraph)
	    {
	    	TouchPoints.startGraph();
	    }
	    else if(evt.getSource()==jlParticipantName) //assigns the name to the global variable in TouchPoints
	    {
	    	TouchPoints.setParticipantName(jlParticipantName.getText());
	    }
	    else
	    {
	    TouchPoints.setConfigValues( // FIXME: comments
	    		Float.parseFloat(jlCircleMinRadius.getText()), 
	    		Float.parseFloat(jlCircleMinArc.getText()), 
	    		Float.parseFloat(jlSwipeMinLength.getText()), 
	    		Float.parseFloat(jlSwipeMinVelocity.getText()), 
	    		Float.parseFloat(jlKeyTapMinDownVelocity.getText()),
	    		Float.parseFloat(jlKeyTapHistorySeconds.getText()), 
	    		Float.parseFloat(jlKeyTapMinDistance.getText()), 
	    		Float.parseFloat(jlScreenTapMinForwardVelocity.getText()), 
	    		Float.parseFloat(jlScreenTapHistorySeconds.getText()), 
	    		Float.parseFloat(jlScreenTapMinDistance.getText()));
	    }
	}

	/**
	 * Processing whenever the checkboxes are checked/unchecked
	 */
	@Override
	public void itemStateChanged(ItemEvent evt) {
		if(evt.getSource()==stopGesture)
    	{
    		logger.debug("Toggling Palmflip");
    		TouchPoints.setStopGestureEnabled(stopGesture.getState());
    	}
    	else if(evt.getSource()==rightClick)
    	{
    		logger.debug("Toggling RightClick");
    		TouchPoints.setRightClickEnabled(rightClick.getState());
    	}
    	else if(evt.getSource()==leftClick)
    	{
    		logger.debug("Toggling LeftClick");
    		TouchPoints.setLeftClickEnabled(leftClick.getState());
    	}
	}
	
	
	/**
	 * Inserts a box onto the screen where the legends are added
	 * because we need to mention the hotkeys
	 */
	private JPanel getLegendPanel() 
	{
		JPanel legendPanel = new JPanel(new GridBagLayout());

		GridBagConstraints gbConstraints = new GridBagConstraints();
		gbConstraints.fill= GridBagConstraints.HORIZONTAL;
		gbConstraints.anchor=GridBagConstraints.WEST;

		// fingers - array
		jlTitleTipPos = new JLabel("Tip");
		jlTitleFingerWidth = new JLabel("Width");
		jlTitleFingerVelocity = new JLabel("Velocity");
		jlTitleFingerDirection = new JLabel("Direction");
		jlTitleFingerLength = new JLabel("Length");

		JLabel jlTitleStartStopLog = new JLabel("Start / Stop [P]ositionLog");
		JLabel jlStartStopLog = new JLabel("CTRL + P");
		JLabel jlTitleWriteLog 	= new JLabel("Write log [O]ut to file");
		JLabel jlWriteLog = new JLabel("CTRL + O");
		JLabel jlTitleToggleMatrix = new JLabel("Toggle Matrix");
		JLabel jlToggleMatrix = new JLabel(" CTRL + <space>");

		
		addRow(jlTitleStartStopLog, jlStartStopLog, legendPanel, gbConstraints, false, true);
		addRow(jlTitleWriteLog, jlWriteLog, legendPanel, gbConstraints, false, true);
		addRow(jlTitleToggleMatrix, jlToggleMatrix, legendPanel, gbConstraints, false, true);

		
		return legendPanel;
	}
}
