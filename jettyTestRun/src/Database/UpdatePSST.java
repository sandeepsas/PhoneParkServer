package Database;
/*
 * Updates the Parking Status SnapShot Table
 * 
 * 
 * */
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.sql.Statement;

import Runner.ServerConfig;

public class UpdatePSST {

	// JDBC driver name and database URL
	static final String JDBC_DRIVER = "com.mysql.jdbc.Driver";  
	static final String DB_URL = ServerConfig.DB_URL;

	//  Database credentials

	static final String USER = ServerConfig.USER;
	static final String PASS = ServerConfig.PASS;

	public boolean update(int StreetBlockID,int total_spaces,int new_availability,String timeStamp) {
		// TODO Auto-generated method stub
		boolean res =false;
		Connection conn = null;
		Statement stmt = null;
		try{
			//STEP 2: Register JDBC driver
			Class.forName("com.mysql.jdbc.Driver");

			//STEP 3: Open a connection
			conn = DriverManager.getConnection(DB_URL,USER,PASS);

			//STEP 4: Execute a query
			stmt = conn.createStatement();
			String sql_sequel =   StreetBlockID + "',"
								+"'"+ total_spaces + "',"
								+"'"+ new_availability + "',"
								+" now()";
			
			String sql;
			sql = "INSERT INTO phonepark01.PSST (StreetBlockID,TotalSpaces,AvailableSpaces,TimeStamp) VALUES ('"+sql_sequel+");";
			System.out.println(sql);
			int rs = stmt.executeUpdate(sql);
			if(rs>0)
				res= true;

			//STEP 6: Clean-up environment
			//rs.close();
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
		return res;
	}

}
