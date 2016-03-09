package ParkRouter;

import java.io.*;

import java.util.*;

import MapDatabase.DirectedEdge;
import MapDatabase.GraphNode;
import Runner.ServerConfig;

public class Router {

	/*Class member declarations*/
	
	/*Recovery function variables*/
	private int k_tau;
	private int h;

	/*Road network variables*/
	ParkStreetNetworkCreator creator;
	private int n = ParkStreetNetworkCreator.N_NODES;
	private int nBlocks; 
	private ArrayList<ArrayList<Integer>> adjNodes = new ArrayList<ArrayList<Integer>>();
	private ParkNetwork road = new ParkNetwork(n);
	private ParkEdge edges[][];
	private double edgeWeights[][];
	private double [][] edgeCosts = new double[n][n];
	private double[][] SP;
	private ParkEdge edgeList[];
	private HashMap<Integer,int[]> blockIdMap = new HashMap<Integer,int[]>();
	private int blockIds[] = new int[nBlocks];
	HashMap<Long,Integer> gNodeMap;
	private HashMap<Integer, ParkNode> nodes = new HashMap<Integer, ParkNode>();
	
	/*GCM variables*/

	private ArrayList<Integer> runningOptTOPath = new ArrayList<Integer>();
	private ArrayList<ArrayList<Integer>> optimalPaths = new ArrayList<ArrayList<Integer>>();
	
	
	/*Statistical Matrices*/
	private double [][] probability;
	private double [][] avail;
	
	/*Node and Edge variable from OSM*/
	LinkedList<GraphNode> ll_nodes;
	LinkedList<DirectedEdge> ll_edges;

	private final int EndOfSearch = -2;

	/*Route constructor
	 * 
	 * This takes nodes and edges of the road network as input
	 * 
	 * */
	public Router(LinkedList<GraphNode> nodes2, LinkedList<DirectedEdge> edges2) throws IOException {
		/*Store the nodes and edges in the class*/
		ll_nodes = nodes2;
		ll_edges = edges2;
		nBlocks = edges2.size(); //Total number of blocks
		this.setBlockIds(new int[nBlocks]);
		creator = new ParkStreetNetworkCreator(nodes2,edges2); //Create the road network graph

		gNodeMap = creator.getGraphNodeMap();

		road = creator.getRoad(); // Retrieve roads
		nodes = creator.getNodes(); //Retrieve nodes
		edges = creator.getEdges(); //Retrieve edges
		edgeWeights = creator.getEdgeWeights(); // Edge length
		SP = creator.getShortestPaths(); // All pair shortest paths
		creator.getSP_direction();// Will contain the first node to move towards in the shortest path between node i and node j.

		edgeList = creator.getEdgeList(); // Indexed list of edges
		/*Compute the adjacency list*/
		computeAdjList();

		/*Compute the cost of the edges in terms of travel time*/
		for (int i = 0; i < n; i++) {
			ArrayList<Integer> adjNodesForI = new ArrayList<Integer>();
			for (int j = 0; j < n; j++) {
				if (edges[i][j] != null) {
					adjNodesForI.add(j);
					edgeCosts[i][j] = edgeWeights[i][j] / ServerConfig.velocity;
				}
			}
			adjNodes.add(i, adjNodesForI);
		}
		/*creates the HashMap that given a blockId will give its connecting nodes*/
		createBlockIdMap();
		
		/*Set recovery function length based on no of blocks or time*/
		h = (int) Math.ceil(ServerConfig.tau / 21.0); // min cost = 21 sec. avg // cost = 59 sec.
		if (h < 6) {
			h = 6;
		}
		k_tau = h;
		System.out.println("initiateHistoryPaths"); //For debug purpose

		/*Compute the paths with respect to the recovery function for individual nodes*/
		initiateHistoryPaths();

		/*Initialize the probability matrix with 0.5*/
		probability = new double[n][n];
		
		/*Initialize availability for all blocks a random value between 0 and 20*/
		//@TODO - This need to be changed as per discussion on 07 Mar 2016
		avail = new double[n][n];
		
		for (int i = 0; i < n; i++) {
			for (int j = 0; j < n; j++) {
				if (edges[i][j] != null) {
					probability[i][j] = 0.5;
					int avail1 = (int) (Math.random() * (20 - 0)) + 0;
					avail[i][j] = avail1;
				}
			}
		}
		/*Compute the optimal paths using GCM function*/
		optimalPaths = optAlgrorithmFinite();
		/*Clear the runningOptTOPath member for Route request */
		runningOptTOPath.clear();
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
				C.get(0).get(i).put(path, (double) ServerConfig.beta);
				NEXT.get(0).get(i).put(path, EndOfSearch);
			}
		}
		/* Iterate for k_m number of nodes */
		for (int k = 1; k <= ServerConfig.k_m; k++) {
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
								if (ServerConfig.tau == 0) {
									//Break if recovery fn time is set to Zero
									break;
								}
								/*Compute the time accumulated from the edge travel times*/
								accumulatedTime = accumulatedTime
										+ edgeCosts[(int) path.get(pathIdx)][(int) path.get(pathIdx - 1)];
								if ((((Integer) path.get(pathIdx)).intValue() == i && path.get(pathIdx - 1) == j)
										|| (path.get(pathIdx) == j
												&& ((Integer) path.get(pathIdx - 1)).intValue() == i)) {
									recentlyTraversed = true;
								}
								if (accumulatedTime >= ServerConfig.tau) {
									// Break if accumulated time is more than 2mins
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
			ArrayList<Integer> finalPath = new ArrayList<Integer>(ServerConfig.k_m);
			ArrayList<Integer> runningHistory = new ArrayList<Integer>();
			finalPath.add(0, i);
			runningHistory.add(0, i);
			for (int k = 1; k <= ServerConfig.k_m; k++) {
				int currentNode = finalPath.get(k - 1);
				if (k > k_tau) {
					new ArrayList<Integer>(runningHistory);
					int nextNode = NEXT.get(ServerConfig.k_m - k).get(currentNode)
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
					int nextNode = NEXT.get(ServerConfig.k_m - k).get(currentNode).get(runningHistoryArg);
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
	
	/*Compute the paths with respect to the recovery function*/
	private ArrayList<ArrayList<ArrayList<Integer>>> historyPathslist = new ArrayList<ArrayList<ArrayList<Integer>>>();
	void initiateHistoryPaths() { // Create possible history paths for each node:
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

	/*This function creates the HashMap that given a blockId will give its connecting nodes. Also
	 will create a function of the blockId's in the array blockIds.*/

	public void createBlockIdMap()
	{
		for(int i=0;i<edgeList.length;i++){
			ParkEdge tempEdge = edgeList[i];
			int blockId = tempEdge.getBlockId1();
			int nodeId1 = tempEdge.getNode1().getId();
			int nodeId2 = tempEdge.getNode2().getId();
			int connectingNodes[] = new int[2];
			connectingNodes[0] = nodeId1;
			connectingNodes[1] = nodeId2;
			blockIds[i] = blockId;
			blockIdMap.put(blockId,connectingNodes);
		}
	}

	/*Compute the adjacency list*/
	private ArrayList<ArrayList<Integer>> adjList= new ArrayList<ArrayList<Integer>>(n);
	public void computeAdjList()
	{
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

	
	/*Getters and Setters */

	public ParkNetwork getRoad() {
		return road;
	}
	
	public HashMap<Long, Integer> getgNodeMap() {
		return gNodeMap;
	}
	
	public void setBlockIds(int[] blockIds) {
		this.blockIds = blockIds;
	}
	
	public ArrayList<Integer> getOPTPath(int initialLocation){
		runningOptTOPath = new ArrayList<Integer>(
				optimalPaths.get(initialLocation));
		return runningOptTOPath;
	}
	
	/*STATISTICAL DEPENDENCY FUNCTIONS TO CALCULATE PROBABILITY FROM EXPECTANCY*/
	
	public static double phi(double x) {
		return Math.exp(-x * x / 2) / Math.sqrt(2 * Math.PI);
	}

	// return phi(x, mu, signma) = Gaussian pdf with mean mu and stddev sigma
	public static double phi(double x, double mu, double sigma) {
		return phi((x - mu) / sigma) / sigma;
	}

	// return Phi(z) = standard Gaussian cdf using Taylor approximation
	public static double Phi(double z) {
		if (z < -8.0)
			return 0.0;
		if (z > 8.0)
			return 1.0;
		double sum = 0.0, term = z;
		for (int i = 3; sum + term != sum; i += 2) {
			sum = sum + term;
			term = term * z * z / i;
		}
		return 0.5 + sum * phi(z);
	}

	// return Phi(z, mu, sigma) = Gaussian cdf with mean mu and stddev sigma
	public static double Phi(double z, double mu, double sigma) {
		return Phi((z - mu) / sigma);
	}


}
