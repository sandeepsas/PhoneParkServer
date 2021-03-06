package Database;
/*
 * @Author Sandeep
 * 
 * This Class fetches data from the Intermediate Table and populate the HPP Table
 * 
 * The mean, variance and the probability computations occur inside this class
 * */

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;

import Runner.StartServer;
import parkAttribs.StatisticMatrices;

public class INTR2HPP {

		public static void Intermediate2Hpp(){
			//  Database credentials
			final String DB_URL = StartServer.getServerconfig().DB_URL;

			final String USER = StartServer.getServerconfig().USER;
			final String PASS = StartServer.getServerconfig().PASS;

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
					float sum_weight = 0;
					int sample_size  = 0;

					List<Integer> a_list = new ArrayList<Integer>();
					List<Float> w_list = new ArrayList<Float>();

					if(ex_rs.isBeforeFirst()){
						while(ex_rs.next()){
							
							float duration = ex_rs.getFloat("Duration");
							sum_weight += duration;
							int availableSpaces = ex_rs.getInt("Availability");
							sum_product =sum_product+ (duration*availableSpaces);
							a_list.add(availableSpaces);
							w_list.add(duration);
							sample_size++;

						}
						
						float avgEstAvail = sum_product/sum_weight;
						double sd_c = 0;
						double variace = 0;
						double probability = 0.5;
						if(sample_size>1){
							for(int i=0;i<a_list.size();i++){
								sd_c += (w_list.get(i))*(a_list.get(i)-avgEstAvail)*(a_list.get(i)-avgEstAvail);
							}
							variace = sd_c/sum_weight;
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
