package ParkRouter;
// Class that takes data from SFPark about their nodes and edges and creates a graph from it

import java.io.BufferedReader;
import java.io.FileReader;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedList;

import MapDatabase.*;

import java.util.ArrayList;

public class ParkStreetNetworkCreator {
	// public static final int N_NODES = 40;
	// public int N_EDGES = 63;
	public static int N_NODES = 2514;
	public int N_EDGES = 2855;
	
/*	public static int N_NODES = 994;
	public int N_EDGES = 1139;*/
	
	
	// private String nodeFilename = "./data/SFPark_nodes_FishermansWharf.csv";
	private FileReader node_fr;
	private BufferedReader node_br;
	// private String edgeFilename = "./data/SFPark_edges_FishermansWharf2.csv";
	private FileReader edge_fr;
	private BufferedReader edge_br;

	private ParkNetwork road = new ParkNetwork(N_NODES);
	private HashMap<Integer, ParkNode> nodes;
	private ParkEdge edges[][];
	private double edgeWeights[][];
	private ParkEdge edgeList[] = new ParkEdge[N_EDGES];

	private double SP[][] = new double[N_NODES][N_NODES]; // Will contain the
	// shortest path
	// distance between
	// node i and node
	// j.
	private int next[][] = new int[N_NODES][N_NODES]; // Used by the
	// Floyd-Warshall
	// algorithm to
	// reconstruct the
	// shortest paths.
	private int SP_direction[][] = new int[N_NODES][N_NODES]; // Will contain
	// the first
	// node to move
	// towards in
	// the shortest
	// path between
	// node i and
	// node j.

	private HashMap<Long,Integer> graphNodeMap;
	
	public HashMap<Long,Integer> getGraphNodeMap(){
		return graphNodeMap;
	}

	public ParkStreetNetworkCreator(LinkedList<GraphNode> nodes2, LinkedList<DirectedEdge> edges2) {
		try {
			N_NODES = nodes2.size();
			N_EDGES = edges2.size();
			road = new ParkNetwork(N_NODES);
			readNodes(nodes2);
			nodes = road.getNodes();
			readEdges(edges2);
			edges = road.getEdges();
			edgeWeights = road.getEdgeWeights();
		} catch (Exception e) {
			e.printStackTrace();
		}
		floydWarshall();
	}

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

		/*
		 * ArrayList<Integer> shortestPaths[][] = new
		 * ArrayList<Integer>[N_NODES][N_NODES]; for ( int i = 0; i < N_NODES;
		 * i++ ) { for ( int j = 0; j < N_NODES; j++ ) { shortestPaths[i][j] =
		 * reconstructPath(i,j); } }
		 */

		// System.out.println("SP value of node 0 and 2 is: " + SP[0][2]);
		for (int i = 0; i < N_NODES; i++) {
			for (int j = 0; j < N_NODES; j++) {
				// System.out.println(i+" "+j);
				ArrayList<Integer> sp = reconstructPath(i, j);
				if(sp!=null){
					SP_direction[i][j] = ((Integer) sp.get(1)).intValue(); // The
					// second
					// element
					// is
					// the
					// next
					// node
					// to
					// visit.
					System.out.println("SP_direction is " + SP_direction[i][j]);
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

	public void readEdges(LinkedList<DirectedEdge> edges2)
	{

		Iterator<DirectedEdge> edge_itr = edges2.iterator();
		int nlines = 0;
		while(edge_itr.hasNext()){
			DirectedEdge tempEdge = edge_itr.next();

			int nodeId1 = graphNodeMap.get(tempEdge.from().getId());
			int nodeId2 =graphNodeMap.get(tempEdge.to().getId());

			int blockId = (int) tempEdge.getWayId();
			int nBlocks = 1;
			int nTotal = 20;

			ParkEdge e = new ParkEdge(nodes.get(nodeId1),nodes.get(nodeId2),nBlocks,blockId,-1,nTotal,0,tempEdge.isOneway());
			road.addEdge(e);

			//System.out.println("Added edge <" + nodeId1 +","+ nodeId2 +","+ nBlocks +","+ blockId +","+ nTotal + ">");
			edgeList[nlines] = e;
			nlines++;
		}


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

	/*public static void main(String args[]) {
		SFParkRoadNetworkCreator s = new SFParkRoadNetworkCreator();
	}*/
}
