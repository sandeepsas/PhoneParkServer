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
import supportPack.ReportPack;

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
		GraphNode closestMapPoint = roadGraph.mapMatch(latitude,longitude);
		sbr = intl.startRouting(closestMapPoint);

		System.out.println(sbr); //For debug  purpose only
		return sbr;
	}

	/*This function is called when the /ping is accessed*/
	private static Object pingServer(Request req) {
		return "Server Listening";
	}

	private static boolean writePAT(JSONObject jsonObj){

		WritePAT wPAT = new WritePAT();
		return (wPAT.write(jsonObj));

	}


	private static Object postActivity(Request req) {
		JSONObject reportJObj = new JSONObject();
		postString.setLength(0);
		boolean res =false;
		try {

			double latitude = Double.parseDouble(req.queryParams("Latitude"));
			double longitude = Double.parseDouble(req.queryParams("Longitude"));
			int activity = Integer.parseInt(req.queryParams("Activity"));
			String timeStamp = req.queryParams("TimeStamp");

			Pair<Integer, String>  Street_pair = findStreetBlock(latitude,longitude).get(0);
			postString.append(Street_pair.getR());
			
			int StreetBlockID = Street_pair.getL();
			long reportID = ReportPack.reportGenerator();
			//reportJObj.put("ReportID",req.queryParams("ReportID"));
			reportJObj.put("ReportID",""+reportID);
			reportJObj.put("UserID",req.queryParams("UserID"));
			reportJObj.put("StreetBlockID",""+StreetBlockID);
			reportJObj.put("Activity",req.queryParams("Activity"));
			reportJObj.put("TimeStamp", req.queryParams("TimeStamp"));
			
			res = writePAT(reportJObj);

			if(res){
				int new_availability = updatePSST(StreetBlockID,activity,timeStamp);
				postString.append(" ->"+new_availability);

			}
			return postString;

		} catch (JSONException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return reportJObj.toString();

	}


	private static int updatePSST(int StreetBlockID,int activity, String timeStamp) {
		//Query PST to fetch record of Street Block
		int new_availability = 0;
		LoadPST pst = new LoadPST();
		Pair<Integer, Integer>  rsPST = pst.fetchRecord(StreetBlockID);
		if(rsPST!=null){
			int total_spaces = rsPST.getL();
			int available_spaces = rsPST.getR();
			postString.append(" Availability = "+available_spaces);
			new_availability = estimateParkingAvailability(total_spaces,available_spaces,activity);
			if(new_availability>total_spaces){
				new_availability = total_spaces;
			}
			if(new_availability<0){
				new_availability = 0;
			}
			pst.updateRecord(StreetBlockID,new_availability);
			UpdatePSST uPsst = new UpdatePSST();
			uPsst.update(StreetBlockID,total_spaces,new_availability,timeStamp);
		}
		return new_availability;
	}

	private static int estimateParkingAvailability(int total_spaces, int available_spaces,int activity) {
		// TODO Auto-generated method stub
		int new_availability = 0;
		double fp = 0.1;
		double fn = 0.1;
		double b = 0.05; //penetration ratio
		double factor = (1-fp)/(b*(1-fn));
		double change_in_availability = /*available_spaces**/factor;

		if(activity==1){//Parking

			new_availability = available_spaces - (int)Math.ceil(change_in_availability);
			if(new_availability<0)
				new_availability=0;

		}else{

			new_availability = available_spaces + (int)Math.round(change_in_availability);

		}

		return new_availability;
	}

	private static List<Pair<Integer, String>> findStreetBlock(double latitude, double longitude) {
		//Max StreetBlockID = '16548'
		List<Pair<Integer, String>> nearest_blocks = new ArrayList<Pair<Integer, String>>();

		KdTree<XYZPoint> streetTree = sb.loadStreetDataTree();
		XYZPoint car_loc = new XYZPoint(null,null,latitude,
				longitude,0,0,0,0,0);
		Collection<XYZPoint> near_bys = streetTree.nearestNeighbourSearch(car_loc,0.5);//1 mile radius
		Iterator<XYZPoint> itr  = near_bys.iterator();
		int count =1;
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

	

	private static List<Pair<Integer, Double>> estimateAvailabilityforPBlocks(List<Pair<Integer, String>> nearestParkingBlocks) {
		List<Pair<Integer, Double>> parkAvailPair = new ArrayList<Pair<Integer, Double>>();
		for(Pair<Integer, String> stPair:nearestParkingBlocks){
			double avilParkingPST = LoadPST.fetchAvailabilityFromPST(stPair);
			double avilParkingHPP = LoadHPP.fetchAvailabilityFromHPP(stPair);
			double avgAvailParking = (avilParkingPST+avilParkingHPP)/2;
			parkAvailPair.add(new Pair<Integer, Double>(stPair.getL(),avgAvailParking));
		}
		Collections.sort(parkAvailPair, Pair.getAttribute2Comparator());
		return parkAvailPair;


	}
}
