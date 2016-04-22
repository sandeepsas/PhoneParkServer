package Database;

/*
 *@Author Sandeep
 *
 * Loads the historic parking profile
 * 
 * */

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.Calendar;
import java.util.HashMap;

import MapDatabase.Pair;
import Runner.StartServer;

public class LoadHPP {
	
	String DB_URL = StartServer.getServerconfig().DB_URL;

	String USER = StartServer.getServerconfig().USER;
	String PASS = StartServer.getServerconfig().PASS;
	
	/*class members*/
	
	HashMap < Pair<Integer, Pair<Integer, Integer> >, Pair<Double, Double>> hPPMap = new 
			HashMap< Pair<Integer, Pair<Integer, Integer> >,
			Pair<Double, Double>>();
	
	public Pair<Double, Double> fetchValFromHPPMap(int StreetID,int day, int hour){
		
		Pair<Integer, Pair<Integer, Integer> > key = 
				new Pair<Integer, Pair<Integer, Integer>>(StreetID, 
						new Pair<Integer, Integer>(day,hour));
		if(hPPMap.containsKey(key)){
			return hPPMap.get(key);
		}else{
			return new Pair<Double, Double>(0.0,0.5);
		}
	}
	
	public LoadHPP(){
		ResultSet rs = null;
		Connection conn = null;
		Statement stmt = null;
		try{
			//STEP 2: Register JDBC driver
			Class.forName("com.mysql.jdbc.Driver");

			//STEP 3: Open a connection
			conn = DriverManager.getConnection(DB_URL,USER,PASS);

			//STEP 4: Execute a query
			stmt = conn.createStatement();
			
			String sql;
			sql = "SELECT * FROM phonepark01.HPP;";
			System.out.println(sql);
			//int rs = stmt.executeUpdate(sql);
			rs = stmt.executeQuery(sql);
			
			while(rs.next()){
				int StreetID = rs.getInt(1);
				int day = rs.getInt(4);
				int hour = rs.getInt(2);
				double available_spaces = rs.getDouble(5);
				double probability = rs.getDouble(9);
				Pair<Integer, Pair<Integer, Integer> > key = 
						new Pair<Integer, Pair<Integer, Integer>>(StreetID, 
								new Pair<Integer, Integer>(day,hour));
				
				hPPMap.put(key, new Pair<Double, Double>(available_spaces,probability));
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
	}
	
	
	
	public static double fetchAvailabilityFromHPP(Pair<Integer, String> streetPair){
		int streetBlockID = streetPair.getL();
		int available_spaces = 0;
		double avg_available_spaces = 0.0;

		String DB_URL = StartServer.getServerconfig().DB_URL;

		String USER = StartServer.getServerconfig().USER;
		String PASS = StartServer.getServerconfig().PASS;
		ResultSet rs = null;
		Connection conn = null;
		Statement stmt = null;
		try{
			//STEP 2: Register JDBC driver
			Class.forName("com.mysql.jdbc.Driver");

			//STEP 3: Open a connection
			conn = DriverManager.getConnection(DB_URL,USER,PASS);

			//STEP 4: Execute a query
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
		return avg_available_spaces;
	}
	
	public static ParkHistory pullDataFromHPP(int streetBlockID){
		
		ParkHistory pH =  new ParkHistory();
		
		Calendar now = Calendar.getInstance();
		int startTime = now.get(Calendar.HOUR_OF_DAY);
		now.add(Calendar.HOUR, 1);
		int endTime = now.get(Calendar.HOUR_OF_DAY);
		int day = now.get(Calendar.DAY_OF_WEEK);

		double avg_available_spaces = 0.0;

		String DB_URL = StartServer.getServerconfig().DB_URL;

		String USER = StartServer.getServerconfig().USER;
		String PASS = StartServer.getServerconfig().PASS;
		ResultSet rs = null;
		Connection conn = null;
		Statement stmt = null;
		try{
			
			//STEP 2: Register JDBC driver
			Class.forName("com.mysql.jdbc.Driver");

			//STEP 3: Open a connection
			conn = DriverManager.getConnection(DB_URL,USER,PASS);

			//STEP 4: Execute a query
			stmt = conn.createStatement();
			String sql_sequel =  ""+streetBlockID;
			
			String sql;
			sql = "SELECT * FROM phonepark01.HPP WHERE StreetBlockID = '"+sql_sequel+"' AND "
					+ "Day = '"+day+"' AND StartTime = '"+startTime+"' AND"
							+ " EndTime = '"+endTime+"' AND Day = '"+day+"';";

			//System.out.println(sql);
			rs = stmt.executeQuery(sql);

			if(rs.next()){
				avg_available_spaces = rs.getDouble(5);
				pH.setTotalSamplesForDateTime(rs.getInt(7));
			}else{
				avg_available_spaces=0.0;
			}
			pH.setAvgParkEstForDateTime(avg_available_spaces);
			
			String avgSQL = sql = "SELECT * FROM phonepark01.HPP WHERE StreetBlockID = '"+sql_sequel+"';";
			//System.out.println(avgSQL);
			rs = stmt.executeQuery(avgSQL);
			
			double sum_of_all_availability = 0.0;
			int sum_of_all_samples = 0;
			while(rs.next()){
				sum_of_all_availability += rs.getDouble(5);
				sum_of_all_samples += rs.getInt(7);
			}
			pH.setAvgParkEstForStreet(sum_of_all_availability);
			pH.setStreetID(streetBlockID);
			pH.setTotalSamplesForStreet(sum_of_all_samples);

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
		return pH;
	}

	public static void writeHPP(int streetBlockID, int new_availability) {
		//Get the time stamp
		Calendar now = Calendar.getInstance();
		int startTime = now.get(Calendar.HOUR_OF_DAY);
		now.add(Calendar.HOUR, 1);
		int endTime = now.get(Calendar.HOUR_OF_DAY);
		int day = now.get(Calendar.DAY_OF_WEEK);
		
		//Get the Street Entry in HPP

		String DB_URL = StartServer.getServerconfig().DB_URL;;

		String USER = StartServer.getServerconfig().USER;
		String PASS = StartServer.getServerconfig().PASS;
		ResultSet rs = null;
		Connection conn = null;
		Statement stmt = null;
		try{
			Class.forName("com.mysql.jdbc.Driver");
			conn = DriverManager.getConnection(DB_URL,USER,PASS);
			stmt = conn.createStatement();
			String sql;
			sql = "SELECT * FROM phonepark01.HPP WHERE StreetBlockID = '"+streetBlockID+"' AND "
					+ "StartTime = '"+startTime+"' AND EndTime = '"+endTime+"' "
							+ "AND Day = '"+day+"';";
			System.out.println(sql);
			rs = stmt.executeQuery(sql);
			if(rs.next()){
				
				int sampleSize = rs.getInt(7);
				double availability = rs.getDouble(5);
				
				int new_sampleSize = sampleSize+1;
				double changed_availability = ((availability*sampleSize)+new_availability)/new_sampleSize;
						
				String updateSQL = "UPDATE phonepark01.HPP SET "
						+ "AvgEstParkAvail='"+changed_availability+"' , SampleSize='"+new_sampleSize+"' "
								+ "WHERE StreetBlockID = '"+streetBlockID+"' "
										+ "AND StartTime = '"+startTime+"' AND"
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
				String writeSQL = "INSERT INTO phonepark01.HPP (StreetBlockID,StartTime,"
						+ "EndTime,Day,AvgEstParkAvail,SampleSize) "
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
		
	}
	public static Pair<Double,Double> fetchAvailTimeBasedFromHPP(int StreetID,int day, int hour){
		int streetBlockID = StreetID;
		double available_spaces = 0;
		double probability = 0.0;

		String DB_URL = StartServer.getServerconfig().DB_URL;

		String USER = StartServer.getServerconfig().USER;
		String PASS = StartServer.getServerconfig().PASS;
		ResultSet rs = null;
		Connection conn = null;
		Statement stmt = null;
		try{
			//STEP 2: Register JDBC driver
			Class.forName("com.mysql.jdbc.Driver");

			//STEP 3: Open a connection
			System.out.println(DB_URL+" "+USER+" "+PASS);
			conn = DriverManager.getConnection(DB_URL,USER,PASS);
			

			//STEP 4: Execute a query
			stmt = conn.createStatement();
			String sql_sequel =  ""+streetBlockID;
			
			String sql;
			sql = "SELECT * FROM phonepark01.HPP WHERE StreetBlockID = '"+sql_sequel+"' AND Day = '"+day+"' AND StartTime = '"+hour+"';";
			//System.out.println(sql);
			//int rs = stmt.executeUpdate(sql);
			rs = stmt.executeQuery(sql);
			if(rs.next()){
				available_spaces = rs.getDouble(5);
				probability = rs.getDouble(9);
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
		return new Pair<Double, Double>(available_spaces,probability);
	}

}
