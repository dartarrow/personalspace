package edu.baylor.hci.Calibration;

import java.awt.Color;
import java.awt.Font;
import java.awt.GridLayout;

import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.SwingConstants;

/**
 * This class shows the calibration view. 
 * NOTE: we get better calibration (and gesture navigation) when the hands follow the eyes
 * So we force the eyes to go to the screen corners by setting the widgets as small 
 *  as possible in the corners we want them to look at
 * 
 * @author Alvin
 */

public class CalibrationView extends JPanel {
	/** Class Constants **/
	private static final long serialVersionUID = 4589925884327869254L;
	// Vertical gaps/padding between widgets
	private static final int HORIZONTAL_GAP = 800;
	// Horizontal gaps/padding between widgets
	private static final int VERTICAL_GAP = 400;
	// default the buttons to some nicer color
	private static final Color BUTTON_BG_COLOR = Color.white;
	// and the fonts to something else
	private static final Color BUTTON_FG_COLOR = Color.black;
	// fonts for the buttons placd on the 4 corners
	private static final Font BUTTON_FONT = new Font(Font.SANS_SERIF, Font.PLAIN, 40);
	// The rest of the panel set the black. Makes things easier to view
	private static final Color PANEL_BG_COLOR  = Color.black;
	
	// Any widget would work here really doesn't *have* to be a button 
	private JButton btnTopLeft;
	private JButton btnBotLeft;
	private JButton btnTopRight;
	private JButton btnBotRight;
	
	// holds the messages we send to the user during calibration
	private JLabel statusCell;
	
	public enum CalibrationButtons 
	{
		TOP_RIGHT,
		TOP_LEFT,
		BOT_LEFT,
		BOT_RIGHT,
		DISABLE,
	}
	public CalibrationButtons activeButton;
	
	public CalibrationView()
	{
		//Set the spaces this way to that the buttons will actually be at the corners
		setLayout(new GridLayout(3, 3, HORIZONTAL_GAP, VERTICAL_GAP));
		// declare all our needed widgets
		btnTopLeft = new JButton("Top Left");
		btnBotLeft = new JButton("Bottom Left");
		btnTopRight = new JButton("Top Right");
		btnBotRight = new JButton("Bottom Right");

		// set the colors to something prettier
		btnTopRight.setBackground(BUTTON_BG_COLOR);
		btnTopLeft.setBackground(BUTTON_BG_COLOR);
		btnBotLeft.setBackground(BUTTON_BG_COLOR);
		btnBotRight.setBackground(BUTTON_BG_COLOR);
		// set the font colors to a contrast
		btnTopRight.setForeground(BUTTON_FG_COLOR);
		btnTopLeft.setForeground(BUTTON_FG_COLOR);
		btnBotLeft.setForeground(BUTTON_FG_COLOR);
		btnBotRight.setForeground(BUTTON_FG_COLOR);
		
		
		btnTopRight.setFont(BUTTON_FONT);
		btnTopLeft.setFont(BUTTON_FONT);
		btnBotLeft.setFont(BUTTON_FONT);
		btnBotRight.setFont(BUTTON_FONT);
		
		// set the view bg and text colors
		setBackground(PANEL_BG_COLOR);
		//statusCell.setForeground(PANEL_FG_COLOR);
		
		statusCell = new JLabel();
		
		// set all the buttons to invisible for now. 
		hideAllButtons();
		
		/* add widgets to the grid. 
		 * The grid is 3x3, we only use the corners(calibration prompts) and the center(status cell)
		 * The rest will need some widget anyway, just set it to some empty jlabel  
		 */
		add(btnTopLeft);
		add(new JLabel(""));
		add(btnTopRight);
		add(new JLabel(""));
		add(statusCell);
		add(new JLabel(""));
		add(btnBotLeft);
		add(new JLabel(""));
		add(btnBotRight);

		// set a message that we're ready
		setStatusCell("Calibration will be starting shortly.");
		
	}
	
	/**
	 * The status cell is in the center of the screen. 
	 * Will need to be extra friendly - seen almost exclusively by users
	 * @param message
	 */
	public void setStatusCell(String message)
	{
		this.statusCell.setText(String.format("<html><h1 style='text-align:center' color='white'> %s </h1></html>", message));
		this.statusCell.setHorizontalAlignment(SwingConstants.CENTER);
	}
	
	/**
	 * Show the 4 calibration points based on input  
	 * @param button
	 */
	public void toggleVisibility(CalibrationButtons button)
	{
		if(button==CalibrationButtons.TOP_LEFT)
		{
			hideAllButtons();
			btnTopLeft.setVisible(true);
			
		}
		else if(button==CalibrationButtons.TOP_RIGHT)
		{
			hideAllButtons();
			btnTopRight.setVisible(true);
		}
		else if(button ==CalibrationButtons.BOT_LEFT)
		{
			hideAllButtons();
			btnBotLeft.setVisible(true);
		}
		else if(button ==CalibrationButtons.BOT_RIGHT)
		{
			hideAllButtons();
			btnBotRight.setVisible(true);
		}
		else if(button == CalibrationButtons.DISABLE)
		{
			hideAllButtons();
		}
	}
	
	private void hideAllButtons()
	{
		btnTopLeft.setVisible(false);
		btnTopRight.setVisible(false);
		btnBotLeft.setVisible(false);
		btnBotRight.setVisible(false);
	}
}
