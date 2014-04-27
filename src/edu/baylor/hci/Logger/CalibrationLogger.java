package edu.baylor.hci.Logger;

import java.util.ArrayList;

import net.sf.javaml.core.Dataset;
import net.sf.javaml.core.Instance;

import org.ejml.data.DenseMatrix64F;

/**
 * This class logs the following data in the calibration:
 * Points collected for each corner,
 * Points found to be in the cluster for each corner
 * Final points chosen to be the boundary for each corner,
 * The palm Normal collected for each corner
 * The palm Normal found to be in the cluster for each corner
 * Final palm normals for each corner
 * Transform Matrix
 * palmNormal Matrix
 * @author guinness
 *
 */
public class CalibrationLogger extends MFileLogger {
	
	/**
	 * Each corner will have the following:
	 * - All points collected
	 * - Points in the cluster created
	 * - Point selected as corner boundary
	 * 
	 * Also collecting the following to allow for PalmFlip action
	 * - PalmNormal vector
	 */
	public class Corner
	{
		public ArrayList<Double> CollectedX = new ArrayList<Double>();
		public ArrayList<Double> CollectedY = new ArrayList<Double>();
		public ArrayList<Double> CollectedZ = new ArrayList<Double>();
		
		public ArrayList<Double> ClusterX = new ArrayList<Double>();
		public ArrayList<Double> ClusterY = new ArrayList<Double>();
		public ArrayList<Double> ClusterZ = new ArrayList<Double>();
		
		public Double PointX;
		public Double PointY;
		public Double PointZ;
		
		public ArrayList<Double> CollectedNormalX = new ArrayList<Double>();
		public ArrayList<Double> CollectedNormalY = new ArrayList<Double>();
		public ArrayList<Double> CollectedNormalZ = new ArrayList<Double>();
		
		public ArrayList<Double> ClusterNormalX = new ArrayList<Double>();
		public ArrayList<Double> ClusterNormalY = new ArrayList<Double>();
		public ArrayList<Double> ClusterNormalZ = new ArrayList<Double>();
		
		public Double NormalPointX;
		public Double NormalPointY;
		public Double NormalPointZ;
	}
	
	/* made public to be able to be accessed outside the class, 
	 * no getters and setters because one would have to be made for each field in Corner
	 */
	public Corner topRight = new Corner();
	public Corner topLeft = new Corner();
	public Corner botLeft = new Corner();
	public Corner botRight = new Corner();
	
	public DenseMatrix64F transformMatrix;
	public DenseMatrix64F calcZTransform;
		
	public CalibrationLogger(String logfilePrefix) {	
			setFilePrefix(logfilePrefix);
	}
	
	private void writePoints(String varname, double x, double y, double z)
	{
		writeScalar(varname+"X", x);
		writeScalar(varname+"Y", y);
		writeScalar(varname+"Z", z);
	}
		
	public void loadMidPts(Corner corner, String label, Double x, Double y, Double z)
	{
		if(label=="Point")
		{
			corner.PointX=x;
			corner.PointY=y;
			corner.PointZ=z;
		}
		else if(label=="NormalPoint")
		{
			corner.NormalPointX=x;
			corner.NormalPointY=y;
			corner.NormalPointZ=z;
		}
	}
	
	public void loadDatasetToArrayLists(Dataset data, ArrayList<Double> x, ArrayList<Double> y, ArrayList<Double> z)
	{
		for(Instance instance : data) 
		{
			x.add(instance.get(0));
			y.add(instance.get(1));
			z.add(instance.get(2));
		}
	}
	
	public void writeCorner(Corner corner, String label)
	{
		writeDoubleVars(label+"CollectedX", corner.CollectedX);
		writeDoubleVars(label+"CollectedY", corner.CollectedY);
		writeDoubleVars(label+"CollectedZ", corner.CollectedZ);
		
		writeDoubleVars(label+"ClusterX", corner.ClusterX);
		writeDoubleVars(label+"ClusterY", corner.ClusterY);
		writeDoubleVars(label+"ClusterZ", corner.ClusterZ);
		
		this.writePoints(label+"Point", corner.PointX, corner.PointY, corner.PointZ);
		
		writeDoubleVars(label+"CollectedNormalX", corner.CollectedNormalX);
		writeDoubleVars(label+"CollectedNormalY", corner.CollectedNormalY);
		writeDoubleVars(label+"CollectedNormalZ", corner.CollectedNormalZ);
		
		writeDoubleVars(label+"ClusterNormalX", corner.ClusterNormalX);
		writeDoubleVars(label+"ClusterNormalY", corner.ClusterNormalY);
		writeDoubleVars(label+"ClusterNormalZ", corner.ClusterNormalZ);
		
		this.writePoints(label+"NormalPoint", corner.NormalPointX, corner.NormalPointY, corner.NormalPointZ);
	}
	
	
	/**
	 * Frontend to the log writing function. 
	 * Initializes, writes, closes file
	 */
	public void writeLog() {
				
		writeCorner(topRight, "topRight");
		writeCorner(topLeft, "topLeft");
		writeCorner(botLeft, "botLeft");
		writeCorner(botRight, "botRight");
		

		writeMatrix(this.transformMatrix, "transformMatrix");
		writeMatrix(this.calcZTransform,  "palmNormalTransform");
		
	}
	
}
