package Database;

/*
 * Loads the historic parking profile
 * 
 * 
 * */

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.sql.Time;
import java.sql.Timestamp;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Calendar;

import MapDatabase.Pair;

public class LoadHPP {
	
	public static double fetchAvailabilityFromHPP(Pair<Integer, String> streetPair){
		int streetBlockID = streetPair.getL();
		int available_spaces = 0;
		double avg_available_spaces = 0.0;

		String DB_URL = "jdbc:mysql://localhost:3306/phonepark01";

		String USER = "root";
		String PASS = "";
		ResultSet rs = null;
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
	
	public static String pullDataFromHPP(int streetBlockID,int day){
		int available_spaces = 0;
		double avg_available_spaces = 0.0;

		String DB_URL = "jdbc:mysql://localhost:3306/phonepark01";

		String USER = "root";
		String PASS = "";
		ResultSet rs = null;
		Connection conn = null;
		Statement stmt = null;
		StringBuilder out = new StringBuilder();
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
			sql = "SELECT * FROM phonepark01.HPP WHERE StreetBlockID = '"+sql_sequel+"' AND Day = '"+day+"';";
			System.out.println(sql);
			//int rs = stmt.executeUpdate(sql);
			rs = stmt.executeQuery(sql);
			int count = 1;
			if(rs.next()){
				
				String str = rs.getTime("startTime").toString(); 
				out.append(str);
				
				/*out.append(rs.getString(1)+","+rs.getInt(2)+","+rs.getInt(3)+","+
						rs.getInt(4)+","+rs.getString(5)+","+rs.getInt(6));*/
				
				//out.append(rs.getString(1)+","+rs.getInt(4)+","+rs.getInt(6));
				//available_spaces += Integer.parseInt(rs.getString(5));
				//count++;
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
		return out.toString();
	}

	public static void writeHPP(int streetBlockID, int new_availability) {
		//Get the time stamp
		Calendar now = Calendar.getInstance();
		int startTime = 12;/*now.get(Calendar.HOUR_OF_DAY)*/;
		now.add(Calendar.HOUR, 1);
		int endTime = 1/*now.get(Calendar.HOUR_OF_DAY)*/;
		int day = now.get(Calendar.DAY_OF_WEEK);
		
		//Get the Street Entry in HPP


		String DB_URL = "jdbc:mysql://localhost:3306/phonepark01";

		String USER = "root";
		String PASS = "";
		ResultSet rs = null;
		Connection conn = null;
		Statement stmt = null;
		try{
			Class.forName("com.mysql.jdbc.Driver");
			conn = DriverManager.getConnection(DB_URL,USER,PASS);
			stmt = conn.createStatement();
			String sql_sequel =  ""+streetBlockID;
			
			String sql;
			sql = "SELECT * FROM phonepark01.HPP WHERE StreetBlockID = '"+streetBlockID+"' AND StartTime = '"+startTime+"' AND EndTime = '"+endTime+"' AND Day = '"+day+"';";
			System.out.println(sql);
			rs = stmt.executeQuery(sql);
			if(rs.next()){
				
				int sampleSize = rs.getInt(7);
				double availability = rs.getDouble(5);
				
				int new_sampleSize = sampleSize+1;
				double changed_availability = ((availability*sampleSize)+new_availability)/new_sampleSize;
						
				String updateSQL = "UPDATE phonepark01.HPP SET "
						+ "AvgEstParkAvail='"+changed_availability+"' , SampleSize='"+new_sampleSize+"' "
								+ "WHERE StreetBlockID = '"+streetBlockID+"' AND StartTime = '"+startTime+"' AND"
										+ " EndTime = '"+endTime+"' AND Day = '"+day+"';";
				System.out.println(updateSQL);
				stmt.executeUpdate(updateSQL);
			}else{
				String sql_sequel_write = streetBlockID + "',"
						+"'"+ startTime + "',"
						+"'"+ endTime + "',"
						+"'"+ day + "',"
						+"'"+ new_availability + "',"
						+"'"+ 1 + "'";
				String writeSQL = "INSERT INTO phonepark01.HPP (StreetBlockID,StartTime,EndTime,Day,AvgEstParkAvail,SampleSize) "
						+ "VALUES ('"+sql_sequel_write+");";
				System.out.println(writeSQL);
				stmt.executeUpdate(writeSQL);
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
		System.out.println("Goodbye!");
		
		
	}

}
