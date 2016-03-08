/*package Generators;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.sql.Timestamp;
import java.text.SimpleDateFormat;
import java.util.Calendar;

import MapDatabase.Pair;

public class HPPGen {

	// JDBC driver name and database URL
	static final String JDBC_DRIVER = "com.mysql.jdbc.Driver";  
	static final String DB_URL = "jdbc:mysql://localhost:3306/phonepark01";

	//  Database credentials

	static final String USER = "root";
	static final String PASS = "";
	public static final String DATE_FORMAT_NOW = "hhmmss";

	public static String timenow() {
	    Calendar cal = Calendar.getInstance();
	    SimpleDateFormat sdf = new SimpleDateFormat(DATE_FORMAT_NOW);
	    return sdf.format(cal.getTime());
	           }

	public static void main(String[] args){

		// TODO Auto-generated method stub
		//boolean res =false;

		Connection conn = null;
		//Statement stmt = null;
		Pair<Integer, Integer> rsPair = new Pair<Integer, Integer> ();
		try{
			//STEP 2: Register JDBC driver
			Class.forName("com.mysql.jdbc.Driver");

			//STEP 3: Open a connection
			System.out.println("Connecting to database...");
			conn = DriverManager.getConnection(DB_URL,USER,PASS);

			//STEP 4: Execute a query
			System.out.println("Creating statement...");


			for (int qid = 3505; qid<=16548; qid++){
				Calendar now = Calendar.getInstance();
				SimpleDateFormat sdf = new SimpleDateFormat(DATE_FORMAT_NOW);
				int dayNo = 1 + (int)(Math.random() * 7);
				int noAvailSpaces = 1 + (int)(Math.random() * 25);
				for (int tid = 0; tid<24; tid++){
					
					String startTime = sdf.format(now.getTime());
					now.add(Calendar.MINUTE, 50);
					String endTime = sdf.format(now.getTime());

					Statement stmt = conn.createStatement();
					int noSpaces = (int) (Math.random() * (25 - 15)) + 15;
					//int noAvailSpaces = (int) (Math.random() * (noSpaces - 5)) + 5;

					String sql_sequel =   ""+qid + "',"
							+"'"+ startTime + "',"
							+"'"+ endTime + "',"
							+"'"+ dayNo + "',"
							+"'"+ noAvailSpaces;

					String sql;
					sql = "INSERT INTO phonepark01.HPP (StreetBlockID, StartTime, EndTime, Day, AvgEstParkAvail) VALUES ('"+sql_sequel+"');";
					System.out.println(sql);
					//int rs = stmt.executeUpdate(sql);
					int rs = stmt.executeUpdate(sql);
					stmt.close();
				}
			}



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
*/