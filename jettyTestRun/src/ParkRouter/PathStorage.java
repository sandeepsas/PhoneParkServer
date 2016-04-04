package ParkRouter;

import java.util.ArrayList;

public class PathStorage {
	
	ArrayList<ArrayList<ArrayList<Integer>>> finalPathsList = new ArrayList<ArrayList<ArrayList<Integer>>>();
	
	public PathStorage(){

	}
	
	public void addGCMPaths(ArrayList<ArrayList<Integer>> arr){
		finalPathsList.add(arr);
	}

}
