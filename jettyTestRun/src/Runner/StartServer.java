package Runner;
import java.util.*;

import org.json.JSONException;
import org.json.JSONObject;
import Database.*;

import MapDatabase.*;

import StreetBlock.KdTree;
import StreetBlock.StreetBlockLoader;

import StreetBlock.KdTree.XYZPoint;
import spark.*;

public class StartServer {

	/*Initialize the road network and computes the GCM Paths*/
	private static Initializers intl = new Initializers();
	/*Load the Street Parking Database*/
	static StreetBlockLoader sb = new StreetBlockLoader();
	/*String object returned by Server*/
	public static StringBuilder postString;

	/*Getter function for StreetBlockLoader*/
	public static StreetBlockLoader getSb() {
		return sb;
	}
	/*Main function -> Entry Point
	 * 	 * The main function will continuously listen for android client requests
	 * */
	public static void main(String[] args) throws JSONException {

		System.out.println("Initializations Ended. Server Listening!!!");//For debug  purpose only
		postString = new StringBuilder();
		
		/*               Web pages mapped with the web service
		 * ******************************************************************************
		 * 
		 *'/'hello '/'post '/'ping are web pages mapped with Server
		 * These pages are accessed by Android Client with certain parameters
		 * All the parameters sent from the android client is available in the
		 * parameter "req". For any request the server responds by sending the 
		 * Response object "res". 
		 * 
		 * Accessing these webpages will internally invokes the functions defined
		 * by the expressions within Spark/get(...)
		 * 
		 * For example, if http:/<your-ip>/hello?<parameters> is accessed by the android 
		 * client, the function searchParking(req) is invoked.
		 * 
		 * A typical request from android client is made by accessing the page url 
		 * concatenated with the parameters.
		 * 
		 * For example:
		 * 
		 * http://73.247.220.84:8080/hello?UserID=a108eec35f0daf33&Latitude=41.8693826&Longitude=-87.6630133
		 * 
		 * where, http://73.247.220.84 is the public IP of the web server and 8080 is the port number
		 * *******************************************************************************/
		
		
		/*Facilitates parking search*/
		Spark.get("/hello", (req, res) -> searchParking(req)); 
		/*Monitor parking de-parking activity*/
		Spark.get("/post", (req, res) ->postActivity(req));
		/*Check if Server is online/listening*/
		Spark.get("/ping", (req, res) ->pingServer(req));
	}
	
	/*This function is called when the web page /hello is accessed
	 * Sample request:
	 * http://73.247.220.84:8080/hello?UserID=a108eec35f0daf33&Latitude=41.8693826&Longitude=-87.6630133
	 * */
	
	public static StringBuilder searchParking(Request req){
		StringBuilder sbr = new StringBuilder();

		/*Parse the parameters for identifying the vehicle location*/
		double latitude = Double.parseDouble(req.queryParams("Latitude"));
		double longitude = Double.parseDouble(req.queryParams("Longitude"));

		/*Retrieve the road network - The road network is stored in the "intl" object
		 * "intl" object is initialized at the beginning (Refer line 20). 
		 */
		RoadGraph roadGraph = intl.getRoadGraph();
		/*Use map matching to locate the nearest node on the road network*/
		GraphNode closestMapPoint = roadGraph.mapMatch(latitude,longitude);
		/*Retrieve the GCM route starting from this location
		 * The route is a set of nodes*/
		sbr = intl.startRouting(closestMapPoint);

		System.out.println(sbr); //For debug  purpose only
		/*Send the route nodes to the Android Client*/
		return sbr;
	}
	
	/*This function is called when the web page /post is accessed
	 * Sample request:
	 * http://73.247.220.84:8080/post?UserID=a108eec35f0daf33&Latitude=41.8693826&
	 * Longitude=-87.6630133&Activity=1&TimeStamp=11255503082016
	 * 
	 * */

	private static Object postActivity(Request req) {
		JSONObject reportJObj = new JSONObject();
		postString.setLength(0);
		boolean res =false;
		try {
			/*Parse the parameters for identifying the vehicle location and activity
			 * Activity = 0->Deparking
			 * Activity = 1->Parked*/
			double latitude = Double.parseDouble(req.queryParams("Latitude"));
			double longitude = Double.parseDouble(req.queryParams("Longitude"));
			int activity = Integer.parseInt(req.queryParams("Activity"));
			String timeStamp = req.queryParams("TimeStamp");

			/*Identify the street block where the activity happened*/
			Pair<Integer, String>  Street_pair = findStreetBlock(latitude,longitude).get(0);
			postString.append(Street_pair.getR());
			int StreetBlockID = Street_pair.getL();
			/*Put all data in a JSON object for sending to the database*/
			reportJObj.put("UserID",req.queryParams("UserID"));
			reportJObj.put("StreetBlockID",""+StreetBlockID);
			reportJObj.put("Activity",req.queryParams("Activity"));
			reportJObj.put("TimeStamp", req.queryParams("TimeStamp"));
			
			/*Write the data to Parking Availability Table (PAT)*/
			res = writePAT(reportJObj);

			/*If updating PAT is successful, then Update Parking Status SnapShot table (PSST)
			 * updatePSST(...) is invoked to update the PSST. This function will also update
			 *  the Parking Status Table (PST) within the function
			 * and retrieve the updated value of availability from
			 * Parking Status Table (PST)*/
			if(res){
				int new_availability = updatePSST(StreetBlockID,activity,timeStamp);
				postString.append(" ->"+new_availability);

			}
			/*The returned output will now contain the Street Block, the old value of parking availability,
			 * the new value of parking availability. This information is returned to the server*/
			return postString;

		} catch (JSONException e) {
			// Exception handling
			e.printStackTrace();
		}
		return reportJObj.toString();

	}
	
	/*This function will update the Parking Allocation Table (PAT)
	 * The Class and write function is present in src/Database folder*/
	private static boolean writePAT(JSONObject jsonObj){
		WritePAT wPAT = new WritePAT();
		return (wPAT.write(jsonObj));

	}
	
	/*This function updates the PST and PSST databases*/
	private static int updatePSST(int StreetBlockID,int activity, String timeStamp) {
		//Query PST to fetch record of Street Block
		int new_availability = 0;
		/*Load PST database*/
		LoadPST pst = new LoadPST();
		/*Fetch the parking availability values from PST database for the street Block*/
		Pair<Integer, Integer>  rsPST = pst.fetchRecord(StreetBlockID);
		if(rsPST!=null){
			int total_spaces = rsPST.getL();
			int available_spaces = rsPST.getR();
			/*Append old value of availability to the output string to
			 *  send to the client for display purpose*/
			postString.append(" Availability = "+available_spaces);
			/*Estimate the real time parking availability*/
			new_availability = estimateParkingAvailability(total_spaces,available_spaces,activity);
			/*Restrict the changes between 0 and total number of parking spaces*/
			if(new_availability>total_spaces){
				new_availability = total_spaces;
			}
			if(new_availability<0){
				new_availability = 0;
			}
			/*Update the new availability value to the Parking Status Table PST*/
			pst.updateRecord(StreetBlockID,new_availability);
			/*Update Parking snap shot table PSST*/
			UpdatePSST uPsst = new UpdatePSST();
			uPsst.update(StreetBlockID,total_spaces,new_availability,timeStamp);
		}
		/*Return the new value of availability after parking/de-parking activity*/
		return new_availability;
	}
	
	/*This function estimates the real time parking availability*/
	private static int estimateParkingAvailability(int total_spaces, int available_spaces,int activity) {
		int new_availability = 0;

		double change_in_availability = (1-ServerConfig.FLASE_POSITIVE)/(ServerConfig.PENETRATION_RATIO*(1-ServerConfig.FLASE_NEGATIVE));

		/*Compute change in Parking availability wrt to activity*/
		if(activity==ServerConfig.ACTIVITY_PARKED){//Parking
			new_availability = available_spaces - (int)Math.ceil(change_in_availability);
			if(new_availability<0)
				new_availability=0;
		}else{
			new_availability = available_spaces + (int)Math.round(change_in_availability);
		}
		return new_availability;
	}

	/*This function is called when the /ping is accessed*/
	private static Object pingServer(Request req) {
		return "Server Listening";
	}


	/*This function will identify the nearest parking block given a location*/
	private static List<Pair<Integer, String>> findStreetBlock(double latitude, double longitude) {

		List<Pair<Integer, String>> nearest_blocks = new ArrayList<Pair<Integer, String>>();
		/*Load the street Block data as a Kd-Tree*/
		KdTree<XYZPoint> streetTree = sb.loadStreetDataTree();
		/*Create the search point object*/
		XYZPoint car_loc = new XYZPoint(null,null,latitude,
				longitude,0,0,0,0,0);
		/*Perform KNN Search*/
		Collection<XYZPoint> near_bys = streetTree.nearestNeighbourSearch(car_loc,0.5);//1 mile radius
		Iterator<XYZPoint> itr  = near_bys.iterator();
		int count =1;
		/*Send 10 nearest Street Blocks*/
		while(itr.hasNext()){
			XYZPoint point = itr.next();
			int streetID = point.streetID;
			String streetName = point.address;
			Pair<Integer, String> street_pair = new Pair<Integer, String>(streetID,streetName);
			nearest_blocks.add(street_pair);
			if(count>10)
				break;
			count++;
		}

		return nearest_blocks;
	}
}
