package edu.baylor.hci.Experiment;

/**
 * the data structure for holding the points. 
 * Will be nedeed for matching
 * @author Alvin
 */

public class DndPair {
	private int dragClusterId;
	private int dropClusterId;
	private int dragPicId;
	private int dropPicId;

	
	/**
	 * Constructor, explicitly requires the cluster and pic id
	 * @param clusterid
	 * @param picId
	 */
	DndPair (int dragClusterid, int dragPicId, int dropClusterId, int dropPicId) {		
		this.dragClusterId = dragClusterid;
		this.dragPicId = dragPicId;
		this.dropClusterId = dropClusterId;
		this.dropPicId = dropPicId;
	}
	

	public int getDragClusterId() {
		return dragClusterId;
	}


	public int getDropClusterId() {
		return dropClusterId;
	}


	public int getDragPicId() {
		return dragPicId;
	}


	public int getDropPicId() {
		return dropPicId;
	}


}