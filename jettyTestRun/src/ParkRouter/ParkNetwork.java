package ParkRouter;

import java.util.HashMap;

public class ParkNetwork {
	private HashMap<Integer, ParkNode> nodes; // Indexed by NodeId
	private ParkEdge edges[][]; // Used to save the edges between the nodes.
	private double edgeWeights[][]; // Used to save the distances between the
									// nodes.

	public static int nodeIdStart = 0; // Used to convert the nodeId's to start
										// at 0. //sandeep changed
										// We assume that the nodes have
										// consecutive id's.

	public ParkNetwork(int n) {
		nodes = new HashMap<Integer, ParkNode>();
		edges = new ParkEdge[n][n];
		edgeWeights = new double[n][n];
		for (int i = 0; i < n; i++) {
			for (int j = 0; j < n; j++) {
				edges[i][j] = null;
				edgeWeights[i][j] = Double.POSITIVE_INFINITY;
			}
		}
	}

	public void addNode(ParkNode node) {
		Integer nodeId = new Integer(node.getId());
		nodes.put(nodeId, node);
	}

	public void addEdge(ParkEdge edge) {
		ParkNode node1 = edge.getNode1();
		ParkNode node2 = edge.getNode2();

		int nodeId1 = node1.getId() - nodeIdStart;
		int nodeId2 = node2.getId() - nodeIdStart;
		
		if(edge.isOneWay()){
			//Handle OneWay
			edges[nodeId1][nodeId2] = edge;
			edges[nodeId2][nodeId1] = edge;
			edgeWeights[nodeId1][nodeId2] = Spatial.distance(node1, node2);
			edgeWeights[nodeId2][nodeId1] = Spatial.distance(node1, node2);
			
		}else{
			edges[nodeId1][nodeId2] = edge;
			edges[nodeId2][nodeId1] = edge;
			edgeWeights[nodeId1][nodeId2] = Spatial.distance(node1, node2);
			edgeWeights[nodeId2][nodeId1] = Spatial.distance(node1, node2);
		}

		
		
		
		//System.out.println(nodeId1 + " -> " + nodeId2 + " " + edgeWeights[nodeId1][nodeId2]);
	}

	public HashMap<Integer, ParkNode> getNodes() {
		return nodes;
	}

	public ParkEdge[][] getEdges() {
		return edges;
	}

	public double[][] getEdgeWeights() {
		return edgeWeights;
	}
}
