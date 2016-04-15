/*package Database;

 * @Author: Sandeep Sasidharan
 * 
 * This class populates Historic Database HPP from PSST Data
 * 
 * Weighted avergae is used to estimate the average parking availability
 * 
import java.sql.*;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;

import MapDatabase.Pair;
import parkAttribs.StatisticMatrices;

public class PSST2HPP {
	// JDBC driver name and database URL
	static final String JDBC_DRIVER = "com.mysql.jdbc.Driver"; 
	static String DB_URL = "jdbc:mysql://localhost:3306/phonepark01";

	static final String HIST_START_DATE = "2016-03-16";
	static final String HIST_END_DATE = "2016-04-20";

	public static void main(String[] args){
		//  Database credentials

		final String USER = "root";
		final String PASS = "";

		Connection conn = null;
		Connection conn_HPP = null;
		Statement stmt = null;
		ResultSet rs = null;

		try{
			Class.forName("com.mysql.jdbc.Driver");
			conn = DriverManager.getConnection(DB_URL,USER,PASS);
			conn_HPP = DriverManager.getConnection(DB_URL,USER,PASS);
			List<String> checkStreetMap = new ArrayList<String>();
			stmt = conn.createStatement();
			Truncate Table
			stmt.executeUpdate("TRUNCATE hpp");
			Get all values between start and end dates of historic data
			String sql = "SELECT * FROM phonepark01.psst WHERE timestamp >= '"+HIST_START_DATE+"' AND "
					+ "timestamp < '"+HIST_END_DATE+"'";
			rs = stmt.executeQuery(sql);
			while(rs.next()){
				int streetBlockID = rs.getInt("StreetBlockID");
				int totalSpaces = rs.getInt("TotalSpaces");
				int availableSpaces = rs.getInt("AvailableSpaces");
				Timestamp timeStamp = rs.getTimestamp("TimeStamp");
				System.out.println(streetBlockID+" "+ totalSpaces+" "+ availableSpaces +" "+ timeStamp+"\n \n");

				For each value, find its mergeable within the time bin
				SimpleDateFormat sdf = new SimpleDateFormat("yyyy-mm-dd");
				Calendar cal = Calendar.getInstance();
				cal.setTime(timeStamp);
				int startTime = cal.get(Calendar.HOUR_OF_DAY);
				cal.add(Calendar.HOUR_OF_DAY, 1);
				int endTime = cal.get(Calendar.HOUR_OF_DAY);

				int date = cal.get(Calendar.DATE);
				int month = cal.get(Calendar.MONTH)+1;	
				int year = cal.get(Calendar.YEAR);
				int day = cal.get(Calendar.DAY_OF_WEEK);

				String startDateTime = year+"-"+month+"-"+date +" "+startTime;
				String endDateTime = year+"-"+month+"-"+date +" "+endTime;


				String sql_bin = "SELECT * FROM phonepark01.psst WHERE StreetBlockID = "
						+ "'"+streetBlockID+"' AND timestamp >= '"+startDateTime+"' AND "
						+ "timestamp < '"+endDateTime+"'";

				System.out.println(sql_bin);
				if(checkStreetMap.contains(sql_bin)){
					continue;
				}
				checkStreetMap.add(sql_bin);

				ResultSet ex_rs = null;
				Statement ex_stmt = conn.createStatement();
				ex_rs = ex_stmt.executeQuery(sql_bin);

				float sum_product = 0;
				//float weight_sum = 0;
				int sample_size  = 0;
				int delta_time = 0;

				List<Integer> a_list = new ArrayList<Integer>();
				List<Integer> w_list = new ArrayList<Integer>();
				
				List<Pair<Pair<Integer,Integer>,Integer>> lp = 
						new ArrayList<Pair<Pair<Integer,Integer>,Integer>>();
				int start_time = 0;
				int end_time = 60;
				if(ex_rs.isBeforeFirst()){
					while(ex_rs.next()){

						int ex_totalSpaces = ex_rs.getInt("TotalSpaces");
						int ex_availableSpaces = ex_rs.getInt("AvailableSpaces");
						a_list.add(ex_availableSpaces);
						Timestamp ex_timeStamp = ex_rs.getTimestamp("TimeStamp");
						
						end_time = ex_timeStamp.getMinutes();
						lp.add(new Pair<Pair<Integer, Integer>, Integer>(new Pair<Integer, Integer>(start_time,end_time),ex_availableSpaces));
						start_time = end_time;
						System.out.println(streetBlockID+" "+ ex_totalSpaces+" "+ ex_availableSpaces +" "+ ex_timeStamp);
						sum_product =sum_product+ ((ex_timeStamp.getMinutes()-delta_time)*ex_availableSpaces);
						w_list.add((ex_timeStamp.getMinutes()-delta_time));
						delta_time += ex_timeStamp.getMinutes();
						
						sample_size++;

					}
					float avgEstAvail = sum_product/60;
					double sd_c = 0;
					double variace = 0;
					double probability = 0.5;
					if(sample_size>1){
						for(int i=0;i<a_list.size();i++){
							sd_c += w_list.get(i)*(a_list.get(i)-avgEstAvail)*(a_list.get(i)-avgEstAvail);
						}
						variace = sd_c/(sample_size);
						probability = 1 - StatisticMatrices.Phi(0.5,avgEstAvail, Math.sqrt(variace));
					}
					Write to HPP
					String sql_sequel_write = streetBlockID + "',"
							+"'"+ startTime + "',"
							+"'"+ endTime + "',"
							+"'"+ day + "',"
							+"'"+ avgEstAvail + "',"
							+"'"+ sample_size + "',"
							+"'"+ variace + "',"
							+"'"+ probability + "'";
					String writeSQL = "INSERT INTO phonepark01.HPP (StreetBlockID,StartTime,"
							+ "EndTime,Day,AvgEstParkAvail,SampleSize,Variance,Probability) "
							+ "VALUES ('"+sql_sequel_write+");";


					Statement stmt_hpp = null;
					stmt_hpp = conn_HPP.createStatement();
					stmt_hpp.executeUpdate(writeSQL);
					stmt_hpp.close();
				}
				ex_rs.close();
			}
			conn_HPP.close();
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

}


*/