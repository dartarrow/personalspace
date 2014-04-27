package edu.baylor.hci.Calibration;

import java.util.Arrays;

import org.apache.log4j.Logger;
import org.ejml.data.DenseMatrix64F;
import org.ejml.ops.CommonOps;
import com.leapmotion.leap.Vector;

/**
 * This class builds the transformation matrix based on different parameters. 
 * The innerworkings of the matrix will be abstracted once initialized. 
 * @author Alvin
 *
 */
public class TransformationMatrix {
	/** 
	 * this enum is required because we'll have different algos here which will be abstracted from the user
	 * with this one exception that they will need to select the MATRIX_TYPE during instantiation
	 */
	public enum MATRIX_TYPE 
	{
		NORMALIZED_Z, // DEFAULT normalize the Z, where we pre-populate a Z value that is actually ON the generated plane (given x & y generate z using calcZTransform matrix to guarantee always directly on plane[issues occurred when directly using z from 3D space])
		IGNORE_Z, // completely ignore the Z value during the generation of the matrix as well as during the transformation
		REGULAR, //  Build a Regular matrix with regular calculation. The Z value will be factored in this calculation. 
	}
	private MATRIX_TYPE matrixType;
	
	/** class constants **/
	public static final int SOURCE_MATRIX_ROW_SIZE = 4;
	public static final int SOURCE_MATRIX_COL_SIZE = 3;
	
	
	/* the transformation matrix we build here */
	private DenseMatrix64F transformationMatrix;
	/* the source matrix, used in the transformation calculation. Is the four coordinates collected during calibration */
	private DenseMatrix64F sourceMatrix;
	/* the destination matrix, used in the transformation calculation. Represents the screen size */
	private DenseMatrix64F destinationMatrix;
	//These three matrices are used to construct the functionality of given (x, y)=>z. So that we can map to the expected Z. This is for NORMALIZED_Z matrix_type
	private DenseMatrix64F calcZSource;
	private DenseMatrix64F calcZDest;
	private DenseMatrix64F calcZTransform;

    private static final Logger logger = Logger.getLogger(TransformationMatrix.class);
	/**
	 * Constructor requires us to set the type of calculation we want. 
	 * Set this to fail hard so that there is no chance of incorrect setting
	 * @param matrixType
	 */
	public TransformationMatrix(MATRIX_TYPE matrixType) 
	{
		this.matrixType = matrixType;
	}
	
	/**
	 * Takes one matrix, but depending upon the given MATRIX_TYPE has two different functions
	 * NORMAL: turns the 4x3 array into a 4x4 array(1 padded) and sets the source matrix
	 * IGNORE_Z: changes Z values [2] index with 0;
	 * NORMALIZED_Z: Takes the source x, y , z, creates a transformationMatrix for calculating Z based on x and y, and sets the sourceMatrix.
	 * Explicitly requires a 4x3 2D array
	 * So basically:
	 * 	{{ X1 Y1 Z1 }
	 *   { X2 Y2 Z2 }
	 *   { X3 Y3 Z3 }
	 *   { X4 Y4 Z4 }}
	 * If there is a need for padding, it will be done here within these walls
	 * @param sourceMatrix - 4x3 matrix, the 4 points of our input coords in 3D
	 * @throws Exception 
	 */
	public void setInputMatrix(double[][] doubleinput) throws Exception 
	{
		/* Check the sizes of the array */
		if(doubleinput.length != SOURCE_MATRIX_ROW_SIZE) 
			throw new Exception("Row must be " + SOURCE_MATRIX_ROW_SIZE + " Currently " + doubleinput.length);
		if(doubleinput[0].length != SOURCE_MATRIX_COL_SIZE) 
			throw new Exception("Cols must be " + SOURCE_MATRIX_COL_SIZE +" Currently " + doubleinput.length);
		
		System.out.println("Original Source");
		logger.debug("Original trans matrix: " + Arrays.deepToString(doubleinput));
		// Add the X and Z offsets
		for(int c = 0; c < SOURCE_MATRIX_ROW_SIZE; c++) 
		{
			System.out.print(doubleinput[c][0]+" ");
			System.out.print(doubleinput[c][1]+" ");
			System.out.println(doubleinput[c][2]);
		}
		logger.debug("Matrix after offsets " + Arrays.deepToString(doubleinput));
		
		// we need a 4x4. This is a 4x3. Some transcoding ensues
		double[][] newDoubleinput = new double[4][4];
		for(int c = 0; c < SOURCE_MATRIX_ROW_SIZE; c++) 
		{
			newDoubleinput[c] = Arrays.copyOf(doubleinput[c], 4);
			//pad it with 1's
			newDoubleinput[c][3] = 1;
		}
		logger.debug("transcoded to 4x4: " + Arrays.deepToString(newDoubleinput));
		
		// if we're ignoring the Z-axis, then set the Z values here to 1
		if(this.matrixType == MATRIX_TYPE.IGNORE_Z) 
		{
			for(int c = 0; c < SOURCE_MATRIX_ROW_SIZE; c++) 
			{
				newDoubleinput[c][2] = 0;
			}
			logger.debug("Ignoring Z-values: " + Arrays.deepToString(newDoubleinput));	
		}
		
		/*
		 * Builds calcZTransform so that we can generate a Z value given x & y from source and destination matrices
		 */
		if(this.matrixType==MATRIX_TYPE.NORMALIZED_Z)
		{
			/* The Z transformation matrix is described below. 
			 * Basically Given X and Y, find Z.
			 * 1. Build Source Matrix: a 4x4 matrix with the 4 points
			 *    then we remove the Z values
			 * 2. Build Destination Matrix: a 4x1 matrix with our Z values
			 * 3. From (S) and (D) find (T)
			 *    (T) will be a 4x1 matrix
			 */
			double[][] normalDest = new double[4][1];
			double[][] normalSource = new double[4][4];
			for(int c = 0; c < SOURCE_MATRIX_ROW_SIZE; c++) 
			{
				normalSource[c][0] = doubleinput[c][0];
				normalSource[c][1] = doubleinput[c][1];
				normalSource[c][2] = 1; //1 padded
				normalSource[c][3] = 1; //1 padded
				normalDest[c][0] = doubleinput[c][2];
			}
			
			this.calcZSource = new DenseMatrix64F(normalSource);
			this.calcZDest = new DenseMatrix64F(normalDest);
			this.calcZTransform = calculateZTransformation(this.calcZSource, this.calcZDest);
		}
		
		
		this.sourceMatrix = new DenseMatrix64F(newDoubleinput);
	}
	
	/**
	 * Explicitly requires a 4x3 2D array
	 * @param destinationMatrix
	 */
	public void setOutputMatrix(double[][] doubleoutput) 
	{
		this.destinationMatrix = new DenseMatrix64F(doubleoutput);
	}

	/**
	 * if the transformation matrix is set, return it, 
	 * else calculate, store, return 
	 * @return transformation matrix
	 * @throws Exception 
	 */
	public DenseMatrix64F getTransformationMatrix() throws Exception 
	{
		// if the transformation matrix is not set, then we need to calculate it.
		if(this.transformationMatrix == null) 
		{
			/* the input and output matrices are mandatory. Throw exceptions if they are not found.
			 * If all the prerequisites set, perform calculations. 
			 */
			if(this.sourceMatrix == null) 
				throw new Exception("Input Matrix cannot be null");
			else if(this.destinationMatrix == null) 
				throw new Exception("Output Matrix cannot be null");
			else 
				this.transformationMatrix = calculateTransformationMatrix(this.sourceMatrix, this.destinationMatrix);
		}
		
		// at this point, we should have a transformation matrix. If it's still null, then we screwed up. 
		if(this.transformationMatrix == null) 
		{
			throw new Exception("Could not calculate transformation matrix. We screwed up. Frowny face");
		}
		
		return this.transformationMatrix;
	}
	
	/**
	 * Just a check to make sure the SourceMatrix is set
	 * @return true is SourceMatrix already set
	 */
	public boolean sourceMatrixIsSet()
	{
		if(this.sourceMatrix != null)
			return true;
		else
			return false;
	}
	
	/**
	 * Front-end to the various types of transformation algos. 
	 * This should be the ONLY place where the algo related decision making is done
	 * @param source 4x4 Matrix containing the 3D coordinates of the input, with Z pruned out and 1-padded
	 * @param dest 4x3 Matrix containing the Z values which is actually from the same input.
	 * @return 4x3 transformation matrix calculated from source and dest
	 */
	private DenseMatrix64F calculateTransformationMatrix(DenseMatrix64F source, DenseMatrix64F dest) 
	{
		// this inverts the source within the same variable. So in matlab equivalent to A = inv(A) (needs pinv()) 
		//CommonOps.invert(this.sourceMatrix);
		DenseMatrix64F invertedSourceMatrix = new DenseMatrix64F(new double[4][4]);
		CommonOps.pinv(source, invertedSourceMatrix);
		
		// create a transformation matrix. Since this is an empty matrix at first, we just set the dimensions. 
		DenseMatrix64F transMatrix = new DenseMatrix64F(new double[4][3]);
		// perform multiplication here. 
		CommonOps.mult(invertedSourceMatrix, dest, transMatrix);
		transMatrix.print();
		logger.debug("Transformation matrix calculated : " + this.transformationMatrix);
		
		return transMatrix;
	}
	
	/**
	 * This does very very similar calculation to calculateTransformationMatrix() but specifically for the Z transform
	 * @param source 4x4 Matrix containing the 3D coordinates of the input, with Z pruned out and 1-padded
	 * @param dest a 4x1 Matrix containing the Z values which is actually from the same input. 
	 * @return 4x1 transformation matrix calculated from source and dest
	 */
	private DenseMatrix64F calculateZTransformation(DenseMatrix64F source, DenseMatrix64F dest)
	{
		DenseMatrix64F invertedSourceMatrix = new DenseMatrix64F(new double[4][4]);
		CommonOps.pinv(source, invertedSourceMatrix);

		// create a transformation matrix. Since this is an empty matrix at first, we just set the dimensions. 
		DenseMatrix64F transMatrix = new DenseMatrix64F(new double[4][1]);
		// perform multiplication here. 
		CommonOps.mult(invertedSourceMatrix, dest, transMatrix);
		logger.debug("Transformation matrix calculated : " + transMatrix);
		return transMatrix;
		
	}

	/**
	 * This is where the 3-Dimensional input space gets transformed through the matrix. 
	 * This function can be used anywhere where there is a 3D input and 3 point output. 
	 * If used to map gesture position to a screen, then the Z coordinates will just be dropped. 
	 * If used for something else, eg: PalmNormal, then the Z will be used. 
	 * SO: The third value here is useful. Very Useful.
	 * @updated changed to return a vector for limiting code later 
	 * @param xCoord
	 * @param yCoord
	 * @param zCoord
	 * @return Vector with x, y, z.
	 * @throws Exception
	 */
	public Vector getOutputCoordinates(double xCoord, double yCoord, double zCoord) throws Exception 
	{

		double[] inputCoordsArray;
		// Some transcoding to build the Source/Input matrix [4x1]
		Vector outputCoords = new Vector();
		switch(this.matrixType) 
		{
		case REGULAR:
			/* For the regular method, we take the input and parse it exactly. 
			 * No actual transcoding of values, just pass through
			 */
	    	inputCoordsArray = new double[]{xCoord, yCoord, zCoord, 1};
	    	break;
		case IGNORE_Z:
			/* For the ignore-Z method, we ignore the depth
			 * So we just drop the given Z value and pad the matrix with 1
			 */
	    	inputCoordsArray = new double[]{xCoord, yCoord, 1, 1};
	    	break;
		case NORMALIZED_Z:
			/* This is slightly more complicated. 
			 * We ignore the provided Z value and substitute with our own. 
			 * So first we find the expected Z value. 
			 * 	"Given X and Y, find Z"
			 * ** What this does is it basically 'moves' (ie Normalizes) the hand onto the plane
			 * Then from the newly formed input matrix featuring the substituted value we calculate the destination.  
			 */
			double normalInput[][]=new double[1][4];
			normalInput[0][0] = xCoord;
			normalInput[0][1] = yCoord;
			normalInput[0][2] = 1;
			normalInput[0][3] = 1;
			// "Given X and Y..."
			DenseMatrix64F normalSource = new DenseMatrix64F(normalInput);
			DenseMatrix64F newZ = new DenseMatrix64F(new double[1][1]);
			// "... Find Z"
			CommonOps.mult(normalSource, this.calcZTransform, newZ);
			logger.debug("Z-Transform input: " + xCoord + " " + yCoord + " " + zCoord);
			logger.debug("Z-Transform output: "+ newZ.data[0]); // returns a 4x1, we only want the first
			inputCoordsArray = new double[]{xCoord, yCoord, newZ.data[0], 1};
			break;
		default:
			inputCoordsArray = new double[0];
				
		}
    	
    	// build the input 1x4 input matrix. 1-padded so last figure is immaterial 
		DenseMatrix64F inputCoords = new DenseMatrix64F(1, 4, true, inputCoordsArray); // 1X4 matrix
		// create storage space for the results
		DenseMatrix64F outputCoordsArr = new DenseMatrix64F(1, 3, true, new double[3]); // 1X3 matrix
		// do the multiplication
		CommonOps.mult(inputCoords, this.getTransformationMatrix(), outputCoordsArr);
		
		outputCoords.setX((float) outputCoordsArr.get(0));
		outputCoords.setY((float) outputCoordsArr.get(1));
		outputCoords.setZ((float) outputCoordsArr.get(2));
		return outputCoords;

	}
}
