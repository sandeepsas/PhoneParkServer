package Database;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.sql.Timestamp;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;

import MapDatabase.Pair;
import parkAttribs.StatisticMatrices;

public class INTR2HPP {

		public static void Intermediate2Hpp(){
			//  Database credentials
			String DB_URL = "jdbc:mysql://localhost:3306/phonepark01";

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
				/*Truncate Table*/
				stmt.executeUpdate("TRUNCATE hpp");
				/*Get all values between start and end dates of historic data*/
				String sql = "SELECT * FROM phonepark01.intermediate";
				rs = stmt.executeQuery(sql);
				while(rs.next()){
					int streetBlockID = rs.getInt("StreetBlockID");
					
			
					int day = rs.getInt("Day");
					int startTime = rs.getInt("StartTime");
					int endTime = rs.getInt("EndTime");


					String sql_bin = "SELECT * FROM phonepark01.intermediate WHERE StreetBlockID = "
							+ "'"+streetBlockID+"' AND StartTime = '"+startTime+"' AND "
							+ "EndTime = '"+endTime+"' AND Day = '"+day+"'";
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

					List<Integer> a_list = new ArrayList<Integer>();
					List<Integer> w_list = new ArrayList<Integer>();
					
					List<Pair<Pair<Integer,Integer>,Integer>> lp = 
							new ArrayList<Pair<Pair<Integer,Integer>,Integer>>();

					if(ex_rs.isBeforeFirst()){
						while(ex_rs.next()){
							
							int duration = ex_rs.getInt("Duration");
							int availableSpaces = ex_rs.getInt("Availability");
							sum_product =sum_product+ (duration*availableSpaces);
							a_list.add(availableSpaces);
							w_list.add(duration);
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
							variace = sd_c/(sample_size-1);
							probability = 1 - StatisticMatrices.Phi(0.5,avgEstAvail, Math.sqrt(variace));
						}
						/*Write to HPP*/
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
