package ParkRouter;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Random;

import MapDatabase.DirectedEdge;
import MapDatabase.GraphNode;

public class Router {

	private boolean testOptTravelCost = true;
	private int k_m = 17; // Maximum number of edges to be displayed
	private int k_tau; // = 4; // length of history in recovery function.
	private long timeDurationInMin;
	private int probDurationInMin;

	private double velocity = 8.9408/4.0; 
	private double velocityWalking = Double.POSITIVE_INFINITY;

	private int tau;
	private int h;
	private int beta;
	ParkStreetNetworkCreator creator;

	private int n = ParkStreetNetworkCreator.N_NODES;
	private int nBlocks; 

	private ArrayList<Integer> runningOptTOPath = new ArrayList<Integer>();
	private double [][] probability;
	private double [][] avail;
	private double [][] var; // variance
	//private double [][] probByExp;

	private ArrayList<ArrayList<Integer>> optimalPaths = new ArrayList<ArrayList<Integer>>();
	private ArrayList<ArrayList<Integer>> adjNodes = new ArrayList<ArrayList<Integer>>();

	private ParkNetwork road = new ParkNetwork(n);
	private HashMap<Integer,ParkNode> nodes;
	private ParkEdge edges[][];
	private double edgeWeights[][];
	private double [][] edgeCosts = new double[n][n];
	private ParkEdge edgeList[];
	private double SP[][];           // Will contain the shortest path distance between node i and node j.
	private HashMap<Integer,int[]> blockIdMap = new HashMap<Integer,int[]>();
	private int blockIds[] = new int[nBlocks];

	private ParkingData groundTruthData;
	private HashMap<Integer,Integer> foundEmpty;
	private HashMap<Integer,Timestamp> foundEmptyTime;
	private ParkLocation currentLocation;


	LinkedList<GraphNode> ll_nodes;
	LinkedList<DirectedEdge> ll_edges;

	private final int EndOfSearch = -2;
	
	HashMap<Long,Integer> gNodeMap;


	public Router(LinkedList<GraphNode> nodes2, LinkedList<DirectedEdge> edges2) throws IOException {
		ll_nodes = nodes2;
		ll_edges = edges2;
		nBlocks = edges2.size();
		this.setBlockIds(new int[nBlocks]);
		creator = new ParkStreetNetworkCreator(nodes2,edges2);
		
		gNodeMap = creator.getGraphNodeMap();

		road = creator.getRoad();
		nodes = creator.getNodes();
		edges = creator.getEdges();
		edgeWeights = creator.getEdgeWeights();
		SP = creator.getShortestPaths();
		creator.getSP_direction();
		edgeList = creator.getEdgeList();
		computeAdjList();

		// Compute edgeCosts and adjNodes:
		for (int i = 0; i < n; i++) {
			ArrayList<Integer> adjNodesForI = new ArrayList<Integer>();
			for (int j = 0; j < n; j++) {
				if (edges[i][j] != null) {
					adjNodesForI.add(j);
					edgeCosts[i][j] = edgeWeights[i][j] / velocity;
				}
			}
			adjNodes.add(i, adjNodesForI);
		}

		createBlockIdMap();
		double timeToParkOptTO;
		tau = 120;
		h = (int) Math.ceil(tau / 21.0); // min cost = 21 sec. avg
		// cost = 59 sec.
		
		if (h < 6) {
			h = 6;
		}
		k_tau = h;
		System.out.println("initiateHistoryPaths");
		initiateHistoryPaths();
		probDurationInMin = 60;
		timeDurationInMin = probDurationInMin;
		groundTruthData = new ParkingData("data/avail.csv");

		Timestamp startingTimeInterval = new Timestamp(2016, 2,
				21, 7, 55, 0, 0);

		groundTruthData.initiateTheParkingMap(new Timestamp(2016, 2, 21, 0, 0, 0, 0));
		probability = new double[n][n];
		avail = new double[n][n];
		var = new double[n][n];
		//probByExp = new double[n][n];
		//computeProbAvail();
		
		for (int i = 0; i < n; i++) {
			for (int j = 0; j < n; j++) {
				if (edges[i][j] != null) {
					
					//int noSpaces = (int) (Math.random() * (100 - 0)) + 0;
					
					//probability[i][j] = noSpaces/100;
					probability[i][j] = 0.5;
					
					int avail1 = (int) (Math.random() * (20 - 0)) + 0;
					avail[i][j] = avail1;
				}
			}
		}

		beta = 60*60;
		timeToParkOptTO = 0.0;

		if (testOptTravelCost) {
			System.out.println("testOptTravelCost");
			optimalPaths = optAlgrorithmFinite();
			File file = new File("pasth.csv");
			if (!file.exists()) {
				file.createNewFile();
			}
			FileWriter fw = new FileWriter(file.getAbsoluteFile());
			BufferedWriter bw = new BufferedWriter(fw);
			for(int kk=0;kk<optimalPaths.size();kk++)
				bw.write("\n"+optimalPaths.get(kk));
			
			bw.close();
		}

		//Run Sim
		runningOptTOPath.clear();
		generateRandomTime(startingTimeInterval,
				timeDurationInMin);


		System.out.println("\r" + 0 + "  " + probDurationInMin + "  " + k_m
				+ "  " + 1 + "  " + 
				+ timeToParkOptTO );

	}
	
	public HashMap<Long, Integer> getgNodeMap() {
		return gNodeMap;
	}

	public ArrayList<Integer> getOPTPath(int initialLocation){
		groundTruthData.restartTheParkingMap();
		runningOptTOPath = new ArrayList<Integer>(
				optimalPaths.get(initialLocation));
		return runningOptTOPath;
	}
	
	public int optTOAlgorithmSingleStep() {
		int optStep = EndOfSearch;
		if (!runningOptTOPath.isEmpty()) { // if there are still more edges in
			// runningOptPath
			optStep = runningOptTOPath.get(0);
			runningOptTOPath.remove(0);
		}
		return optStep;
	}

	public double runSim(int iNode, int dest, Timestamp it) {
		boolean parked = false;
		currentLocation = new ParkLocation(edges[0][1], 0, 0); // Needs to be
		int currentNode = iNode;
		foundEmpty = new HashMap<Integer, Integer>();
		foundEmptyTime = new HashMap<Integer, Timestamp>();
		Timestamp currentTime = new Timestamp(it.getTime());
		double timeToPark = 0.0;
		while (!parked) {
			double costOfContinueSearch = 0.0;
			if (true) {
				System.out.print((currentNode + 1) + " ");
			}


			// Update foundEmpty map for this vehicle
			int nBlocks;

			if (true) { //Sandeep Commented
				outputVis(currentLocation);
			}

			// ****** Moving towards node, move the vehicle to the node and
			// choose the next block to take with offset 0.

			int newDirection = -1;
			newDirection = optTOAlgorithmSingleStep();


			if (newDirection == EndOfSearch) {
				//optTOUnsuccessCount++; Commented by Sandeep
				return timeToPark;
				// System.out.println("EndOfSearch Returned!");
				// System.exit(-1);
			}

			double blockDistance = edgeWeights[currentNode][newDirection];
			// System.out.println("alg" + algorithm + "[" + currentNode + "][" +
			// newDirection + "]");

			currentTime.getTime();
			currentTime = addTimes(currentTime, blockDistance / velocity);
			groundTruthData.advanceToTime(currentTime);
			// probabilityProfile.advanceToTime(currentTime);

			currentLocation.setEdge(edges[currentNode][newDirection]);
			currentLocation.setDirection(newDirection);

			double newOffset = blockDistance;
			currentLocation.setOffset(newOffset);
			currentNode = newDirection;

			ParkEdge currentEdge = currentLocation.getEdge();
			nBlocks = currentEdge.getNBlocks();
			if (nBlocks > 0) {
				// Reporting was erroneous for this block and so we need to save
				// this info.

				int blockId = currentEdge.getBlockId1();
				int currAv = (int) groundTruthData.getAvailability(blockId);
				int blockId2 = -1;

				if (nBlocks == 2) {
					blockId2 = currentEdge.getBlockId2();
					// currAv += (int)currMap.get(blockId2)[0];
					foundEmpty.put(new Integer(blockId2), new Integer(groundTruthData.getAvailability(blockId2)));
					foundEmptyTime.put(new Integer(blockId2), currentTime);
					// System.out.println("Block2 Added to the List: " +
					// blockId2);
				}
				foundEmpty.put(new Integer(blockId), new Integer(currAv));
				foundEmptyTime.put(new Integer(blockId), currentTime);
				// System.out.println("Block1 Added to the List: " + blockId);

			}
			parked = willPark(currentEdge, currentTime);
			if (costOfContinueSearch < SP[currentNode][dest] / velocityWalking) {
				parked = false;
			}

			if (parked) {
				if (true) {
					System.out.print((currentNode + 1) + " ");
				}
				timeToPark = (((double) currentTime.getTime() - (double) it.getTime()) / 1000.0)
						+ SP[currentNode][dest] / velocityWalking; // Time to
				// park in
				// seconds
			}
			// }
		}
		return timeToPark;
	}
	public void outputVis(ParkLocation loc)
	{
		ParkEdge e = loc.getEdge();
		int direction = loc.getDirection();
		double offset = loc.getOffset();

		ParkNode node1 = e.getNode1();
		ParkNode node2 = e.getNode2();
		int node1Id = node1.getId();//-7001;
		int node2Id = node2.getId();//-7001;

		ParkNode start = null;
		ParkNode end = null;
		int startId = -1;
		int endId = -1;

		if (node1Id == direction)
		{
			start = node2;
			startId = node2Id;
			end = node1;
			endId = node1Id;
		}
		else if (node2Id == direction)
		{
			start = node1;
			startId = node1Id;
			end = node2;
			endId = node2Id;
		}
		else
		{
			System.out.println("ERROR!!");
			System.exit(-1);
		}

		double startLat = start.getLatitude();
		double startLon = start.getLongitude();
		double endLat = end.getLatitude();
		double endLon = end.getLongitude();
		double dist = edgeWeights[startId][endId];

		double ratio = offset/dist;

		double lat = startLat + (endLat-startLat)*ratio;
		double lon = startLon + (endLon-startLon)*ratio;
		System.out.println(lat+","+lon);
	}

	public boolean willPark(ParkEdge e, Timestamp t) { // With anomalous
		// blocks removed.
		int nBlocks = e.getNBlocks();
		e.getNode1().getId();
		e.getNode2().getId();

		if (nBlocks == 0) {
			return false;
		} else if (nBlocks == 1) {
			int blockId = e.getBlockId1();
			int availability = groundTruthData.getAvailability(blockId);// +
			// (map.get(blockId)[0]-initMap.get(blockId)[0]);
			if (blockId == 326061 || blockId == 546282) {
				availability = 0;
			}

			if (availability > 0) {
				return true;
			} else {
				return false;
			}
		} else // nBlocks == 2
		{
			int blockId1 = e.getBlockId1();
			int blockId2 = e.getBlockId2();
			int availability = ((blockId1 == 326061 || blockId1 == 546282) ? 0
					: groundTruthData.getAvailability(blockId1))
					+ ((blockId2 == 326061 || blockId2 == 546282) ? 0 : groundTruthData.getAvailability(blockId2));

			// System.out.println("Block id's are: " + blockId1 + " and " +
			// blockId2 + ". Current availability is " + availability );
			if (availability > 0) {
				return true;
			} else {
				return false;
			}
		}
	}

	public Timestamp addTimes(Timestamp t, double s) {
		return new Timestamp(t.getTime() + ((long) (s * 1000)));
	}

	public int generateRandomNode() {
		Random generator = new Random();
		int randomNode = generator.nextInt(n);
		return randomNode;
	}
	public ArrayList<ArrayList<Integer>> optAlgrorithmFinite()
	{
		ArrayList<ArrayList<HashMap<ArrayList<Integer>,Double>>> C = new ArrayList<ArrayList<HashMap<ArrayList<Integer>,Double>>>();
		ArrayList<ArrayList<HashMap<ArrayList<Integer>,Integer>>> NEXT = new ArrayList<ArrayList<HashMap<ArrayList<Integer>,Integer>>>();
		// Create possible history paths for each node:
		
		C.add(new ArrayList<HashMap<ArrayList<Integer>,Double>>());
		NEXT.add(new ArrayList<HashMap<ArrayList<Integer>,Integer>>());
		for(int i = 0; i < n; i++)
		{
			// System.out.println("History path before " + i + ":\n" + historyPathslist.get(i)) ;
			for (ArrayList path : historyPathslist.get(i))
			{
				C.get(0).add(new HashMap<ArrayList<Integer>,Double>());
				NEXT.get(0).add(new HashMap<ArrayList<Integer>,Integer>());
				C.get(0).get(i).put(path,(double)beta);
				NEXT.get(0).get(i).put(path,EndOfSearch);
				// System.out.println(NEXT.get(0).get(i)); //correct
			}
		}
		
		for (int k = 1; k <= k_m; k++ )
		{
			C.add(new ArrayList<HashMap<ArrayList<Integer>,Double>>());
			NEXT.add(new ArrayList<HashMap<ArrayList<Integer>,Integer>>());
			for(int i = 0; i < n; i++)
			{
				
				for (ArrayList path : historyPathslist.get(i))
				{
					C.get(k).add(new HashMap<ArrayList<Integer>,Double>());
					NEXT.get(k).add(new HashMap<ArrayList<Integer>,Integer>());
					C.get(k).get(i).put(path,Double.POSITIVE_INFINITY);
					NEXT.get(k).get(i).put(path,EndOfSearch);
					// for (int j = 0; j < n; j++)
					for (Integer j : adjList.get(i))
					{
						// if (edges[i][j] != null)
						if (j.intValue() != ((Integer)path.get(path.size()-1)).intValue() || adjList.get(i).size() == 1)
						 {
							ArrayList<Integer> lastHistoryList = new ArrayList<>();
							for (int idx = 0; idx < path.size(); idx++)
							{
								lastHistoryList.add((int)path.get(idx));
							}
							lastHistoryList.add(i);
							lastHistoryList.remove(0);
							boolean recentlyTraversed = false;
							double accumulatedTime = 0;
							for (int pathIdx = 1;  pathIdx < path.size(); pathIdx++)
							{
								if (tau == 0)
								{
									break;
								}
								accumulatedTime = accumulatedTime + edgeCosts[(int)path.get(pathIdx)][(int)path.get(pathIdx-1)];
								if ((((Integer)path.get(pathIdx)).intValue() == i && path.get(pathIdx-1) == j) || (path.get(pathIdx) == j && ((Integer)path.get(pathIdx-1)).intValue() == i))
								{
									recentlyTraversed = true;
								}
								if (accumulatedTime >= tau) //commented as per Qings suggestion
								{
									break;
								}
							}
							double p_ij = recentlyTraversed ? 0 : probability[i][j];
							// double p_ij = recentlyTraversed ? 0 : (1-1/(avail[i][j]+1));
							double C_ijk = edgeCosts[i][j] + (1-p_ij) * C.get(k-1).get(j).get(lastHistoryList);
							
							if(C_ijk < C.get(k).get(i).get(path))
							{
								C.get(k).get(i).put(path,C_ijk);
								NEXT.get(k).get(i).put(path,j);
							}
						 }
						
					}
				}
			}
		}
		// System.out.println(C.get(2));
		ArrayList<ArrayList<Integer>> finalPaths = new ArrayList<ArrayList<Integer>>();
		
		for ( int i = 0; i < n; i++ )
		{
			ArrayList<Integer> finalPath = new ArrayList<Integer>(k_m);
			ArrayList<Integer> runningHistory = new ArrayList<Integer>();
			finalPath.add(0, i);
			runningHistory.add(0, i);
			for (int k = 1; k <= k_m; k++)
			{
				int currentNode = finalPath.get(k-1);
				if (k > k_tau)
				{
					new ArrayList<Integer>(runningHistory);
					int nextNode = NEXT.get(k_m-k).get(currentNode).get(runningHistory.subList(0,(int)runningHistory.size()-1));
					finalPath.add(nextNode);
					runningHistory.add(nextNode);
				
					runningHistory.remove(0);
				}
				else
				{
					ArrayList<Integer> runningHistoryArg = new ArrayList<Integer>();
					for (ArrayList path : historyPathslist.get(currentNode))
					{
						if(runningHistory.subList(0,(int)runningHistory.size()-1).equals(path.subList(k_tau-k+1, k_tau)))
						{	
							// System.out.println(runningHistory + "  vs  " + path.subList(k_tau-k, k_tau));
							for (int idx = 0; idx < path.size(); idx++)
							{
								runningHistoryArg.add((int)path.get(idx));
							}
							break;
						}
					}
					// System.out.println(runningHistory);
					int nextNode = NEXT.get(k_m-k).get(currentNode).get(runningHistoryArg);
					finalPath.add(nextNode);
					runningHistory.add(nextNode);
				}
			}
			finalPath.remove(0);
			
			finalPaths.add(i, finalPath);
		}
		return finalPaths;
	}
	public void accumulatedProb() { // With anomalous blocks removed.
		for (int i = 0; i < n; i++) {
			for (int j = 0; j < n; j++) {
				if (edges[i][j] != null) {
					int nBlocks = edges[i][j].getNBlocks();
					if (nBlocks == 0) {
						probability[i][j] = 0;
					} else if (nBlocks == 1) {
						int blockId1 = edges[i][j].getBlockId1();
						if (blockId1 != 326061 && blockId1 != 546282 && groundTruthData.getAvailability(blockId1) > 0) {
							probability[i][j]++;
						}

					} else // nBlocks == 2
					{
						int blockId1 = edges[i][j].getBlockId1();
						int blockId2 = edges[i][j].getBlockId2();
						double avail1 = (blockId1 == 326061 || blockId1 == 546282) ? 0
								: groundTruthData.getAvailability(blockId1);
						double avail2 = (blockId2 == 326061 || blockId2 == 546282) ? 0
								: groundTruthData.getAvailability(blockId2);
						if ((avail1 + avail2) > 0) {
							probability[i][j]++;
						}
						// System.out.println(blockId1 + ": " +
						// groundTruthData.getAvailability(blockId1));
					}
				} else {
					probability[i][j] = 0;
				}
			}
		}
	}
	public void computeProbAvail() {
		List<String> dates = new ArrayList<String>();
		// dates.add("306"); // 4/6/2012
		dates.add("309");
		dates.add("310");
		dates.add("311");
		dates.add("312");
		dates.add("313");
		dates.add("316");
		dates.add("317");
		dates.add("318");
		dates.add("319");
		dates.add("320");
		dates.add("323");
		dates.add("324");
		dates.add("325");
		dates.add("326");
		dates.add("327");
		dates.add("330");
		dates.add("401");
		dates.add("402");
		dates.add("403");
		dates.add("404");
		double numberOfAtoms = dates.size();
		Timestamp[] randomTime = new Timestamp[(int) numberOfAtoms];
		int randomTimeIdx = 0;
		for (String date : dates) {
			Timestamp startTime = new Timestamp(2016, Integer.parseInt(date.substring(0, 1)),
					Integer.parseInt(date.substring(1)), 0, 0, 0, 0); // One
			// time.
			// Timestamp randomTime;
			randomTime[randomTimeIdx] = generateRandomTime(startTime, probDurationInMin);
			groundTruthData.advanceToTime(randomTime[randomTimeIdx]);
			accumulatedProb();
			accumulatedAvail();
			randomTimeIdx++;
		}
		for (int i = 0; i < n; i++) {
			for (int j = 0; j < n; j++) {
				if (edges[i][j] != null) {
					probability[i][j] = probability[i][j] / numberOfAtoms;
					avail[i][j] = avail[i][j] / numberOfAtoms;
					// System.out.println(probability[i][j]);
				}
			}
		}

		randomTimeIdx = 0;
		for (String date : dates) {
			// Timestamp startTime = new Timestamp(initYear-1900,
			// Integer.parseInt(date.substring(0,1)),
			// Integer.parseInt(date.substring(1)), initHour1, initMinute,
			// initSecond, 0); // One millisecond is the atom time.
			// Timestamp randomTime;
			// randomTime = generateRandomTime(startTime, probDurationInMin);
			groundTruthData.advanceToTime(randomTime[randomTimeIdx]);
			accumulatedVar();
			randomTimeIdx++;
		}

		for (int i = 0; i < n; i++) {
			for (int j = 0; j < n; j++) {
				if (edges[i][j] != null) {
					var[i][j] = var[i][j] / (numberOfAtoms - 1);
					//probByExp[i][j] = 1 - Phi(0.5, avail[i][j], Math.sqrt(var[i][j]));
					// System.out.println(probability[i][j] + " Est: " +
					// probByExp[i][j]);
				}
			}
		}

		groundTruthData.restartTheParkingMap();
	}
	public void accumulatedVar() { // With anomalous blocks removed.
		for (int i = 0; i < n; i++) {
			for (int j = 0; j < n; j++) {
				if (edges[i][j] != null) {
					int nBlocks = edges[i][j].getNBlocks();
					if (nBlocks == 0) {
						var[i][j] += 0;
					} else if (nBlocks == 1) {
						int blockId1 = edges[i][j].getBlockId1();
						if (blockId1 != 326061 && blockId1 != 546282) {
							double number = groundTruthData.getAvailability(blockId1);
							var[i][j] += (avail[i][j] - number) * (avail[i][j] - number);
						}

					} else // nBlocks == 2
					{
						int blockId1 = edges[i][j].getBlockId1();
						int blockId2 = edges[i][j].getBlockId2();
						double avail1 = (blockId1 == 326061 || blockId1 == 546282) ? 0
								: groundTruthData.getAvailability(blockId1);
						double avail2 = (blockId2 == 326061 || blockId2 == 546282) ? 0
								: groundTruthData.getAvailability(blockId2);
						double number = avail1 + avail2;
						var[i][j] += (avail[i][j] - number) * (avail[i][j] - number);
					}
				} else {
					var[i][j] += 0;
				}
			}
		}
	}
	public void accumulatedAvail() { // With anomalous blocks removed.
		for (int i = 0; i < n; i++) {
			for (int j = 0; j < n; j++) {
				if (edges[i][j] != null) {
					int nBlocks = edges[i][j].getNBlocks();
					if (nBlocks == 0) {
						avail[i][j] += 0;
					} else if (nBlocks == 1) {
						int blockId1 = edges[i][j].getBlockId1();
						if (blockId1 != 326061 && blockId1 != 546282) {
							avail[i][j] += groundTruthData.getAvailability(blockId1);
						}

					} else // nBlocks == 2
					{
						int blockId1 = edges[i][j].getBlockId1();
						int blockId2 = edges[i][j].getBlockId2();
						double avail1 = (blockId1 == 326061 || blockId1 == 546282) ? 0
								: groundTruthData.getAvailability(blockId1);
						double avail2 = (blockId2 == 326061 || blockId2 == 546282) ? 0
								: groundTruthData.getAvailability(blockId2);
						avail[i][j] += avail1 + avail2;
					}
				} else {
					avail[i][j] += 0;
				}
			}
		}
	}
	public Timestamp generateRandomTime(Timestamp startingTimeInterval, long timeDurationInMin) {
		long startInMilli = startingTimeInterval.getTime();
		long durationInMilli = timeDurationInMin * 60 * 1000;
		long randomTimeInMilli = startInMilli + (long) (Math.random() * (durationInMilli + 1));
		return new Timestamp(randomTimeInMilli);
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
		// System.out.println(historyPathslist);
	}


	public void createBlockIdMap()
	{
		// This function creates the HashMap that given a blockId will give its connecting nodes. Also
		// will create a function of the blockId's in the array blockIds.

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

	public void setBlockIds(int[] blockIds) {
		this.blockIds = blockIds;
	}
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
	
	public ParkNetwork getRoad() {
		return road;
	}
	

}
