package parkAttribs;

import java.util.ArrayList;
import java.util.LinkedList;

import MapDatabase.DirectedEdge;
import MapDatabase.GraphNode;
import ParkRouter.ParkEdge;
import ParkRouter.ParkNetwork;
import ParkRouter.ParkStreetNetworkCreator;
import Runner.ServerConfig;

public class ParkBlockContainer {

	ParkStreetNetworkCreator parkNetwork;
	private ParkNetwork road;
	private ParkEdge edges[][];
	private ArrayList<ArrayList<Integer>> adjNodes;
	private double [][] edgeCosts;
	private double edgeWeights[][];
	
	private int n = ParkStreetNetworkCreator.N_NODES;

	public ParkBlockContainer(LinkedList<GraphNode> nodes2, LinkedList<DirectedEdge> edges2){

		parkNetwork =  new ParkStreetNetworkCreator(nodes2,edges2);
		road = new ParkNetwork(n);
		edges = parkNetwork.getEdges();
		edgeWeights = parkNetwork.getEdgeWeights();
		adjNodes = new ArrayList<ArrayList<Integer>>();
		edgeCosts = new double[n][n];
		
		
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
	}

	public ParkNetwork getRoad() {
		return road;
	}

	public ParkEdge[][] getEdges() {
		return edges;
	}

	public ArrayList<ArrayList<Integer>> getAdjNodes() {
		return adjNodes;
	}

	public double[][] getEdgeCosts() {
		return edgeCosts;
	}

	public double[][] getEdgeWeights() {
		return edgeWeights;
	}

	public ParkStreetNetworkCreator getParkNetwork() {
		return parkNetwork;
	}

	public void setParkNetwork(ParkStreetNetworkCreator parkNetwork) {
		this.parkNetwork = parkNetwork;
	}

}
