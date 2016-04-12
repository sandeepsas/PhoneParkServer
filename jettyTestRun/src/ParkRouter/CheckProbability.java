/*package ParkRouter;

import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.PrintStream;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.xmlpull.v1.XmlPullParserException;

import Database.LoadHPP;
import MapDatabase.DirectedEdge;
import MapDatabase.GraphNode;
import MapDatabase.Pair;
import MapDatabase.ParseOSM;
import MapDatabase.RoadGraph;
import Runner.StartServer;
import parkAttribs.StatisticMatrices;

public class CheckProbability {
	
	private static final Logger log= Logger.getLogger( CheckProbability.class.getName() );

	 Statistical Matrices 
	private static double[][] probability;
	private static double[][] avail;
	private static double[][] var; // variance
	private static double[][] probByExp; // estimated probabilities from mean and

	 Road network variables 
	static ParkStreetNetworkCreator creator;
	private static int n;
	private static int nBlocks;
	private static ArrayList<ArrayList<Integer>> adjNodes = new ArrayList<ArrayList<Integer>>();
	private static ParkNetwork road = new ParkNetwork(n);
	private static ParkEdge edges[][];
	private static double edgeWeights[][];
	private static double[][] edgeCosts;
	private double[][] SP;
	private static ParkEdge edgeList[];
	private HashMap<Integer, int[]> blockIdMap = new HashMap<Integer, int[]>();
	private int blockIds[] = new int[nBlocks];
	HashMap<Long, Integer> gNodeMap;
	private HashMap<Integer, ParkNode> nodes = new HashMap<Integer, ParkNode>();


	public CheckProbability() throws FileNotFoundException {

		System.out.println("Run started at "+ LocalDateTime.now() );
		PrintStream out = new PrintStream(new FileOutputStream("output.txt"));
		System.setOut(out);
		Parse openStreetMaps and store as class member
		try {
			ParseOSM parseOSM = new ParseOSM();
			RoadGraph roadGraph = parseOSM.getRoadGraph();
			LinkedList<GraphNode> nodes2  = roadGraph.nodes;
			LinkedList<DirectedEdge> edges2 = roadGraph.edges;

			nBlocks = edges2.size();
			creator = new ParkStreetNetworkCreator(nodes2, edges2);
			n = ParkStreetNetworkCreator.N_NODES;
			edgeCosts = new double[n][n];
			
			road = creator.getRoad(); // Retrieve roads
			nodes = creator.getNodes(); // Retrieve nodes
			edges = creator.getEdges(); // Retrieve edges
			//parkingBlockTree = creator.getParkingBlockList();
			edgeWeights = creator.getEdgeWeights(); // Edge length

			edgeList = creator.getEdgeList(); // Indexed list of edges
			 Compute the adjacency list 
			computeAdjList();

			 Compute the cost of the edges in terms of travel time 
			for (int i = 0; i < n; i++) {
				ArrayList<Integer> adjNodesForI = new ArrayList<Integer>();
				for (int j = 0; j < n; j++) {
					if (edges[i][j] != null) {
						adjNodesForI.add(j);
						edgeCosts[i][j] = edgeWeights[i][j] / 2.2352;
					}
				}
				adjNodes.add(i, adjNodesForI);
			}

			 Initialize the probability matrix with 0.5 
			StatisticMatrices statisticMatrices = new StatisticMatrices(n);
			probability = statisticMatrices.getProbabilityMatrix();

			avail = statisticMatrices.getAvailMatrix();

			for (int i = 0; i < n; i++) {
				for (int j = 0; j < n; j++) {
					if (edges[i][j] != null) {
						int streetID = edges[i][j].getStreetID();
						probability[i][j] = edges[i][j].getProbability();
						avail[i][j] = edges[i][j].getTotalAvailability();
					}
				}
			}
			 New Probability Calculation 
			var = new double[n][n]; // Variance need to be re-initialized every
			// time.
			probByExp = new double[n][n]; // Prob need to be re-initialized every
			// time.
			log.log( Level.INFO, "computeProbAvail()");
			computeProbAvail();
			log.info("DONE - > computeProbAvail()");

			System.out.println("\n ************************************************************************************************************* \n ");

			for(int i = 0; i < n; i++){
				for(int j = 0; j < n; j++){
					System.out.printf("%5d ", probByExp[i][j]);
				}
				System.out.println();
			}
			out.close();
		} catch (IOException | XmlPullParserException e) {
			e.printStackTrace();
		}



	}
	 Compute the adjacency list 
	private static ArrayList<ArrayList<Integer>> adjList = new ArrayList<ArrayList<Integer>>(n);

	public static void computeAdjList() {
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
	private static void computeProbAvail() {
		accumulatedAvail();
		accumulatedVar();
		log.info(" DONE - >accumulatedVar() "); 
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

	private static void accumulatedAvail() {
		
		log.info( "accumulatedAvail() "); 
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
		System.out.println("\n ************************************************************************************************************* \n ");

		for(int i = 0; i < n; i++){
			for(int j = 0; j < n; j++){
				System.out.print(avail[i][j]+"  ");
			}
			System.out.println();
		}

	}

	private static void accumulatedVar() {
		log.info( "accumulatedVar() "); 
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
		
		System.out.println("\n ************************************************************************************************************* \n ");

		for(int i = 0; i < n; i++){
			for(int j = 0; j < n; j++){
				System.out.print(var[i][j]+"   ");
			}
			System.out.println();
		}

	}
	
	


}
*/