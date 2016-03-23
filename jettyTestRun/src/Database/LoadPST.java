package Database;
/*
 * Loads the Parking Status Table
 * 
 * 
 * */
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.HashMap;
import java.util.Map;


import MapDatabase.GraphNode;
import MapDatabase.Pair;
import Runner.ServerConfig;
import Runner.StartServer;
import StreetBlock.KdTree.XYZPoint;

public class LoadPST {

	// JDBC driver name and database URL
	static final String JDBC_DRIVER = "com.mysql.jdbc.Driver";  
	static final String DB_URL = ServerConfig.DB_URL;
	//static final String DB_NAME = "phonepark01.uicstatemid";

	//  Database credentials

	static final String USER = ServerConfig.USER;
	static final String PASS = ServerConfig.PASS;



	public Pair<Integer, Integer>  fetchRecord(int StreetBlockID) {
		// TODO Auto-generated method stub
		//boolean res =false;
		ResultSet rs = null;
		Connection conn = null;
		Statement stmt = null;
		Pair<Integer, Integer> rsPair = new Pair<Integer, Integer> ();
		try{
			//STEP 2: Register JDBC driver
			Class.forName("com.mysql.jdbc.Driver");

			//STEP 3: Open a connection
			conn = DriverManager.getConnection(DB_URL,USER,PASS);

			//STEP 4: Execute a query
			stmt = conn.createStatement();
			String sql_sequel =  ""+StreetBlockID;

			String sql;
			sql = "SELECT * FROM phonepark01.PST WHERE StreetBlockID = '"+sql_sequel+"';";
			System.out.println(sql);
			//int rs = stmt.executeUpdate(sql);
			rs = stmt.executeQuery(sql);

			if(rs.next()){
				int total_spaces = Integer.parseInt(rs.getString(2));
				int available_spaces = Integer.parseInt(rs.getString(3));
				rsPair = new Pair<Integer, Integer>(total_spaces,available_spaces);
			}

			rs.close();
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
		return rsPair;
	}
	/*
	 * Update the Parking Status Table
	 * 
	 * 
	 * */
	public void updateRecord(int StreetBlockID, int availability) {
		Connection conn = null;
		Statement stmt = null;
		try{
			//STEP 2: Register JDBC driver
			Class.forName("com.mysql.jdbc.Driver");

			//STEP 3: Open a connection
			conn = DriverManager.getConnection(DB_URL,USER,PASS);

			//STEP 4: Execute a query
			stmt = conn.createStatement();
			String sql_sequel1 =  ""+availability;
			String sql_sequel2 =  ""+StreetBlockID;

			String sql;
			/*UPDATE phonepark01.psst
			SET AvailableSpaces='20'
			WHERE StreetBlockID='4';*/
			sql = "UPDATE phonepark01.PST SET AvailableSpaces='"+sql_sequel1+"' WHERE StreetBlockID = '"+sql_sequel2+"';";
			System.out.println(sql);
			stmt.executeUpdate(sql);

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
	}
	/*
	 * Fetch availability from the Parking Status Table
	 * 
	 * 
	 * */
	public static int fetchAvailabilityFromPST(Pair<Integer, String> streetPair){
		int streetBlockID = streetPair.getL();
		int available_spaces = 0;

		String DB_URL= ServerConfig.DB_URL;

		String USER = ServerConfig.USER;
		String PASS = ServerConfig.PASS;
		ResultSet rs = null;
		Connection conn = null;
		Statement stmt = null;
		new Pair<Integer, Integer> ();
		try{
			//STEP 2: Register JDBC driver
			Class.forName("com.mysql.jdbc.Driver");

			//STEP 3: Open a connection
			conn = DriverManager.getConnection(DB_URL,USER,PASS);

			//STEP 4: Execute a query
			stmt = conn.createStatement();
			String sql_sequel =  ""+streetBlockID;

			String sql;
			sql = "SELECT * FROM phonepark01.PST WHERE StreetBlockID = '"+sql_sequel+"';";
			System.out.println(sql);
			//int rs = stmt.executeUpdate(sql);
			rs = stmt.executeQuery(sql);

			if(rs.next()){
				available_spaces = Integer.parseInt(rs.getString(3));
			}

			rs.close();
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
		return available_spaces;
	}

	public static HashMap<Pair<GraphNode, GraphNode>, Double> fetchAllParkingAvailability(){
		HashMap<Pair<GraphNode, GraphNode>,Double> edgeAvailabilitySet = new HashMap<Pair<GraphNode, GraphNode>,Double>();
		Map<Integer, XYZPoint> parkingBlockMap_1= StartServer.getSb().getParkingBlockMap();
		ResultSet rs = null;
		Connection conn = null;
		Statement stmt = null;
		try{
			Class.forName("com.mysql.jdbc.Driver");
			conn = DriverManager.getConnection(DB_URL,USER,PASS);
			stmt = conn.createStatement();

			String sql;
			sql = "SELECT * FROM phonepark01.PST;";
			System.out.println(sql);
			//int rs = stmt.executeUpdate(sql);
			rs = stmt.executeQuery(sql);

			if(rs.next()){
				int streetID = Integer.parseInt(rs.getString(1));
				Integer.parseInt(rs.getString(2));
				int available_spaces = Integer.parseInt(rs.getString(3));

				XYZPoint parking_block = parkingBlockMap_1.get(streetID);
				GraphNode startNode = new GraphNode(parking_block.start_lat,parking_block.start_long,streetID);
				GraphNode endNode = new GraphNode(parking_block.end_lat,parking_block.end_long,streetID);

				edgeAvailabilitySet.put(new Pair<GraphNode, GraphNode>(startNode,endNode), (double) available_spaces);

			}

			rs.close();
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
		return edgeAvailabilitySet;
	}

	public void writePST(int StreetBlockID, int totalSpaces, int availability) {
		Connection conn = null;
		Statement stmt = null;
		try{
			//STEP 2: Register JDBC driver
			Class.forName("com.mysql.jdbc.Driver");

			//STEP 3: Open a connection
			conn = DriverManager.getConnection(DB_URL,USER,PASS);

			//STEP 4: Execute a query
			stmt = conn.createStatement();
			String sql_sequel1 =  ""+availability;
			String sql_sequel2 =  ""+StreetBlockID;
			String sql_sequel3 =  ""+totalSpaces;

			String sql;
			/*UPDATE phonepark01.psst
			SET AvailableSpaces='20'
			WHERE StreetBlockID='4';*/
			sql = "UPDATE phonepark01.PST SET AvailableSpaces='"+sql_sequel1+"', TotalSpaces='"+sql_sequel3+"' WHERE StreetBlockID = '"+sql_sequel2+"';";
			System.out.println(sql);
			stmt.executeUpdate(sql);

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
	}

}
