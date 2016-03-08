package Database;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;

import MapDatabase.Pair;

public class LoadHPP {
	
	public static double fetchAvailabilityFromHPP(Pair<Integer, String> streetPair){
		int streetBlockID = streetPair.getL();
		int available_spaces = 0;
		double avg_available_spaces = 0.0;

		String JDBC_DRIVER = "com.mysql.jdbc.Driver";  
		String DB_URL = "jdbc:mysql://localhost:3306/phonepark01";

		String USER = "root";
		String PASS = "";
		ResultSet rs = null;
		Connection conn = null;
		Statement stmt = null;
		Pair<Integer, Integer> rsPair = new Pair<Integer, Integer> ();
		try{
			//STEP 2: Register JDBC driver
			Class.forName("com.mysql.jdbc.Driver");

			//STEP 3: Open a connection
			System.out.println("Connecting to database...");
			conn = DriverManager.getConnection(DB_URL,USER,PASS);

			//STEP 4: Execute a query
			System.out.println("Creating statement...");
			stmt = conn.createStatement();
			String sql_sequel =  ""+streetBlockID;
			
			String sql;
			sql = "SELECT * FROM phonepark01.HPP WHERE StreetBlockID = '"+sql_sequel+"';";
			System.out.println(sql);
			//int rs = stmt.executeUpdate(sql);
			rs = stmt.executeQuery(sql);
			int count = 1;
			if(rs.next()){
				available_spaces += Integer.parseInt(rs.getString(5));
				count++;
			}
			avg_available_spaces = available_spaces/count;

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
		System.out.println("Goodbye!");
		return avg_available_spaces;
	}

}
