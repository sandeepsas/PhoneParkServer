package Database;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.Calendar;
import java.util.HashMap;

import MapDatabase.Pair;
import Runner.StartServer;

public class LoadPRT {

	
	public static HashMap<Pair<Integer, Integer>,Integer>   fetchRecord() {
		
		// JDBC driver name and database URL
		final String JDBC_DRIVER = "com.mysql.jdbc.Driver";  
		final String DB_URL = StartServer.getServerconfig().DB_URL;
		//static final String DB_NAME = "phonepark01.uicstatemid";

		//  Database credentials

		final String USER = StartServer.getServerconfig().USER;
		final String PASS = StartServer.getServerconfig().PASS;
		// TODO Auto-generated method stub
		//boolean res =false;
		ResultSet rs = null;
		Connection conn = null;
		Statement stmt = null;
		HashMap<Pair<Integer, Integer>,Integer> rsMap = new HashMap<Pair<Integer, Integer>,Integer> ();
		try{
			//STEP 2: Register JDBC driver
			Class.forName("com.mysql.jdbc.Driver");

			//STEP 3: Open a connection
			conn = DriverManager.getConnection(DB_URL,USER,PASS);

			//STEP 4: Execute a query
			stmt = conn.createStatement();
			String sql;
			sql = "SELECT * FROM phonepark01.parkingrestriction;";
			rs = stmt.executeQuery(sql);
			
			Calendar now = Calendar.getInstance();
			int week = now.get(Calendar.DAY_OF_WEEK);

			while(rs.next()){
				int streetID = rs.getInt(2);
				int startTime = Integer.parseInt(rs.getString(3));
				int endTime = Integer.parseInt(rs.getString(4));
				int status = rs.getInt(5);
				int day_of_week = rs.getInt(6);
				if(status == 1 && day_of_week == week){
					for(int i=startTime;i<endTime;i++){
						rsMap.put(new Pair<Integer, Integer>(streetID,i),i+1 );
					}
					
				}
					
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
		return rsMap;
	}

}
