package ParkRouter;
//Class that creates a graph

import java.util.*;

import Database.LoadHPP;
import Database.LoadPRT;
import MapDatabase.*;
import StreetBlock.KdTree;
import StreetBlock.KdTree.XYZPoint;

public class ParkStreetNetworkCreator {

	public static int N_NODES;
	public int N_EDGES;
	/*Initialize empty graph*/
	private ParkNetwork road;
	private HashMap<Integer, ParkNode> nodes;
	private ParkEdge edges[][];
	private double edgeWeights[][];
	private ParkEdge edgeList[];

	private double SP[][]; // Shortest path between i and j
	private int next[][]; // Used by the Floyds Algorithm

	private int SP_direction[][]; // first node to move toward in a shortest path

	private HashMap<Long,Integer> graphNodeMap;

	public HashMap<Long,Integer> getGraphNodeMap(){
		return graphNodeMap;
	}

	public ParkStreetNetworkCreator(LinkedList<GraphNode> nodes2, LinkedList<DirectedEdge> edges2) {
		try {
			N_NODES = nodes2.size();
			N_EDGES = edges2.size();
			road = new ParkNetwork(N_NODES);
			edgeList = new ParkEdge[N_EDGES];
			SP_direction = new int[N_NODES][N_NODES];
			next = new int[N_NODES][N_NODES];
			SP = new double[N_NODES][N_NODES];
			readNodes(nodes2);
			nodes = road.getNodes();
			readEdges(edges2);
			populateKDTree(edges2);
			edges = road.getEdges();
			edgeWeights = road.getEdgeWeights();



		} catch (Exception e) {
			e.printStackTrace();
		}
		floydWarshall();
	}

	public void floydWarshall() {
		// This function will use the Floyd-Warshall algorithm to compute the
		// shortest paths between all nodes
		// and the first edge to take for each of those shortest paths.

		for (int i = 0; i < N_NODES; i++) {
			SP[i][i] = 0.0;
		}

		for (int i = 0; i < N_NODES; i++) {
			for (int j = 0; j < N_NODES; j++) {
				if (i != j) {
					SP[i][j] = edgeWeights[i][j];
				}
			}
		}

		for (int i = 0; i < N_NODES; i++) {
			for (int j = 0; j < N_NODES; j++) {
				next[i][j] = -1;
			}
		}

		for (int k = 0; k < N_NODES; k++) {
			for (int i = 0; i < N_NODES; i++) {
				for (int j = 0; j < N_NODES; j++) {
					if ((SP[i][k] + SP[k][j]) < SP[i][j]) {
						SP[i][j] = SP[i][k] + SP[k][j];
						next[i][j] = k;
					}
				}
			}
		}

		for (int i = 0; i < N_NODES; i++) {
			for (int j = 0; j < N_NODES; j++) {
				// System.out.println(i+" "+j);
				ArrayList<Integer> sp = reconstructPath(i, j);
				if(sp!=null){
					SP_direction[i][j] = ((Integer) sp.get(1)).intValue(); 
					/*The second element is the next node to visit*/
				}
			}
		}
	}

	public ArrayList<Integer> reconstructPath(int i, int j) {
		// Assumes that the next matrix has been set by the FW algorithm.

		if (SP[i][j] == Double.POSITIVE_INFINITY) {
			return null;
		}

		int intermediate = next[i][j];
		if (intermediate == -1) {
			// The edge between them is their shortest path.
			ArrayList<Integer> sp = new ArrayList<Integer>();
			sp.add(new Integer(i));
			sp.add(new Integer(j));
			return sp;
		} else {
			ArrayList<Integer> leftSP = reconstructPath(i, intermediate);
			ArrayList<Integer> rightSP = reconstructPath(intermediate, j);

			for (int k = 1; k < rightSP.size(); k++) {
				leftSP.add((Integer) rightSP.get(k));
			}
			return leftSP;
		}
	}

	HashMap<Integer, KdTree<XYZPoint>> kdTreeMap = new HashMap<Integer, KdTree<XYZPoint>> ();

	public void populateKDTree(LinkedList<DirectedEdge> edges2){
		HashMap<Pair<Integer, Integer>,Integer> rsMap = LoadPRT.fetchRecord();
		/*Load 24 hour parking profile*/
		for(int hh=0;hh<24;hh++){
			List<XYZPoint> parkingBlockList = new ArrayList<XYZPoint>(); 
			KdTree<XYZPoint> kdtreePB = new KdTree<XYZPoint> () ;

			Iterator<DirectedEdge> edge_itr = edges2.iterator();
			int streetID = 1;
			while(edge_itr.hasNext()){
				DirectedEdge tempEdge = edge_itr.next();
				/*Get probability from HPP*/
				Calendar cal = Calendar.getInstance();
				int day = cal.get(Calendar.DAY_OF_WEEK);
				Pair<Double,Double> mu_sigma =  LoadHPP.fetchAvailTimeBasedFromHPP(streetID,day, hh);
				double probability = 0.5;
				if(mu_sigma.getL()>0){
					double avg_avail = mu_sigma.getL();
					probability = mu_sigma.getR();
				}
				double length = RoadGraph.distanceInMilesBetweenPoints(tempEdge.from().getLat(),
						tempEdge.from().getLon(), tempEdge.to().getLat(), tempEdge.to().getLon());
				if(length<0.01){
					probability = 0.0;
				}
				Pair<Integer,Integer> key = new Pair<Integer,Integer>(streetID,hh);
				if(rsMap.containsKey(key)){
					probability = 0.0;
					int startTimeRestriction  = hh;
					int endTimeRestriction  = rsMap.get(key);
					if((hh>=startTimeRestriction) && 
							(hh<endTimeRestriction)){
						probability = 0;
					}
				}
				//Build KD-Tree here
				Pair<Double,Double>mid_LATLONG = OsmConstants.midPoint(tempEdge.from().getLat(),
						tempEdge.from().getLon(), tempEdge.to().getLat(), tempEdge.to().getLon());

				parkingBlockList.add(new XYZPoint(""+probability,"",mid_LATLONG.getR(),mid_LATLONG.getL(),
						tempEdge.from().getLat(),
						tempEdge.from().getLon(), tempEdge.to().getLat(), 
						tempEdge.to().getLon(),streetID));

				streetID++;
			}

			kdtreePB = new KdTree<XYZPoint>(parkingBlockList);
			kdTreeMap.put(hh, kdtreePB);
		}
	}
	public void readEdges(LinkedList<DirectedEdge> edges2)
	{
		Iterator<DirectedEdge> edge_itr = edges2.iterator();
		int nlines = 0;
		int streetID = 1;
		while(edge_itr.hasNext()){
			DirectedEdge tempEdge = edge_itr.next();

			int nodeId1 = graphNodeMap.get(tempEdge.from().getId());
			int nodeId2 =graphNodeMap.get(tempEdge.to().getId());

			int blockId = (int) tempEdge.getWayId();
			int nBlocks = 1;
			int nTotal = 20;
			double probability = 0.5;
			double length = RoadGraph.distanceInMilesBetweenPoints(tempEdge.from().getLat(),
					tempEdge.from().getLon(), tempEdge.to().getLat(), tempEdge.to().getLon());
/*			if(length<0.01){
				probability = 0.0;
			}*/
			int totalSpaces = 0;
			if(length>=0.015){
				totalSpaces = (int)(150*length);
			}

			ParkEdge e = new ParkEdge(streetID,nodes.get(nodeId1),
					nodes.get(nodeId2),nBlocks,blockId,-1,nTotal,0,
					tempEdge.isOneway(),probability,length,totalSpaces);

			road.addEdge(e);

			edgeList[nlines] = e;
			nlines++;
			streetID++;
		}
	}

/*	public KdTree<XYZPoint> getParkingBlockList() {
		return kdtreePB;
	}*/

	public HashMap<Integer, KdTree<XYZPoint>> getKDTreeMap() {
		return kdTreeMap;
	}

	public void readNodes(LinkedList<GraphNode> nodes2)
	{
		graphNodeMap = new HashMap<Long,Integer>();
		Iterator<GraphNode> node_itr = nodes2.iterator();
		int no_id_ctr = 0;
		while(node_itr.hasNext()){
			GraphNode tempNode = node_itr.next();
			road.addNode(new ParkNode(no_id_ctr,tempNode.getLat(),tempNode.getLon()));
			graphNodeMap.put(tempNode.getId(), no_id_ctr);
			no_id_ctr++;
		}
	}
	/*Getter and Setters*/
	public int[][] getSP_direction() {
		return SP_direction;
	}

	public double[][] getShortestPaths() {
		return SP;
	}

	public double[][] getEdgeWeights() {
		return edgeWeights;
	}

	public ParkEdge[][] getEdges() {
		return edges;
	}

	public ParkNetwork getRoad() {
		return road;
	}

	public ParkEdge[] getEdgeList() {
		return edgeList;
	}

	public HashMap<Integer, ParkNode> getNodes() {
		return nodes;
	}

}
