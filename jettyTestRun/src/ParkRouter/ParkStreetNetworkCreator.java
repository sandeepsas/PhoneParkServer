package ParkRouter;
//Class that creates a graph

import java.util.*;

import MapDatabase.*;

import java.util.ArrayList;

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
