package Runner;
import java.io.IOException;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map.Entry;
import java.util.Objects;

import org.xmlpull.v1.XmlPullParserException;

import MapDatabase.GraphNode;
import MapDatabase.ParseOSM;
import MapDatabase.RoadGraph;
import ParkRouter.Router;
import ParkRouter.ParkNode;


public class Initializers {
	
	static ParseOSM parseOSM;
	static RoadGraph roadGraph;
	static Router rnLoader;
	HashMap<Long,Integer> gNodeMap;
	
	public ParseOSM getParseOSM() {
		return parseOSM;
	}

	public static RoadGraph getRoadGraph() {
		return roadGraph;
	}
	
	public Initializers(){
		
		try {
			System.out.println("Executed Before Spark!!");
			recalculateHPP();
			parseOSM = new ParseOSM();
		} catch (IOException | XmlPullParserException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		roadGraph = parseOSM.getRoadGraph();
		try {
			rnLoader = new Router(roadGraph.nodes,roadGraph.edges);
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		gNodeMap = rnLoader.getgNodeMap();
	}
	
	public static void recalculateHPP() {
		//Get historical mean from 
		
	}

	public StringBuilder startRouting(GraphNode closestMapPoint) {
		// Get Current SFPark Location
		// Get the node set
		int  myKey = 0;
		ParkNode current_node = new ParkNode(0, closestMapPoint.getLat(), closestMapPoint.getLon());
		HashMap<Integer,ParkNode> sfNwNodes = rnLoader.getRoad().getNodes();
		
		for(Entry<Integer, ParkNode> entry : sfNwNodes.entrySet()){
			if (Objects.equals(current_node, entry.getValue())) {
				 myKey = entry.getKey();
	        }
			
		}
		ParkNode new_current_node = sfNwNodes.get(myKey);
		
		final String DATE_FORMAT_NOW = "hhmmss";
		Calendar now = Calendar.getInstance();
		Timestamp startTime = new Timestamp(now.getTimeInMillis());
	 
		Timestamp initialTime = generateRandomTime(startTime,10);
		
		//rnLoader.startRouting(new_current_node.getId(), new_current_node.getId(), initialTime);
		/*double timeToParkOptTO = 0.0;
		timeToParkOptTO = timeToParkOptTO += rnLoader.runSim(new_current_node.getId(), new_current_node.getId(), 
				initialTime);*/
		ArrayList<Integer> path = rnLoader.getOPTPath(new_current_node.getId());
		System.out.println(path);
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
/*			if(path_itr.hasNext()){
				int p_y = path_itr.next();
				if(p_y<0)
					break;
				SFParkNode sfNode2 = sfNwNodes.get(p_y);
				String e1 = sfNode1.getLatitude()+","+sfNode1.getLongitude()+","+sfNode2.getLatitude()+","+sfNode2.getLongitude();
				sbr.append(e1+",");
			}*/
		}
		return sbr;
		
		
		
/*		//Convert path to node sequence
		
		for(int i=0;i<path.size()-1;i++){
			SFParkNode sfNode1 = sfNwNodes.get(i);
			SFParkNode sfNode2 = sfNwNodes.get(i+1);
			String e1 = sfNode1.getLatitude()+","+sfNode1.getLongitude()+","+sfNode2.getLatitude()+","+sfNode2.getLongitude();
			sbr.append(e1+",");
		}
		return sbr;*/
	}
	public Timestamp generateRandomTime(Timestamp startingTimeInterval, long timeDurationInMin)
	{
		long startInMilli = startingTimeInterval.getTime();
		long durationInMilli = timeDurationInMin * 60 * 1000;
		long randomTimeInMilli = startInMilli + (long)(Math.random() * (durationInMilli + 1));
		return new Timestamp(randomTimeInMilli);
	}
	

}
