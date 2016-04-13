package Runner;

import java.io.IOException;
import java.time.LocalDateTime;
import java.util.Map.Entry;
import java.util.*;

import org.xmlpull.v1.XmlPullParserException;

import Database.PopulateHPP;
import MapDatabase.*;

import ParkRouter.*;
import StreetBlock.KdTree;
import StreetBlock.KdTree.XYZPoint;


/**
 * @author Sandeep
 * 
 * This class initializes the road network and 
 * performs necessary precomputations
 *
 */
public class Initializers {
	
	/*Class members*/
	static ParseOSM parseOSM;
	static RoadGraph roadGraph;
	static Router routeLoader;
	HashMap<Long,Integer> gNodeMap;
	KdTree<XYZPoint> pbTree = new KdTree<XYZPoint>();
	HashMap<Integer, KdTree<XYZPoint>> kdTreeMap;
	
	/*Class constructor*/
	public Initializers(){
		
		try {
			System.out.println("Run started at "+ LocalDateTime.now() );
			System.out.println("Loading Historic Parking Profile Tables");
			PopulateHPP.loadHistory();
			/*Parse openStreetMaps and store as class member*/
			parseOSM = new ParseOSM();
		} catch (IOException | XmlPullParserException e) {
			e.printStackTrace();
		}
		/*Get the Road Network*/
		roadGraph = parseOSM.getRoadGraph();
		try {
			/*Initialize and run the GCM algorithm for all the nodes*/
			routeLoader = new Router(roadGraph.nodes,roadGraph.edges);
			pbTree = routeLoader.getParkingBlockTree();
			kdTreeMap = routeLoader.getCreator().getKDTreeMap();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		/*Get a mapping for node IDs and LatLong values*/
		gNodeMap = routeLoader.getgNodeMap();
	}
	public KdTree<XYZPoint> getPbTree() {
		return pbTree;
	}
	/*This function retrieves the GCM path for a specific node*/
	public StringBuilder startRouting(GraphNode closestMapPoint) {
		/*Get the key corresponding to the closest intersection point 
		 * stored in the precomputed GCM paths*/
		int  myKey = 0;
		ParkNode current_node = new ParkNode(0, closestMapPoint.getLat(), closestMapPoint.getLon());
		HashMap<Integer,ParkNode> sfNwNodes = routeLoader.getRoad().getNodes();
		/*Searching for the key*/
		for(Entry<Integer, ParkNode> entry : sfNwNodes.entrySet()){
			if (Objects.equals(current_node, entry.getValue())) {
				 myKey = entry.getKey();
	        }
			
		}
		/*Get the node correspoding to the key*/
		ParkNode new_current_node = sfNwNodes.get(myKey);
		/*Get the GCM path corresponding to the node*/
		ArrayList<Integer> path = routeLoader.getOPTPath(new_current_node.getId());
		System.out.println(path); //For debug purpose
		
		/*The path returned by the function is a sequence of nodes. Extract lat longs
		 * from these nodes as strings and return*/
		
		Iterator<Integer> path_itr = path.iterator();
		StringBuilder sbr = new StringBuilder();
		String e1 = new_current_node.getLatitude()+","+new_current_node.getLongitude();
		sbr.append(e1+",");
		while(path_itr.hasNext()){
			int p_x = path_itr.next();
			if(p_x<0)
				break;
			ParkNode sfNode1 = sfNwNodes.get(p_x);
			e1 = sfNode1.getLatitude()+","+sfNode1.getLongitude();
			sbr.append(e1+",");
		}
		return sbr;
	}
	
	/*Getter functions*/
	public ParseOSM getParseOSM() {
		return parseOSM;
	}

	public RoadGraph getRoadGraph() {
		return roadGraph;
	}
	

}
