package edu.baylor.hci.Logger;


import java.util.ArrayList;




import org.apache.log4j.Logger;

import com.leapmotion.leap.Vector;



public class PositionLog extends MFileLogger {

	/* Allows a programatic change of which point is fetched by any external system that uses PositionLog's positions
	 * For now, that means we can immediately(ish) change our graph to use the finger tip position.
	 */
	private enum PointProvider { PALM, STABLE_PALM, AVG_FINGER_TIP;	}
	private static final PointProvider POINT_PROVIDER = PointProvider.STABLE_PALM;
	
	/** Arrays to store the positions **/
	// Stable hand positions. Normalized by the API
	private ArrayList<Float> stablePalmX = new ArrayList<Float>();
	private ArrayList<Float> stablePalmY = new ArrayList<Float>();
	private ArrayList<Float> stablePalmZ = new ArrayList<Float>();
	// Regular hand positions
	private ArrayList<Float> palmX = new ArrayList<Float>();
	private ArrayList<Float> palmY = new ArrayList<Float>();
	private ArrayList<Float> palmZ = new ArrayList<Float>();
	// average finger position
	private ArrayList<Float> fingerX = new ArrayList<Float>();
	private ArrayList<Float> fingerY = new ArrayList<Float>();
	private ArrayList<Float> fingerZ = new ArrayList<Float>();
	// the NormalLine to the palm.
	private ArrayList<Float> palmNormalX = new ArrayList<Float>();
	private ArrayList<Float> palmNormalY = new ArrayList<Float>();
	private ArrayList<Float> palmNormalZ = new ArrayList<Float>();
	//sphereRadius
	private ArrayList<Float> sphereRadius = new ArrayList<Float>();
	
	// some meta shit. 
    final static Logger logger = Logger.getLogger(PositionLog.class);
	
	/**
	 * Constructor. Requires the prefix for the filename.
	 * The actual filename will be constructed as
	 *    <PREFIX>_<DATE>_<TIME>.m
	 * just run the whole .m file from the matlab terminal
	 * @param logfilePrefix
	 */
	public PositionLog(String logfilePrefix) {
        setFilePrefix(logfilePrefix);
	}
	
	/**
	 * Frontend to the log writing function. 
	 * Initializes, writes, closes file
	 */
	public void writeLog() {
		setLogStatus(LogStatus.IDLE);
		// first disable the addition to the log. 
		writeFloatVars("X", palmX);
		writeFloatVars("Y", palmY);
		writeFloatVars("Z", palmZ);
		// stable palm pos
		writeFloatVars("SX", stablePalmX);
		writeFloatVars("SY", stablePalmY);
		writeFloatVars("SZ", stablePalmZ);
		// finger pos
		writeFloatVars("FX", fingerX);
		writeFloatVars("FY", fingerY);
		writeFloatVars("FZ", fingerZ);
		// palm Normal
		writeFloatVars("PNX", palmNormalX);
		
		writeFloatVars("PNZ", palmNormalZ);
		writeFloatVars("SphereRadius", sphereRadius);
		
		clearArrays();
		
		logger.info("Log successfully written");
	}
	public void clearArrays()
	{
		palmX.clear();
		palmY.clear();
		palmZ.clear();
		stablePalmX.clear();
		stablePalmY.clear();
		stablePalmZ.clear();
		fingerX.clear();
		fingerY.clear();
		fingerZ.clear();
		palmNormalX.clear();
		palmNormalY.clear();
		palmNormalZ.clear();
		sphereRadius.clear();
	}


	
	/* Access methods, adding to the x/y/z vars done here
	 * doing it this way allows better control over the DataStructs and the permissions
	 */
	
	public void addStablePalmX(Float stablePalmX) {
		if(getLogStatus()==LogStatus.START)
			this.stablePalmX.add(stablePalmX);
	}

	public void addPalmX(Float palmX) {
		if(getLogStatus()==LogStatus.START)
			this.palmX.add(palmX);
	}

	public void addStablePalmY(Float stablePalmY) {
		if(getLogStatus()==LogStatus.START)
			this.stablePalmY.add(stablePalmY);
	}

	public void addPalmY(Float palmY) {
		if(getLogStatus()==LogStatus.START)
			this.palmY.add(palmY);
	}

	public void addStablePalmZ(Float stablePalmZ) {
		if(getLogStatus()==LogStatus.START)
			this.stablePalmZ.add(stablePalmZ);
	}

	public void addPalmZ(Float palmZ) {
		if(getLogStatus()==LogStatus.START)
			this.palmZ.add(palmZ);
	}
	
	public void addAvgFingerX(Float avgFinger) {
		if(getLogStatus() == LogStatus.START) {
			this.fingerX.add(avgFinger);
		}
	}
	
	public void addAvgFingerY(Float avgFinger) {
		if(getLogStatus() == LogStatus.START) {
			this.fingerY.add(avgFinger);
		}
	}

	public void addAvgFingerZ(Float avgFinger) {
		if(getLogStatus() == LogStatus.START) {
			this.fingerZ.add(avgFinger);
		}
	}
	
	/**
	 * adds the Normal (90 degrees) line to the palm
	 * @param palmNormal
	 */
	public void addPalmNormal(Vector palmNormal) {
		if(getLogStatus() == LogStatus.START) {
			this.palmNormalX.add(palmNormal.getX());
			this.palmNormalY.add(palmNormal.getY());
			this.palmNormalZ.add(palmNormal.getZ());
		}
	}
	public void addSphereRadius(Float radius) {
		if(getLogStatus()==LogStatus.START) {
			this.sphereRadius.add(radius);
		}
	}

	
	/* These are generic getters. We will use them to get the coords, 
	 * However these are generic enough to ensure that no one particular `provider` is expected
	 * The provider is specified above with the POINT_PROVIDER constant. 
	 */
	public ArrayList<Float> getX() {
		if(POINT_PROVIDER == PointProvider.AVG_FINGER_TIP) {
			return fingerX;
		} else if (POINT_PROVIDER == PointProvider.PALM) {
			return palmX;
		} else if (POINT_PROVIDER == PointProvider.STABLE_PALM) {
			return stablePalmX;
		} else { // catchall
			return new ArrayList<Float>();
		}
	}

	public ArrayList<Float> getY() {
		if(POINT_PROVIDER == PointProvider.AVG_FINGER_TIP) {
			return fingerY;
		} else if (POINT_PROVIDER == PointProvider.PALM) {
			return palmY;
		} else if (POINT_PROVIDER == PointProvider.STABLE_PALM) {
			return stablePalmY;
		} else { // catchall
			return new ArrayList<Float>();
		}
	}

	public ArrayList<Float> getZ() {
		if(POINT_PROVIDER == PointProvider.AVG_FINGER_TIP) {
			return fingerZ;
		} else if (POINT_PROVIDER == PointProvider.PALM) {
			return palmZ;
		} else if (POINT_PROVIDER == PointProvider.STABLE_PALM) {
			return stablePalmZ;
		} else { // catchall
			return new ArrayList<Float>();
		}

	}
	
	public ArrayList<Float> getPalmNormalX() {
		return palmNormalX;
	}

	public ArrayList<Float> getPalmNormalY() {
		return palmNormalY;
	}

	public ArrayList<Float> getPalmNormalZ() {
		return palmNormalZ;
	}

}
