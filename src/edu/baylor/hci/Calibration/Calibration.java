package edu.baylor.hci.Calibration;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.util.Arrays;
import java.util.Vector;

import com.leapmotion.leap.Controller;

import edu.baylor.hci.Calibration.CalibrationView.CalibrationButtons;
import edu.baylor.hci.LeapOMatic.GuiListener;
import edu.baylor.hci.LeapOMatic.TouchPoints;
import edu.baylor.hci.LeapOMatic.TouchPointsView;
import edu.baylor.hci.Logger.CalibrationLogger;
import edu.baylor.hci.Logger.PositionLog;
import edu.baylor.hci.Logger.MFileLogger.LogStatus;
import net.sf.javaml.clustering.Clusterer;
import net.sf.javaml.clustering.DensityBasedSpatialClustering;


import net.sf.javaml.core.Dataset;
import net.sf.javaml.core.DefaultDataset;
import net.sf.javaml.core.DenseInstance;
import net.sf.javaml.core.Instance;

import javax.swing.JFrame;
import javax.swing.Timer;

import org.apache.log4j.Logger;

/**
 * Master class for controlling the calibration
 * @TODO there's redundancies in getLeftHandCalibration and getRightHand. 
 * needs to be consolidated somehow. Managing the code between the two is very copy/paste 
 * @author Alvin
 *
 */

public class Calibration extends JFrame implements ActionListener {
	/** class constants **/
	private static final long serialVersionUID = 1093784509731L;
	// screen dimensions are critical to build the destination the matrix
	private static final int SCREEN_HEIGHT = 1600;
	private static final int SCREEN_WIDTH = 2560;
    // countdown, happens one per calibration run
	private static final int COUNTDOWN_SECONDS = 5;
	private static final int COUNTDOWN_INITIAL_DELAY = 2000;
	private static final int COUNTDOWN_INTERVAL = 1000;
	// these here used during each point
	private static final int INITIAL_DELAY = 0;
	private static final int TIMER_INTERVAL = 2500;
	// Dimensions of the calibration window (set it full screen)
	private static final int WINDOW_HEIGHT = 1000;
	private static final int WINDOW_WIDTH = 1000;
	// dbscan clustering algo needs a minimum points (how many points are required in this cluster),
	// and an epsilon value, which is distance
	private static final int MIN_PNTS = 70; // <-- This only works on Leaps "Balanced". if using "precision" change to 75
	private static final double EPSILON = 0.2d;

	private Timer countDownTimer;
	private Controller controller;
	private Timer timer;

	/* Two types of points collected, positions, and palmnormal
	 * PalmNormal's expected position allows us to collect points to facilitate the 
	 *   palm-flip action. 
	 */
	private Dataset topRight;
	private Dataset topRightNormal;
	private Dataset topLeft;
	private Dataset topLeftNormal;
	private Dataset botLeft;
	private Dataset botLeftNormal;
	private Dataset botRight;
	private Dataset botRightNormal;
	private CalibrationView view;
	private PositionLog positionLog;
	private GuiListener guiListener;
	
	private int calibrationHand;
	
	private CalibrationLogger calibrationLog;
	
    private static final Logger logger = Logger.getLogger(Calibration.class);

    
	private int timerCount=0;
	public Calibration(int handUsed, String logFileSuffix)
	{
		calibrationHand = handUsed;
		
		this.setTitle("Calibration Exercise");
	    this.setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
	    this.setSize(WINDOW_WIDTH, WINDOW_HEIGHT);
	    this.setVisible(true);
	    // places this frame in the center of the DefaultScreen. 
	    this.setLocationRelativeTo(null);

	    calibrationLog = new CalibrationLogger("calibrationLog"+"_"+logFileSuffix);
	    /** FIXME: remove this positionLogger. Not used (I think) **/
	    positionLog = new PositionLog("log1");
        guiListener = new GuiListener(positionLog);
        
	    view = new CalibrationView();
	    add(view);
	    this.setExtendedState(JFrame.MAXIMIZED_BOTH);
	    
	    controller 	= new Controller();
	    controller.addListener(guiListener);
	    countDownTimer = new Timer(COUNTDOWN_INTERVAL, this);
	    countDownTimer.setInitialDelay(COUNTDOWN_INITIAL_DELAY);
	    countDownTimer.start();
	    timer= new Timer(TIMER_INTERVAL, this);
	    timer.setInitialDelay(INITIAL_DELAY);
	    //timer.start();
	    
	    this.addWindowListener(new WindowAdapter(){
            public void windowClosing(WindowEvent e){
            	countDownTimer.stop();
            	timer.stop();               
            }
        });
	    
	}
	
	public void actionPerformed(ActionEvent e)
	{
		timerCount++;
		if(calibrationHand==TouchPointsView.LEFT_HAND)
		{
			getLeftHandedCalibrationDatasets();
		}
		else
		{
			getRightHandedCalibrationDatasets();
		}
	}
	
	public void getLeftHandedCalibrationDatasets()
	{    
		if(timerCount-COUNTDOWN_SECONDS >= -5 &&  timerCount-COUNTDOWN_SECONDS < 0)
		{
			view.setStatusCell("Starting in:"+(Math.abs(timerCount-COUNTDOWN_SECONDS)));
		}
		else if(timerCount-COUNTDOWN_SECONDS ==0)
		{
			view.setStatusCell("Calibrating...");
			countDownTimer.stop();
			timer.start();
		}
		else if(timerCount-COUNTDOWN_SECONDS==1)
		{
			view.toggleVisibility(CalibrationButtons.TOP_LEFT);	
		}
		else if(timerCount-COUNTDOWN_SECONDS == 2 )
		{
			positionLog.clearArrays();
			positionLog.setLogStatus(LogStatus.START);		
		}
		else if(timerCount-COUNTDOWN_SECONDS == 3)
		{
			//extract Points
			positionLog.setLogStatus(LogStatus.STOP);
			topLeft= collectPoints();
			topLeftNormal= collectPalmNormal();
			view.toggleVisibility(CalibrationButtons.TOP_RIGHT);
		}
		else if(timerCount-COUNTDOWN_SECONDS == 4)
		{
			positionLog.clearArrays();
			positionLog.setLogStatus(LogStatus.START);
		}
		else if(timerCount-COUNTDOWN_SECONDS ==5)
		{	
			//extract points
			positionLog.setLogStatus(LogStatus.STOP);
			topRight = collectPoints();
			topRightNormal=collectPalmNormal();
			view.toggleVisibility(CalibrationButtons.BOT_RIGHT);
		}
		else if(timerCount-COUNTDOWN_SECONDS == 6)
		{
			//toggle Point collection
			//
			positionLog.clearArrays();
			positionLog.setLogStatus(LogStatus.START);
		}
		else if(timerCount-COUNTDOWN_SECONDS ==7)
		{
			//extract Points
			positionLog.setLogStatus(LogStatus.STOP);
			botRight = collectPoints();
			botRightNormal = collectPalmNormal();
			view.toggleVisibility(CalibrationButtons.BOT_LEFT);
		}
		else if(timerCount-COUNTDOWN_SECONDS == 8)
		{
			//togglePoint collection
			positionLog.clearArrays();
			positionLog.setLogStatus(LogStatus.START);
			
		}
		else if(timerCount-COUNTDOWN_SECONDS == 9)
		{
			positionLog.setLogStatus(LogStatus.STOP);
			botLeft = collectPoints();
			botLeftNormal = collectPalmNormal();	
			view.toggleVisibility(CalibrationButtons.DISABLE);
			if(botLeft != null)view.setStatusCell("Calibration done, Sit back while we calculate...");
		}
		else if(timerCount-COUNTDOWN_SECONDS == 10)
		{
			timer.stop();
			try	{
				generateClustersAndTransformationMatrix();
				view.setStatusCell("Personal Space built");
			} catch(Exception e) {
				view.setStatusCell("That didn't work out. Sorry. We'll try again");
			}
			
		}

	}
	/*
	 * Every 2.5 seconds(specified by TIMER_INTERVAL) this function is called. On odd calls we toggle the visibility indicating the user to move their hand to this position.
	 * On even calls we start the collection of points, so the user has 2.5 seconds to react and place their hand to the specified position.
	 * At the start of the next odd call we extract the points into a data set for the clustering algorithm, and toggle the next visibility.
	 * 
	 */
	public void getRightHandedCalibrationDatasets()
	{    
		if(timerCount-COUNTDOWN_SECONDS >= -5 &&  timerCount-COUNTDOWN_SECONDS < 0)
		{
			view.setStatusCell(String.format("Starting in: %d", Math.abs(timerCount-COUNTDOWN_SECONDS)));
		}
		else if(timerCount-COUNTDOWN_SECONDS ==0)
		{
			view.setStatusCell("Calibrating...");
			countDownTimer.stop();
			timer.start();
		}
		else if(timerCount-COUNTDOWN_SECONDS==1)
		{
			view.toggleVisibility(CalibrationButtons.TOP_RIGHT);	
		}
		else if(timerCount-COUNTDOWN_SECONDS == 2 )
		{
			positionLog.clearArrays();
			positionLog.setLogStatus(LogStatus.START);		
		}
		else if(timerCount-COUNTDOWN_SECONDS == 3)
		{
			//extract Points
			positionLog.setLogStatus(LogStatus.STOP);
			topRight= collectPoints();
			topRightNormal= collectPalmNormal();
			view.toggleVisibility(CalibrationButtons.TOP_LEFT);
		}
		else if(timerCount-COUNTDOWN_SECONDS == 4)
		{
			positionLog.clearArrays();
			positionLog.setLogStatus(LogStatus.START);
		}
		else if(timerCount-COUNTDOWN_SECONDS ==5)
		{	
			//extract points
			positionLog.setLogStatus(LogStatus.STOP);
			topLeft= collectPoints();
			topLeftNormal=collectPalmNormal();
			view.toggleVisibility(CalibrationButtons.BOT_LEFT);
		}
		else if(timerCount-COUNTDOWN_SECONDS == 6)
		{
			//toggle Point collection
			//
			positionLog.clearArrays();
			positionLog.setLogStatus(LogStatus.START);
		}
		else if(timerCount-COUNTDOWN_SECONDS ==7)
		{
			//extract Points
			positionLog.setLogStatus(LogStatus.STOP);
			botLeft = collectPoints();
			botLeftNormal = collectPalmNormal();
			view.toggleVisibility(CalibrationButtons.BOT_RIGHT);
		}
		else if(timerCount-COUNTDOWN_SECONDS == 8)
		{
			//togglePoint collection
			positionLog.clearArrays();
			positionLog.setLogStatus(LogStatus.START);
			
		}
		else if(timerCount-COUNTDOWN_SECONDS == 9)
		{
			positionLog.setLogStatus(LogStatus.STOP);
			botRight = collectPoints();
			botRightNormal = collectPalmNormal();
			view.toggleVisibility(CalibrationButtons.DISABLE);
			if(botRight != null) view.setStatusCell("Calibration done, Sit back while we calculate...");
		}
		else if(timerCount-COUNTDOWN_SECONDS == 10)
		{
			timer.stop();
			try	{
				generateClustersAndTransformationMatrix();
				view.setStatusCell("Personal Space built");
			} catch(Exception e) {
				view.setStatusCell("That didn't work out. Sorry. We'll try again");
			}
			
		}
	}

	/**
	 * Goes through the positionLog that has been filled by each calibration point and builds the data set for the clustering algorithm.
	 * @return
	 */
	public Dataset collectPoints()
	{
		Dataset handCoordinates=new DefaultDataset();
		Instance instance = null;
		double point[]; 
		System.out.println(positionLog.getX().size());
		if(positionLog.getX().size() < MIN_PNTS)
		{	
			timer.stop();
			logger.error("Insufficient points collected. Expected :" + MIN_PNTS + ". Obtained: " + positionLog.getX().size());
			view.setStatusCell("Oops a problem occured. <br/>Please allow the researchers to assist you");
			return null;
		} 
		else 
		{
			for(int i=0;i<positionLog.getX().size();i++) 
			{
				point= new double[3]; 
				point[0]=positionLog.getX().get(i);
				point[1]=positionLog.getY().get(i);
				point[2]=positionLog.getZ().get(i);

				instance = new DenseInstance(point);
				handCoordinates.add(instance);
				
			}
			return handCoordinates;	
		}
	}
	
	/**
	 * Obtains newly populated positionLog palmNormal points after the 2.5 second calibration point 
	 * stores for later clustering in order to obtain an "expected" palm normal position for the calibration point.
	 * Used in palm-flip gesture
	 * @return
	 */
	public Dataset collectPalmNormal()
	{
		Dataset palmNormal=new DefaultDataset();
		Instance instance = null;
		double point[]; 
		System.out.println(positionLog.getX().size());
		for(int i=0;i<positionLog.getPalmNormalX().size();i++) 
		{
			//x,y,z
			point= new double[3]; 
			point[0]=positionLog.getPalmNormalX().get(i);
			point[1]=positionLog.getPalmNormalY().get(i);
			point[2]=positionLog.getPalmNormalZ().get(i);

			instance = new DenseInstance(point);
			palmNormal.add(instance);
		}
		return palmNormal;
	}
	
	/**
	 * First gets the cluster of points for each source coordinate(topRight, topLeft, botLeft, botRight).
	 * Then obtains the actual boundary we will be using from each cluster, and finally builds the transform matrix 
	 * @throws Exception 
	 */
	public void generateClustersAndTransformationMatrix() throws Exception
	{
		
		// top left position & PalmNormal (for palmflip)
		System.out.println("Generating Top Left Clusters");
		Calibrator topLeftCalibrator = new Calibrator(topLeft);
		Calibrator topLeftPalmNormalCalibrator = new Calibrator(topLeftNormal);
		topLeftCalibrator.start();
		topLeftPalmNormalCalibrator.start();
		// top right position & palmNormal
		System.out.println("Generating Top Right Clusters");
		Calibrator topRightCalibrator = new Calibrator(topRight);
		Calibrator topRightPalmNormalCalibrator = new Calibrator(topRightNormal);
		topRightCalibrator.start();
		topRightPalmNormalCalibrator.start();
		// bottom right
		System.out.println("Generating Bot Right Clusters");
		Calibrator botRightCalibrator = new Calibrator(botRight);
		Calibrator botRightPalmNormalCalibrator = new Calibrator(botRightNormal);
		botRightCalibrator.start();
		botRightPalmNormalCalibrator.start();
		// bottom left
		System.out.println("Generating Bot Left Clusters");
		Calibrator botLeftCalibrator = new Calibrator(botLeft);
		Calibrator botLeftPalmNormalCalibrator = new Calibrator(botLeftNormal);
		botLeftCalibrator.start();
		botLeftPalmNormalCalibrator.start();
		
		// join the threads
		try {
			topRightCalibrator.join();
			topRightPalmNormalCalibrator.join();
			topLeftCalibrator.join();
			topLeftPalmNormalCalibrator.join();
			botRightCalibrator.join();
			botRightPalmNormalCalibrator.join();
			botLeftCalibrator.join();
			botLeftPalmNormalCalibrator.join();
		} catch (InterruptedException e) {
			logger.fatal("Threading issue, " + e.getMessage());
			e.printStackTrace();
		}
		
		// top right
		Dataset topRightCluster, topRightNormalCluster, topLeftCluster, topLeftNormalCluster, botLeftCluster, botLeftNormalCluster, botRightCluster, botRightNormalCluster;
		try{
			topRightCluster = topRightCalibrator.getMaxCluster();
			topRightNormalCluster = topRightPalmNormalCalibrator.getMaxCluster();
			// top left
			topLeftCluster = topLeftCalibrator.getMaxCluster();
			topLeftNormalCluster = topLeftPalmNormalCalibrator.getMaxCluster();		
			// bottom left
			botLeftCluster = botLeftCalibrator.getMaxCluster();
			botLeftNormalCluster = botLeftPalmNormalCalibrator.getMaxCluster();
			// bottom right
			botRightCluster = botRightCalibrator.getMaxCluster();
			botRightNormalCluster = botRightPalmNormalCalibrator.getMaxCluster();
		} catch(Exception e) {
			throw e;
		}
		// [DONE]
		System.out.println("Obtained Max Clusters");
		
		/*
		 * Feed collectedPoints and clusters to the calibrationLog
		 */
		calibrationLog.loadDatasetToArrayLists(topRight, calibrationLog.topRight.CollectedX, calibrationLog.topRight.CollectedY, calibrationLog.topRight.CollectedZ);
		calibrationLog.loadDatasetToArrayLists(topLeft, calibrationLog.topLeft.CollectedX, calibrationLog.topLeft.CollectedY, calibrationLog.topLeft.CollectedZ);
		calibrationLog.loadDatasetToArrayLists(botLeft, calibrationLog.botLeft.CollectedX, calibrationLog.botLeft.CollectedY, calibrationLog.botLeft.CollectedZ);
		calibrationLog.loadDatasetToArrayLists(botRight, calibrationLog.botRight.CollectedX, calibrationLog.botRight.CollectedY, calibrationLog.botRight.CollectedZ);
		
		calibrationLog.loadDatasetToArrayLists(topRightCluster, calibrationLog.topRight.ClusterX, calibrationLog.topRight.ClusterY, calibrationLog.topRight.ClusterZ);
		calibrationLog.loadDatasetToArrayLists(topLeftCluster, calibrationLog.topLeft.ClusterX, calibrationLog.topLeft.ClusterY, calibrationLog.topLeft.ClusterZ);
		calibrationLog.loadDatasetToArrayLists(botLeftCluster, calibrationLog.botLeft.ClusterX, calibrationLog.botLeft.ClusterY, calibrationLog.botLeft.ClusterZ);
		calibrationLog.loadDatasetToArrayLists(botRightCluster, calibrationLog.botRight.ClusterX, calibrationLog.botRight.ClusterY, calibrationLog.botRight.ClusterZ);
		
		calibrationLog.loadDatasetToArrayLists(topRightNormal, calibrationLog.topRight.CollectedNormalX, calibrationLog.topRight.CollectedNormalY, calibrationLog.topRight.CollectedNormalZ);
		calibrationLog.loadDatasetToArrayLists(topLeftNormal, calibrationLog.topLeft.CollectedNormalX, calibrationLog.topLeft.CollectedNormalY, calibrationLog.topLeft.CollectedNormalZ);
		calibrationLog.loadDatasetToArrayLists(botLeftNormal, calibrationLog.botLeft.CollectedNormalX, calibrationLog.botLeft.CollectedNormalY, calibrationLog.botLeft.CollectedNormalZ);
		calibrationLog.loadDatasetToArrayLists(botRightNormal, calibrationLog.botRight.CollectedNormalX, calibrationLog.botRight.CollectedNormalY, calibrationLog.botRight.CollectedNormalZ);
		
		calibrationLog.loadDatasetToArrayLists(topRightNormalCluster, calibrationLog.topRight.ClusterNormalX, calibrationLog.topRight.ClusterNormalY, calibrationLog.topRight.ClusterNormalZ);
		calibrationLog.loadDatasetToArrayLists(topLeftNormalCluster, calibrationLog.topLeft.ClusterNormalX, calibrationLog.topLeft.ClusterNormalY, calibrationLog.topLeft.ClusterNormalZ);
		calibrationLog.loadDatasetToArrayLists(botLeftNormalCluster, calibrationLog.botLeft.ClusterNormalX, calibrationLog.botLeft.ClusterNormalY, calibrationLog.botLeft.ClusterNormalZ);
		calibrationLog.loadDatasetToArrayLists(botRightNormalCluster, calibrationLog.botRight.ClusterNormalX, calibrationLog.botRight.ClusterNormalY, calibrationLog.botRight.ClusterNormalZ);
		
		//we now have all maxClusters, now to approximate midpoints
		Vector<Double> topRightMidPt = getBounds(topRightCluster, "topRight");
		Vector<Double> topRightNormal = getBounds(topRightNormalCluster, "palmNormal");
		Vector<Double> topLeftMidPt = getBounds(topLeftCluster, "topLeft");
		Vector<Double> topLeftNormal = getBounds(topLeftNormalCluster, "palmNormal");
		Vector<Double> botLeftMidPt = getBounds(botLeftCluster, "botLeft");
		Vector<Double> botLeftNormal = getBounds(botLeftNormalCluster, "palmNormal");
		Vector<Double> botRightMidPt = getBounds(botRightCluster, "botRight");
		Vector<Double> botRightNormal = getBounds(botRightNormalCluster, "palmNormal");
		
		calibrationLog.loadMidPts(calibrationLog.topRight, "Point", topRightMidPt.get(0), topRightMidPt.get(1), topRightMidPt.get(2));
		calibrationLog.loadMidPts(calibrationLog.topLeft, "Point", topLeftMidPt.get(0), topLeftMidPt.get(1), topLeftMidPt.get(2));
		calibrationLog.loadMidPts(calibrationLog.botLeft, "Point", botLeftMidPt.get(0), botLeftMidPt.get(1), botLeftMidPt.get(2));
		calibrationLog.loadMidPts(calibrationLog.botRight, "Point", botRightMidPt.get(0), botRightMidPt.get(1), botRightMidPt.get(2));
		
		calibrationLog.loadMidPts(calibrationLog.topRight, "NormalPoint", topRightNormal.get(0), topRightNormal.get(1), topRightNormal.get(2));
		calibrationLog.loadMidPts(calibrationLog.topLeft, "NormalPoint", topLeftNormal.get(0), topLeftNormal.get(1), topLeftNormal.get(2));
		calibrationLog.loadMidPts(calibrationLog.botLeft, "NormalPoint", botLeftNormal.get(0), botLeftNormal.get(1), botLeftNormal.get(2));
		calibrationLog.loadMidPts(calibrationLog.botRight, "NormalPoint", botRightNormal.get(0), botRightNormal.get(1), botRightNormal.get(2));
		
		System.out.println("NORMALS:");
		System.out.println(topRightNormal);
		System.out.println(topLeftNormal);
		System.out.println(botLeftNormal);
		System.out.println(botRightNormal);
		// print to stdout
		System.out.println("Top Right " + topRightMidPt.toString());
		System.out.println("Top Left  " + topLeftMidPt.toString());
		System.out.println("Bot Left  " + botLeftMidPt.toString());
		System.out.println("Bot Right " + botRightMidPt.toString());
		
		logger.trace("Cluster Members, Top right: " + topRightCluster.toString());		
		logger.debug("Top Right " + topRightMidPt.toString());
		logger.trace("Cluster Members, Top left: " + topLeftCluster.toString());
		logger.debug("Top Left  " + topLeftMidPt.toString());
		logger.trace("Cluster Members, Bottom left: " + botLeftCluster.toString());
		logger.debug("Top Right " + botLeftMidPt.toString());
		logger.trace("Cluster Members, Bottom right: " + botRightCluster.toString());
		logger.debug("Bot Right " + botRightMidPt.toString());
		logger.debug("Top Right Normal"+topRightNormal);
		logger.debug("Top Left Normal"+topLeftNormal);
		logger.debug("Bot Left Normal"+botLeftNormal);
		logger.debug("Bot Right Normal"+botRightNormal);
		
		//now we have the source points
		buildTransformMatrix(topLeftMidPt, topRightMidPt, botLeftMidPt, botRightMidPt, topLeftNormal, topRightNormal, botLeftNormal, botRightNormal);	
	}
	/**
	 * Builds transform Matrix, and sets TouchPoints.transform to this matrix for computation later
	 * @param topLeftMidPt
	 * @param topRightMidPt
	 * @param botLeftMidPt
	 * @param botRightMidPt
	 */
	private void buildTransformMatrix(Vector<Double> topLeftMidPt,
									  Vector<Double> topRightMidPt, 
									  Vector<Double> botLeftMidPt,
									  Vector<Double> botRightMidPt, 
									  Vector<Double> topLeftNormal, 
									  Vector<Double> topRightNormal,
									  Vector<Double> botLeftNormal,
									  Vector<Double> botRightNormal) {
		
		// represents the hand area
		double S1[][]= new double[][] {
				{topRightMidPt.get(0), topRightMidPt.get(1), topRightMidPt.get(2) },
				{topLeftMidPt.get(0) , topLeftMidPt.get(1) , topLeftMidPt.get(2)  },
				{botLeftMidPt.get(0) , botLeftMidPt.get(1) , botLeftMidPt.get(2)  },
				{botRightMidPt.get(0), botRightMidPt.get(1), botRightMidPt.get(2) }
			};
		//represents screen coordinates
		double screenDest[][]= new double[][]{
				{SCREEN_WIDTH, 0, 0}, //topRight
				{0, 0, 0}, //topLeft
				{0, SCREEN_HEIGHT, 0}, //botLeft
				{SCREEN_WIDTH, SCREEN_HEIGHT, 0} //botRight
		};
		double palmDest[][] = new double[][] {
				{topRightNormal.get(0), topRightNormal.get(1), topRightNormal.get(2) },
				{topLeftNormal.get(0) , topLeftNormal.get(1) , topLeftNormal.get(2)  },
				{botLeftNormal.get(0) , botLeftNormal.get(1) , botLeftNormal.get(2)  },
				{botRightNormal.get(0), botRightNormal.get(1), botRightNormal.get(2) }
		};
		logger.debug("Source		: " + Arrays.deepToString(S1));
		logger.debug("Destination 	: " + Arrays.deepToString(screenDest));
		
		try {
			TouchPoints.clearTransformMatrices();
			TouchPoints.getCoordinateTransformer().setInputMatrix(S1);
			TouchPoints.getPalmNormalTransformer().setInputMatrix(S1);
		} catch (Exception e) {
			logger.error(e.getMessage());
			e.printStackTrace();
		}
		
		TouchPoints.getCoordinateTransformer().setOutputMatrix(screenDest);
		TouchPoints.getPalmNormalTransformer().setOutputMatrix(palmDest);
		try {
			calibrationLog.calcZTransform=TouchPoints.getPalmNormalTransformer().getTransformationMatrix();
			calibrationLog.transformMatrix=TouchPoints.getCoordinateTransformer().getTransformationMatrix();
			calibrationLog.writeLog();
		} catch (Exception e) {
			logger.error(e.getMessage());
			e.printStackTrace();
		}
		
	}
	
	
	/**
	 * Returns the particular bound for each cluster so that the point is most reachable e.g. minY maxX for topLeft.
	 * @param data
	 * @param cluster
	 * @return
	 */
	private Vector<Double> getBounds(Dataset data, String cluster)
	{
		Vector<Double> point3D= new Vector<Double>(); //will contain [0]=>x, [1]=>y, [2]=>z
		Double maxX=Double.NEGATIVE_INFINITY; 
		Double minX=Double.POSITIVE_INFINITY; 
		Double maxY=Double.NEGATIVE_INFINITY; 
		Double minY=Double.POSITIVE_INFINITY; 
		Double maxZ=Double.NEGATIVE_INFINITY; 
		Double minZ=Double.POSITIVE_INFINITY;
		
		Double X, Y, Z;
		for(Instance instance : data) //calculate running max and mins for each variable independently
		{
			X = instance.get(0);
			Y = instance.get(1);
			Z = instance.get(2);
			
			if(maxX<X){
				maxX=X;
			}
			if(minX>X)
			{
				minX=X;
			}
			if(maxY<Y)
			{
				maxY=Y;
			}
			if(minY>Y)
			{
				minY=Y;
			}
			if(maxZ<Z)
			{
				maxZ=Z;
			}
			if(minZ>Z)
			{
				minZ=Z;
			}
		}
		if(cluster=="topRight")
		{
			point3D.add(minX);
			point3D.add(minY);
			point3D.add(minZ);
		}
		else if(cluster=="topLeft")
		{
			point3D.add(maxX);
			point3D.add(minY);
			point3D.add(minZ);
		}
		else if(cluster=="botLeft")
		{
			point3D.add(maxX);
			point3D.add(maxY);
			point3D.add(maxZ);
		}
		else if(cluster =="botRight")
		{
			point3D.add(minX);
			point3D.add(maxY);
			point3D.add(maxZ);
		}
		else if(cluster =="palmNormal")
		{
			point3D.add((maxX+minX)/2);
			point3D.add((maxY+minY)/2);
			point3D.add((maxZ+minZ)/2);

		}
		return point3D;
	}
	
	

	/**
	 * Allows the calibration to be done in a different thread.
	 * Speeds up calibration time from 40 secs to <10secs when done with Leaps "balanced" settings. 
	 *  
	 * @author Alvin
	 *
	 */

	private class Calibrator extends Thread 
	{
		Dataset maxCluster;
		Dataset inputData;
		
		
		public Calibrator(Dataset data)
		{
			this.inputData = data;
		}
		
		public void run() 
		{
			int clusterSize=0;
			Dataset[] clusters;
			
			Clusterer dbScan= new DensityBasedSpatialClustering(EPSILON, MIN_PNTS);
			clusters = dbScan.cluster(inputData);
			// there may be multiple clusters generated. Select and return the largest one. 
			for(Dataset dataset : clusters)
			{
				if(dataset.size()>clusterSize)
				{
					maxCluster=dataset;
					clusterSize=maxCluster.size();
				}
			}
		}
		
		/**
		 * 
		 * @param data
		 * @return
		 * @throws Exception 
		 */
		public Dataset getMaxCluster() throws Exception
		{
			// we need AT LEAST one cluster. if there isn't at least one, then we throw an error. Expect this to be caught somewhere. 
			if(maxCluster.size() == 0 || maxCluster == null)
			{
				throw new Exception("Incufficient clusters found");
			}
			logger.fatal("No clusters found");

			System.out.println("MaxCluster size: " + maxCluster.size());
			return maxCluster;
		}
	}
}
