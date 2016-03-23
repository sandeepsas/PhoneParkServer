package Runner;

import java.io.*;
import java.util.*;

public class ServerConfig {
	

	public static final int ACTIVITY_PARKED = 1;
	public static final int ACTIVITY_DEPARKED = 0;
	
	/*GCM parameters*/
	public static int k_m; // Maximum number of edges to be displayed
	public static int tau;

	public static double velocity = 8.9408/4.0; 
	public static double velocityWalking = Double.POSITIVE_INFINITY;
	public static int beta = 60*60;
	

	public static double FLASE_POSITIVE;
	public static double FLASE_NEGATIVE;;
	public static double PENETRATION_RATIO;
	
	
	public static String DB_URL;
	public static String USER;
	public static String PASS;

	ServerConfig(){
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
