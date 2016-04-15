package Database;
/*
 * @Author Sandeep
 * 
 * This class populates Historic Parking Profile. The class internally calls INTR2HPP class.
 * 
 * The main function of this class is to fetch data from PSST Table and populate Intermediate Table
 * */
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.sql.Timestamp;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;
import MapDatabase.Pair;
import Runner.StartServer;

public class PopulateHPP {

	// JDBC driver name and database URL
	static final String JDBC_DRIVER = "com.mysql.jdbc.Driver"; 
	final static String DB_URL = StartServer.getServerconfig().DB_URL;

	static final String HIST_START_DATE = StartServer.getServerconfig().HIST_START_DATE;
	static final String HIST_END_DATE = StartServer.getServerconfig().HIST_END_DATE;

	final static String USER = StartServer.getServerconfig().USER;
	final static String PASS = StartServer.getServerconfig().PASS;
	//  Database credentials
	public static void loadHistory(){


		Connection conn = null;
		Connection conn_HPP = null;
		Connection conn_final = null;
		Statement stmt = null;
		ResultSet rs = null;

		try{
			Class.forName("com.mysql.jdbc.Driver");
			conn = DriverManager.getConnection(DB_URL,USER,PASS);
			conn_HPP = DriverManager.getConnection(DB_URL,USER,PASS);
			conn_final = DriverManager.getConnection(DB_URL,USER,PASS);

			stmt = conn.createStatement();
			/*Truncate Table*/
			stmt.executeUpdate("TRUNCATE intermediate");
			/*Get all values between start and end dates of historic data*/
			String sql = "SELECT * FROM phonepark01.psst WHERE timestamp >= '"+HIST_START_DATE+"' AND "
					+ "timestamp < '"+HIST_END_DATE+"'";
			rs = stmt.executeQuery(sql);
			Map<Pair<Integer,Pair<Integer,Integer>>, PsstData> prevStreetBlockAvail = new HashMap<Pair<Integer,Pair<Integer,Integer>>, PsstData>();

			while(rs.next()){
				int recordID = rs.getInt("RecordID");
				int streetBlockID = rs.getInt("StreetBlockID");
				int totalSpaces = rs.getInt("TotalSpaces");
				int availableSpaces = rs.getInt("AvailableSpaces");
				Timestamp timeStamp = rs.getTimestamp("TimeStamp");
				String sql_sequel_write  = null;
				
				Calendar cal = Calendar.getInstance();
				cal.setTime(timeStamp);
				int day = cal.get(Calendar.DAY_OF_WEEK);
				int startTime = cal.get(Calendar.HOUR_OF_DAY);
				cal.add(Calendar.HOUR_OF_DAY, 1);
				int endTime = cal.get(Calendar.HOUR_OF_DAY);

				long minutes = timeStamp.getTime();
				//long duration = timeStamp.getMinutes();
				long duration = timeStamp.getTime();
				
				int insert_availability = 0;
				
				
				if(prevStreetBlockAvail.containsKey(new Pair<Integer, Pair<Integer,Integer>> 
				(streetBlockID,new Pair<Integer,Integer>(startTime,day)))){
					
					PsstData prev_spaces = prevStreetBlockAvail.get(new Pair<Integer, 
							Pair<Integer,Integer>> (streetBlockID,new Pair<Integer,Integer>(startTime,day)));
					//prevStreetBlockAvail.remove(streetBlockID);
					duration = duration - prev_spaces.timeStamp;
					int mins = (int) ((duration / (1000*60)));

					/*Log to intermediate*/
					sql_sequel_write = streetBlockID + "',"
							+"'"+ day + "',"
							+"'"+ startTime + "',"
							+"'"+ endTime + "',"
							+"'"+ mins + "',"
							+"'"+ prev_spaces.availableSpaces+ "'";

				}else{
					if(availableSpaces==0){
						insert_availability = totalSpaces;
					}

					sql_sequel_write = streetBlockID + "',"
							+"'"+ day + "',"
							+"'"+ startTime + "',"
							+"'"+ endTime + "',"
							+"'"+ timeStamp.getMinutes() + "',"
							+"'"+ insert_availability + "'";
					
				}
				String writeSQL = "INSERT INTO phonepark01.intermediate (StreetBlockID,Day,StartTime,"
						+ "EndTime,Duration,Availability) "
						+ "VALUES ('"+sql_sequel_write+");";
				Statement stmt_intr = conn_HPP.createStatement();
				stmt_intr.executeUpdate(writeSQL);
				stmt_intr.close();

				prevStreetBlockAvail.put(new Pair<Integer, Pair<Integer, Integer>> (streetBlockID,new Pair<Integer, Integer>(startTime,day)),
						new PsstData(recordID,streetBlockID,
						 day,availableSpaces,minutes));
			}
			for (Entry<Pair<Integer, Pair<Integer,Integer>>, PsstData> entry : prevStreetBlockAvail.entrySet()) {
				Pair<Integer,Pair<Integer,Integer>> key = entry.getKey();
			    PsstData value = entry.getValue();
			    
			    Date date = new Date(value.timeStamp );
			    Statement stmt_final = conn_final.createStatement();

			    String sql_sequel_final= key.getL() + "',"
						+"'"+ key.getR().getR()+ "',"
						+"'"+  key.getR().getL() + "',"
						+"'"+ (key.getR().getL()+1) + "',"
						+"'"+ (60-date.getMinutes()) + "',"
						+"'"+ value.availableSpaces+ "'";
			    
			    String finalSQL = "INSERT INTO phonepark01.intermediate (StreetBlockID,Day,StartTime,"
						+ "EndTime,Duration,Availability) "
						+ "VALUES ('"+sql_sequel_final+");";
			    stmt_final.executeUpdate(finalSQL);
			    stmt_final.close();
			    
			    // Build Historic Parking Profile
			    INTR2HPP.Intermediate2Hpp();
			}

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
