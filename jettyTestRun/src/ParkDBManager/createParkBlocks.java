//DO NOT RUN THIS FILE

/*package ParkDBManager;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.Iterator;
import java.util.LinkedList;

import org.xmlpull.v1.XmlPullParserException;

import MapDatabase.DirectedEdge;
import MapDatabase.GraphNode;
import MapDatabase.Pair;
import MapDatabase.ParseOSM;
import MapDatabase.RoadGraph;

public class createParkBlocks {
	
	public static void main(String[] args){
		try {
			ParseOSM parseOSM = new ParseOSM();
			RoadGraph roadGraph = parseOSM.getRoadGraph();
			
			LinkedList<GraphNode> nodes = roadGraph.nodes;
			LinkedList<DirectedEdge> edges = roadGraph.edges ;
			
			Iterator<DirectedEdge> edge_itr = edges.iterator();
			int streetID = 1;
			
			while(edge_itr.hasNext()){
				
				DirectedEdge e = edge_itr.next();
				
				int StreetBlockID = streetID;
				String StreetName = e.getName();
				String RoadType = e.getType();
				
				writeToParkBlocks(StreetBlockID,StreetName,RoadType,
						e.from().getLon(), e.from().getLat(),
						e.to().getLon(),e.to().getLat(),
						midPoint(e.from().getLon(), e.from().getLat(),
						e.to().getLon(),e.to().getLat()).getL(),
						midPoint(e.from().getLon(), e.from().getLat(),
								e.to().getLon(),e.to().getLat()).getR(),
						e.getWayId()
						);
				
				streetID++;
				
			}
			
		} catch (FileNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (XmlPullParserException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	
	public static Pair<Double,Double> midPoint(double lat1,double lon1,double lat2,double lon2){

	    double dLon = Math.toRadians(lon2 - lon1);

	    //convert to radians
	    lat1 = Math.toRadians(lat1);
	    lat2 = Math.toRadians(lat2);
	    lon1 = Math.toRadians(lon1);

	    double Bx = Math.cos(lat2) * Math.cos(dLon);
	    double By = Math.cos(lat2) * Math.sin(dLon);
	    double lat3 = Math.atan2(Math.sin(lat1) + Math.sin(lat2), Math.sqrt((Math.cos(lat1) + Bx) * (Math.cos(lat1) + Bx) + By * By));
	    double lon3 = lon1 + Math.atan2(By, Math.cos(lat1) + Bx);

	    //print out in degrees
	    System.out.println(Math.toDegrees(lat3) + " " + Math.toDegrees(lon3));
		return new Pair<Double,Double>(Math.toDegrees(lon3),Math.toDegrees(lat3));
	}

	private static boolean writeToParkBlocks(int StreetBlockID,String StreetName,String RoadType,
			double StartLng,double StartLat,double EndLng,double EndLat,double MidLng,double MidLat,
			long wayID) {
		// JDBC driver name and database URL
		final String JDBC_DRIVER = "com.mysql.jdbc.Driver";  
		final String DB_URL = "jdbc:mysql://localhost:3306/phonepark01";
		final String DB_NAME = "phonepark01.parkblocks";

		//  Database credentials

		final String USER = "root";
		final String PASS = "";
		
		boolean res =false;
		Connection conn = null;
		Statement stmt = null;
		try{
			//STEP 2: Register JDBC driver
			Class.forName("com.mysql.jdbc.Driver");

			conn = DriverManager.getConnection(DB_URL,USER,PASS);

			stmt = conn.createStatement();
			
			String sql_sequel = StreetBlockID + "',"
					+"'"+ StreetName + "',"
					+"'"+ RoadType + "',"
					+"'"+ StartLng + "',"
					+"'"+ StartLat + "',"
					+"'"+ EndLng + "',"
					+"'"+ EndLat + "',"
					+"'"+ MidLng + "',"
					+"'"+ MidLat + "',"
					+"'"+ wayID + "'";
			
			String sql;
			sql = "INSERT INTO "+DB_NAME+" (StreetBlockID,StreetName,RoadType,StartLng,StartLat,EndLng,EndLat,MidLng,MidLat,OSMID) VALUES ('"+sql_sequel+");";
			
			System.out.println(sql);
			int rs = stmt.executeUpdate(sql);
			if(rs>0)
				res= true;

			//STEP 6: Clean-up environment
			//rs.close();
			stmt.close();
			conn.close();
		}catch(SQLException se){
			//Handle errors for JDBC
			se.printStackTrace();
		}catch(Exception e){
			//Handle errors for Class.forName
			e.printStackTrace();
		}finally{
			//finally block used to close resources
			try{
				if(stmt!=null)
					stmt.close();
			}catch(SQLException se2){
			}// nothing we can do
			try{
				if(conn!=null)
					conn.close();
			}catch(SQLException se){
				se.printStackTrace();
			}//end finally try
		}//end try
		System.out.println("Goodbye!");
		return res;
	}

}
*/