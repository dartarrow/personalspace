package edu.baylor.hci.Experiment;

import java.awt.Color;
import java.awt.Cursor;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.GridLayout;
import java.awt.Image;
import java.awt.MouseInfo;
import java.awt.Point;
import java.awt.Toolkit;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.awt.event.MouseMotionListener;
import java.io.IOException;
import java.util.ArrayList;

import javax.imageio.ImageIO;
import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JComponent;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.SwingConstants;
import javax.swing.SwingUtilities;
import javax.swing.Timer;

import org.apache.log4j.Logger;

import edu.baylor.hci.Experiment.ConfigCell.Size;
import edu.baylor.hci.Experiment.ConfigCell.Type;
import edu.baylor.hci.Experiment.ExpConfig.ExperimentNum;

public class SequentialHoverView extends JPanel implements MouseListener, ActionListener, MouseMotionListener{
	/* Class Constants */
	// border color of the cells in the grid
	private static final Color CELL_BORDER_COLOR_RCLICK = Color.RED;
	private static final Color CELL_BORDER_COLOR_LCLICK = Color.BLUE;
	private static final Color CELL_BORDER_COLOR_HOVER = Color.WHITE;
	// border thickness 
	private static final int CELL_BORDER_THICKNESS = 5;
	// fonts used in the cells
	private static final Font CELL_FONT = new Font("Arial", Font.BOLD, 40);
	// background color of the cells in the grid
	private static final Color CELL_BACKGROUND_COLOR = Color.WHITE;
	// font color inside the cells
	private static final Color STATUS_FOREGROUND_COLOR = Color.BLACK;
	// cell color
	private static final Color STATUS_BACKGROUND_COLOR = Color.GRAY;
	// window color
	private static final Color WINDOW_BACKGROUND_COLOR = Color.BLACK;
	// how long need to hover
	private static final int HOVER_DURATION = 500;
	private static final int TIMER_INITIAL_DELAY = 0;
	// Should the grid lines be visible?
	private static final boolean GRID_VISIBILE = false;
	
	// prereq sUID
	private static final long serialVersionUID = 8051026090568327533L;
	
	private static final Logger logger = Logger.getLogger(SequentialHoverView.class);
	
	/** declare global variables used here. **/
	// control variable set true when cursor enters widget
	private boolean hasEntered;
	private long widgetEnterTime;
	private JLabel statusCell;
	private JComponent widgets[][];
	private JComponent panels[][];
	private SequentialHover controller;
	private int mouseEnterWidgetX, mouseEnterWidgetY;
	private int ticks;
	private JButton target; //button to Click
	private int targetWidgetRow;
	private int targetWidgetCol;
	private boolean finished=false;
	
	
	private ArrayList<ArrayList<ConfigCell>> cellConfig;
	private ArrayList<Cell> experimentOrder;
	private Timer timer;
	/**
	 * 
	 * @param controller
	 * @throws Exception 
	 */
	public SequentialHoverView(SequentialHover controller, ExperimentNum expNum) throws Exception 
	{	
		// set up cursor
        Toolkit toolkit = Toolkit.getDefaultToolkit();
        Image image;
		ClassLoader classLoader = Thread.currentThread().getContextClassLoader();
		try {
			image = ImageIO.read(classLoader.getResourceAsStream("crosshair.gif"));
			// windows requires the cursor to be a 32x32px image MAX. 
			// Here we place the hotspot right in the center of a crosshair
			Point hotspot = new Point(16, 16);
	        Cursor cursor = toolkit.createCustomCursor(image, hotspot, "cursor");
	        this.setCursor(cursor);
		} catch (IOException e) {
			logger.error(e.getMessage());
			e.printStackTrace();
		}
        
		this.setBackground(WINDOW_BACKGROUND_COLOR);
		timer= new Timer(HOVER_DURATION, this);
	    timer.setInitialDelay(TIMER_INITIAL_DELAY);
		
		this.controller=controller;
		
		widgets = new JButton[controller.getRowCount()][controller.getColCount()];
		panels = new JPanel[controller.getRowCount()][controller.getColCount()];
		
		try //this function may throw a diverse range of errors, see ExpConfig.getExperimentOrder() for more info
		{
			cellConfig=ExpConfig.getGrid(expNum);
			experimentOrder = ExpConfig.getExperimentOrder(expNum, cellConfig);
		}
		catch(Exception e)
		{
			e.printStackTrace();
			logger.fatal(e.getMessage());
			System.exit(0);
		}
		int numRows=cellConfig.size(), numCols=cellConfig.get(0).size();
		setSizeEnums();
		
		
		setLayout(new GridLayout(numRows, numCols));
		loadWidgetsFromConfig();
		addMouseListener(this);
		addMouseMotionListener(this);
	}
	/**
	 * for each value of the enum adds this name and value to arrayLists in the experiment logger to be written as a matlab scalar in the log. 
	 */
	private void setSizeEnums()
	{
		for(Size val : Size.values()) //adds the size enums as variable names and values to be writen as matlab variables
		{
			Dimension temp = ExpConfig.returnPreferredSize(val);
			controller.getExperimentLogger().addVariableNames(val.toString()+"_width");
			controller.getExperimentLogger().addVariableValues(temp.width);
			
			controller.getExperimentLogger().addVariableNames(val.toString()+"_height");
			controller.getExperimentLogger().addVariableValues(temp.height);
			
			controller.getExperimentLogger().addVariableNames(val.toString());
			controller.getExperimentLogger().addVariableValues(temp.height*temp.width);
		}
		int i=0;
		for(Type type : Type.values())
		{
			controller.getExperimentLogger().addVariableNames(type.toString());
			controller.getExperimentLogger().addVariableValues(i++);
		}
	}
	/**
	 * from the grid object (cellconfig) we iterate through all cells and call their respective configuration functions
	 */
	private void loadWidgetsFromConfig()
	{
		JButton temp=null;
		for(ArrayList<ConfigCell> row : cellConfig)
		{
			for(ConfigCell cell : row)
			{
				if(cell.type == Type.STATUSCELL)
				{
					configureStatusCell();
				}
				else
				{
					configureStandardButton(cell, temp);
				}
			}
		}
	}
	
	/**
	 * this needs to be changed, but right now when we have identified a statusCell type given the cellConfig
	 * configures the status cell to our static parameters, and adds it to the grid
	 */
	private void configureStatusCell() {
		statusCell = new JLabel("<html><h2>Status</h2></html>");
		statusCell.addMouseListener(new MouseListener() {
			@Override
			public void mouseEntered(MouseEvent arg0) { 
				controller.userReady = true; 
			}

			@Override
			public void mouseExited(MouseEvent arg0) { 
				controller.userReady = false;
			}

			// the other methods do nothing. NOTHING. 
		    @Override
		    public void mouseClicked(MouseEvent e) { /* nothing */ }

			@Override
			public void mousePressed(MouseEvent arg0) { /* nothing */ }

			@Override
			public void mouseReleased(MouseEvent arg0) { /* nothing */ }
			
		});

		/**
		 * Required, because the listener from JPanel is not inherited here.
		 * Redundant because code replicated multiple places.  
		 */
		statusCell.addMouseMotionListener(new MouseMotionListener() {
			@Override
			public void mouseMoved(MouseEvent e) {
				Point mouseCoords = MouseInfo.getPointerInfo().getLocation(); 
				controller.getMouseLogger().addMouseXY(mouseCoords.x, mouseCoords.y);
			}
			
			@Override
			public void mouseDragged(MouseEvent e) {
				// TODO Auto-generated method stub
				
			}
		});
		
		statusCell.setVisible(true);
		//statusCell.setEnabled(false);
		statusCell.setHorizontalAlignment(SwingConstants.CENTER);
		statusCell.setOpaque(true);
		statusCell.setForeground(STATUS_FOREGROUND_COLOR);
		statusCell.setBackground(STATUS_BACKGROUND_COLOR);
		add(statusCell);
	}
	/**
	 * configures a standard button given a grid cell and the button object
	 * sets colors, and size then adds the cell to our widgets(grid) and then adds the cell to a panel so we can resize easily (hacky)
	 * @param cell
	 * @param temp
	 */
	private void configureStandardButton(ConfigCell cell, JButton temp)
	{
		/* We use panels as a plceholder for the buttons. 
		 * This way we can set the buttons to be any size, and the actual grid is irrelevant from the size of the buttons. 
		 * Additionally, we use a gridbaglayout as a hackish way to make sure the buttons are in the center of their grid. 
		 */
		JPanel tmpanel = new JPanel();
		tmpanel.setBackground(WINDOW_BACKGROUND_COLOR);
        tmpanel.setLayout(new GridBagLayout());
        GridBagConstraints gbc = new GridBagConstraints();
		panels[cell.row][cell.col] = tmpanel;
		
		// add the panel to the grid
		this.add(panels[cell.row][cell.col]);
		
		// create the widget
		temp=new JButton("");
		temp.addMouseListener(this);
		temp.setBackground(Color.white);
		temp.setVisible(GRID_VISIBILE);
		temp.setBackground(CELL_BACKGROUND_COLOR);
		// set the cell border colors, different depending on type
		if(cell.type == Type.LEFT_CLICK)
		{
			temp.setBorder(BorderFactory.createLineBorder(CELL_BORDER_COLOR_LCLICK, CELL_BORDER_THICKNESS));
			temp.setForeground(CELL_BORDER_COLOR_LCLICK);
			temp.setFont(CELL_FONT);
			temp.setText("L");
		}
		else if(cell.type == Type.RIGHT_CLICK)
		{
			temp.setBorder(BorderFactory.createLineBorder(CELL_BORDER_COLOR_RCLICK, CELL_BORDER_THICKNESS));
			temp.setForeground(CELL_BORDER_COLOR_RCLICK);
			temp.setFont(CELL_FONT);
			temp.setText("R");
		}
		else if(cell.type == Type.HOVER)
		{
			temp.setBorder(BorderFactory.createLineBorder(CELL_BORDER_COLOR_HOVER, CELL_BORDER_THICKNESS));
		}
		
		temp.setPreferredSize(ExpConfig.returnPreferredSize(cell.size));
		widgets[cell.row][cell.col] = temp;

		panels[cell.row][cell.col].add(widgets[cell.row][cell.col], gbc);	
	}
	
	
	public void setStatusCell(int countDownInteger)
	{	
		if(statusCell!=null)
		{
			statusCell.setText("<html><h1>Game Starts: "+countDownInteger +"</h1> </html>");
		}
	}
	
	public void setStatusCell(int time, int score, int missclicks, int wrongAction)
	{
		if(statusCell!=null)
		{
			statusCell.setText(String.format("<html><h1>score:%d <br />time:%d <br/>misclicks:%d <br/> wrong action:%d</h1> </html>", score, time, missclicks, wrongAction));
		}
	}
	
	public void toggleCellVisibility(int row, int col)
	{
		targetWidgetRow=row;
		
		targetWidgetCol=col;
		target=(JButton) widgets[row][col];
		target.setVisible(!target.isVisible());
		
		/**
		 * Required, because the listener from JPanel is not inherited here.
		 * Redundant because code replicated multiple places.  
		 */
		target.addMouseMotionListener(new MouseMotionListener() {
			
			@Override
			public void mouseMoved(MouseEvent e) {
				Point mouseCoords = MouseInfo.getPointerInfo().getLocation(); 
				controller.getMouseLogger().addMouseXY(mouseCoords.x, mouseCoords.y);
			}
			
			@Override
			public void mouseDragged(MouseEvent e) { /** do nothing **/}
		});
	}
	
	/**Toggles the next cell to be made visible. removes the cell from the experiment order, and starts the distance calculation for moving to the next cell.
	 * mouselog.startCalcDistance() being called here
	 */
	public void toggleExperimentOrderCell()
	{	
		if(experimentOrder.size()>0)
		{
			controller.getMouseLogger().startCalcDistance();
			toggleCellVisibility(experimentOrder.get(0).row, experimentOrder.get(0).col);
			experimentOrder.remove(0);

			// do the logging
			Point mousePos = MouseInfo.getPointerInfo().getLocation();
			controller.getExperimentLogger().addWidgetAppearPosXY(mousePos.x, mousePos.y);
		}		
	}
	/**
	 * stops the view timer, sets any targets to invisible, and writes the experiment and Mouse Loggers
	 */
	public void stopViewAndWriteLogs()
	{
		timer.stop();
		// Check for null, which means there's no button set yet
		// Happens if try to his escape *BEFORE* game starts
		if(target !=null) 
		{
			target.setVisible(false);
			target.setEnabled(false);
		}
	}
	
	@Override
	public void mouseClicked(MouseEvent e) {
	}
	
	//min distance = mousePos@enter -mosPos@appear
	/**
	 * This function is called whenever the user enters the button to be clicked area. If it is the first time the user has hovered over this button we 
	 * log the time for hoverTime later.
	 */
	@Override
	public void mouseEntered(MouseEvent e) {
		if(e.getSource() instanceof JButton && !hasEntered) 
		{	
			hasEntered=true;
			if(cellConfig.get(targetWidgetRow).get(targetWidgetCol).type==Type.HOVER) //if the grid cell specifies a hover start timer
			{
				timer.restart(); //start timer now that we are hovering
			}
			
			// set the values for mouseEnter. Will be used if successful trial completion
			Point mousePos = MouseInfo.getPointerInfo().getLocation();
		    mouseEnterWidgetX = mousePos.x;
		    mouseEnterWidgetY = mousePos.y;
			widgetEnterTime=System.currentTimeMillis();

		}
	}

	/**
	 * upon an exit restart timer and reset ticks (timer bound)
	 * set hasEntered to false
	 */
	@Override
	public void mouseExited(MouseEvent e) {
		if(e.getSource() instanceof JButton && hasEntered)
		{
			ticks=0;
			hasEntered=!hasEntered;
			if(cellConfig.get(targetWidgetRow).get(targetWidgetCol).type==Type.HOVER)
			{
				timer.stop();
			}
		} 
	}
	/**
	 * Whenever the targetDisappears, whether it be from a click or a hover, we obtain the time, add hoverTime, hoverPos to the expLog, we stop the distance calculation
	 * Increment the score on the statusCell, and check if the game needs to be stopped if there are no more entries in the experimentOrder arrayList(stopping condition).
	 * If there are still entries we set the clickButton(button just pressed) to not visible, and toggle the next cell, and reset hasEntered to false
	 */
	private void targetDisappear()
	{
		// increase the score by one. 
		controller.incrementScore(); 
		// send values for the experiment logger
		Point mouseCoords = MouseInfo.getPointerInfo().getLocation(); // 
		controller.getExperimentLogger().addWidgetEnterPosXY(mouseEnterWidgetX, mouseEnterWidgetY, widgetEnterTime); //adds the hoverEnter point to the log
		controller.getExperimentLogger().addWidgetDisappearPosXY(mouseCoords.x, mouseCoords.y);
		controller.getExperimentLogger().addSizePerTrial(cellConfig.get(targetWidgetRow).get(targetWidgetCol).size.toString());
		controller.getExperimentLogger().addTypePerTrial(cellConfig.get(targetWidgetRow).get(targetWidgetCol).type.toString());

		controller.getMouseLogger().endCalcDistance(); //stops the distance calculation and logs the total
		
		if(experimentOrder.size()<1) //if true stop the game
		{	
			controller.stopGame();
			finished=true;
		}
		
		target.setVisible(false);
		toggleExperimentOrderCell();
		hasEntered=false;
	}
	/**
	 * If the button is a of click type, and the user has performed the proper click(Right, Left) then we call targetDisppear() to do the corresponding actions for destroying
	 * the old target and selecting a new one or stopping the game if no more targets are left in the expOrder
	 */
	
	@Override
	public void mousePressed(MouseEvent e) {
		if(e.getSource()==target)
		{
			// if user does the correct action on the button then do this.. 
			if((cellConfig.get(targetWidgetRow).get(targetWidgetCol).type==Type.LEFT_CLICK && !finished && SwingUtilities.isLeftMouseButton(e) )||
			   (cellConfig.get(targetWidgetRow).get(targetWidgetCol).type==Type.RIGHT_CLICK && !finished && SwingUtilities.isRightMouseButton(e) )	)
			{
				targetDisappear(); //when button is clicked call this				
			}
			else // if incorrect action on the button then increment the wrongClickAction counter and log coords 
			{
				controller.incrementWrongAction();
				// get the current coords of the mouse
				Point mouseCoords = MouseInfo.getPointerInfo().getLocation(); 
				controller.getExperimentLogger().addWrongActionPosXY(mouseCoords.x, mouseCoords.y);
			}
		}
		else
		{ // if click on anywhere that ISN'T a button, then increment the missClick counter and log coords
			controller.incrementMissclick();
			// get the current coords of the mouse
			Point mouseCoords = MouseInfo.getPointerInfo().getLocation(); 
			controller.getExperimentLogger().addMissclickPosXY(mouseCoords.x, mouseCoords.y);
		}
	}
	@Override
	public void mouseReleased(MouseEvent e) {
	}
	
	/**
	 * For hover cells only this function is called from our timer
	 * If the ticks is >0 then we know that we have hovered for 1 second because we restart the timer whenever we enter the button(hover).
	 * 		add hoverTime, and enterX, enterY, CALLS MOUSELOG.ENDCALCDISTANCE(), and calls toggleExperimentOrderCell() if the game is not over which in turn calls MOUSELOG.STARTCALCDISTANCE()
	 *  	increments the score and sets hasEntered to false for next hover
	 * else we incremment ticks
	 * 
	 */
	@Override
	public void actionPerformed(ActionEvent arg0) {
		if(cellConfig.get(targetWidgetRow).get(targetWidgetCol).type==Type.HOVER && !finished)
		{
			if(ticks==1) //when ticks ==1 this means we have hovered for 1 timer tick currently (HOVER_INTERVAL or 500ms =>.5 sec)
			{
				targetDisappear();
				timer.stop(); //stop timer until next hover
				ticks=0; //reset timer ticks to 0
			}
			else //timer starts and runs actionPerformed immediatley, so we don't want to log anything
			{
				ticks++;
			}
		}
	}
	@Override
	public void mouseDragged(MouseEvent e) {
	}
	

	/**
	 * With every move of the cursor, send the coordinates to the mouse log
	 */
	@Override
	public void mouseMoved(MouseEvent e) {
		Point mouseCoords = MouseInfo.getPointerInfo().getLocation(); 
		controller.getMouseLogger().addMouseXY(mouseCoords.x, mouseCoords.y);
	}
	
}
