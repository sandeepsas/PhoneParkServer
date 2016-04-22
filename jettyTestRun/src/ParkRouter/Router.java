package ParkRouter;

/*
 * Router.java
 * 
 * @Author: Qing Guo
 * @Modified by: Sandeep Sasidharan
 * 
 * Starts the server
 * */
import java.io.*;
import java.time.LocalDateTime;
import java.util.*;

import Database.LoadHPP;
import Database.LoadPRT;
import MapDatabase.DirectedEdge;
import MapDatabase.GraphNode;
import MapDatabase.Pair;
import Runner.StartServer;
import StreetBlock.KdTree;
import StreetBlock.KdTree.XYZPoint;
import parkAttribs.StatisticMatrices;

public class Router {

	/* Class member declarations */

	/* Recovery function variables */
	private int k_tau;
	private int h;

	/* Road network variables */
	ParkStreetNetworkCreator creator;
	private int n;
	private int nBlocks;
	private ArrayList<ArrayList<Integer>> adjNodes = new ArrayList<ArrayList<Integer>>();
	private ParkNetwork road = new ParkNetwork(n);
	private ParkEdge edges[][];
	private double edgeWeights[][];
	private double[][] edgeCosts;
	private double[][] SP;
	private ParkEdge edgeList[];
	private HashMap<Integer, int[]> blockIdMap = new HashMap<Integer, int[]>();
	private int blockIds[] = new int[nBlocks];
	HashMap<Long, Integer> gNodeMap;
	private HashMap<Integer, ParkNode> nodes = new HashMap<Integer, ParkNode>();

	/* GCM variables */

	private ArrayList<Integer> runningOptTOPath = new ArrayList<Integer>();
	private ArrayList<ArrayList<Integer>> optimalPaths = new ArrayList<ArrayList<Integer>>();
	ArrayList<ArrayList<ArrayList<Integer>>> finalPathsList = new ArrayList<ArrayList<ArrayList<Integer>>>();

	/* Statistical Matrices */
	private double[][] probability;
	private double[][] avail;
	private double[][] var; // variance
	private double[][] probByExp; // estimated probabilities from mean and
	// variance.

	/* Node and Edge variable from OSM */
	LinkedList<GraphNode> ll_nodes;
	LinkedList<DirectedEdge> ll_edges;

	private final int EndOfSearch = -2;

	KdTree<XYZPoint> parkingBlockTree = new KdTree<XYZPoint>();

	public KdTree<XYZPoint> getParkingBlockTree() {
		return parkingBlockTree;
	}
/*
 * Date Member
 * */
	int day = 1;
	/*
	 * Route constructor
	 * 
	 * This takes nodes and edges of the road network as input
	 * 
	 */
	public Router(LinkedList<GraphNode> nodes2, LinkedList<DirectedEdge> edges2) throws IOException {
		/* Store the nodes and edges in the class */
		Calendar cal = Calendar.getInstance();
		day = cal.get(Calendar.DAY_OF_WEEK);
		
		ll_nodes = nodes2;
		ll_edges = edges2;
		nBlocks = edges2.size(); // Total number of blocks
		this.setBlockIds(new int[nBlocks]);
		System.out.println("Started Filling Road Graph");
		creator = new ParkStreetNetworkCreator(nodes2, edges2); // Create the
		// road network
		// graph
		n = ParkStreetNetworkCreator.N_NODES;
		edgeCosts = new double[n][n];
		gNodeMap = creator.getGraphNodeMap();

		road = creator.getRoad(); // Retrieve roads
		nodes = creator.getNodes(); // Retrieve nodes
		edges = creator.getEdges(); // Retrieve edges
		//parkingBlockTree = creator.getParkingBlockList();
		edgeWeights = creator.getEdgeWeights(); // Edge length
		SP = creator.getShortestPaths(); // All pair shortest paths
		creator.getSP_direction();// Will contain the first node to move towards
		// in the shortest path between node i and
		// node j.

		edgeList = creator.getEdgeList(); // Indexed list of edges
		/* Compute the adjacency list */
		computeAdjList();

		/* Compute the cost of the edges in terms of travel time */
		for (int i = 0; i < n; i++) {
			ArrayList<Integer> adjNodesForI = new ArrayList<Integer>();
			for (int j = 0; j < n; j++) {
				if (edges[i][j] != null) {
					adjNodesForI.add(j);
					edgeCosts[i][j] = edgeWeights[i][j] / StartServer.getServerconfig().velocity;
				}
			}
			adjNodes.add(i, adjNodesForI);
		}
		/*
		 * creates the HashMap that given a blockId will give its connecting
		 * nodes
		 */
		createBlockIdMap();

		/* Set recovery function length based on no of blocks or time */
		h = (int) Math.ceil(StartServer.getServerconfig().tau / 21.0); // min cost = 21 sec. avg
		// // cost = 59 sec.
		if (h < 6) {
			h = 6;
		}
		k_tau = h;
		System.out.println("History Path Computation"); // For debug purpose

		/*
		 * Compute the paths with respect to the recovery function for
		 * individual nodes
		 */
		initiateHistoryPaths();

		/* Initialize the probability matrix with 0.5 */
		StatisticMatrices statisticMatrices = new StatisticMatrices(n);
		probability = statisticMatrices.getProbabilityMatrix();

		/*
		 * Initialize availability for all blocks a random value between 0 and
		 * 20
		 */
		// @TODO - This need to be changed as per discussion on 07 Mar 2016
		HashMap<Pair<Integer, Integer>,Integer> rsMap = LoadPRT.fetchRecord();
		/*Load 24 hour parking profile*/
		for(int hh=0;hh<24;hh++){
			avail = statisticMatrices.getAvailMatrix();

			for (int i = 0; i < n; i++) {
				for (int j = 0; j < n; j++) {
					if (edges[i][j] != null) {
						int streetID = edges[i][j].getStreetID();
						Pair<Double,Double> mu_sigma =  creator.getLoadHPP().fetchValFromHPPMap(streetID,day, hh);
						probability[i][j] = mu_sigma.getR();
						//probability[i][j] = edges[i][j].getProbability();
						avail[i][j] = mu_sigma.getL();;
						Pair<Integer,Integer> key = new Pair<Integer,Integer>(streetID,hh);
						if(rsMap.containsKey(key)){
							int startTimeRestriction  = hh;
							int endTimeRestriction  = rsMap.get(key);
							if((hh>=startTimeRestriction) && 
									(hh<endTimeRestriction)){
								probability[i][j] = 0;
								avail[i][j] = 0;
							}
						}
					}
				}
			}
			/* New Probability Calculation */
			var = new double[n][n]; // Variance need to be re-initialized every
			// time.
			probByExp = new double[n][n]; // Prob need to be re-initialized every
			// time.
			//computeProbAvail();

			/* Compute the optimal paths using GCM function */
			System.out.println("GCM Path Calculation #"+hh);
			optimalPaths = optAlgrorithmFinite();
			//System.out.println(optimalPaths.get(0).toString());
			finalPathsList.add(optimalPaths);
			/* Clear the runningOptTOPath member for Route request */
			runningOptTOPath.clear();
			
		}
		System.out.println("Process Finished");
		System.out.println("Run started at "+ LocalDateTime.now() );
	}

	public ParkStreetNetworkCreator getCreator() {
		return creator;
	}

	private void computeProbAvail() {
		accumulatedAvail();
		accumulatedVar();
		for (int i = 0; i < n; i++) {
			for (int j = 0; j < n; j++) {
				if (edges[i][j] != null) {
					int streetID = edges[i][j].getStreetID();
					int no_samples = LoadHPP.pullDataFromHPP(streetID).getTotalSamplesForStreet();
					var[i][j] = var[i][j] / (no_samples - 1);
					probByExp[i][j] = 1 - StatisticMatrices.Phi(0.5, avail[i][j], Math.sqrt(var[i][j]));
				}
			}
		}

	}

	private void accumulatedAvail() {
		for (int i = 0; i < n; i++) {
			for (int j = 0; j < n; j++) {
				if (edges[i][j] != null) {
					int nBlocks = edges[i][j].getNBlocks();
					if (nBlocks == 0) {
						avail[i][j] += 0;
					} else if (nBlocks == 1) {
						int streetID = edges[i][j].getStreetID();
						avail[i][j] += LoadHPP.pullDataFromHPP(streetID).getAvgParkEstForDateTime();
					}
				} else {
					avail[i][j] += 0;
				}
			}
		}

	}

	private void accumulatedVar() {
		for (int i = 0; i < n; i++) {
			for (int j = 0; j < n; j++) {
				if (edges[i][j] != null) {
					int nBlocks = edges[i][j].getNBlocks();
					if (nBlocks == 0) {
						var[i][j] += 0;
					} else if (nBlocks == 1) {
						int streetID = edges[i][j].getStreetID();
						double number = LoadHPP.pullDataFromHPP(streetID).getAvgParkEstForDateTime();
						var[i][j] += (avail[i][j] - number) * (avail[i][j] - number);
					}
				} else {
					var[i][j] += 0;
				}
			}
		}

	}


	/* GCM ALGORITHM */
	public ArrayList<ArrayList<Integer>> optAlgrorithmFinite() {
		/* Initializations */
		ArrayList<ArrayList<HashMap<ArrayList<Integer>, Double>>> C = new ArrayList<ArrayList<HashMap<ArrayList<Integer>, Double>>>();
		ArrayList<ArrayList<HashMap<ArrayList<Integer>, Integer>>> NEXT = new ArrayList<ArrayList<HashMap<ArrayList<Integer>, Integer>>>();

		// Create possible history paths for each node:

		C.add(new ArrayList<HashMap<ArrayList<Integer>, Double>>());
		NEXT.add(new ArrayList<HashMap<ArrayList<Integer>, Integer>>());
		/* Iterate for all the nodes */
		for (int i = 0; i < n; i++) {
			/* Iterate for all the historic paths */
			for (ArrayList<Integer> path : historyPathslist.get(i)) {
				C.get(0).add(new HashMap<ArrayList<Integer>, Double>());
				NEXT.get(0).add(new HashMap<ArrayList<Integer>, Integer>());
				C.get(0).get(i).put(path, (double) StartServer.getServerconfig().beta);
				NEXT.get(0).get(i).put(path, EndOfSearch);
			}
		}
		/* Iterate for k_m number of nodes */
		for (int k = 1; k <= StartServer.getServerconfig().k_m; k++) {
			C.add(new ArrayList<HashMap<ArrayList<Integer>, Double>>());
			NEXT.add(new ArrayList<HashMap<ArrayList<Integer>, Integer>>());
			for (int i = 0; i < n; i++) {
				for (ArrayList<Integer> path : historyPathslist.get(i)) {
					C.get(k).add(new HashMap<ArrayList<Integer>, Double>());
					NEXT.get(k).add(new HashMap<ArrayList<Integer>, Integer>());
					C.get(k).get(i).put(path, Double.POSITIVE_INFINITY);
					NEXT.get(k).get(i).put(path, EndOfSearch);

					for (Integer j : adjList.get(i)) {
						if (j.intValue() != ((Integer) path.get(path.size() - 1)).intValue()
								|| adjList.get(i).size() == 1) {
							ArrayList<Integer> lastHistoryList = new ArrayList<>();
							for (int idx = 0; idx < path.size(); idx++) {
								lastHistoryList.add((int) path.get(idx));
							}
							lastHistoryList.add(i);
							lastHistoryList.remove(0);
							boolean recentlyTraversed = false;
							double accumulatedTime = 0;
							for (int pathIdx = 1; pathIdx < path.size(); pathIdx++) {
								if (StartServer.getServerconfig().tau == 0) {
									// Break if recovery fn time is set to Zero
									break;
								}
								/*
								 * Compute the time accumulated from the edge
								 * travel times
								 */
								accumulatedTime = accumulatedTime
										+ edgeCosts[(int) path.get(pathIdx)][(int) path.get(pathIdx - 1)];
								if ((((Integer) path.get(pathIdx)).intValue() == i && path.get(pathIdx - 1) == j)
										|| (path.get(pathIdx) == j
										&& ((Integer) path.get(pathIdx - 1)).intValue() == i)) {
									recentlyTraversed = true;
								}
								if (accumulatedTime >= StartServer.getServerconfig().tau) {
									// Break if accumulated time is more than
									// 2mins
									break;
								}
							}
							double p_ij = recentlyTraversed ? 0 : probability[i][j];
							double C_ijk = edgeCosts[i][j] + (1 - p_ij) * C.get(k - 1).get(j).get(lastHistoryList);

							if (C_ijk < C.get(k).get(i).get(path)) {
								C.get(k).get(i).put(path, C_ijk);
								NEXT.get(k).get(i).put(path, j);
							}
						}

					}
				}
			}
		}
		/* Initialize the final set of paths to be retrieved */
		ArrayList<ArrayList<Integer>> finalPaths = new ArrayList<ArrayList<Integer>>();

		for (int i = 0; i < n; i++) {
			ArrayList<Integer> finalPath = new ArrayList<Integer>(StartServer.getServerconfig().k_m);
			ArrayList<Integer> runningHistory = new ArrayList<Integer>();
			finalPath.add(0, i);
			runningHistory.add(0, i);
			for (int k = 1; k <= StartServer.getServerconfig().k_m; k++) {
				int currentNode = finalPath.get(k - 1);
				if (k > k_tau) {
					new ArrayList<Integer>(runningHistory);
					int nextNode = NEXT.get(StartServer.getServerconfig().k_m - k).get(currentNode)
							.get(runningHistory.subList(0, (int) runningHistory.size() - 1));
					finalPath.add(nextNode);
					runningHistory.add(nextNode);
					runningHistory.remove(0);
				} else {
					ArrayList<Integer> runningHistoryArg = new ArrayList<Integer>();
					for (ArrayList<Integer> path : historyPathslist.get(currentNode)) {
						if (runningHistory.subList(0, (int) runningHistory.size() - 1)
								.equals(path.subList(k_tau - k + 1, k_tau))) {
							for (int idx = 0; idx < path.size(); idx++) {
								runningHistoryArg.add((int) path.get(idx));
							}
							break;
						}
					}
					int nextNode = NEXT.get(StartServer.getServerconfig().k_m - k).get(currentNode).get(runningHistoryArg);
					finalPath.add(nextNode);
					runningHistory.add(nextNode);
				}
			}
			finalPath.remove(0);
			finalPaths.add(i, finalPath);
		}
		return finalPaths;
	}

	public void DFSforHistoryPaths(ArrayList<ArrayList<Integer>> historyPathsForI, ArrayList<Integer> currentPath,
			int root, int depth) {

		if (depth != k_tau) {
			int currentNode = currentPath.get(0);
			for (int i = 0; i < n; i++) {
				if (edges[i][currentNode] != null) {
					currentPath.add(0, i);
					DFSforHistoryPaths(historyPathsForI, currentPath, root, depth + 1);
					currentPath.remove(0);
				}
			}
		} else {
			ArrayList<Integer> finalPath = new ArrayList<Integer>();
			for (int idx = 0; idx < currentPath.size(); idx++) {
				finalPath.add((int) currentPath.get(idx));
			}
			historyPathsForI.add(finalPath);
		}
	}

	/* Compute the paths with respect to the recovery function */
	private ArrayList<ArrayList<ArrayList<Integer>>> historyPathslist = new ArrayList<ArrayList<ArrayList<Integer>>>();

	void initiateHistoryPaths() { // Create possible history paths for each
		// node:
		for (int i = 0; i < n; i++) {
			ArrayList<ArrayList<Integer>> historyPathsForI = new ArrayList<ArrayList<Integer>>();
			ArrayList<Integer> currentPath = new ArrayList<Integer>();
			for (int i_1 = 0; i_1 < n; i_1++) {
				if (edges[i_1][i] != null) { // k_tau == 0 is not considered.
					currentPath.add(0, i_1);
					DFSforHistoryPaths(historyPathsForI, currentPath, i, 1);
					currentPath.remove(0);
				}
			}
			historyPathslist.add(historyPathsForI);
		}
	}

	/*
	 * This function creates the HashMap that given a blockId will give its
	 * connecting nodes. Also will create a function of the blockId's in the
	 * array blockIds.
	 */

	public void createBlockIdMap() {
		for (int i = 0; i < edgeList.length; i++) {
			ParkEdge tempEdge = edgeList[i];
			int blockId = tempEdge.getBlockId1();
			int nodeId1 = tempEdge.getNode1().getId();
			int nodeId2 = tempEdge.getNode2().getId();
			int connectingNodes[] = new int[2];
			connectingNodes[0] = nodeId1;
			connectingNodes[1] = nodeId2;
			blockIds[i] = blockId;
			blockIdMap.put(blockId, connectingNodes);
		}
	}

	/* Compute the adjacency list */
	private ArrayList<ArrayList<Integer>> adjList = new ArrayList<ArrayList<Integer>>(n);

	public void computeAdjList() {
		for (int i = 0; i < n; i++) {
			ArrayList<Integer> adjNodesForI = new ArrayList<Integer>();
			for (int j = 0; j < n; j++) {
				if (edges[i][j] != null) {
					adjNodesForI.add(j);
				}
			}
			adjList.add(adjNodesForI);
		}
	}

	/* Getters and Setters */

	public ParkNetwork getRoad() {
		return road;
	}

	public HashMap<Long, Integer> getgNodeMap() {
		return gNodeMap;
	}

	public void setBlockIds(int[] blockIds) {
		this.blockIds = blockIds;
	}

	public ArrayList<Integer> getOPTPath(int initialLocation) {
		Calendar now = Calendar.getInstance();
		int time = now.get(Calendar.HOUR_OF_DAY);
		runningOptTOPath = new ArrayList<Integer>(finalPathsList.get(time).get(initialLocation));
		return runningOptTOPath;
	}

}
