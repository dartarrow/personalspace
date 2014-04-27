package edu.baylor.hci.Experiment;

import java.awt.Dimension;
import java.util.ArrayList;

import edu.baylor.hci.Experiment.ConfigCell.Size;
import edu.baylor.hci.Experiment.ConfigCell.Type;


/**
 * basic cell class for construction of the experimental ordering, or to be inherited by ConfigCell
 * @author guinness
 *
 */
class Cell
{
	public int row, col;
	public Cell(int row, int col)
	{
		this.row=row;
		this.col=col;
	}
}

/**
 * This class is used to create a gridCell object containing row, col, type, and size of a cell within the grid.
 * @author guinness
 *
 */
class ConfigCell extends Cell
{
	public int row, col;
	public Type type;
	public Size size;
	public enum Type { NONE, LEFT_CLICK, RIGHT_CLICK, HOVER, STATUSCELL }
	public enum Size { SMALL, MEDIUM, LARGE, XLARGE}
	public ConfigCell(int row, int col, Type type, Size size){
		super(row, col);
		this.row=row;
		this.col=col;
		this.type=type;
		this.size=size;
	}
}
 
public class ExpConfig {
	
	/** Class Constants **/
	// the dimensions are for square cells, so only one side is defined
	private static final int CELL_DIMENSION_SMALL = 90;
	private static final int CELL_DIMENSION_MEDIUM = 120;
	private static final int CELL_DIMENSION_LARGE = 160;
	private static final int CELL_DIMENSION_XLARGE = 220;
	
	public enum ExperimentNum {
		// BLAH (<grid>, <order>)
		TEST_CLICK(2, 0), // use EO-2 for debug because the status cell is smack in center
		TEST_HOVER(6, 0), // Same with E0-2, except this is a hover
		TEST_HYBRID(10, 0), // Same with E0-2, except this is a hybrid
		CLICK_ONE(1, 1), 
		CLICK_TWO(2, 2), 
		CLICK_THREE(3, 3), 
		CLICK_FOUR(4, 4),
		HOVER_ONE(5, 1), 
		HOVER_TWO(6, 2), 
		HOVER_THREE(7, 3), 
		HOVER_FOUR(8, 4), 
		HYBRID_ONE(9, 1),
		HYBRID_TWO(10, 2),
		;

		
		private int experimentOrder;
		private int gridNumber;
			
		ExperimentNum(int gridNum,int expOrder )
		{
			this.experimentOrder = expOrder;
			this.gridNumber = gridNum;
		}
		
		public int getExperimentOrder() {
			return experimentOrder;
		}
		
		public int getGridNumber() {
			return gridNumber;
		}

	}
	
	
	/**
	 * grid holds the configuration options of each button, experimentOrder simple holds the order in which they will be toggled
	 */
	
	/**
	 * Definition of Dimensions. Used by the HoverView to populate the widgets. 
	 * Maps to the Size ENUM.
	 * @param size
	 * @return Dimension based on size
	 */
	public static Dimension returnPreferredSize(ConfigCell.Size size)
	{
		Dimension temp;
		switch(size)
		{
		case SMALL:
			temp= new Dimension(CELL_DIMENSION_SMALL, CELL_DIMENSION_SMALL);
			break;
		case MEDIUM:
			temp = new Dimension (CELL_DIMENSION_MEDIUM, CELL_DIMENSION_MEDIUM);
			break;
		case LARGE:
			temp = new Dimension(CELL_DIMENSION_LARGE, CELL_DIMENSION_LARGE);
		break;
		case XLARGE:
			temp = new Dimension(CELL_DIMENSION_XLARGE, CELL_DIMENSION_XLARGE);
		break;
		default:
			temp = new Dimension(CELL_DIMENSION_XLARGE, CELL_DIMENSION_XLARGE);
			System.out.println("Unrecognized Dimension");
			break;
		}
		return temp;
	}	
	
	/**
	 * takes an integer corresponding to the experimentOrder that the function will return
	 * We have 4 Experiment Orders, and one for debug. 
	 * 
	 * 0 => Debug Order, 
	 * 1 => first experiment order, 
	 * 2 => second experiment order, 
	 * 3 => third experiment order, 
	 * 4 => fourth experiment order
	 * 
	 * @param order
	 * @return ArrayList<Cell> containing Experiment Orders
	 * @throws Exception 
	 */
	public static ArrayList<Cell> getExperimentOrder(ExperimentNum num, ArrayList<ArrayList<ConfigCell>> grid) throws Exception
	{
		ArrayList<Cell> experimentOrder; 

		switch (num.experimentOrder)	{
		case 0:
			experimentOrder = getDebugExperimentOrder();
			break;
		case 1:
			experimentOrder = getProductionExperimentOrder1();
			break;
		case 2:
			experimentOrder = getProductionExperimentOrder2();
			break;
		case 3:
			experimentOrder = getProductionExperimentOrder3();
			break;
		case 4:
			experimentOrder = getProductionExperimentOrder4();
			break;
		default:
			throw new Exception(num.experimentOrder+" order not found");
		}
		//compare grid to experiment order and report any errors (index out of bound errors, status cell being called)
		compareGridToOrder(grid, experimentOrder);
		return experimentOrder;
	}
	
	/**
	 * This function takes in the grid (2D array of ConfigCells) and the experimentOrder to check for compatibility issues.
	 * Issues include gridBounds < orderBounds for rows and cols, and the experimentOrder containing the index associated with the StatusCell (shouldn't be toggled)
	 * 
	 * Once an issue is found we will throw an exception which will be caught in the view class. 
	 * 
	 * 
	 * @FIXME add orderBounds< gridBounds (meaning if a row of the order(say -1 or 0) is less than the boundary to gridBounds(say 1) return an error. 
	 * @FIXME add check for multiple statusCells as this will screw stuff up.
	 * Though this seems to not be as important, but these are relatively dynamic so we should do it anyways.
	 * @param grid
	 * @param order
	 * @throws Exception
	 */
	private static void compareGridToOrder(ArrayList<ArrayList<ConfigCell>> grid, ArrayList<Cell> order ) throws Exception
	{
		//find statusCell indices
		int statusRow=-1, statusCol=-1;
		int statusCellCnt=0;
		for(int row=0; row<grid.size(); row++)
		{
			for(int col=0; col<grid.get(row).size(); col++)
			{
				if(grid.get(row).get(col).type == Type.STATUSCELL)
				{
					statusRow=row;
					statusCol=col;
					statusCellCnt++;
				}
			}
		}
		if(statusRow ==-1 || statusCol ==-1)
		{
			throw new Exception("StatusCell not found");
		}
		else if(statusCellCnt>1)
		{
			throw new Exception("multiple StatusCells found, please correct your grid to allow only one.");
		}
		int orderColMin=-1, orderColMax=-1, orderRowMin=-1, orderRowMax=-1;
		String statusException="";
		int cellIndex=0;
		for(Cell cell: order)
		{
			if(cell.row>orderRowMax)
			{
				orderRowMax=cell.row;
			}
			if(cell.row<orderRowMin)
			{
				orderRowMin=cell.row;
			}
			if(cell.col>orderColMax)
			{
				orderColMax=cell.col;
			}
			if(cell.col<orderColMin)
			{
				orderColMin=cell.col;
			}
			if(cell.row == statusRow && cell.col==statusCol)
			{
				statusException+=cellIndex+" => ("+cell.row+", "+cell.col+") ";
			}
			cellIndex++;
		}
		if(statusException!="") //we have found atleast one instance of the statusCell being toggled, will list all instances.
		{
			throw new Exception("StatusCell is being toggled in experimentOrder "+statusException);
		}
		if(grid.size()-1<orderRowMax) // order has a larger order.row value than our grid (index out of bounds)
		{
			throw new Exception("GridRowMaximum:"+(grid.size()-1)+" orderRowMax:"+orderRowMax);
		}
		else if(grid.get(0).size()-1 <orderColMax) //this should be guaranteed uniform in the seqHover check, so we only need to compare against the first row's columns
		{
			throw new Exception("GridColMaximum:"+(grid.get(0).size()-1)+" orderColMax:"+orderColMax);
		}
	}
	/**
	 * At this moment there is only one grid so this function builds and returns the grid object.
	 * ArrayList<ArrayList<ConfigCell>> grid:
	 * outer array : all rows 
	 * inner array : columns within the corresponding row.
	 * within column(grid.get(0).get(0)) contains the following items:
	 * row : row within the grid
	 * col : col within the grid
	 * type : ToggleType i.e {NONE, LEFT_CLICK, RIGHT_CLICK, STATUSCELL} meaning what will toggle the Button (add hover?)
	 * size : size of the button within the grid { SMALL, MEDIUM, LARGE, XLARGE} these sizes are defined in seqHoverView.returnPreferredSize()
	 * @return
	 */
	public static ArrayList<ArrayList<ConfigCell>> getGrid(ExperimentNum num) throws Exception
	{
		
		ArrayList<ArrayList<ConfigCell>>grid;
		
		switch(num.gridNumber)
		{
		case 1:
			grid=getGridConfig1();
			break;
		case 2:
			grid=getGridConfig2();
			break;
		case 3:
			grid=getGridConfig3();
			break;
		case 4:
			grid=getGridConfig4();
			break;
		case 5:
			grid=getGridConfig5();
			break;
		case 6:
			grid=getGridConfig6();
			break;
		case 7:
			grid=getGridConfig7();
			break;
		case 8:
			grid=getGridConfig8();
			break;
		case 9:
			grid=getGridConfig9();
			break;
		case 10:
			grid=getGridConfig10();
			break;
		default:
			throw new Exception("Incorrect Grid number given (" + num.gridNumber +") ");
		}
		
		//this checks for uniform number of columns within the grid and logs an error if an inconsistency is found.
		int numCols=-1;
		for(ArrayList<ConfigCell> row : grid )
		{
			if(numCols ==-1)
			{
				numCols=row.size();
			}
			else if(numCols != row.size())
			{
				throw new Exception("Configuration Error number of columns do not match");
			}
		}
		return grid;
	}

	/** =================================================================================================== **/
	/** =================================================================================================== **/
	/** =========== [ HERE BE CONFIGS ] Which will almost definitely never ever change. Ever. ============= **/
	/** =================================================================================================== **/
	/** =================================================================================================== **/

	/**
	 * Grid configuration matches to EO-1 on Alvin's forms
	 * Uses CLICKs, half of the grids are Left clicks, and another half right clicks
	 * No guarantee of equality. Because not all the (77-1) here are actually used. 
	 * returns a grid which is a 2D-Arraylist 
	 *  Arrylist containing the rows which contains arraylist containing the widgets
	 * @returns grid in 2D
	 */
	private static ArrayList<ArrayList<ConfigCell>> getGridConfig1()
	{
		ArrayList<ArrayList<ConfigCell>> tmpGrid = new ArrayList<ArrayList<ConfigCell>>();
		ArrayList<ConfigCell> row;
		row = new ArrayList<ConfigCell>();
		/* [0] 0-10 */
		row.add(new ConfigCell(0, 0, Type.LEFT_CLICK, Size.XLARGE));
		row.add(new ConfigCell(0, 1, Type.LEFT_CLICK, Size.LARGE));
		row.add(new ConfigCell(0, 2, Type.RIGHT_CLICK, Size.MEDIUM));
		row.add(new ConfigCell(0, 3, Type.LEFT_CLICK, Size.SMALL));
		row.add(new ConfigCell(0, 4, Type.RIGHT_CLICK, Size.MEDIUM));
		row.add(new ConfigCell(0, 5, Type.LEFT_CLICK, Size.SMALL));
		row.add(new ConfigCell(0, 6, Type.LEFT_CLICK, Size.MEDIUM));
		row.add(new ConfigCell(0, 7, Type.RIGHT_CLICK, Size.XLARGE));
		row.add(new ConfigCell(0, 8, Type.LEFT_CLICK, Size.LARGE));
		row.add(new ConfigCell(0, 9, Type.RIGHT_CLICK, Size.MEDIUM));
		row.add(new ConfigCell(0, 10, Type.RIGHT_CLICK, Size.XLARGE));
		
		tmpGrid.add(row);
		row = new ArrayList<ConfigCell>();
		/* [1] 0-10 */
		row.add(new ConfigCell(1, 0, Type.LEFT_CLICK, Size.MEDIUM));
		row.add(new ConfigCell(1, 1, Type.RIGHT_CLICK, Size.LARGE));
		row.add(new ConfigCell(1, 2, Type.LEFT_CLICK, Size.XLARGE));
		row.add(new ConfigCell(1, 3, Type.RIGHT_CLICK, Size.MEDIUM));
		row.add(new ConfigCell(1, 4, Type.RIGHT_CLICK, Size.LARGE));
		row.add(new ConfigCell(1, 5, Type.LEFT_CLICK, Size.MEDIUM));
		row.add(new ConfigCell(1, 6, Type.RIGHT_CLICK, Size.LARGE));
		row.add(new ConfigCell(1, 7, Type.RIGHT_CLICK, Size.MEDIUM));
		row.add(new ConfigCell(1, 8, Type.LEFT_CLICK, Size.XLARGE));
		row.add(new ConfigCell(1, 9, Type.LEFT_CLICK, Size.SMALL));
		row.add(new ConfigCell(1, 10, Type.RIGHT_CLICK, Size.LARGE));
		
		tmpGrid.add(row);
		row = new ArrayList<ConfigCell>();
		/* [2] 0-10 */
		row.add(new ConfigCell(2, 0, Type.RIGHT_CLICK, Size.LARGE));
		row.add(new ConfigCell(2, 1, Type.LEFT_CLICK, Size.SMALL));
		row.add(new ConfigCell(2, 2, Type.RIGHT_CLICK, Size.LARGE));
		row.add(new ConfigCell(2, 3, Type.LEFT_CLICK, Size.SMALL));
		row.add(new ConfigCell(2, 4, Type.RIGHT_CLICK, Size.MEDIUM));
		row.add(new ConfigCell(2, 5, Type.STATUSCELL, Size.XLARGE)); // ** //
		row.add(new ConfigCell(2, 6, Type.RIGHT_CLICK, Size.LARGE));
		row.add(new ConfigCell(2, 7, Type.LEFT_CLICK, Size.SMALL));
		row.add(new ConfigCell(2, 8, Type.LEFT_CLICK, Size.LARGE));
		row.add(new ConfigCell(2, 9, Type.RIGHT_CLICK, Size.LARGE));
		row.add(new ConfigCell(2, 10, Type.LEFT_CLICK, Size.MEDIUM));
		
		tmpGrid.add(row);
		row = new ArrayList<ConfigCell>();
		/* [3] 0-10 */
		row.add(new ConfigCell(3, 0, Type.LEFT_CLICK, Size.MEDIUM));
		row.add(new ConfigCell(3, 1, Type.RIGHT_CLICK, Size.XLARGE));
		row.add(new ConfigCell(3, 2, Type.RIGHT_CLICK, Size.LARGE));
		row.add(new ConfigCell(3, 3, Type.LEFT_CLICK, Size.XLARGE));
		row.add(new ConfigCell(3, 4, Type.LEFT_CLICK, Size.LARGE));
		row.add(new ConfigCell(3, 5, Type.RIGHT_CLICK, Size.XLARGE));
		row.add(new ConfigCell(3, 6, Type.RIGHT_CLICK, Size.MEDIUM)); 
		row.add(new ConfigCell(3, 7, Type.LEFT_CLICK, Size.XLARGE));
		row.add(new ConfigCell(3, 8, Type.RIGHT_CLICK, Size.MEDIUM));
		row.add(new ConfigCell(3, 9, Type.RIGHT_CLICK, Size.XLARGE));
		row.add(new ConfigCell(3, 10, Type.LEFT_CLICK, Size.SMALL));
		
		tmpGrid.add(row);
		row=new ArrayList<ConfigCell>(); 
		/* [4] 0-10 */
		row.add(new ConfigCell(4, 0, Type.RIGHT_CLICK, Size.SMALL));
		row.add(new ConfigCell(4, 1, Type.LEFT_CLICK, Size.LARGE));
		row.add(new ConfigCell(4, 2, Type.LEFT_CLICK, Size.MEDIUM));
		row.add(new ConfigCell(4, 3, Type.RIGHT_CLICK, Size.SMALL));
		row.add(new ConfigCell(4, 4, Type.LEFT_CLICK, Size.LARGE));
		row.add(new ConfigCell(4, 5, Type.RIGHT_CLICK, Size.SMALL));
		row.add(new ConfigCell(4, 6, Type.LEFT_CLICK, Size.LARGE));
		row.add(new ConfigCell(4, 7, Type.RIGHT_CLICK, Size.SMALL));
		row.add(new ConfigCell(4, 8, Type.LEFT_CLICK, Size.LARGE));
		row.add(new ConfigCell(4, 9, Type.RIGHT_CLICK, Size.MEDIUM));
		row.add(new ConfigCell(4, 10, Type.LEFT_CLICK, Size.LARGE));
		
		tmpGrid.add(row);
		row = new ArrayList<ConfigCell>();
		/* [5] 0-10 */
		row.add(new ConfigCell(5, 0, Type.RIGHT_CLICK, Size.MEDIUM));
		row.add(new ConfigCell(5, 1, Type.LEFT_CLICK, Size.SMALL));
		row.add(new ConfigCell(5, 2, Type.RIGHT_CLICK, Size.XLARGE));
		row.add(new ConfigCell(5, 3, Type.LEFT_CLICK, Size.MEDIUM));
		row.add(new ConfigCell(5, 4, Type.LEFT_CLICK, Size.LARGE));
		row.add(new ConfigCell(5, 5, Type.RIGHT_CLICK, Size.MEDIUM));
		row.add(new ConfigCell(5, 6, Type.RIGHT_CLICK, Size.LARGE));
		row.add(new ConfigCell(5, 7, Type.LEFT_CLICK, Size.MEDIUM));
		row.add(new ConfigCell(5, 8, Type.LEFT_CLICK, Size.XLARGE));
		row.add(new ConfigCell(5, 9, Type.RIGHT_CLICK, Size.SMALL));
		row.add(new ConfigCell(5, 10, Type.RIGHT_CLICK, Size.MEDIUM));
		
		tmpGrid.add(row);
		row = new ArrayList<ConfigCell>();
		/* [6] 0-10 */
		row.add(new ConfigCell(6, 0, Type.LEFT_CLICK, Size.XLARGE));
		row.add(new ConfigCell(6, 1, Type.RIGHT_CLICK, Size.MEDIUM));
		row.add(new ConfigCell(6, 2, Type.RIGHT_CLICK, Size.SMALL));
		row.add(new ConfigCell(6, 3, Type.LEFT_CLICK, Size.MEDIUM));
		row.add(new ConfigCell(6, 4, Type.LEFT_CLICK, Size.LARGE));
		row.add(new ConfigCell(6, 5, Type.LEFT_CLICK, Size.SMALL));
		row.add(new ConfigCell(6, 6, Type.RIGHT_CLICK, Size.MEDIUM));
		row.add(new ConfigCell(6, 7, Type.RIGHT_CLICK, Size.XLARGE));
		row.add(new ConfigCell(6, 8, Type.LEFT_CLICK, Size.LARGE));
		row.add(new ConfigCell(6, 9, Type.RIGHT_CLICK, Size.MEDIUM));
		row.add(new ConfigCell(6, 10, Type.LEFT_CLICK, Size.XLARGE));
		
		tmpGrid.add(row);
		row = new ArrayList<ConfigCell>();
		return tmpGrid;
	}
	
	/**
	 * Grid configuration matches to EO-2 on Alvin's forms
	 * Uses CLICKs, half of the grids are Left clicks, and another half right clicks
	 * No guarantee of equality. Because not all the (77-1) here are actually used. 
	 * returns a grid which is a 2D-Arraylist 
	 *  Arrylist containing the rows which contains arraylist containing the widgets
	 * @returns grid in 2D
	 */
	private static ArrayList<ArrayList<ConfigCell>> getGridConfig2()
	{
		ArrayList<ArrayList<ConfigCell>> tmpGrid = new ArrayList<ArrayList<ConfigCell>>();
		ArrayList<ConfigCell> row;
		row = new ArrayList<ConfigCell>();
		/* [0] 0-10 */
		row.add(new ConfigCell(0, 0, Type.RIGHT_CLICK, Size.XLARGE));
		row.add(new ConfigCell(0, 1, Type.LEFT_CLICK, Size.XLARGE));
		row.add(new ConfigCell(0, 2, Type.RIGHT_CLICK, Size.SMALL));
		row.add(new ConfigCell(0, 3, Type.LEFT_CLICK, Size.MEDIUM));
		row.add(new ConfigCell(0, 4, Type.RIGHT_CLICK, Size.XLARGE));
		row.add(new ConfigCell(0, 5, Type.LEFT_CLICK, Size.SMALL));
		row.add(new ConfigCell(0, 6, Type.RIGHT_CLICK, Size.XLARGE));
		row.add(new ConfigCell(0, 7, Type.LEFT_CLICK, Size.MEDIUM));
		row.add(new ConfigCell(0, 8, Type.RIGHT_CLICK, Size.SMALL));
		row.add(new ConfigCell(0, 9, Type.LEFT_CLICK, Size.LARGE));
		row.add(new ConfigCell(0, 10, Type.LEFT_CLICK, Size.XLARGE));
		
		tmpGrid.add(row);
		row = new ArrayList<ConfigCell>();
		/* [1] 0-10 */
		row.add(new ConfigCell(1, 0, Type.LEFT_CLICK, Size.SMALL));
		row.add(new ConfigCell(1, 1, Type.RIGHT_CLICK, Size.LARGE));
		row.add(new ConfigCell(1, 2, Type.RIGHT_CLICK, Size.XLARGE));
		row.add(new ConfigCell(1, 3, Type.LEFT_CLICK, Size.SMALL));
		row.add(new ConfigCell(1, 4, Type.RIGHT_CLICK, Size.LARGE));
		row.add(new ConfigCell(1, 5, Type.RIGHT_CLICK, Size.XLARGE));
		row.add(new ConfigCell(1, 6, Type.LEFT_CLICK, Size.LARGE));
		row.add(new ConfigCell(1, 7, Type.RIGHT_CLICK, Size.SMALL));
		row.add(new ConfigCell(1, 8, Type.RIGHT_CLICK, Size.XLARGE));
		row.add(new ConfigCell(1, 9, Type.LEFT_CLICK, Size.MEDIUM));
		row.add(new ConfigCell(1, 10, Type.LEFT_CLICK, Size.SMALL));
		
		tmpGrid.add(row);
		row = new ArrayList<ConfigCell>();
		/* [2] 0-10 */
		row.add(new ConfigCell(2, 0, Type.LEFT_CLICK, Size.LARGE));
		row.add(new ConfigCell(2, 1, Type.RIGHT_CLICK, Size.MEDIUM));
		row.add(new ConfigCell(2, 2, Type.LEFT_CLICK, Size.MEDIUM));
		row.add(new ConfigCell(2, 3, Type.RIGHT_CLICK, Size.SMALL));
		row.add(new ConfigCell(2, 4, Type.LEFT_CLICK, Size.LARGE));
		row.add(new ConfigCell(2, 5, Type.LEFT_CLICK, Size.XLARGE));
		row.add(new ConfigCell(2, 6, Type.RIGHT_CLICK, Size.LARGE));
		row.add(new ConfigCell(2, 7, Type.RIGHT_CLICK, Size.SMALL));
		row.add(new ConfigCell(2, 8, Type.LEFT_CLICK, Size.MEDIUM));
		row.add(new ConfigCell(2, 9, Type.LEFT_CLICK, Size.LARGE));
		row.add(new ConfigCell(2, 10, Type.RIGHT_CLICK, Size.LARGE));
		
		tmpGrid.add(row);
		row = new ArrayList<ConfigCell>();
		/* [3] 0-10 */
		row.add(new ConfigCell(3, 0, Type.LEFT_CLICK, Size.MEDIUM));
		row.add(new ConfigCell(3, 1, Type.RIGHT_CLICK, Size.SMALL));
		row.add(new ConfigCell(3, 2, Type.RIGHT_CLICK, Size.LARGE));
		row.add(new ConfigCell(3, 3, Type.LEFT_CLICK, Size.XLARGE));
		row.add(new ConfigCell(3, 4, Type.RIGHT_CLICK, Size.MEDIUM));
		row.add(new ConfigCell(3, 5, Type.STATUSCELL, Size.XLARGE)); //**//
		row.add(new ConfigCell(3, 6, Type.RIGHT_CLICK, Size.MEDIUM)); 
		row.add(new ConfigCell(3, 7, Type.LEFT_CLICK, Size.XLARGE));
		row.add(new ConfigCell(3, 8, Type.LEFT_CLICK, Size.LARGE));
		row.add(new ConfigCell(3, 9, Type.RIGHT_CLICK, Size.SMALL));
		row.add(new ConfigCell(3, 10, Type.RIGHT_CLICK, Size.MEDIUM));
		
		tmpGrid.add(row);
		row=new ArrayList<ConfigCell>(); 
		/* [4] 0-10 */
		row.add(new ConfigCell(4, 0, Type.LEFT_CLICK, Size.LARGE));
		row.add(new ConfigCell(4, 1, Type.LEFT_CLICK, Size.MEDIUM));
		row.add(new ConfigCell(4, 2, Type.RIGHT_CLICK, Size.LARGE));
		row.add(new ConfigCell(4, 3, Type.RIGHT_CLICK, Size.XLARGE));
		row.add(new ConfigCell(4, 4, Type.RIGHT_CLICK, Size.MEDIUM));
		row.add(new ConfigCell(4, 5, Type.LEFT_CLICK, Size.LARGE));
		row.add(new ConfigCell(4, 6, Type.LEFT_CLICK, Size.MEDIUM));
		row.add(new ConfigCell(4, 7, Type.LEFT_CLICK, Size.XLARGE));
		row.add(new ConfigCell(4, 8, Type.RIGHT_CLICK, Size.LARGE));
		row.add(new ConfigCell(4, 9, Type.RIGHT_CLICK, Size.MEDIUM));
		row.add(new ConfigCell(4, 10, Type.LEFT_CLICK, Size.LARGE));
		
		tmpGrid.add(row);
		row = new ArrayList<ConfigCell>();
		/* [5] 0-10 */
		row.add(new ConfigCell(5, 0, Type.RIGHT_CLICK, Size.SMALL));
		row.add(new ConfigCell(5, 1, Type.LEFT_CLICK, Size.MEDIUM));
		row.add(new ConfigCell(5, 2, Type.RIGHT_CLICK, Size.MEDIUM));
		row.add(new ConfigCell(5, 3, Type.LEFT_CLICK, Size.LARGE));
		row.add(new ConfigCell(5, 4, Type.RIGHT_CLICK, Size.SMALL));
		row.add(new ConfigCell(5, 5, Type.LEFT_CLICK, Size.MEDIUM));
		row.add(new ConfigCell(5, 6, Type.LEFT_CLICK, Size.SMALL));
		row.add(new ConfigCell(5, 7, Type.RIGHT_CLICK, Size.LARGE));
		row.add(new ConfigCell(5, 8, Type.LEFT_CLICK, Size.MEDIUM));
		row.add(new ConfigCell(5, 9, Type.RIGHT_CLICK, Size.LARGE));
		row.add(new ConfigCell(5, 10, Type.LEFT_CLICK, Size.SMALL));
		
		tmpGrid.add(row);
		row = new ArrayList<ConfigCell>();
		/* [6] 0-10 */
		row.add(new ConfigCell(6, 0, Type.RIGHT_CLICK, Size.XLARGE));
		row.add(new ConfigCell(6, 1, Type.LEFT_CLICK, Size.XLARGE));
		row.add(new ConfigCell(6, 2, Type.RIGHT_CLICK, Size.MEDIUM));
		row.add(new ConfigCell(6, 3, Type.RIGHT_CLICK, Size.LARGE));
		row.add(new ConfigCell(6, 4, Type.RIGHT_CLICK, Size.SMALL));
		row.add(new ConfigCell(6, 5, Type.LEFT_CLICK, Size.MEDIUM));
		row.add(new ConfigCell(6, 6, Type.LEFT_CLICK, Size.SMALL));
		row.add(new ConfigCell(6, 7, Type.RIGHT_CLICK, Size.LARGE));
		row.add(new ConfigCell(6, 8, Type.LEFT_CLICK, Size.MEDIUM));
		row.add(new ConfigCell(6, 9, Type.RIGHT_CLICK, Size.XLARGE));
		row.add(new ConfigCell(6, 10, Type.LEFT_CLICK, Size.XLARGE));
		
		tmpGrid.add(row);
		row = new ArrayList<ConfigCell>();
		return tmpGrid;
	}

	/**
	 * Grid configuration matches to EO-3 on Alvin's forms
	 * Uses CLICKs, half of the grids are Left clicks, and another half right clicks
	 * No guarantee of equality. Because not all the (77-1) here are actually used. 
	 * returns a grid which is a 2D-Arraylist 
	 *  Arrylist containing the rows which contains arraylist containing the widgets
	 * @returns grid in 2D
	 */
	private static ArrayList<ArrayList<ConfigCell>> getGridConfig3()
	{
		ArrayList<ArrayList<ConfigCell>> tmpGrid = new ArrayList<ArrayList<ConfigCell>>();
		ArrayList<ConfigCell> row;
		row = new ArrayList<ConfigCell>();
		/* [0] 0-10 */
		row.add(new ConfigCell(0, 0, Type.LEFT_CLICK, Size.XLARGE));
		row.add(new ConfigCell(0, 1, Type.RIGHT_CLICK, Size.MEDIUM));
		row.add(new ConfigCell(0, 2, Type.LEFT_CLICK, Size.SMALL));
		row.add(new ConfigCell(0, 3, Type.RIGHT_CLICK, Size.MEDIUM));
		row.add(new ConfigCell(0, 4, Type.LEFT_CLICK, Size.XLARGE));
		row.add(new ConfigCell(0, 5, Type.LEFT_CLICK, Size.LARGE));
		row.add(new ConfigCell(0, 6, Type.RIGHT_CLICK, Size.XLARGE));
		row.add(new ConfigCell(0, 7, Type.LEFT_CLICK, Size.MEDIUM));
		row.add(new ConfigCell(0, 8, Type.RIGHT_CLICK, Size.SMALL));
		row.add(new ConfigCell(0, 9, Type.LEFT_CLICK, Size.MEDIUM));
		row.add(new ConfigCell(0, 10, Type.RIGHT_CLICK, Size.XLARGE));
		
		tmpGrid.add(row);
		row = new ArrayList<ConfigCell>();
		/* [1] 0-10 */
		row.add(new ConfigCell(1, 0, Type.LEFT_CLICK, Size.MEDIUM));
		row.add(new ConfigCell(1, 1, Type.RIGHT_CLICK, Size.LARGE));
		row.add(new ConfigCell(1, 2, Type.LEFT_CLICK, Size.MEDIUM));
		row.add(new ConfigCell(1, 3, Type.LEFT_CLICK, Size.SMALL));
		row.add(new ConfigCell(1, 4, Type.RIGHT_CLICK, Size.MEDIUM));
		row.add(new ConfigCell(1, 5, Type.STATUSCELL, Size.XLARGE)); // ** //
		row.add(new ConfigCell(1, 6, Type.RIGHT_CLICK, Size.MEDIUM));
		row.add(new ConfigCell(1, 7, Type.LEFT_CLICK, Size.SMALL));
		row.add(new ConfigCell(1, 8, Type.RIGHT_CLICK, Size.MEDIUM));
		row.add(new ConfigCell(1, 9, Type.LEFT_CLICK, Size.LARGE));
		row.add(new ConfigCell(1, 10, Type.RIGHT_CLICK, Size.MEDIUM));
		
		tmpGrid.add(row);
		row = new ArrayList<ConfigCell>();
		/* [2] 0-10 */
		row.add(new ConfigCell(2, 0, Type.LEFT_CLICK, Size.SMALL));
		row.add(new ConfigCell(2, 1, Type.RIGHT_CLICK, Size.LARGE));
		row.add(new ConfigCell(2, 2, Type.LEFT_CLICK, Size.LARGE));
		row.add(new ConfigCell(2, 3, Type.RIGHT_CLICK, Size.XLARGE));
		row.add(new ConfigCell(2, 4, Type.LEFT_CLICK, Size.SMALL));
		row.add(new ConfigCell(2, 5, Type.RIGHT_CLICK, Size.XLARGE));
		row.add(new ConfigCell(2, 6, Type.LEFT_CLICK, Size.XLARGE));
		row.add(new ConfigCell(2, 7, Type.RIGHT_CLICK, Size.XLARGE));
		row.add(new ConfigCell(2, 8, Type.LEFT_CLICK, Size.LARGE));
		row.add(new ConfigCell(2, 9, Type.RIGHT_CLICK, Size.MEDIUM));
		row.add(new ConfigCell(2, 10, Type.RIGHT_CLICK, Size.SMALL));
		
		tmpGrid.add(row);
		row = new ArrayList<ConfigCell>();
		/* [3] 0-10 */
		row.add(new ConfigCell(3, 0, Type.LEFT_CLICK, Size.LARGE));
		row.add(new ConfigCell(3, 1, Type.RIGHT_CLICK, Size.SMALL));
		row.add(new ConfigCell(3, 2, Type.RIGHT_CLICK, Size.XLARGE));
		row.add(new ConfigCell(3, 3, Type.LEFT_CLICK, Size.LARGE));
		row.add(new ConfigCell(3, 4, Type.LEFT_CLICK, Size.MEDIUM));
		row.add(new ConfigCell(3, 5, Type.RIGHT_CLICK, Size.SMALL)); 
		row.add(new ConfigCell(3, 6, Type.LEFT_CLICK, Size.MEDIUM)); 
		row.add(new ConfigCell(3, 7, Type.RIGHT_CLICK, Size.LARGE));
		row.add(new ConfigCell(3, 8, Type.LEFT_CLICK, Size.XLARGE));
		row.add(new ConfigCell(3, 9, Type.RIGHT_CLICK, Size.SMALL));
		row.add(new ConfigCell(3, 10, Type.LEFT_CLICK, Size.LARGE));
		
		tmpGrid.add(row);
		row=new ArrayList<ConfigCell>(); 
		/* [4] 0-10 */
		row.add(new ConfigCell(4, 0, Type.LEFT_CLICK, Size.SMALL));
		row.add(new ConfigCell(4, 1, Type.RIGHT_CLICK, Size.LARGE));
		row.add(new ConfigCell(4, 2, Type.RIGHT_CLICK, Size.LARGE));
		row.add(new ConfigCell(4, 3, Type.LEFT_CLICK, Size.XLARGE));
		row.add(new ConfigCell(4, 4, Type.RIGHT_CLICK, Size.XLARGE));
		row.add(new ConfigCell(4, 5, Type.LEFT_CLICK, Size.MEDIUM));
		row.add(new ConfigCell(4, 6, Type.RIGHT_CLICK, Size.XLARGE));
		row.add(new ConfigCell(4, 7, Type.LEFT_CLICK, Size.SMALL));
		row.add(new ConfigCell(4, 8, Type.LEFT_CLICK, Size.LARGE));
		row.add(new ConfigCell(4, 9, Type.RIGHT_CLICK, Size.MEDIUM));
		row.add(new ConfigCell(4, 10, Type.RIGHT_CLICK, Size.SMALL));
		
		tmpGrid.add(row);
		row = new ArrayList<ConfigCell>();
		/* [5] 0-10 */
		row.add(new ConfigCell(5, 0, Type.LEFT_CLICK, Size.MEDIUM));
		row.add(new ConfigCell(5, 1, Type.RIGHT_CLICK, Size.LARGE));
		row.add(new ConfigCell(5, 2, Type.LEFT_CLICK, Size.MEDIUM));
		row.add(new ConfigCell(5, 3, Type.LEFT_CLICK, Size.SMALL));
		row.add(new ConfigCell(5, 4, Type.RIGHT_CLICK, Size.LARGE));
		row.add(new ConfigCell(5, 5, Type.LEFT_CLICK, Size.XLARGE));
		row.add(new ConfigCell(5, 6, Type.LEFT_CLICK, Size.LARGE));
		row.add(new ConfigCell(5, 7, Type.RIGHT_CLICK, Size.SMALL));
		row.add(new ConfigCell(5, 8, Type.LEFT_CLICK, Size.MEDIUM));
		row.add(new ConfigCell(5, 9, Type.RIGHT_CLICK, Size.LARGE));
		row.add(new ConfigCell(5, 10, Type.RIGHT_CLICK, Size.MEDIUM));
		
		tmpGrid.add(row);
		row = new ArrayList<ConfigCell>();
		/* [6] 0-10 */
		row.add(new ConfigCell(6, 0, Type.LEFT_CLICK, Size.XLARGE));
		row.add(new ConfigCell(6, 1, Type.RIGHT_CLICK, Size.MEDIUM));
		row.add(new ConfigCell(6, 2, Type.LEFT_CLICK, Size.SMALL));
		row.add(new ConfigCell(6, 3, Type.LEFT_CLICK, Size.MEDIUM));
		row.add(new ConfigCell(6, 4, Type.RIGHT_CLICK, Size.XLARGE));
		row.add(new ConfigCell(6, 5, Type.RIGHT_CLICK, Size.LARGE));
		row.add(new ConfigCell(6, 6, Type.LEFT_CLICK, Size.XLARGE));
		row.add(new ConfigCell(6, 7, Type.RIGHT_CLICK, Size.MEDIUM));
		row.add(new ConfigCell(6, 8, Type.LEFT_CLICK, Size.SMALL));
		row.add(new ConfigCell(6, 9, Type.RIGHT_CLICK, Size.MEDIUM));
		row.add(new ConfigCell(6, 10, Type.RIGHT_CLICK, Size.XLARGE));
		
		tmpGrid.add(row);
		row = new ArrayList<ConfigCell>();
		return tmpGrid;
	}

	/**
	 * Grid configuration matches to EO-4 on Alvin's forms
	 * Uses CLICKs, half of the grids are Left clicks, and another half right clicks
	 * No guarantee of equality. Because not all the (77-1) here are actually used. 
	 * returns a grid which is a 2D-Arraylist 
	 *  Arrylist containing the rows which contains arraylist containing the widgets
	 * @returns grid in 2D
	 */
	private static ArrayList<ArrayList<ConfigCell>> getGridConfig4()
	{
		ArrayList<ArrayList<ConfigCell>> tmpGrid = new ArrayList<ArrayList<ConfigCell>>();
		ArrayList<ConfigCell> row;
		row = new ArrayList<ConfigCell>();
		/* [0] 0-10 */
		row.add(new ConfigCell(0, 0, Type.RIGHT_CLICK, Size.XLARGE));
		row.add(new ConfigCell(0, 1, Type.LEFT_CLICK, Size.SMALL));
		row.add(new ConfigCell(0, 2, Type.RIGHT_CLICK, Size.XLARGE));
		row.add(new ConfigCell(0, 3, Type.LEFT_CLICK, Size.LARGE));
		row.add(new ConfigCell(0, 4, Type.LEFT_CLICK, Size.MEDIUM));
		row.add(new ConfigCell(0, 5, Type.LEFT_CLICK, Size.SMALL));
		row.add(new ConfigCell(0, 6, Type.LEFT_CLICK, Size.LARGE));
		row.add(new ConfigCell(0, 7, Type.RIGHT_CLICK, Size.MEDIUM));
		row.add(new ConfigCell(0, 8, Type.RIGHT_CLICK, Size.XLARGE));
		row.add(new ConfigCell(0, 9, Type.LEFT_CLICK, Size.LARGE));
		row.add(new ConfigCell(0, 10, Type.RIGHT_CLICK, Size.XLARGE));
		
		tmpGrid.add(row);
		row = new ArrayList<ConfigCell>();
		/* [1] 0-10 */
		row.add(new ConfigCell(1, 0, Type.LEFT_CLICK, Size.LARGE));
		row.add(new ConfigCell(1, 1, Type.RIGHT_CLICK, Size.MEDIUM));
		row.add(new ConfigCell(1, 2, Type.RIGHT_CLICK, Size.SMALL));
		row.add(new ConfigCell(1, 3, Type.LEFT_CLICK, Size.LARGE));
		row.add(new ConfigCell(1, 4, Type.RIGHT_CLICK, Size.XLARGE));
		row.add(new ConfigCell(1, 5, Type.RIGHT_CLICK, Size.MEDIUM)); 
		row.add(new ConfigCell(1, 6, Type.LEFT_CLICK, Size.XLARGE));
		row.add(new ConfigCell(1, 7, Type.RIGHT_CLICK, Size.LARGE));
		row.add(new ConfigCell(1, 8, Type.LEFT_CLICK, Size.SMALL));
		row.add(new ConfigCell(1, 9, Type.LEFT_CLICK, Size.LARGE));
		row.add(new ConfigCell(1, 10, Type.RIGHT_CLICK, Size.LARGE));
		
		tmpGrid.add(row);
		row = new ArrayList<ConfigCell>();
		/* [2] 0-10 */
		row.add(new ConfigCell(2, 0, Type.RIGHT_CLICK, Size.MEDIUM));
		row.add(new ConfigCell(2, 1, Type.LEFT_CLICK, Size.XLARGE));
		row.add(new ConfigCell(2, 2, Type.RIGHT_CLICK, Size.MEDIUM));
		row.add(new ConfigCell(2, 3, Type.LEFT_CLICK, Size.SMALL));
		row.add(new ConfigCell(2, 4, Type.LEFT_CLICK, Size.MEDIUM));
		row.add(new ConfigCell(2, 5, Type.RIGHT_CLICK, Size.SMALL));
		row.add(new ConfigCell(2, 6, Type.LEFT_CLICK, Size.MEDIUM));
		row.add(new ConfigCell(2, 7, Type.RIGHT_CLICK, Size.XLARGE));
		row.add(new ConfigCell(2, 8, Type.LEFT_CLICK, Size.MEDIUM));
		row.add(new ConfigCell(2, 9, Type.RIGHT_CLICK, Size.LARGE));
		row.add(new ConfigCell(2, 10, Type.RIGHT_CLICK, Size.MEDIUM));
		
		tmpGrid.add(row);
		row = new ArrayList<ConfigCell>();
		/* [3] 0-10 */
		row.add(new ConfigCell(3, 0, Type.RIGHT_CLICK, Size.SMALL));
		row.add(new ConfigCell(3, 1, Type.LEFT_CLICK, Size.MEDIUM));
		row.add(new ConfigCell(3, 2, Type.RIGHT_CLICK, Size.LARGE));
		row.add(new ConfigCell(3, 3, Type.LEFT_CLICK, Size.MEDIUM));
		row.add(new ConfigCell(3, 4, Type.LEFT_CLICK, Size.LARGE));
		row.add(new ConfigCell(3, 5, Type.RIGHT_CLICK, Size.XLARGE)); 
		row.add(new ConfigCell(3, 6, Type.LEFT_CLICK, Size.LARGE)); 
		row.add(new ConfigCell(3, 7, Type.LEFT_CLICK, Size.MEDIUM));
		row.add(new ConfigCell(3, 8, Type.RIGHT_CLICK, Size.LARGE));
		row.add(new ConfigCell(3, 9, Type.LEFT_CLICK, Size.MEDIUM));
		row.add(new ConfigCell(3, 10, Type.RIGHT_CLICK, Size.SMALL));
		
		tmpGrid.add(row);
		row=new ArrayList<ConfigCell>(); 
		/* [4] 0-10 */
		row.add(new ConfigCell(4, 0, Type.RIGHT_CLICK, Size.XLARGE));
		row.add(new ConfigCell(4, 1, Type.LEFT_CLICK, Size.LARGE));
		row.add(new ConfigCell(4, 2, Type.LEFT_CLICK, Size.SMALL));
		row.add(new ConfigCell(4, 3, Type.RIGHT_CLICK, Size.SMALL));
		row.add(new ConfigCell(4, 4, Type.LEFT_CLICK, Size.MEDIUM));
		row.add(new ConfigCell(4, 5, Type.STATUSCELL, Size.XLARGE)); // ** //
		row.add(new ConfigCell(4, 6, Type.RIGHT_CLICK, Size.MEDIUM));
		row.add(new ConfigCell(4, 7, Type.LEFT_CLICK, Size.SMALL));
		row.add(new ConfigCell(4, 8, Type.LEFT_CLICK, Size.LARGE));
		row.add(new ConfigCell(4, 9, Type.RIGHT_CLICK, Size.MEDIUM));
		row.add(new ConfigCell(4, 10, Type.RIGHT_CLICK, Size.XLARGE));
		
		tmpGrid.add(row);
		row = new ArrayList<ConfigCell>();
		/* [5] 0-10 */
		row.add(new ConfigCell(5, 0, Type.RIGHT_CLICK, Size.LARGE));
		row.add(new ConfigCell(5, 1, Type.LEFT_CLICK, Size.SMALL));
		row.add(new ConfigCell(5, 2, Type.LEFT_CLICK, Size.XLARGE));
		row.add(new ConfigCell(5, 3, Type.RIGHT_CLICK, Size.MEDIUM));
		row.add(new ConfigCell(5, 4, Type.RIGHT_CLICK, Size.MEDIUM));
		row.add(new ConfigCell(5, 5, Type.LEFT_CLICK, Size.SMALL));
		row.add(new ConfigCell(5, 6, Type.RIGHT_CLICK, Size.LARGE));
		row.add(new ConfigCell(5, 7, Type.LEFT_CLICK, Size.MEDIUM));
		row.add(new ConfigCell(5, 8, Type.LEFT_CLICK, Size.XLARGE));
		row.add(new ConfigCell(5, 9, Type.RIGHT_CLICK, Size.SMALL));
		row.add(new ConfigCell(5, 10, Type.RIGHT_CLICK, Size.LARGE));
		
		tmpGrid.add(row);
		row = new ArrayList<ConfigCell>();
		/* [6] 0-10 */
		row.add(new ConfigCell(6, 0, Type.LEFT_CLICK, Size.XLARGE));
		row.add(new ConfigCell(6, 1, Type.RIGHT_CLICK, Size.MEDIUM));
		row.add(new ConfigCell(6, 2, Type.LEFT_CLICK, Size.LARGE));
		row.add(new ConfigCell(6, 3, Type.RIGHT_CLICK, Size.MEDIUM));
		row.add(new ConfigCell(6, 4, Type.RIGHT_CLICK, Size.LARGE));
		row.add(new ConfigCell(6, 5, Type.RIGHT_CLICK, Size.SMALL));
		row.add(new ConfigCell(6, 6, Type.LEFT_CLICK, Size.LARGE));
		row.add(new ConfigCell(6, 7, Type.LEFT_CLICK, Size.MEDIUM));
		row.add(new ConfigCell(6, 8, Type.RIGHT_CLICK, Size.LARGE));
		row.add(new ConfigCell(6, 9, Type.LEFT_CLICK, Size.MEDIUM));
		row.add(new ConfigCell(6, 10, Type.RIGHT_CLICK, Size.XLARGE));
		
		tmpGrid.add(row);
		row = new ArrayList<ConfigCell>();
		return tmpGrid;
	}
		
	/**
	 * Grid configuration matches to EO-1 on Alvin's forms
	 * Uses HOVERS ONLY
	 * returns a grid which is a 2D-Arraylist 
	 *  Arrylist containing the rows which contains arraylist containing the widgets
	 * @returns grid in 2D
	 */
	private static ArrayList<ArrayList<ConfigCell>> getGridConfig5()
	{
		ArrayList<ArrayList<ConfigCell>> tmpGrid = new ArrayList<ArrayList<ConfigCell>>();
		ArrayList<ConfigCell> row;
		row = new ArrayList<ConfigCell>();
		/* [0] 0-10 */
		row.add(new ConfigCell(0, 0, Type.HOVER, Size.XLARGE));
		row.add(new ConfigCell(0, 1, Type.HOVER, Size.LARGE));
		row.add(new ConfigCell(0, 2, Type.HOVER, Size.MEDIUM));
		row.add(new ConfigCell(0, 3, Type.HOVER, Size.SMALL));
		row.add(new ConfigCell(0, 4, Type.HOVER, Size.MEDIUM));
		row.add(new ConfigCell(0, 5, Type.HOVER, Size.SMALL));
		row.add(new ConfigCell(0, 6, Type.HOVER, Size.MEDIUM));
		row.add(new ConfigCell(0, 7, Type.HOVER, Size.XLARGE));
		row.add(new ConfigCell(0, 8, Type.HOVER, Size.LARGE));
		row.add(new ConfigCell(0, 9, Type.HOVER, Size.MEDIUM));
		row.add(new ConfigCell(0, 10, Type.HOVER, Size.XLARGE));
		
		tmpGrid.add(row);
		row = new ArrayList<ConfigCell>();
		/* [1] 0-10 */
		row.add(new ConfigCell(1, 0, Type.HOVER, Size.MEDIUM));
		row.add(new ConfigCell(1, 1, Type.HOVER, Size.LARGE));
		row.add(new ConfigCell(1, 2, Type.HOVER, Size.XLARGE));
		row.add(new ConfigCell(1, 3, Type.HOVER, Size.MEDIUM));
		row.add(new ConfigCell(1, 4, Type.HOVER, Size.LARGE));
		row.add(new ConfigCell(1, 5, Type.HOVER, Size.MEDIUM));
		row.add(new ConfigCell(1, 6, Type.HOVER, Size.LARGE));
		row.add(new ConfigCell(1, 7, Type.HOVER, Size.MEDIUM));
		row.add(new ConfigCell(1, 8, Type.HOVER, Size.XLARGE));
		row.add(new ConfigCell(1, 9, Type.HOVER, Size.SMALL));
		row.add(new ConfigCell(1, 10, Type.HOVER, Size.LARGE));
		
		tmpGrid.add(row);
		row = new ArrayList<ConfigCell>();
		/* [2] 0-10 */
		row.add(new ConfigCell(2, 0, Type.HOVER, Size.LARGE));
		row.add(new ConfigCell(2, 1, Type.HOVER, Size.SMALL));
		row.add(new ConfigCell(2, 2, Type.HOVER, Size.LARGE));
		row.add(new ConfigCell(2, 3, Type.HOVER, Size.SMALL));
		row.add(new ConfigCell(2, 4, Type.HOVER, Size.MEDIUM));
		row.add(new ConfigCell(2, 5, Type.STATUSCELL, Size.XLARGE)); // ** //
		row.add(new ConfigCell(2, 6, Type.HOVER, Size.LARGE));
		row.add(new ConfigCell(2, 7, Type.HOVER, Size.SMALL));
		row.add(new ConfigCell(2, 8, Type.HOVER, Size.LARGE));
		row.add(new ConfigCell(2, 9, Type.HOVER, Size.LARGE));
		row.add(new ConfigCell(2, 10, Type.HOVER, Size.MEDIUM));
		
		tmpGrid.add(row);
		row = new ArrayList<ConfigCell>();
		/* [3] 0-10 */
		row.add(new ConfigCell(3, 0, Type.HOVER, Size.MEDIUM));
		row.add(new ConfigCell(3, 1, Type.HOVER, Size.XLARGE));
		row.add(new ConfigCell(3, 2, Type.HOVER, Size.LARGE));
		row.add(new ConfigCell(3, 3, Type.HOVER, Size.XLARGE));
		row.add(new ConfigCell(3, 4, Type.HOVER, Size.LARGE));
		row.add(new ConfigCell(3, 5, Type.HOVER, Size.XLARGE));
		row.add(new ConfigCell(3, 6, Type.HOVER, Size.MEDIUM)); 
		row.add(new ConfigCell(3, 7, Type.HOVER, Size.XLARGE));
		row.add(new ConfigCell(3, 8, Type.HOVER, Size.MEDIUM));
		row.add(new ConfigCell(3, 9, Type.HOVER, Size.XLARGE));
		row.add(new ConfigCell(3, 10, Type.HOVER, Size.SMALL));
		
		tmpGrid.add(row);
		row=new ArrayList<ConfigCell>(); 
		/* [4] 0-10 */
		row.add(new ConfigCell(4, 0, Type.HOVER, Size.SMALL));
		row.add(new ConfigCell(4, 1, Type.HOVER, Size.LARGE));
		row.add(new ConfigCell(4, 2, Type.HOVER, Size.MEDIUM));
		row.add(new ConfigCell(4, 3, Type.HOVER, Size.SMALL));
		row.add(new ConfigCell(4, 4, Type.HOVER, Size.LARGE));
		row.add(new ConfigCell(4, 5, Type.HOVER, Size.SMALL));
		row.add(new ConfigCell(4, 6, Type.HOVER, Size.LARGE));
		row.add(new ConfigCell(4, 7, Type.HOVER, Size.SMALL));
		row.add(new ConfigCell(4, 8, Type.HOVER, Size.LARGE));
		row.add(new ConfigCell(4, 9, Type.HOVER, Size.MEDIUM));
		row.add(new ConfigCell(4, 10, Type.HOVER, Size.LARGE));
		
		tmpGrid.add(row);
		row = new ArrayList<ConfigCell>();
		/* [5] 0-10 */
		row.add(new ConfigCell(5, 0, Type.HOVER, Size.MEDIUM));
		row.add(new ConfigCell(5, 1, Type.HOVER, Size.SMALL));
		row.add(new ConfigCell(5, 2, Type.HOVER, Size.XLARGE));
		row.add(new ConfigCell(5, 3, Type.HOVER, Size.MEDIUM));
		row.add(new ConfigCell(5, 4, Type.HOVER, Size.LARGE));
		row.add(new ConfigCell(5, 5, Type.HOVER, Size.MEDIUM));
		row.add(new ConfigCell(5, 6, Type.HOVER, Size.LARGE));
		row.add(new ConfigCell(5, 7, Type.HOVER, Size.MEDIUM));
		row.add(new ConfigCell(5, 8, Type.HOVER, Size.XLARGE));
		row.add(new ConfigCell(5, 9, Type.HOVER, Size.SMALL));
		row.add(new ConfigCell(5, 10, Type.HOVER, Size.MEDIUM));
		
		tmpGrid.add(row);
		row = new ArrayList<ConfigCell>();
		/* [6] 0-10 */
		row.add(new ConfigCell(6, 0, Type.HOVER, Size.XLARGE));
		row.add(new ConfigCell(6, 1, Type.HOVER, Size.MEDIUM));
		row.add(new ConfigCell(6, 2, Type.HOVER, Size.SMALL));
		row.add(new ConfigCell(6, 3, Type.HOVER, Size.MEDIUM));
		row.add(new ConfigCell(6, 4, Type.HOVER, Size.LARGE));
		row.add(new ConfigCell(6, 5, Type.HOVER, Size.SMALL));
		row.add(new ConfigCell(6, 6, Type.HOVER, Size.MEDIUM));
		row.add(new ConfigCell(6, 7, Type.HOVER, Size.XLARGE));
		row.add(new ConfigCell(6, 8, Type.HOVER, Size.LARGE));
		row.add(new ConfigCell(6, 9, Type.HOVER, Size.MEDIUM));
		row.add(new ConfigCell(6, 10, Type.HOVER, Size.XLARGE));
		
		tmpGrid.add(row);
		row = new ArrayList<ConfigCell>();
		return tmpGrid;
	}
	
	/**
	 * Grid configuration matches to EO-2 on Alvin's forms
	 * Uses HOVERS ONLY
	 * returns a grid which is a 2D-Arraylist 
	 *  Arrylist containing the rows which contains arraylist containing the widgets
	 * @returns grid in 2D
	 */
	private static ArrayList<ArrayList<ConfigCell>> getGridConfig6()
	{
		ArrayList<ArrayList<ConfigCell>> tmpGrid = new ArrayList<ArrayList<ConfigCell>>();
		ArrayList<ConfigCell> row;
		row = new ArrayList<ConfigCell>();
		/* [0] 0-10 */
		row.add(new ConfigCell(0, 0, Type.HOVER, Size.XLARGE));
		row.add(new ConfigCell(0, 1, Type.HOVER, Size.XLARGE));
		row.add(new ConfigCell(0, 2, Type.HOVER, Size.SMALL));
		row.add(new ConfigCell(0, 3, Type.HOVER, Size.MEDIUM));
		row.add(new ConfigCell(0, 4, Type.HOVER, Size.XLARGE));
		row.add(new ConfigCell(0, 5, Type.HOVER, Size.SMALL));
		row.add(new ConfigCell(0, 6, Type.HOVER, Size.XLARGE));
		row.add(new ConfigCell(0, 7, Type.HOVER, Size.MEDIUM));
		row.add(new ConfigCell(0, 8, Type.HOVER, Size.SMALL));
		row.add(new ConfigCell(0, 9, Type.HOVER, Size.LARGE));
		row.add(new ConfigCell(0, 10, Type.HOVER, Size.XLARGE));
		
		tmpGrid.add(row);
		row = new ArrayList<ConfigCell>();
		/* [1] 0-10 */
		row.add(new ConfigCell(1, 0, Type.HOVER, Size.SMALL));
		row.add(new ConfigCell(1, 1, Type.HOVER, Size.LARGE));
		row.add(new ConfigCell(1, 2, Type.HOVER, Size.XLARGE));
		row.add(new ConfigCell(1, 3, Type.HOVER, Size.SMALL));
		row.add(new ConfigCell(1, 4, Type.HOVER, Size.LARGE));
		row.add(new ConfigCell(1, 5, Type.HOVER, Size.XLARGE));
		row.add(new ConfigCell(1, 6, Type.HOVER, Size.LARGE));
		row.add(new ConfigCell(1, 7, Type.HOVER, Size.SMALL));
		row.add(new ConfigCell(1, 8, Type.HOVER, Size.XLARGE));
		row.add(new ConfigCell(1, 9, Type.HOVER, Size.MEDIUM));
		row.add(new ConfigCell(1, 10, Type.HOVER, Size.SMALL));
		
		tmpGrid.add(row);
		row = new ArrayList<ConfigCell>();
		/* [2] 0-10 */
		row.add(new ConfigCell(2, 0, Type.HOVER, Size.LARGE));
		row.add(new ConfigCell(2, 1, Type.HOVER, Size.MEDIUM));
		row.add(new ConfigCell(2, 2, Type.HOVER, Size.MEDIUM));
		row.add(new ConfigCell(2, 3, Type.HOVER, Size.SMALL));
		row.add(new ConfigCell(2, 4, Type.HOVER, Size.LARGE));
		row.add(new ConfigCell(2, 5, Type.HOVER, Size.XLARGE));
		row.add(new ConfigCell(2, 6, Type.HOVER, Size.LARGE));
		row.add(new ConfigCell(2, 7, Type.HOVER, Size.SMALL));
		row.add(new ConfigCell(2, 8, Type.HOVER, Size.MEDIUM));
		row.add(new ConfigCell(2, 9, Type.HOVER, Size.LARGE));
		row.add(new ConfigCell(2, 10, Type.HOVER, Size.LARGE));
		
		tmpGrid.add(row);
		row = new ArrayList<ConfigCell>();
		/* [3] 0-10 */
		row.add(new ConfigCell(3, 0, Type.HOVER, Size.MEDIUM));
		row.add(new ConfigCell(3, 1, Type.HOVER, Size.SMALL));
		row.add(new ConfigCell(3, 2, Type.HOVER, Size.LARGE));
		row.add(new ConfigCell(3, 3, Type.HOVER, Size.XLARGE));
		row.add(new ConfigCell(3, 4, Type.HOVER, Size.MEDIUM));
		row.add(new ConfigCell(3, 5, Type.STATUSCELL, Size.XLARGE)); //**//
		row.add(new ConfigCell(3, 6, Type.HOVER, Size.MEDIUM)); 
		row.add(new ConfigCell(3, 7, Type.HOVER, Size.XLARGE));
		row.add(new ConfigCell(3, 8, Type.HOVER, Size.LARGE));
		row.add(new ConfigCell(3, 9, Type.HOVER, Size.SMALL));
		row.add(new ConfigCell(3, 10, Type.HOVER, Size.MEDIUM));
		
		tmpGrid.add(row);
		row=new ArrayList<ConfigCell>(); 
		/* [4] 0-10 */
		row.add(new ConfigCell(4, 0, Type.HOVER, Size.LARGE));
		row.add(new ConfigCell(4, 1, Type.HOVER, Size.MEDIUM));
		row.add(new ConfigCell(4, 2, Type.HOVER, Size.LARGE));
		row.add(new ConfigCell(4, 3, Type.HOVER, Size.XLARGE));
		row.add(new ConfigCell(4, 4, Type.HOVER, Size.MEDIUM));
		row.add(new ConfigCell(4, 5, Type.HOVER, Size.LARGE));
		row.add(new ConfigCell(4, 6, Type.HOVER, Size.MEDIUM));
		row.add(new ConfigCell(4, 7, Type.HOVER, Size.XLARGE));
		row.add(new ConfigCell(4, 8, Type.HOVER, Size.LARGE));
		row.add(new ConfigCell(4, 9, Type.HOVER, Size.MEDIUM));
		row.add(new ConfigCell(4, 10, Type.HOVER, Size.LARGE));
		
		tmpGrid.add(row);
		row = new ArrayList<ConfigCell>();
		/* [5] 0-10 */
		row.add(new ConfigCell(5, 0, Type.HOVER, Size.SMALL));
		row.add(new ConfigCell(5, 1, Type.HOVER, Size.MEDIUM));
		row.add(new ConfigCell(5, 2, Type.HOVER, Size.MEDIUM));
		row.add(new ConfigCell(5, 3, Type.HOVER, Size.LARGE));
		row.add(new ConfigCell(5, 4, Type.HOVER, Size.SMALL));
		row.add(new ConfigCell(5, 5, Type.HOVER, Size.MEDIUM));
		row.add(new ConfigCell(5, 6, Type.HOVER, Size.SMALL));
		row.add(new ConfigCell(5, 7, Type.HOVER, Size.LARGE));
		row.add(new ConfigCell(5, 8, Type.HOVER, Size.MEDIUM));
		row.add(new ConfigCell(5, 9, Type.HOVER, Size.LARGE));
		row.add(new ConfigCell(5, 10, Type.HOVER, Size.SMALL));
		
		tmpGrid.add(row);
		row = new ArrayList<ConfigCell>();
		/* [6] 0-10 */
		row.add(new ConfigCell(6, 0, Type.HOVER, Size.XLARGE));
		row.add(new ConfigCell(6, 1, Type.HOVER, Size.XLARGE));
		row.add(new ConfigCell(6, 2, Type.HOVER, Size.MEDIUM));
		row.add(new ConfigCell(6, 3, Type.HOVER, Size.LARGE));
		row.add(new ConfigCell(6, 4, Type.HOVER, Size.SMALL));
		row.add(new ConfigCell(6, 5, Type.HOVER, Size.MEDIUM));
		row.add(new ConfigCell(6, 6, Type.HOVER, Size.SMALL));
		row.add(new ConfigCell(6, 7, Type.HOVER, Size.LARGE));
		row.add(new ConfigCell(6, 8, Type.HOVER, Size.MEDIUM));
		row.add(new ConfigCell(6, 9, Type.HOVER, Size.XLARGE));
		row.add(new ConfigCell(6, 10, Type.HOVER, Size.XLARGE));
		
		tmpGrid.add(row);
		row = new ArrayList<ConfigCell>();
		return tmpGrid;
	}

	/**
	 * Grid configuration matches to EO-3 on Alvin's forms
	 * Uses HOVERS ONLY
	 * returns a grid which is a 2D-Arraylist 
	 *  Arrylist containing the rows which contains arraylist containing the widgets
	 * @returns grid in 2D
	 */
	private static ArrayList<ArrayList<ConfigCell>> getGridConfig7()
	{
		ArrayList<ArrayList<ConfigCell>> tmpGrid = new ArrayList<ArrayList<ConfigCell>>();
		ArrayList<ConfigCell> row;
		row = new ArrayList<ConfigCell>();
		/* [0] 0-10 */
		row.add(new ConfigCell(0, 0, Type.HOVER, Size.XLARGE));
		row.add(new ConfigCell(0, 1, Type.HOVER, Size.MEDIUM));
		row.add(new ConfigCell(0, 2, Type.HOVER, Size.SMALL));
		row.add(new ConfigCell(0, 3, Type.HOVER, Size.MEDIUM));
		row.add(new ConfigCell(0, 4, Type.HOVER, Size.XLARGE));
		row.add(new ConfigCell(0, 5, Type.HOVER, Size.LARGE));
		row.add(new ConfigCell(0, 6, Type.HOVER, Size.XLARGE));
		row.add(new ConfigCell(0, 7, Type.HOVER, Size.MEDIUM));
		row.add(new ConfigCell(0, 8, Type.HOVER, Size.SMALL));
		row.add(new ConfigCell(0, 9, Type.HOVER, Size.MEDIUM));
		row.add(new ConfigCell(0, 10, Type.HOVER, Size.XLARGE));
		
		tmpGrid.add(row);
		row = new ArrayList<ConfigCell>();
		/* [1] 0-10 */
		row.add(new ConfigCell(1, 0, Type.HOVER, Size.MEDIUM));
		row.add(new ConfigCell(1, 1, Type.HOVER, Size.LARGE));
		row.add(new ConfigCell(1, 2, Type.HOVER, Size.MEDIUM));
		row.add(new ConfigCell(1, 3, Type.HOVER, Size.SMALL));
		row.add(new ConfigCell(1, 4, Type.HOVER, Size.MEDIUM));
		row.add(new ConfigCell(1, 5, Type.STATUSCELL, Size.XLARGE)); // ** //
		row.add(new ConfigCell(1, 6, Type.HOVER, Size.MEDIUM));
		row.add(new ConfigCell(1, 7, Type.HOVER, Size.SMALL));
		row.add(new ConfigCell(1, 8, Type.HOVER, Size.MEDIUM));
		row.add(new ConfigCell(1, 9, Type.HOVER, Size.LARGE));
		row.add(new ConfigCell(1, 10, Type.HOVER, Size.MEDIUM));
		
		tmpGrid.add(row);
		row = new ArrayList<ConfigCell>();
		/* [2] 0-10 */
		row.add(new ConfigCell(2, 0, Type.HOVER, Size.SMALL));
		row.add(new ConfigCell(2, 1, Type.HOVER, Size.LARGE));
		row.add(new ConfigCell(2, 2, Type.HOVER, Size.LARGE));
		row.add(new ConfigCell(2, 3, Type.HOVER, Size.XLARGE));
		row.add(new ConfigCell(2, 4, Type.HOVER, Size.SMALL));
		row.add(new ConfigCell(2, 5, Type.HOVER, Size.XLARGE));
		row.add(new ConfigCell(2, 6, Type.HOVER, Size.XLARGE));
		row.add(new ConfigCell(2, 7, Type.HOVER, Size.XLARGE));
		row.add(new ConfigCell(2, 8, Type.HOVER, Size.LARGE));
		row.add(new ConfigCell(2, 9, Type.HOVER, Size.MEDIUM));
		row.add(new ConfigCell(2, 10, Type.HOVER, Size.SMALL));
		
		tmpGrid.add(row);
		row = new ArrayList<ConfigCell>();
		/* [3] 0-10 */
		row.add(new ConfigCell(3, 0, Type.HOVER, Size.LARGE));
		row.add(new ConfigCell(3, 1, Type.HOVER, Size.SMALL));
		row.add(new ConfigCell(3, 2, Type.HOVER, Size.XLARGE));
		row.add(new ConfigCell(3, 3, Type.HOVER, Size.LARGE));
		row.add(new ConfigCell(3, 4, Type.HOVER, Size.MEDIUM));
		row.add(new ConfigCell(3, 5, Type.HOVER, Size.SMALL)); 
		row.add(new ConfigCell(3, 6, Type.HOVER, Size.MEDIUM)); 
		row.add(new ConfigCell(3, 7, Type.HOVER, Size.LARGE));
		row.add(new ConfigCell(3, 8, Type.HOVER, Size.XLARGE));
		row.add(new ConfigCell(3, 9, Type.HOVER, Size.SMALL));
		row.add(new ConfigCell(3, 10, Type.HOVER, Size.LARGE));
		
		tmpGrid.add(row);
		row=new ArrayList<ConfigCell>(); 
		/* [4] 0-10 */
		row.add(new ConfigCell(4, 0, Type.HOVER, Size.SMALL));
		row.add(new ConfigCell(4, 1, Type.HOVER, Size.LARGE));
		row.add(new ConfigCell(4, 2, Type.HOVER, Size.LARGE));
		row.add(new ConfigCell(4, 3, Type.HOVER, Size.XLARGE));
		row.add(new ConfigCell(4, 4, Type.HOVER, Size.XLARGE));
		row.add(new ConfigCell(4, 5, Type.HOVER, Size.MEDIUM));
		row.add(new ConfigCell(4, 6, Type.HOVER, Size.XLARGE));
		row.add(new ConfigCell(4, 7, Type.HOVER, Size.SMALL));
		row.add(new ConfigCell(4, 8, Type.HOVER, Size.LARGE));
		row.add(new ConfigCell(4, 9, Type.HOVER, Size.MEDIUM));
		row.add(new ConfigCell(4, 10, Type.HOVER, Size.SMALL));
		
		tmpGrid.add(row);
		row = new ArrayList<ConfigCell>();
		/* [5] 0-10 */
		row.add(new ConfigCell(5, 0, Type.HOVER, Size.MEDIUM));
		row.add(new ConfigCell(5, 1, Type.HOVER, Size.LARGE));
		row.add(new ConfigCell(5, 2, Type.HOVER, Size.MEDIUM));
		row.add(new ConfigCell(5, 3, Type.HOVER, Size.SMALL));
		row.add(new ConfigCell(5, 4, Type.HOVER, Size.LARGE));
		row.add(new ConfigCell(5, 5, Type.HOVER, Size.XLARGE));
		row.add(new ConfigCell(5, 6, Type.HOVER, Size.LARGE));
		row.add(new ConfigCell(5, 7, Type.HOVER, Size.SMALL));
		row.add(new ConfigCell(5, 8, Type.HOVER, Size.MEDIUM));
		row.add(new ConfigCell(5, 9, Type.HOVER, Size.LARGE));
		row.add(new ConfigCell(5, 10, Type.HOVER, Size.MEDIUM));
		
		tmpGrid.add(row);
		row = new ArrayList<ConfigCell>();
		/* [6] 0-10 */
		row.add(new ConfigCell(6, 0, Type.HOVER, Size.XLARGE));
		row.add(new ConfigCell(6, 1, Type.HOVER, Size.MEDIUM));
		row.add(new ConfigCell(6, 2, Type.HOVER, Size.SMALL));
		row.add(new ConfigCell(6, 3, Type.HOVER, Size.MEDIUM));
		row.add(new ConfigCell(6, 4, Type.HOVER, Size.XLARGE));
		row.add(new ConfigCell(6, 5, Type.HOVER, Size.LARGE));
		row.add(new ConfigCell(6, 6, Type.HOVER, Size.XLARGE));
		row.add(new ConfigCell(6, 7, Type.HOVER, Size.MEDIUM));
		row.add(new ConfigCell(6, 8, Type.HOVER, Size.SMALL));
		row.add(new ConfigCell(6, 9, Type.HOVER, Size.MEDIUM));
		row.add(new ConfigCell(6, 10, Type.HOVER, Size.XLARGE));
		
		tmpGrid.add(row);
		row = new ArrayList<ConfigCell>();
		return tmpGrid;
	}

	/**
	 * Grid configuration matches to EO-4 on Alvin's forms
	 * Uses HOVERS ONLY
	 * returns a grid which is a 2D-Arraylist 
	 *  Arrylist containing the rows which contains arraylist containing the widgets
	 * @returns grid in 2D
	 */
	private static ArrayList<ArrayList<ConfigCell>> getGridConfig8()
	{
		ArrayList<ArrayList<ConfigCell>> tmpGrid = new ArrayList<ArrayList<ConfigCell>>();
		ArrayList<ConfigCell> row;
		row = new ArrayList<ConfigCell>();
		/* [0] 0-10 */
		row.add(new ConfigCell(0, 0, Type.HOVER, Size.XLARGE));
		row.add(new ConfigCell(0, 1, Type.HOVER, Size.SMALL));
		row.add(new ConfigCell(0, 2, Type.HOVER, Size.XLARGE));
		row.add(new ConfigCell(0, 3, Type.HOVER, Size.LARGE));
		row.add(new ConfigCell(0, 4, Type.HOVER, Size.MEDIUM));
		row.add(new ConfigCell(0, 5, Type.HOVER, Size.SMALL));
		row.add(new ConfigCell(0, 6, Type.HOVER, Size.LARGE));
		row.add(new ConfigCell(0, 7, Type.HOVER, Size.MEDIUM));
		row.add(new ConfigCell(0, 8, Type.HOVER, Size.XLARGE));
		row.add(new ConfigCell(0, 9, Type.HOVER, Size.LARGE));
		row.add(new ConfigCell(0, 10, Type.HOVER, Size.XLARGE));
		
		tmpGrid.add(row);
		row = new ArrayList<ConfigCell>();
		/* [1] 0-10 */
		row.add(new ConfigCell(1, 0, Type.HOVER, Size.LARGE));
		row.add(new ConfigCell(1, 1, Type.HOVER, Size.MEDIUM));
		row.add(new ConfigCell(1, 2, Type.HOVER, Size.SMALL));
		row.add(new ConfigCell(1, 3, Type.HOVER, Size.LARGE));
		row.add(new ConfigCell(1, 4, Type.HOVER, Size.XLARGE));
		row.add(new ConfigCell(1, 5, Type.HOVER, Size.MEDIUM)); 
		row.add(new ConfigCell(1, 6, Type.HOVER, Size.XLARGE));
		row.add(new ConfigCell(1, 7, Type.HOVER, Size.LARGE));
		row.add(new ConfigCell(1, 8, Type.HOVER, Size.SMALL));
		row.add(new ConfigCell(1, 9, Type.HOVER, Size.LARGE));
		row.add(new ConfigCell(1, 10, Type.HOVER, Size.LARGE));
		
		tmpGrid.add(row);
		row = new ArrayList<ConfigCell>();
		/* [2] 0-10 */
		row.add(new ConfigCell(2, 0, Type.HOVER, Size.MEDIUM));
		row.add(new ConfigCell(2, 1, Type.HOVER, Size.XLARGE));
		row.add(new ConfigCell(2, 2, Type.HOVER, Size.MEDIUM));
		row.add(new ConfigCell(2, 3, Type.HOVER, Size.SMALL));
		row.add(new ConfigCell(2, 4, Type.HOVER, Size.MEDIUM));
		row.add(new ConfigCell(2, 5, Type.HOVER, Size.SMALL));
		row.add(new ConfigCell(2, 6, Type.HOVER, Size.MEDIUM));
		row.add(new ConfigCell(2, 7, Type.HOVER, Size.XLARGE));
		row.add(new ConfigCell(2, 8, Type.HOVER, Size.MEDIUM));
		row.add(new ConfigCell(2, 9, Type.HOVER, Size.LARGE));
		row.add(new ConfigCell(2, 10, Type.HOVER, Size.MEDIUM));
		
		tmpGrid.add(row);
		row = new ArrayList<ConfigCell>();
		/* [3] 0-10 */
		row.add(new ConfigCell(3, 0, Type.HOVER, Size.SMALL));
		row.add(new ConfigCell(3, 1, Type.HOVER, Size.MEDIUM));
		row.add(new ConfigCell(3, 2, Type.HOVER, Size.LARGE));
		row.add(new ConfigCell(3, 3, Type.HOVER, Size.MEDIUM));
		row.add(new ConfigCell(3, 4, Type.HOVER, Size.LARGE));
		row.add(new ConfigCell(3, 5, Type.HOVER, Size.XLARGE)); 
		row.add(new ConfigCell(3, 6, Type.HOVER, Size.LARGE)); 
		row.add(new ConfigCell(3, 7, Type.HOVER, Size.MEDIUM));
		row.add(new ConfigCell(3, 8, Type.HOVER, Size.LARGE));
		row.add(new ConfigCell(3, 9, Type.HOVER, Size.MEDIUM));
		row.add(new ConfigCell(3, 10, Type.HOVER, Size.SMALL));
		
		tmpGrid.add(row);
		row=new ArrayList<ConfigCell>(); 
		/* [4] 0-10 */
		row.add(new ConfigCell(4, 0, Type.HOVER, Size.XLARGE));
		row.add(new ConfigCell(4, 1, Type.HOVER, Size.LARGE));
		row.add(new ConfigCell(4, 2, Type.HOVER, Size.SMALL));
		row.add(new ConfigCell(4, 3, Type.HOVER, Size.SMALL));
		row.add(new ConfigCell(4, 4, Type.HOVER, Size.MEDIUM));
		row.add(new ConfigCell(4, 5, Type.STATUSCELL, Size.XLARGE)); // ** //
		row.add(new ConfigCell(4, 6, Type.HOVER, Size.MEDIUM));
		row.add(new ConfigCell(4, 7, Type.HOVER, Size.SMALL));
		row.add(new ConfigCell(4, 8, Type.HOVER, Size.LARGE));
		row.add(new ConfigCell(4, 9, Type.HOVER, Size.MEDIUM));
		row.add(new ConfigCell(4, 10, Type.HOVER, Size.XLARGE));
		
		tmpGrid.add(row);
		row = new ArrayList<ConfigCell>();
		/* [5] 0-10 */
		row.add(new ConfigCell(5, 0, Type.HOVER, Size.LARGE));
		row.add(new ConfigCell(5, 1, Type.HOVER, Size.SMALL));
		row.add(new ConfigCell(5, 2, Type.HOVER, Size.XLARGE));
		row.add(new ConfigCell(5, 3, Type.HOVER, Size.MEDIUM));
		row.add(new ConfigCell(5, 4, Type.HOVER, Size.MEDIUM));
		row.add(new ConfigCell(5, 5, Type.HOVER, Size.SMALL));
		row.add(new ConfigCell(5, 6, Type.HOVER, Size.LARGE));
		row.add(new ConfigCell(5, 7, Type.HOVER, Size.MEDIUM));
		row.add(new ConfigCell(5, 8, Type.HOVER, Size.XLARGE));
		row.add(new ConfigCell(5, 9, Type.HOVER, Size.SMALL));
		row.add(new ConfigCell(5, 10, Type.HOVER, Size.LARGE));
		
		tmpGrid.add(row);
		row = new ArrayList<ConfigCell>();
		/* [6] 0-10 */
		row.add(new ConfigCell(6, 0, Type.HOVER, Size.XLARGE));
		row.add(new ConfigCell(6, 1, Type.HOVER, Size.MEDIUM));
		row.add(new ConfigCell(6, 2, Type.HOVER, Size.LARGE));
		row.add(new ConfigCell(6, 3, Type.HOVER, Size.MEDIUM));
		row.add(new ConfigCell(6, 4, Type.HOVER, Size.LARGE));
		row.add(new ConfigCell(6, 5, Type.HOVER, Size.SMALL));
		row.add(new ConfigCell(6, 6, Type.HOVER, Size.LARGE));
		row.add(new ConfigCell(6, 7, Type.HOVER, Size.MEDIUM));
		row.add(new ConfigCell(6, 8, Type.HOVER, Size.LARGE));
		row.add(new ConfigCell(6, 9, Type.HOVER, Size.MEDIUM));
		row.add(new ConfigCell(6, 10, Type.HOVER, Size.XLARGE));
		
		tmpGrid.add(row);
		row = new ArrayList<ConfigCell>();
		return tmpGrid;
	}


	/**
	 * Uses the grids from GridConfig2 or EO2 on Alvin's design. 
	 * This moves to some locations near the screen, and then to the four corners. 
	 * Only uses the 4 XLarge buttons.
	 */
	private static ArrayList<Cell> getDebugExperimentOrder()
	{
		
		ArrayList<Cell> tmpExperimentOrder = new ArrayList<Cell>();
		
		tmpExperimentOrder = new ArrayList<Cell>();
		// some locations near the starting point
		tmpExperimentOrder.add(new Cell(2, 5));
		tmpExperimentOrder.add(new Cell(1, 5 ));
		tmpExperimentOrder.add(new Cell(4, 3 ));
		tmpExperimentOrder.add(new Cell(4, 7 ));
		// move to 4 corners
		tmpExperimentOrder.add(new Cell(0, 0 ));
		tmpExperimentOrder.add(new Cell(0, 10 ));
		tmpExperimentOrder.add(new Cell(6, 10 ));
		tmpExperimentOrder.add(new Cell(6, 0 ));
		// move to center of edges.
		tmpExperimentOrder.add(new Cell(3, 10 ));
		tmpExperimentOrder.add(new Cell(6, 5 ));
		tmpExperimentOrder.add(new Cell(3, 0 ));
		tmpExperimentOrder.add(new Cell(0, 5 ));
		
		return tmpExperimentOrder;
	}
	
	/**
	 * Maps to EO-1 on Alvin's design. 
	 * Used by GridConfig 1 (clicks )and 5 (hover)
	 * sets a unique trial ordering for the experiment.
	 * 1D array of cells that correspond to buttons in the grid (cell.row, cell.col). 
	 */
	private static ArrayList<Cell> getProductionExperimentOrder1()
	{
		ArrayList<Cell> experimentOrder = new ArrayList<Cell>();
		
		/* 1-10 */
		experimentOrder.add(new Cell(3, 5));
		experimentOrder.add(new Cell(3, 3));
		experimentOrder.add(new Cell(1, 2));
		experimentOrder.add(new Cell(3, 1));
		experimentOrder.add(new Cell(5, 2));
		experimentOrder.add(new Cell(4, 6));
		experimentOrder.add(new Cell(2, 8));
		experimentOrder.add(new Cell(4, 10));
		experimentOrder.add(new Cell(5, 6));
		experimentOrder.add(new Cell(6, 10));
		
		/* 11-20 */
		experimentOrder.add(new Cell(2, 9));
		experimentOrder.add(new Cell(1, 5));
		experimentOrder.add(new Cell(3, 2));
		experimentOrder.add(new Cell(5, 5));
		experimentOrder.add(new Cell(3, 9));
		experimentOrder.add(new Cell(6, 6));
		experimentOrder.add(new Cell(4, 4));
		experimentOrder.add(new Cell(1, 4));
		experimentOrder.add(new Cell(0, 0));
		experimentOrder.add(new Cell(3, 6));
		
		/* 21-30 */
		experimentOrder.add(new Cell(6, 0));
		experimentOrder.add(new Cell(2, 2));
		experimentOrder.add(new Cell(4, 1));
		experimentOrder.add(new Cell(3, 0));
		experimentOrder.add(new Cell(0, 2));
		experimentOrder.add(new Cell(2, 0));
		experimentOrder.add(new Cell(0, 1));
		experimentOrder.add(new Cell(5, 3));
		experimentOrder.add(new Cell(4, 3));
		experimentOrder.add(new Cell(2, 6));
		
		/* 31-40 */
		experimentOrder.add(new Cell(0, 8));
		experimentOrder.add(new Cell(5, 8));
		experimentOrder.add(new Cell(2, 7));
		experimentOrder.add(new Cell(0, 10));
		experimentOrder.add(new Cell(4, 7));
		experimentOrder.add(new Cell(1, 8));
		experimentOrder.add(new Cell(3, 8));
		experimentOrder.add(new Cell(0, 6));
		experimentOrder.add(new Cell(4, 5));
		experimentOrder.add(new Cell(0, 5));
		
		/* 41-50 */
		experimentOrder.add(new Cell(1, 0));
		experimentOrder.add(new Cell(5, 4));
		experimentOrder.add(new Cell(2, 10));
		experimentOrder.add(new Cell(4, 8));
		experimentOrder.add(new Cell(1, 6));
		experimentOrder.add(new Cell(5, 0));
		experimentOrder.add(new Cell(6, 2));
		experimentOrder.add(new Cell(2, 3));
		experimentOrder.add(new Cell(6, 5));
		experimentOrder.add(new Cell(1, 10));
		
		/* 51-60 */
		experimentOrder.add(new Cell(5, 9));
		experimentOrder.add(new Cell(3, 10));
		experimentOrder.add(new Cell(6, 7));
		experimentOrder.add(new Cell(6, 9));
		experimentOrder.add(new Cell(0, 3));
		experimentOrder.add(new Cell(6, 3));
		experimentOrder.add(new Cell(6, 4));
		experimentOrder.add(new Cell(4, 9));
		experimentOrder.add(new Cell(6, 7));
		experimentOrder.add(new Cell(6, 1));
		
		/* 61-70 */
		experimentOrder.add(new Cell(5, 1));
		experimentOrder.add(new Cell(1, 9));
		experimentOrder.add(new Cell(0, 9));
		experimentOrder.add(new Cell(2, 1));
		experimentOrder.add(new Cell(5, 10));
		experimentOrder.add(new Cell(6, 8));
		experimentOrder.add(new Cell(4, 0));
		experimentOrder.add(new Cell(3, 7));
		experimentOrder.add(new Cell(0, 4));
		experimentOrder.add(new Cell(0, 7));
		
		return experimentOrder;

	}
	
	/**
	 * Maps to EO-2 on Alvin's design. 
	 * Used by GridConfig 2 (clicks )and 6 (hover)
	 * sets a unique trial ordering for the experiment.
	 * 1D array of cells that correspond to buttons in the grid (cell.row, cell.col). 
	 */
	private static ArrayList<Cell> getProductionExperimentOrder2()
	{
		ArrayList<Cell> experimentOrder = new ArrayList<Cell>();
		
		/* 1-10 */
		experimentOrder.add(new Cell(2, 5 ));
		experimentOrder.add(new Cell(1, 8 ));
		experimentOrder.add(new Cell(0, 6 ));
		experimentOrder.add(new Cell(4, 6 ));
		experimentOrder.add(new Cell(4, 2 ));
		experimentOrder.add(new Cell(6, 5 ));
		experimentOrder.add(new Cell(4, 9 ));
		experimentOrder.add(new Cell(1, 9 ));
		experimentOrder.add(new Cell(1, 5 ));
		experimentOrder.add(new Cell(1, 1 ));
		
		/* 11-20 */
		experimentOrder.add(new Cell(5, 3 ));
		experimentOrder.add(new Cell(4, 8 ));
		experimentOrder.add(new Cell(1, 4 ));
		experimentOrder.add(new Cell(0, 10 ));
		experimentOrder.add(new Cell(4, 7 ));
		experimentOrder.add(new Cell(5, 1 ));
		experimentOrder.add(new Cell(0, 0 ));
		experimentOrder.add(new Cell(0, 7 ));
		experimentOrder.add(new Cell(6, 9 ));
		experimentOrder.add(new Cell(6, 1 ));
		
		/* 21-30 */
		experimentOrder.add(new Cell(3, 2 ));
		experimentOrder.add(new Cell(5, 5 ));
		experimentOrder.add(new Cell(3, 8 ));
		experimentOrder.add(new Cell(0, 3 ));
		experimentOrder.add(new Cell(3, 4 ));
		experimentOrder.add(new Cell(5, 8 ));
		experimentOrder.add(new Cell(5, 6 ));
		experimentOrder.add(new Cell(3, 9 ));
		experimentOrder.add(new Cell(5, 10 ));
		experimentOrder.add(new Cell(1, 10 ));
		
		/* 31-40 */
		experimentOrder.add(new Cell(6, 0 ));
		experimentOrder.add(new Cell(3, 1 ));
		experimentOrder.add(new Cell(0, 5 ));
		experimentOrder.add(new Cell(2, 7 ));
		experimentOrder.add(new Cell(5, 9 ));
		experimentOrder.add(new Cell(6, 7 ));
		experimentOrder.add(new Cell(6, 6 ));
		experimentOrder.add(new Cell(1, 6 ));
		experimentOrder.add(new Cell(6, 3 ));
		experimentOrder.add(new Cell(1, 2 ));
		
		/* 41-50 */
		experimentOrder.add(new Cell(4, 5 ));
		experimentOrder.add(new Cell(0, 1 ));
		experimentOrder.add(new Cell(4, 0 ));
		experimentOrder.add(new Cell(6, 8 ));
		experimentOrder.add(new Cell(0, 8 ));
		experimentOrder.add(new Cell(6, 10 ));
		experimentOrder.add(new Cell(0, 9 ));
		experimentOrder.add(new Cell(3, 3 ));
		experimentOrder.add(new Cell(1, 7 ));
		experimentOrder.add(new Cell(3, 0 ));
		
		/* 51-60 */
		experimentOrder.add(new Cell(6, 4 ));
		experimentOrder.add(new Cell(0, 4 ));
		experimentOrder.add(new Cell(2, 9 ));
		experimentOrder.add(new Cell(2, 3 ));
		experimentOrder.add(new Cell(2, 6 ));
		experimentOrder.add(new Cell(5, 4 ));
		experimentOrder.add(new Cell(6, 2 ));
		experimentOrder.add(new Cell(4, 1 ));
		experimentOrder.add(new Cell(0, 2 ));
		experimentOrder.add(new Cell(4, 4 ));
		
		/* 61-70 */
		experimentOrder.add(new Cell(2, 1 ));
		experimentOrder.add(new Cell(2, 10 ));
		experimentOrder.add(new Cell(5, 7 ));
		experimentOrder.add(new Cell(2, 8 ));
		experimentOrder.add(new Cell(2, 4 ));
		experimentOrder.add(new Cell(1, 0 ));
		experimentOrder.add(new Cell(5, 2 ));
		experimentOrder.add(new Cell(2, 0 ));
		experimentOrder.add(new Cell(3, 10 ));
		experimentOrder.add(new Cell(2, 2 ));
		
		return experimentOrder;

	}
	
	/**
	 * Maps to EO-3 on Alvin's design. 
	 * Used by GridConfig 3 (clicks )and 7 (hover)
	 * sets a unique trial ordering for the experiment.
	 * 1D array of cells that correspond to buttons in the grid (cell.row, cell.col). 
	 */
	private static ArrayList<Cell> getProductionExperimentOrder3()
	{
		ArrayList<Cell> experimentOrder = new ArrayList<Cell>();
		/* 1-10 */
		experimentOrder.add(new Cell(2, 5 ));
		experimentOrder.add(new Cell(0, 4 ));
		experimentOrder.add(new Cell(2, 7 ));
		experimentOrder.add(new Cell(1, 4 ));
		experimentOrder.add(new Cell(3, 2 ));
		experimentOrder.add(new Cell(1, 2 ));
		experimentOrder.add(new Cell(4, 3 ));
		experimentOrder.add(new Cell(5, 6 ));
		experimentOrder.add(new Cell(3, 4 ));
		experimentOrder.add(new Cell(0, 6 ));
		
		/* 11-20 */
		experimentOrder.add(new Cell(2, 8 ));
		experimentOrder.add(new Cell(4, 8 ));
		experimentOrder.add(new Cell(5, 4 ));
		experimentOrder.add(new Cell(4, 2 ));
		experimentOrder.add(new Cell(1, 0 ));
		experimentOrder.add(new Cell(3, 3 ));
		experimentOrder.add(new Cell(6, 4 ));
		experimentOrder.add(new Cell(6, 5 ));
		experimentOrder.add(new Cell(2, 6 ));
		experimentOrder.add(new Cell(3, 6 ));
		
		/* 21-30 */
		experimentOrder.add(new Cell(1, 9 ));
		experimentOrder.add(new Cell(0, 7 ));
		experimentOrder.add(new Cell(3, 10 ));
		experimentOrder.add(new Cell(5, 5 ));
		experimentOrder.add(new Cell(1, 6 ));
		experimentOrder.add(new Cell(2, 2 ));
		experimentOrder.add(new Cell(5, 1 ));
		experimentOrder.add(new Cell(5, 3 ));
		experimentOrder.add(new Cell(2, 3 ));
		experimentOrder.add(new Cell(0, 5 ));
		
		/* 31-40 */
		experimentOrder.add(new Cell(2, 4 ));
		experimentOrder.add(new Cell(3, 1 ));
		experimentOrder.add(new Cell(6, 0 ));
		experimentOrder.add(new Cell(4, 5 ));
		experimentOrder.add(new Cell(5, 8 ));
		experimentOrder.add(new Cell(2, 10 ));
		experimentOrder.add(new Cell(6, 10 ));
		experimentOrder.add(new Cell(3, 8 ));
		experimentOrder.add(new Cell(0, 3 ));
		experimentOrder.add(new Cell(0, 0 ));
		
		/* 41-50 */
		experimentOrder.add(new Cell(5, 0 ));
		experimentOrder.add(new Cell(4, 7 ));
		experimentOrder.add(new Cell(0, 10 ));
		experimentOrder.add(new Cell(3, 5 ));
		experimentOrder.add(new Cell(0, 8 ));
		experimentOrder.add(new Cell(4, 9 ));
		experimentOrder.add(new Cell(0, 9 ));
		experimentOrder.add(new Cell(4, 10 ));
		experimentOrder.add(new Cell(5, 9 ));
		experimentOrder.add(new Cell(5, 7 ));
		
		/* 51-60 */
		experimentOrder.add(new Cell(3, 9 ));
		experimentOrder.add(new Cell(6, 3 ));
		experimentOrder.add(new Cell(1, 1 ));
		experimentOrder.add(new Cell(6, 2 ));
		experimentOrder.add(new Cell(3, 7 ));
		experimentOrder.add(new Cell(6, 8 ));
		experimentOrder.add(new Cell(1, 7 ));
		experimentOrder.add(new Cell(2, 9 ));
		experimentOrder.add(new Cell(4, 1 ));
		experimentOrder.add(new Cell(6, 7 ));
		
		/* 61-70 */
		experimentOrder.add(new Cell(0, 2 ));
		experimentOrder.add(new Cell(6, 1 ));
		experimentOrder.add(new Cell(2, 0 ));
		experimentOrder.add(new Cell(6, 9 ));
		experimentOrder.add(new Cell(1, 3 ));
		experimentOrder.add(new Cell(6, 6 ));
		experimentOrder.add(new Cell(1, 8 ));
		experimentOrder.add(new Cell(5, 10 ));
		experimentOrder.add(new Cell(3, 0 ));
		experimentOrder.add(new Cell(2, 1 ));	
		
		return experimentOrder;

	}
	
	/**
	 * Maps to EO-4 on Alvin's design. 
	 * Used by GridConfig 4 (clicks )and 8 (hover)
	 * sets a unique trial ordering for the experiment.
	 * 1D array of cells that correspond to buttons in the grid (cell.row, cell.col). 
	 */
	private static ArrayList<Cell> getProductionExperimentOrder4()
	{
		ArrayList<Cell> experimentOrder = new ArrayList<Cell>();
		/* 1-10 */
		experimentOrder.add(new Cell(3, 5 ));
		experimentOrder.add(new Cell(1, 6 ));
		experimentOrder.add(new Cell(3, 8 ));
		experimentOrder.add(new Cell(5, 6 ));
		experimentOrder.add(new Cell(2, 6 ));
		experimentOrder.add(new Cell(2, 9 ));
		experimentOrder.add(new Cell(3, 6 ));
		experimentOrder.add(new Cell(0, 8 ));
		experimentOrder.add(new Cell(1, 4 ));
		experimentOrder.add(new Cell(3, 2 ));
		
		/* 11-20 */
		experimentOrder.add(new Cell(0, 2 ));
		experimentOrder.add(new Cell(3, 4 ));
		experimentOrder.add(new Cell(0, 6 ));
		experimentOrder.add(new Cell(2, 8 ));
		experimentOrder.add(new Cell(4, 10 ));
		experimentOrder.add(new Cell(5, 8 ));
		experimentOrder.add(new Cell(5, 3 ));
		experimentOrder.add(new Cell(4, 0 ));
		experimentOrder.add(new Cell(6, 0 ));
		experimentOrder.add(new Cell(4, 4 ));
		
		/* 21-30 */
		experimentOrder.add(new Cell(1, 0 ));
		experimentOrder.add(new Cell(4, 1 ));
		experimentOrder.add(new Cell(0, 0 ));
		experimentOrder.add(new Cell(3, 1 ));
		experimentOrder.add(new Cell(2, 4 ));
		experimentOrder.add(new Cell(6, 4 ));
		experimentOrder.add(new Cell(5, 5 ));
		experimentOrder.add(new Cell(1, 3 ));
		experimentOrder.add(new Cell(2, 7 ));
		experimentOrder.add(new Cell(4, 8 ));
		
		/* 31-40 */
		experimentOrder.add(new Cell(3, 3 ));
		experimentOrder.add(new Cell(0, 1 ));
		experimentOrder.add(new Cell(2, 0 ));
		experimentOrder.add(new Cell(4, 3 ));
		experimentOrder.add(new Cell(0, 7 ));
		experimentOrder.add(new Cell(4, 6 ));
		experimentOrder.add(new Cell(0, 10 ));
		experimentOrder.add(new Cell(4, 7 ));
		experimentOrder.add(new Cell(5, 1 ));
		experimentOrder.add(new Cell(6, 3 ));
		
		/* 41-50 */
		experimentOrder.add(new Cell(2, 2 ));
		experimentOrder.add(new Cell(5, 2 ));
		experimentOrder.add(new Cell(3, 7 ));
		experimentOrder.add(new Cell(6, 7 ));
		experimentOrder.add(new Cell(3, 10 ));
		experimentOrder.add(new Cell(5, 9 ));
		experimentOrder.add(new Cell(6, 6 ));
		experimentOrder.add(new Cell(3, 9 ));
		experimentOrder.add(new Cell(6, 5 ));
		experimentOrder.add(new Cell(0, 5 ));
		
		/* 51-60 */
		experimentOrder.add(new Cell(6, 9 ));
		experimentOrder.add(new Cell(1, 10 ));
		experimentOrder.add(new Cell(5, 7 ));
		experimentOrder.add(new Cell(0, 4 ));
		experimentOrder.add(new Cell(6, 10 ));
		experimentOrder.add(new Cell(2, 5 ));
		experimentOrder.add(new Cell(6, 1 ));
		experimentOrder.add(new Cell(5, 10 ));
		experimentOrder.add(new Cell(6, 8 ));
		experimentOrder.add(new Cell(2, 3 ));
		
		/* 61-70 */
		experimentOrder.add(new Cell(0, 9 ));
		experimentOrder.add(new Cell(4, 2 ));
		experimentOrder.add(new Cell(6, 2 ));
		experimentOrder.add(new Cell(1, 1 ));
		experimentOrder.add(new Cell(5, 0 ));
		experimentOrder.add(new Cell(1, 8 ));
		experimentOrder.add(new Cell(1, 9 ));
		experimentOrder.add(new Cell(1, 5 ));
		experimentOrder.add(new Cell(2, 1 ));
		experimentOrder.add(new Cell(1, 2 ));
		
		return experimentOrder;

	}
	
	/**
	 * Grid configuration matches to EO-1 on Alvin's forms
	 * Uses A HYBRID of 40 hovers and 30 Clicks
	 * returns a grid which is a 2D-Arraylist 
	 *  Arrylist containing the rows which contains arraylist containing the widgets
	 * @returns grid in 2D
	 */
	private static ArrayList<ArrayList<ConfigCell>> getGridConfig9()
	{
		ArrayList<ArrayList<ConfigCell>> tmpGrid = new ArrayList<ArrayList<ConfigCell>>();
		ArrayList<ConfigCell> row;
		row = new ArrayList<ConfigCell>();
		/* [0] 0-10 */
		row.add(new ConfigCell(0, 0, Type.HOVER, Size.XLARGE));
		row.add(new ConfigCell(0, 1, Type.HOVER, Size.LARGE));
		row.add(new ConfigCell(0, 2, Type.LEFT_CLICK, Size.MEDIUM));
		row.add(new ConfigCell(0, 3, Type.LEFT_CLICK, Size.SMALL));
		row.add(new ConfigCell(0, 4, Type.HOVER, Size.MEDIUM));
		row.add(new ConfigCell(0, 5, Type.HOVER, Size.SMALL));
		row.add(new ConfigCell(0, 6, Type.RIGHT_CLICK, Size.MEDIUM));
		row.add(new ConfigCell(0, 7, Type.LEFT_CLICK, Size.XLARGE));
		row.add(new ConfigCell(0, 8, Type.HOVER, Size.LARGE));
		row.add(new ConfigCell(0, 9, Type.LEFT_CLICK, Size.MEDIUM));
		row.add(new ConfigCell(0, 10, Type.LEFT_CLICK, Size.XLARGE));
		
		tmpGrid.add(row);
		row = new ArrayList<ConfigCell>();
		/* [1] 0-10 */
		row.add(new ConfigCell(1, 0, Type.RIGHT_CLICK, Size.MEDIUM));
		row.add(new ConfigCell(1, 1, Type.HOVER, Size.LARGE));
		row.add(new ConfigCell(1, 2, Type.RIGHT_CLICK, Size.XLARGE));
		row.add(new ConfigCell(1, 3, Type.HOVER, Size.MEDIUM));
		row.add(new ConfigCell(1, 4, Type.RIGHT_CLICK, Size.LARGE));
		row.add(new ConfigCell(1, 5, Type.HOVER, Size.MEDIUM));
		row.add(new ConfigCell(1, 6, Type.HOVER, Size.LARGE));
		row.add(new ConfigCell(1, 7, Type.HOVER, Size.MEDIUM));
		row.add(new ConfigCell(1, 8, Type.HOVER, Size.XLARGE));
		row.add(new ConfigCell(1, 9, Type.HOVER, Size.SMALL));
		row.add(new ConfigCell(1, 10, Type.LEFT_CLICK, Size.LARGE));
		
		tmpGrid.add(row);
		row = new ArrayList<ConfigCell>();
		/* [2] 0-10 */
		row.add(new ConfigCell(2, 0, Type.HOVER, Size.LARGE));
		row.add(new ConfigCell(2, 1, Type.HOVER, Size.SMALL));
		row.add(new ConfigCell(2, 2, Type.RIGHT_CLICK, Size.LARGE));
		row.add(new ConfigCell(2, 3, Type.HOVER, Size.SMALL));
		row.add(new ConfigCell(2, 4, Type.HOVER, Size.MEDIUM));
		row.add(new ConfigCell(2, 5, Type.STATUSCELL, Size.XLARGE)); // ** //
		row.add(new ConfigCell(2, 6, Type.LEFT_CLICK, Size.LARGE));
		row.add(new ConfigCell(2, 7, Type.RIGHT_CLICK, Size.SMALL));
		row.add(new ConfigCell(2, 8, Type.HOVER, Size.LARGE));
		row.add(new ConfigCell(2, 9, Type.HOVER, Size.LARGE));
		row.add(new ConfigCell(2, 10, Type.HOVER, Size.MEDIUM));
		
		tmpGrid.add(row);
		row = new ArrayList<ConfigCell>();
		/* [3] 0-10 */
		row.add(new ConfigCell(3, 0, Type.HOVER, Size.MEDIUM));
		row.add(new ConfigCell(3, 1, Type.HOVER, Size.XLARGE));
		row.add(new ConfigCell(3, 2, Type.HOVER, Size.LARGE));
		row.add(new ConfigCell(3, 3, Type.LEFT_CLICK, Size.XLARGE));
		row.add(new ConfigCell(3, 4, Type.HOVER, Size.LARGE));
		row.add(new ConfigCell(3, 5, Type.HOVER, Size.XLARGE));
		row.add(new ConfigCell(3, 6, Type.LEFT_CLICK, Size.MEDIUM)); 
		row.add(new ConfigCell(3, 7, Type.HOVER, Size.XLARGE));
		row.add(new ConfigCell(3, 8, Type.HOVER, Size.MEDIUM));
		row.add(new ConfigCell(3, 9, Type.HOVER, Size.XLARGE));
		row.add(new ConfigCell(3, 10, Type.LEFT_CLICK, Size.SMALL));
		
		tmpGrid.add(row);
		row=new ArrayList<ConfigCell>(); 
		/* [4] 0-10 */
		row.add(new ConfigCell(4, 0, Type.RIGHT_CLICK, Size.SMALL));
		row.add(new ConfigCell(4, 1, Type.HOVER, Size.LARGE));
		row.add(new ConfigCell(4, 2, Type.HOVER, Size.MEDIUM));
		row.add(new ConfigCell(4, 3, Type.HOVER, Size.SMALL));
		row.add(new ConfigCell(4, 4, Type.RIGHT_CLICK, Size.LARGE));
		row.add(new ConfigCell(4, 5, Type.HOVER, Size.SMALL));
		row.add(new ConfigCell(4, 6, Type.LEFT_CLICK, Size.LARGE));
		row.add(new ConfigCell(4, 7, Type.HOVER, Size.SMALL));
		row.add(new ConfigCell(4, 8, Type.LEFT_CLICK, Size.LARGE));
		row.add(new ConfigCell(4, 9, Type.LEFT_CLICK, Size.MEDIUM));
		row.add(new ConfigCell(4, 10, Type.HOVER, Size.LARGE));
		
		tmpGrid.add(row);
		row = new ArrayList<ConfigCell>();
		/* [5] 0-10 */
		row.add(new ConfigCell(5, 0, Type.LEFT_CLICK, Size.MEDIUM));
		row.add(new ConfigCell(5, 1, Type.HOVER, Size.SMALL));
		row.add(new ConfigCell(5, 2, Type.HOVER, Size.XLARGE));
		row.add(new ConfigCell(5, 3, Type.RIGHT_CLICK, Size.MEDIUM));
		row.add(new ConfigCell(5, 4, Type.LEFT_CLICK, Size.LARGE));
		row.add(new ConfigCell(5, 5, Type.HOVER, Size.MEDIUM));
		row.add(new ConfigCell(5, 6, Type.RIGHT_CLICK, Size.LARGE));
		row.add(new ConfigCell(5, 7, Type.HOVER, Size.MEDIUM));
		row.add(new ConfigCell(5, 8, Type.HOVER, Size.XLARGE));
		row.add(new ConfigCell(5, 9, Type.HOVER, Size.SMALL));
		row.add(new ConfigCell(5, 10, Type.RIGHT_CLICK, Size.MEDIUM));
		
		tmpGrid.add(row);
		row = new ArrayList<ConfigCell>();
		/* [6] 0-10 */
		row.add(new ConfigCell(6, 0, Type.HOVER, Size.XLARGE));
		row.add(new ConfigCell(6, 1, Type.HOVER, Size.MEDIUM));
		row.add(new ConfigCell(6, 2, Type.RIGHT_CLICK, Size.SMALL));
		row.add(new ConfigCell(6, 3, Type.HOVER, Size.MEDIUM));
		row.add(new ConfigCell(6, 4, Type.RIGHT_CLICK, Size.LARGE));
		row.add(new ConfigCell(6, 5, Type.HOVER, Size.SMALL));
		row.add(new ConfigCell(6, 6, Type.HOVER, Size.MEDIUM));
		row.add(new ConfigCell(6, 7, Type.HOVER, Size.XLARGE));
		row.add(new ConfigCell(6, 8, Type.HOVER, Size.LARGE));
		row.add(new ConfigCell(6, 9, Type.RIGHT_CLICK, Size.MEDIUM));
		row.add(new ConfigCell(6, 10, Type.RIGHT_CLICK, Size.XLARGE));
		
		tmpGrid.add(row);
		row = new ArrayList<ConfigCell>();
		return tmpGrid;
	}

	/**
	 * Grid configuration matches to EO-2 on Alvin's forms
	 * Uses HOVERS ONLY
	 * returns a grid which is a 2D-Arraylist 
	 *  Arrylist containing the rows which contains arraylist containing the widgets
	 * @returns grid in 2D
	 */
	private static ArrayList<ArrayList<ConfigCell>> getGridConfig10()
	{
		ArrayList<ArrayList<ConfigCell>> tmpGrid = new ArrayList<ArrayList<ConfigCell>>();
		ArrayList<ConfigCell> row;
		row = new ArrayList<ConfigCell>();
		/* [0] 0-10 */
		row.add(new ConfigCell(0, 0, Type.RIGHT_CLICK, Size.XLARGE));
		row.add(new ConfigCell(0, 1, Type.LEFT_CLICK, Size.XLARGE));
		row.add(new ConfigCell(0, 2, Type.HOVER, Size.SMALL));
		row.add(new ConfigCell(0, 3, Type.LEFT_CLICK, Size.MEDIUM));
		row.add(new ConfigCell(0, 4, Type.HOVER, Size.XLARGE));
		row.add(new ConfigCell(0, 5, Type.HOVER, Size.SMALL));
		row.add(new ConfigCell(0, 6, Type.HOVER, Size.XLARGE));
		row.add(new ConfigCell(0, 7, Type.RIGHT_CLICK, Size.MEDIUM));
		row.add(new ConfigCell(0, 8, Type.HOVER, Size.SMALL));
		row.add(new ConfigCell(0, 9, Type.LEFT_CLICK, Size.LARGE));
		row.add(new ConfigCell(0, 10, Type.HOVER, Size.XLARGE));
		
		tmpGrid.add(row);
		row = new ArrayList<ConfigCell>();
		/* [1] 0-10 */
		row.add(new ConfigCell(1, 0, Type.HOVER, Size.SMALL));
		row.add(new ConfigCell(1, 1, Type.HOVER, Size.LARGE));
		row.add(new ConfigCell(1, 2, Type.HOVER, Size.XLARGE));
		row.add(new ConfigCell(1, 3, Type.HOVER, Size.SMALL));
		row.add(new ConfigCell(1, 4, Type.LEFT_CLICK, Size.LARGE));
		row.add(new ConfigCell(1, 5, Type.HOVER, Size.XLARGE));
		row.add(new ConfigCell(1, 6, Type.HOVER, Size.LARGE));
		row.add(new ConfigCell(1, 7, Type.HOVER, Size.SMALL));
		row.add(new ConfigCell(1, 8, Type.HOVER, Size.XLARGE));
		row.add(new ConfigCell(1, 9, Type.LEFT_CLICK, Size.MEDIUM));
		row.add(new ConfigCell(1, 10, Type.LEFT_CLICK, Size.SMALL));
		
		tmpGrid.add(row);
		row = new ArrayList<ConfigCell>();
		/* [2] 0-10 */
		row.add(new ConfigCell(2, 0, Type.HOVER, Size.LARGE));
		row.add(new ConfigCell(2, 1, Type.HOVER, Size.MEDIUM));
		row.add(new ConfigCell(2, 2, Type.RIGHT_CLICK, Size.MEDIUM));
		row.add(new ConfigCell(2, 3, Type.LEFT_CLICK, Size.SMALL));
		row.add(new ConfigCell(2, 4, Type.HOVER, Size.LARGE));
		row.add(new ConfigCell(2, 5, Type.HOVER, Size.XLARGE));
		row.add(new ConfigCell(2, 6, Type.LEFT_CLICK, Size.LARGE));
		row.add(new ConfigCell(2, 7, Type.RIGHT_CLICK, Size.SMALL));
		row.add(new ConfigCell(2, 8, Type.RIGHT_CLICK, Size.MEDIUM));
		row.add(new ConfigCell(2, 9, Type.HOVER, Size.LARGE));
		row.add(new ConfigCell(2, 10, Type.HOVER, Size.LARGE));
		
		tmpGrid.add(row);
		row = new ArrayList<ConfigCell>();
		/* [3] 0-10 */
		row.add(new ConfigCell(3, 0, Type.HOVER, Size.MEDIUM));
		row.add(new ConfigCell(3, 1, Type.HOVER, Size.SMALL));
		row.add(new ConfigCell(3, 2, Type.LEFT_CLICK, Size.LARGE));
		row.add(new ConfigCell(3, 3, Type.HOVER, Size.XLARGE));
		row.add(new ConfigCell(3, 4, Type.HOVER, Size.MEDIUM));
		row.add(new ConfigCell(3, 5, Type.STATUSCELL, Size.XLARGE)); //**//
		row.add(new ConfigCell(3, 6, Type.LEFT_CLICK, Size.MEDIUM)); 
		row.add(new ConfigCell(3, 7, Type.HOVER, Size.XLARGE));
		row.add(new ConfigCell(3, 8, Type.RIGHT_CLICK, Size.LARGE));
		row.add(new ConfigCell(3, 9, Type.LEFT_CLICK, Size.SMALL));
		row.add(new ConfigCell(3, 10, Type.HOVER, Size.MEDIUM));
		
		tmpGrid.add(row);
		row=new ArrayList<ConfigCell>(); 
		/* [4] 0-10 */
		row.add(new ConfigCell(4, 0, Type.HOVER, Size.LARGE));
		row.add(new ConfigCell(4, 1, Type.HOVER, Size.MEDIUM));
		row.add(new ConfigCell(4, 2, Type.HOVER, Size.LARGE));
		row.add(new ConfigCell(4, 3, Type.HOVER, Size.XLARGE));
		row.add(new ConfigCell(4, 4, Type.LEFT_CLICK, Size.MEDIUM));
		row.add(new ConfigCell(4, 5, Type.RIGHT_CLICK, Size.LARGE));
		row.add(new ConfigCell(4, 6, Type.HOVER, Size.MEDIUM));
		row.add(new ConfigCell(4, 7, Type.RIGHT_CLICK, Size.XLARGE));
		row.add(new ConfigCell(4, 8, Type.HOVER, Size.LARGE));
		row.add(new ConfigCell(4, 9, Type.HOVER, Size.MEDIUM));
		row.add(new ConfigCell(4, 10, Type.HOVER, Size.LARGE));
		
		tmpGrid.add(row);
		row = new ArrayList<ConfigCell>();
		/* [5] 0-10 */
		row.add(new ConfigCell(5, 0, Type.HOVER, Size.SMALL));
		row.add(new ConfigCell(5, 1, Type.HOVER, Size.MEDIUM));
		row.add(new ConfigCell(5, 2, Type.HOVER, Size.MEDIUM));
		row.add(new ConfigCell(5, 3, Type.RIGHT_CLICK, Size.LARGE));
		row.add(new ConfigCell(5, 4, Type.HOVER, Size.SMALL));
		row.add(new ConfigCell(5, 5, Type.HOVER, Size.MEDIUM));
		row.add(new ConfigCell(5, 6, Type.HOVER, Size.SMALL));
		row.add(new ConfigCell(5, 7, Type.RIGHT_CLICK, Size.LARGE));
		row.add(new ConfigCell(5, 8, Type.RIGHT_CLICK, Size.MEDIUM));
		row.add(new ConfigCell(5, 9, Type.RIGHT_CLICK, Size.LARGE));
		row.add(new ConfigCell(5, 10, Type.HOVER, Size.SMALL));
		
		tmpGrid.add(row);
		row = new ArrayList<ConfigCell>();
		/* [6] 0-10 */
		row.add(new ConfigCell(6, 0, Type.HOVER, Size.XLARGE));
		row.add(new ConfigCell(6, 1, Type.RIGHT_CLICK, Size.XLARGE));
		row.add(new ConfigCell(6, 2, Type.LEFT_CLICK, Size.MEDIUM));
		row.add(new ConfigCell(6, 3, Type.LEFT_CLICK, Size.LARGE));
		row.add(new ConfigCell(6, 4, Type.HOVER, Size.SMALL));
		row.add(new ConfigCell(6, 5, Type.RIGHT_CLICK, Size.MEDIUM));
		row.add(new ConfigCell(6, 6, Type.RIGHT_CLICK, Size.SMALL));
		row.add(new ConfigCell(6, 7, Type.HOVER, Size.LARGE));
		row.add(new ConfigCell(6, 8, Type.HOVER, Size.MEDIUM));
		row.add(new ConfigCell(6, 9, Type.HOVER, Size.XLARGE));
		row.add(new ConfigCell(6, 10, Type.LEFT_CLICK, Size.XLARGE));
		
		tmpGrid.add(row);
		row = new ArrayList<ConfigCell>();
		return tmpGrid;
	}
	
}
