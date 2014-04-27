/**
 * 
 */
package edu.baylor.hci.Logger;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.Iterator;

import org.apache.log4j.Logger;
import org.ejml.data.DenseMatrix64F;
import edu.baylor.hci.LeapOMatic.Settings;



/**
 * This is a general / generic Logger that logs to the mfile format used by Matlab
 * @author Alvin
 *
 */
public abstract class MFileLogger 
{
	public enum LogStatus { IDLE, START, STOP, PAUSE; }
	/** object vars **/
	private ArrayList<String> comments = new ArrayList<String>();

	
	private LogStatus logStatus=LogStatus.IDLE;
	// full name of the log file. Calculated dynamically here. 
	private String logfile;
	// Buffered Writer, all appends here before to the outfile. 
	private BufferedWriter outfile;
	// Logger as usual. 
	final static Logger logger = Logger.getLogger(MFileLogger.class);

	/**
	 * The full name of the file is calculated here. That way, all logfiles are standardized in their naming conventions
	 * @param filePrefix
	 */
	protected void setFilePrefix(String logfilePrefix) 
	{
		// if the directory does not exist, create it or die trying. 
		File dir = new File(Settings.LOGFILE_DIR); 
		if (!dir.exists()) 
		{
			logger.debug("creating directory: " + dir);
			if(dir.mkdir()) 
			{    
				logger.debug("dir created");  
			} 
			else 
			{
				logger.fatal("Could not create directory");
				System.exit(1);
			}
		} 
		else 
		{
			logger.debug("Dir exists");
		}
		
		Date dateNow = new Date();
		SimpleDateFormat dateFormat = new SimpleDateFormat(Settings.DATE_FORMAT);
        StringBuilder strDatenow = new StringBuilder(dateFormat.format(dateNow));
        
		this.logfile = dir + File.separator + logfilePrefix + "_" + strDatenow + ".m";

	}
	
	/**
	 * Actually appends the string to the outfile
	 * One Function to rule them all
	 * @param value
	 */
	private final void writeFile(String value) 
	{

		try 
		{
			this.outfile.append(value);
		} 
		catch (IOException e) 
		{
			logger.fatal(e.getMessage());
			e.printStackTrace();
			System.exit(1);
		}
	}
	
	/**
	 * Open the file for writing. Should logically be called by any Write() function by the children of this class
	 */
	private final void openFile() 
	{
		// first disable the addition to the log.
		logStatus=LogStatus.IDLE;
		// init the output file
		FileWriter fstream = null;
		try 
		{
			fstream = new FileWriter(this.logfile, true); // use append mode
		} 
		catch (IOException e) 
		{
			e.printStackTrace();
			logger.fatal(e.getMessage());
			System.exit(1);
		}
		this.outfile = new BufferedWriter(fstream);

	}
	
	/**
	 * Close the outfile once we're done. 
	 * If this fails, we're in big trouble. So. Some form of HUGE failhard should be included. 
	 */
	private final void closeFile() 
	{
		try 
		{
			this.outfile.close();
		} 
		catch (IOException e) 
		{
			e.printStackTrace();
			logger.fatal(e.getMessage());
			System.exit(1);
		}
	}
	

	/**
	 * Adds any comments to this file. 
	 * Will (or should) be prepended to the top of the file
	 * @param comment
	 */
	public final void addComment(String comment)
	{
		this.comments.add(comment);
	}

	/**
	 * Writes to file this one Variable which is stored in a Long format
	 * @param varname
	 * @param arraylist
	 */
	protected void writeLongVars(String varname, ArrayList<Long> arraylist) 
	{
		// 1. Open File
		this.openFile();
		
		// 2. write contents (this one var in string format)
		writeFile(varname + " = [");
		
		Iterator<Long> iterator = arraylist.iterator();
		while(iterator.hasNext()) {
			this.writeFile(iterator.next().toString());
			if(iterator.hasNext()) 
			{
				this.writeFile(",");
			}
		}
		this.writeFile("]");
		this.writeFile("\n");
		
		// 3. close file
		this.closeFile();
	}
	
	/**
	 * Writes to file this one Variable which is stored in a Long format
	 * @param varname
	 * @param arraylist
	 */
	protected void writeIntegerVars(String varname, ArrayList<Integer> arraylist) 
	{
		// 1. Open File
		this.openFile();
		
		// 2. write var as string
		writeFile(varname + " = [");
		
		Iterator<Integer> iterator = arraylist.iterator();
		while(iterator.hasNext()) 
		{
			writeFile(iterator.next().toString());
			if(iterator.hasNext()) 
			{
				writeFile(",");
			}
		}
		writeFile("]");
		writeFile("\n");

		// 3. close file
		this.closeFile();
	}
	
	/**
	 * Writes the values of this particular variable to the file in float format.
	 * @param varname
	 * @param arraylist
	 */
	protected void writeFloatVars(String varname, ArrayList<Float> arraylist) 
	{
		// write this one var
		this.openFile();
		writeFile(varname + " = [");
		
		Iterator<Float> iterator = arraylist.iterator();
		while(iterator.hasNext()) 
		{
			writeFile(iterator.next().toString());
			if(iterator.hasNext()) 
			{
				writeFile(",");
			}
		}
		writeFile("]");
		writeFile("\n");
		// flush per var
		this.closeFile();
	}
	
	protected void writeDoubleVars(String varname, ArrayList<Double> arraylist) 
	{
		// write this one var
		this.openFile();
		writeFile(varname + " = [");
		
		Iterator<Double> iterator = arraylist.iterator();
		while(iterator.hasNext()) 
		{
			writeFile(iterator.next().toString());
			if(iterator.hasNext()) 
			{
				writeFile(",");
			}
		}
		writeFile("]");
		writeFile("\n");
		// flush per var
		this.closeFile();
	}
	protected void writeVariableVars(String varname, ArrayList<String> arraylist) 
	{
		// write this one var
		this.openFile();
		writeFile(varname + " = [");
		
		Iterator<String> iterator = arraylist.iterator();
		while(iterator.hasNext()) 
		{
			writeFile(iterator.next().toString());
			if(iterator.hasNext()) 
			{
				writeFile(",");
			}
		}
		writeFile("]");
		writeFile("\n");
		// flush per var
		this.closeFile();
	}
	
	protected void writeScalar(String varname, double value)
	{
		this.openFile();
		writeFile(varname+" = "+value+"\n");
		this.closeFile();
	}
	
	
	public void writeMatrix(DenseMatrix64F m, String label)
	{
		this.openFile();
		//this.writeDoubleVars(label,m.data);
		writeFile(label + " = [");
		for(int row=0;row<m.numRows;row++)
		{
			for(int col=0;col<m.numCols;col++)
			{
				writeFile(" "+m.get(row, col));
			}
			writeFile(";");
		}
		writeFile("] \n");
		this.closeFile();
	}
	/**
	 * writes basically a matrix containing vectors of unknown size(not uniform) used in mouseLogger for position and time vectors for Integer Datatypes
	 * @param vectors
	 * @param Label
	 */
	protected void writeIntegerVectors(String label, ArrayList<ArrayList<Integer>> vectors)
	{
		// rows. obvious count
		int rows = vectors.size();
		// cols - This is less obvious. We need to take the largest row as col
		int cols = 0;
		for(ArrayList<Integer> vector : vectors) 
		{
			int vectorsize = vector.size();
			if(vectorsize > cols) cols = vectorsize;
		}
		
		// place items into double
		double[][] doubleMatrix = new double[rows][cols];
		
		for(int row = 0; row < rows; row++) 
		{
			for(int col = 0; col < cols; col++)
			{
				/* if there's a value, set it. Else use NAN
				 * This creates a sparse matrix with lots of empty cells 
				 * 
				 */
				if(col < vectors.get(row).size())
				{
					doubleMatrix[row][col] = vectors.get(row).get(col);
				}
				else
				{
					doubleMatrix[row][col] = Double.NaN;
				}
			}
		}
		DenseMatrix64F matrix = new DenseMatrix64F(doubleMatrix);
		writeMatrix(matrix, label);
		
		/**
		 * This is the initial implementation of this function
		 * Keeping it here just in case we need to use it ever again. 
		int i=0;
		for(ArrayList<Integer> vec : vectors)
		{
			writeIntegerVars(label+i++, vec);
		}
		**/
	}
	/**
	 * writes basically a matrix containing vectors of unknown size(not uniform) used in mouseLogger for position and time vectors for long datatypes
	 * @param label
	 * @param vectors
	 */
	protected void writeLongVectors( String label , ArrayList<ArrayList<Long>> vectors)
	{
		int rows = vectors.size();
		// cols - This is less obvious. We need to take the largest row as col
		int cols = 0;
		for(ArrayList<Long> vector : vectors) 
		{
			int vectorsize = vector.size();
			if(vectorsize > cols) cols = vectorsize;
		}
		
		// place items into double
		double[][] doubleMatrix = new double[rows][cols];
		
		for(int row = 0; row < rows; row++) 
		{
			for(int col = 0; col < cols; col++)
			{
				/* if there's a value, set it. Else use NAN
				 * This creates a sparse matrix with lots of empty cells 
				 * 
				 */
				if(col < vectors.get(row).size())
				{
					doubleMatrix[row][col] = vectors.get(row).get(col);
				}
				else
				{
					doubleMatrix[row][col] = Double.NaN;
				}
			}
		}
		DenseMatrix64F matrix = new DenseMatrix64F(doubleMatrix);
		writeMatrix(matrix, label);
		
		
		/*
		int i=0;
		for(ArrayList<Long> vec : vectors)
		{
			writeLongVars(label+i++, vec);
		}*/
	}
	/**
	 * Triggers the write comments actions to append the commented to the bufferedwriter
	 */
	protected void writeComments()
	{
		// 1. open file 
		this.openFile();
		// 2. write contents
		for(String comment :comments){
			this.writeFile("%"+comment +"\n");
		}
		// 3. close file
		this.closeFile();
	}

	public LogStatus getLogStatus() 
	{
		return logStatus;
	}

	public void setLogStatus(LogStatus logStatus)
	{
		this.logStatus = logStatus;
	}
	
	/**
	 * Returns the euclidean distance between 2 points. 
	 * Uses integers because of the context: we measure in Pixels, floating precision is pointless
	 * @param x1
	 * @param y1
	 * @param x2
	 * @param y2
	 * @return euclidean Distance 
	 */
	protected int getDistance(int x1, int y1, int x2, int y2)
	{
		int xDist = x1 - x2;
		int yDist = y1 - y2; 
		return (int) Math.sqrt(xDist * xDist + yDist * yDist);	
	}

}
