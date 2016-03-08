package StreetBlock;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import StreetBlock.KdTree.XYZPoint;

public class StreetBlockLoader {
	
	// JDBC driver name and database URL
	static final String JDBC_DRIVER = "com.mysql.jdbc.Driver";  
	static final String DB_URL = "jdbc:mysql://localhost:3306/phonepark01";
	static final String DB_NAME = "phonepark01.uicstatemid";

	//  Database credentials

	static final String USER = "root";
	static final String PASS = "";
	List<XYZPoint> parkingBlockList = new ArrayList<XYZPoint>(); 
	
	Map<Integer, XYZPoint> parkingBlockMap = new HashMap<Integer, XYZPoint> ();
	
	public List<XYZPoint> getParkingBlockList() {
		return parkingBlockList;
	}
	public Map<Integer, XYZPoint> getParkingBlockMap() {
		return parkingBlockMap;
	}

	public KdTree<XYZPoint> loadStreetDataTree(){
		
		KdTree<XYZPoint> kdtree = new KdTree<XYZPoint> () ; 
		Connection conn = null;
		Statement stmt = null;
		try{
			//STEP 2: Register JDBC driver
			Class.forName("com.mysql.jdbc.Driver");

			//STEP 3: Open a connection
			System.out.println("Connecting to database...");
			conn = DriverManager.getConnection(DB_URL,USER,PASS);

			//STEP 4: Execute a query
			System.out.println("Creating statement...");
			stmt = conn.createStatement();
			String sql;
			sql = "SELECT * FROM phonepark01.uicstatemid;";
			ResultSet parkingDB_cursor = stmt.executeQuery(sql);

			//STEP 5: Extract data from result set
			while(parkingDB_cursor.next()){
				
				String linearid = parkingDB_cursor.getString(1);
				String roadName = parkingDB_cursor.getString(2);

				double start_long = Double.parseDouble(parkingDB_cursor.getString(4));
				double start_lat = Double.parseDouble(parkingDB_cursor.getString(5));
				double end_long = Double.parseDouble(parkingDB_cursor.getString(6));
				double end_lat = Double.parseDouble(parkingDB_cursor.getString(7));
				double mid_long = Double.parseDouble(parkingDB_cursor.getString(8));
				double mid_lat = Double.parseDouble(parkingDB_cursor.getString(9));
				int street_id = Integer.parseInt(parkingDB_cursor.getString(10));

				parkingBlockList.add(new XYZPoint(linearid,roadName,mid_lat,mid_long,
						start_lat,start_long,end_lat,end_long,street_id));
				parkingBlockMap.put(street_id, new XYZPoint(linearid,roadName,mid_lat,mid_long,
						start_lat,start_long,end_lat,end_long,street_id));

			}
			
			kdtree = new KdTree<XYZPoint>(parkingBlockList);
			//STEP 6: Clean-up environment
			parkingDB_cursor.close();
			stmt.close();
			conn.close();
		}catch (NumberFormatException nfe){
			System.out.println("NumberFormatException");
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

		return kdtree;
	}

}
