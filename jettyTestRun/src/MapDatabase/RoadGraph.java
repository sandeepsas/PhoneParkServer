package MapDatabase;
/**
 * @author Sandeep Sasidharan
 * 
 * Parse the OSM format to Graph Data Structure. Note that only
 * Graph elements nodes and edges are stored by this class. The actual
 * Graph has to be constructed by using these elements.
 * 
 * Reference: https://github.com/COMSYS/FootPath
 */

import java.io.IOException;
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.xmlpull.v1.XmlPullParser;
import org.xmlpull.v1.XmlPullParserException;

import StreetBlock.KdTree;

public class RoadGraph {

	/*Class memebers*/
	public LinkedList<GraphNode> nodes;
	public LinkedList<DirectedEdge> edges;

	//For kDTree construction and storage for fast search
	public static KdTree<KdTree.XYZPoint> kdtree;
	public static List<KdTree.XYZPoint> graphNodeList;

	public RoadGraph(){
		/*Initialize nodes and edges as linkedlists of class type GraphNode and DirectedEdge
		 * The functionalities of these classes can be found in MapDatabase folder*/
		nodes = new LinkedList<GraphNode>();
		edges = new LinkedList<DirectedEdge>();
		/*Initialize empty KDTree*/
		kdtree = new KdTree<KdTree.XYZPoint>();
		graphNodeList = new ArrayList<KdTree.XYZPoint>();
	}
	/*Parser function
	 * 
	 * This function converts OSM in XML format to Graph elements nodes and edges. It fills
	 * the class members as linkedlist of all edges and nodes.
	 * 
	 * Refer OSM documentation for more details on OSM data specifications
	 * 
	 * */
	public boolean osmGraphParser(XmlPullParser xrp) throws XmlPullParserException, IOException{
		/*Initialization of temporary variables */
		boolean ret = false;
		boolean isOsmData = false;	
		GraphNode tempNode = new GraphNode();					
		GraphNode NULL_NODE = new GraphNode();					
		GraphWay tempWay = new GraphWay();						
		GraphWay NULL_WAY = new GraphWay();						
		LinkedList<GraphNode> allNodes = new LinkedList<GraphNode>();	
		LinkedList<GraphWay> allWays = new LinkedList<GraphWay>();		

		if(xrp == null){
			return ret;
		}

		xrp.next();
		int eventType = xrp.getEventType();
		/*Parsing xml based on Tags*/
		while (eventType != XmlPullParser.END_DOCUMENT) {
			switch(eventType){
			case XmlPullParser.START_DOCUMENT:
				break;
			case XmlPullParser.START_TAG:
				/*Checking the format*/
				if(xrp.getName().equals("osm")){
					isOsmData = true;
				}else {
					int attributeCount = xrp.getAttributeCount();
					/*Extracting the nodes and values*/
					if(xrp.getName().equals("node")){
						/*The node values are temporarily stored in tempNode*/
						tempNode = new GraphNode();
						for(int i = 0; i < attributeCount; i++){
							if(xrp.getAttributeName(i).equals("id")){
								tempNode.setId(Long.parseLong(xrp.getAttributeValue(i)));			
							} if(xrp.getAttributeName(i).equals("lat")){
								tempNode.setLat(Double.parseDouble(xrp.getAttributeValue(i)));	
							} if(xrp.getAttributeName(i).equals("lon")){
								tempNode.setLon(Double.parseDouble(xrp.getAttributeValue(i)));	
							}
						}

					}
					/*Extracting road attributes*/
					else if(xrp.getName().equals("tag")){
						if(tempNode == NULL_NODE)	{
							for(int i = 0; i < attributeCount; i++){
								if(xrp.getAttributeName(i).equals("k")
										&& xrp.getAttributeValue(i).equals("highway")){		
									String v = xrp.getAttributeValue(i + 1);
									tempWay.setType(v);
									tempWay.setSpeedMax(OsmConstants.roadTypeToSpeed(v));
								} else if(xrp.getAttributeName(i).equals("k")
										&& xrp.getAttributeValue(i).equals("name")){	
									String v = xrp.getAttributeValue(i + 1);
									tempWay.setName(v);
								} else if(xrp.getAttributeName(i).equals("k")
										&& xrp.getAttributeValue(i).equals("other_tags")){	
									String v = xrp.getAttributeValue(i + 1);
									OtherTags ot = parseOtherTags(v);
									tempWay.setOtherTags(v);
									tempWay.setOneway(ot.isOneWay);
									if(ot.maxspeed != -1){
										tempWay.setSpeedMax(ot.maxspeed);
									}
								}
							}
						}
						/*Extracting roadways */
					}else if(xrp.getName().equals("way")){							
						tempWay = new GraphWay();
						for(int i = 0; i < attributeCount; i++){
							if(xrp.getAttributeName(i).equals("id")){
								tempWay.setId(Long.parseLong(xrp.getAttributeValue(i)));
							}
						}	
					} else if(xrp.getName().equals("nd")){										
						for(int i = 0; i < attributeCount; i++){
							if(xrp.getAttributeName(i).equals("ref")){							
								String v = xrp.getAttributeValue(i);
								long ref = Long.parseLong(v);
								tempWay.addRef(ref);
							}
						}
					}
				}
				break;
			case XmlPullParser.END_TAG:
				if(isOsmData){
					if(xrp.getName().equals("osm")){
						ret = true;
					} else if(xrp.getName().equals("node")){						
						allNodes.add(tempNode);
						tempNode = NULL_NODE;		
					} else if(xrp.getName().equals("tag")){							

					} else if(xrp.getName().equals("way")){							
						allWays.add(tempWay);
						tempWay = NULL_WAY;
					} else if(xrp.getName().equals("nd")){							

					}
				}
				break;
			}
			eventType = xrp.next();
		}
		/*Extracting the Node - Edge relations*/
		LinkedList<GraphWay> remainingWays = new LinkedList<GraphWay>();
		for(GraphWay way : allWays){	
			LinkedList<Long> refs = way.getRefs();
			boolean stop = false;
			for(Long ref : refs){							
				for(GraphNode node : allNodes){
					if(node.getId() == ref){
						remainingWays.add(way);
						stop = true;							
					}
					if(stop)
						break;
				}
				if(stop)
					break;
			}
		}
		if(remainingWays.size() == 0)	
			return false;
		for(GraphWay way : remainingWays){

			GraphNode firstNode = getNode(allNodes,way.getRefs().get(0));
			for(int i = 1; i <= way.getRefs().size() - 1; i++){
				GraphNode nextNode = getNode(allNodes,way.getRefs().get(i));
				double len = distanceInMilesBetweenPoints(firstNode.getLat(),firstNode.getLon(),
						nextNode.getLat(),nextNode.getLon());

				if(way.getType()==null){
					way.setSpeedMax(30);
				}
				float travel_time_weight = (float) (len/way.getSpeedMax());

				DirectedEdge tempEdge = new DirectedEdge(firstNode, nextNode,
						len, way.getSpeedMax(), way.getOneway(),way.getType(),
						way.getName(),travel_time_weight,way.getId());
				edges.add(tempEdge);

				if(!nodes.contains(firstNode)){
					nodes.add(firstNode);							
				}
				firstNode = nextNode;
			}

			if(!nodes.contains(firstNode)){
				nodes.add(firstNode);										
			}

		}
		
		/*This function returns true if parsing is successful.
		 * 
		 * The extracted nodes and edges are stored within this class as memebers*/

		return ret;
	}
	/*Inner class defined for specifically extracting the maximum speed attribute 
	 * and road traffic direction (oneWay or not) of the edges
	 * Refer OSM documentation for more details on OSM data specs
	 * */
	private OtherTags parseOtherTags(String v) {
		String[] other_tags = v.split(",");
		OtherTags output = new OtherTags();

		for(int i =0; i< other_tags.length;i++){
			Pattern p = Pattern.compile("\"([^\"]*)\"");
			Matcher m = p.matcher(other_tags[i]);
			int flag =0;
			while (m.find()) {
				if(m.group(1).equals("oneway")){
					flag = 1;
				}
				else if(m.group(1).equals("maxspeed")){
					flag = 2;
				}else{

					if(flag ==1){
						if(m.group(1).equals("yes"))
							output.isOneWay = true;
						else
							output.isOneWay = false;
						flag =0;
					}
					else if(flag ==2){
						String[] maxspeed = m.group(1).split("\\s+");
						try{
							output.maxspeed = Integer.parseInt(maxspeed[0]);
						}catch (NumberFormatException nfe){
							System.out.println("NFE "+maxspeed[0]);
						}
						flag =0;
					}
				}
			}
		}

		return output;
	}
	// This is the slower version which is used during parsing
	private GraphNode getNode(LinkedList<GraphNode> list, long id){
		for(GraphNode node: list){
			if(node.getId() == id)
				return node;
		}
		return null;
	}
	/**
	 * Returns the distance between two points in Kilometers given in latitude/longitude
	 * @param lat_1 latitude of first point
	 * @param lon_1 longitude of first point
	 * @param lat_2 latitude of second point
	 * @param lon_2 longitude of second point
	 * @return the distance in meters
	 */
	public double getDistance(double lat_1, double lon_1, double lat_2, double lon_2) {
		// source: http://www.movable-type.co.uk/scripts/latlong.html
		double dLon = lon_2 - lon_1;
		double dLat = lat_2 - lat_1;
		lat_1 = Math.toRadians(lat_1);
		lon_1 = Math.toRadians(lon_1);
		lat_2 = Math.toRadians(lat_2);
		lon_2 = Math.toRadians(lon_2);
		dLon = Math.toRadians(dLon);
		dLat = Math.toRadians(dLat);

		double r = 6378137; // km
		double a = Math.sin(dLat/2)*Math.sin(dLat/2) + 
				Math.cos(lat_1)*Math.cos(lat_2) *
				Math.sin(dLon/2)*Math.sin(dLon/2);
		double c = 2 * Math.atan2(Math.sqrt(a), Math.sqrt(1-a));
		return c*r;
	}
	/**
	 * Returns the distance between two points in miles given in latitude/longitude
	 * @param lat_1 latitude of first point
	 * @param lon_1 longitude of first point
	 * @param lat_2 latitude of second point
	 * @param lon_2 longitude of second point
	 * @return the distance in meters
	 */
	public static double distanceInMilesBetweenPoints(double source_lat,
			double source_lng, double dest_lat, double dest_lng) {
		double earthRadius = 3958.75;
		double dLat = Math.toRadians(dest_lat-source_lat);
		double dLng = Math.toRadians(dest_lng-source_lng);
		double sindLat = Math.sin(dLat / 2);
		double sindLng = Math.sin(dLng / 2);
		double a = Math.pow(sindLat, 2) + Math.pow(sindLng, 2)
				* Math.cos(Math.toRadians(source_lat)) 
				* Math.cos(Math.toRadians(dest_lat));
		double c = 2 * Math.atan2(Math.sqrt(a), Math.sqrt(1-a));
		double dist = earthRadius * c;

		return dist;
	}
	/*Inner class defined to store the road attributes temporarily*/
	class OtherTags
	{
		private boolean isOneWay;
		private int maxspeed;

		OtherTags(boolean isOneWay, int maxspeed)
		{
			this.isOneWay = isOneWay;
			this.maxspeed = maxspeed;
		}
		OtherTags()
		{
			this.isOneWay = false;
			this.maxspeed = -1;
		}
	}

	/*Map Match function. This function will map match any location to the
	 * nearest intersection point on the map*/
	public GraphNode mapMatch(double latitude, double longitude) {
		/*Loads KDTree and perform a Knn search*/
		loadKdTree();
		KdTree.XYZPoint nearestNode = getNNNode(new KdTree.XYZPoint("", "address" ,
				latitude, longitude,0,0,0,0,0));
		/*Intersection of type GraphNode is returned*/
		return new GraphNode(nearestNode.x,nearestNode.y,nearestNode.streetID);
	}

	/*Load all the nodes to a KdTree to facilitate knn search*/
	private void loadKdTree() {
		
		Iterator <GraphNode> node_itr = nodes.iterator();
		while(node_itr.hasNext()){
			GraphNode tempNode = node_itr.next();
			graphNodeList.add(new KdTree.XYZPoint(""+tempNode.getId(), "address" ,
					tempNode.getLat(), tempNode.getLon(),
					0,0,0,0,0));
		}
		kdtree = new KdTree<KdTree.XYZPoint>(graphNodeList);
		
	}
	/*Returns a nearest node*/
	public KdTree.XYZPoint getNNNode(KdTree.XYZPoint node){

		//Search for nearest vertex
		Collection<KdTree.XYZPoint> near_bys = kdtree.nearestNeighbourSearch(node,0.06);
		Iterator<KdTree.XYZPoint> near_bys_itr =
				near_bys.iterator();
		KdTree.XYZPoint elt = near_bys_itr.next();

		return elt;

	}

}
