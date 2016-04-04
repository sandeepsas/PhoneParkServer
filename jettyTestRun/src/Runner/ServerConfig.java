package Runner;
/*
 * @Author: Sandeep Sasidharan
 * Singleton class to read the server configuration file
 * 
 * */
import java.io.*;
import java.util.*;

public final class ServerConfig {


	public final int ACTIVITY_PARKED = 1;
	public final int ACTIVITY_DEPARKED = 0;

	/*GCM parameters*/
	public  int k_m; // Maximum number of edges to be displayed
	public  int tau;

	public  double velocity = 8.9408/4.0; 
	public  double velocityWalking = Double.POSITIVE_INFINITY;
	public  int beta = 60*60;


	public  double FLASE_POSITIVE;
	public  double FLASE_NEGATIVE;;
	public  double PENETRATION_RATIO;


	public  String DB_URL;
	public  String USER;
	public  String PASS;

	public  String HIST_START_DATE;
	public  String HIST_END_DATE;
	
	private static ServerConfig serverConfig = new ServerConfig( );

	/* Singleton Class - Static 'instance' method */
	public static ServerConfig getInstance( ) {
		return serverConfig;
	}

	private ServerConfig(){
		Properties prop = new Properties();
		InputStream input = null;

		try {
			String filename = "config.properties";
			input = ServerConfig.class.getClassLoader().getResourceAsStream(filename);
			if(input==null){
				System.out.println("Sorry, unable to find " + filename);
				return;
			}

			//load a properties file from class path, inside static method
			prop.load(input);

			//get the property value
			FLASE_POSITIVE = Double.parseDouble(prop.getProperty("FLASE_POSITIVE"));
			FLASE_NEGATIVE = Double.parseDouble(prop.getProperty("FLASE_NEGATIVE"));
			PENETRATION_RATIO = Double.parseDouble(prop.getProperty("PENETRATION_RATIO"));
			k_m = Integer.parseInt(prop.getProperty("NO_BLOCKS_DISPLAYED"))+2;
			tau = Integer.parseInt(prop.getProperty("RECOVERY_FN_TIME"));

			DB_URL = prop.getProperty("DB_URL");
			USER = prop.getProperty("USER");
			PASS = prop.getProperty("PASS");

			HIST_START_DATE = prop.getProperty("HIST_START_DATE");
			HIST_END_DATE = prop.getProperty("HIST_END_DATE");


		}catch (IOException ex) {
			ex.printStackTrace();
		} finally{
			if(input!=null){
				try {
					input.close();
				} catch (IOException e) {
					e.printStackTrace();
				}
			}
		}

	}

}
