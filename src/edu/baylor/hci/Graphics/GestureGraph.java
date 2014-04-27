package edu.baylor.hci.Graphics;
import java.awt.Rectangle;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.Timer;

import org.apache.log4j.Logger;
import org.jzy3d.chart.Chart;
import org.jzy3d.chart.ChartLauncher;
import org.jzy3d.colors.Color;
import org.jzy3d.global.Settings;
import org.jzy3d.maths.BoundingBox3d;
import org.jzy3d.maths.Coord3d;
import org.jzy3d.plot3d.primitives.Scatter;
import org.jzy3d.plot3d.rendering.canvas.Quality;

import edu.baylor.hci.LeapOMatic.GuiListener;
import edu.baylor.hci.LeapOMatic.TouchPoints;
/**
 * Front End to jzy3d, builds the 3D graph with blue points and red trail. 
 * Dependent on PositionLog
 * @author hariharan
 *
 */
public class GestureGraph{
	/** class constants **/
	/* timer stuff. notaion in milliseconds */
	 // private static int TIMER_INITIAL_DELAY = 1000; // FIXME : Currently unused. Decide if it's necessary, else remove
	private static int TIMER_TICK_INTERVAL = 100;
	/* we'll have a red point bigger than the usual point*/
	private static final float NEW_SCATTER_POINT_WIDTH = 5.0f;
	private static final Color NEW_SCATTER_POINT_COLOR = Color.RED;
	
	/** object variables **/
	private Scatter trailScatter;
	private int previousCnt=0;
	private Chart chart;

	private static final Logger logger = Logger.getLogger(GestureGraph.class);
	
	/**
	 * Rudimentary start script. The graph window will not actually show up unless this is called
	 *   
	 */
	public void start()
	{
		logger.debug("Starting our Gesture Graph");
		
		this.chart= new Chart(Quality.Advanced, "awt");
		/* Initialize some point when the graph is created
		 * solves problem where everything crashes and burns if
		 * graph is created without any points
		 */
		Coord3d[] points = new Coord3d[1];
		points[0]= new Coord3d(0,1,1);
		trailScatter= new Scatter(points, NEW_SCATTER_POINT_COLOR);
		trailScatter.width=10.0f;
		chart.getScene().getGraph().add(trailScatter);
		
		/* this part is mandatory as per jzy3d 0.9.0
		 * will not work with 0.9.1 but little problem to be expected in migration
		 */
		Settings.getInstance().setHardwareAccelerated(true);
		// no idea why this is required.. shrugs.
		ChartLauncher.instructions();
		ChartLauncher.openChart(chart, new Rectangle(0,0,1000,1000), "3D Graph");
		
		/**
		 * Embeds the action listener right here. 
		 * PROS: easier to construct
		 * CONS: less control. eg: had to remove the INITIAL_DELAY
		 */
		new Timer(TIMER_TICK_INTERVAL, new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				paintGraph();				
			}
		}).start();
	}
	/**
	 * When resizing needs to occur, instead of letting the graph handle it as normal we manually resize 
	 * to maintain a non-skewed graph or 1:1:1 X-Y-Z scaling.
	 * 
	 * @param points
	 */
	public void resizeBoundingBox(Coord3d points[])
	{
		Float min=GuiListener.min, max=GuiListener.max;
		chart.getView().setBoundManual(new BoundingBox3d(min, max, min, max, min, max)); //for a 1-1-1 scaling on X,Y,Z	
	}
	
	public  void paintGraph()
	{
	    // the total size of the set
	    int size = TouchPoints.getPositionLog().getX().size();
	    //Integer size = new Integer()
	    // the subset which is the new count
	    if(size<previousCnt) //toggled when we output the points
	    {
	    	//chart.getScene().clear();
	    	chart.getScene().getGraph().getAll().clear();
	    	previousCnt=0;
	    	chart.getScene().getGraph().add(trailScatter);
	    }
	    int newsize = size - previousCnt;
	    
	    /* take a snapshop of all the points in X, Y, and Z. there's no need to loop on
	     * the arraylist to get it individually. We just take the arraylist and convert it to an Array
	     * TODO: see if there's a built-in function to just take a subset from START_INDEX to END_INDEX
	     * FIXME: this may not be thread safe because the size of getPalmX may not be the same in between the calculation of
	     * the size and the initialization of the array. A proper engineering of this function will allow the redraw function 
	     * to not require any synchronization. Or at least it will allow fewer lines inside the synchronize, as opposed to synchronizing 
	     * the entire function 
	     */
	    Float arrayX[] = TouchPoints.getPositionLog().getX().toArray(new Float[size]);
		Float arrayY[] = TouchPoints.getPositionLog().getY().toArray(new Float[size]);
		Float arrayZ[] = TouchPoints.getPositionLog().getZ().toArray(new Float[size]);
		
		/* FIXME This loop should/could be rewritten for readability 
		 * basically we just loop over the NEW points, 
		 * the new points are everything from the end of the last size to the end of the current size
		 * note the pointCount var counts from the 0 to newsize
		 */
	    Coord3d[] points = new Coord3d[newsize];
	    Color[]   colors = new Color[newsize];
		for(int pointCount = 0, coordCount = previousCnt; pointCount < newsize; pointCount++, coordCount++) {
			points[pointCount] = new Coord3d(arrayZ[coordCount], arrayX[coordCount], arrayY[coordCount]);
			colors[pointCount] = new Color(.5f, .5f,.7f, 0.5f);
		}
	    trailScatter.coordinates=points; //set trailpoints to the refreshed(new) points, will be dynamically redrawn by(jzy3d) when these are changed since the object has changed.
	    
		// build the new points to be appended to the current graph
		Scatter newScatter = new Scatter(points, colors);
		newScatter.width = NEW_SCATTER_POINT_WIDTH;
		/* when the window is closed, the chart will have no scene
		 * so, we check that the scene here is valid first before we actually move forward
		 */
		if(chart.getScene() != null){
			chart.getScene().getGraph().add(newScatter);
		}

		/** If we need to resize the graph in one axis we resize the graph in all axies so that we remain scaled 1-1-1 x,y,z**/
		if(points.length!=0){
			resizeBoundingBox(points);
		}

		// set the old size count to the current size. Will be used to calculate the subset (for new points) later
		previousCnt=size;
	}
	
}
