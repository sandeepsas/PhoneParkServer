package MapDatabase;

/*
 * StartServer.java
 * 
 * @author: Sandeep Sasidharan
 * 
 * Parse openStreetMaps
 * */

import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.util.LinkedList;

import org.xmlpull.v1.XmlPullParser;
import org.xmlpull.v1.XmlPullParserException;
import org.xmlpull.v1.XmlPullParserFactory;

public class ParseOSM {

	/*Class Members*/
	LinkedList<GraphNode> nodes;
	LinkedList<DirectedEdge> edges;
	
	/*Initialize RoadGraph class. This class parse and stores the OpenStreetMap
	 * in Graph Data Structure Format*/
	RoadGraph g = new RoadGraph();

	/*Constructor*/
	public ParseOSM () throws FileNotFoundException, IOException, XmlPullParserException{

		/*OSM data resembles XML format. So use any XML Parser to parse the OSM info*/
		XmlPullParserFactory factory = XmlPullParserFactory.newInstance();
		factory.setNamespaceAware(true);
		XmlPullParser xpp = factory.newPullParser();
		/*Read the raw OSM file located in the folder jettyTestRun/data/osm/*/
		System.out.println("Started Parsing OSM");
		xpp.setInput ( new FileReader ("data/osm/UICCampus.osm"));

		/*Start the parser*/
		g.osmGraphParser(xpp);
		/*store the nodes and edges as class members*/
		nodes = g.nodes;
		edges = g.edges;

		System.out.println("Edges = "+edges.size());//For debug purpose
		System.out.println("Nodes = "+nodes.size());//For debug purpose
	}
	
	//Getter and Setter functions of class members
	public LinkedList<GraphNode> getNodes() {
		return nodes;
	}
	public void setNodes(LinkedList<GraphNode> nodes) {
		this.nodes = nodes;
	}
	public LinkedList<DirectedEdge> getEdges() {
		return edges;
	}
	public void setEdges(LinkedList<DirectedEdge> edges) {
		this.edges = edges;
	}
	public RoadGraph getRoadGraph() {
		return g;
	}
	public void setG(RoadGraph g) {
		this.g = g;
	}

}
