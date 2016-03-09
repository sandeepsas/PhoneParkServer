package Runner;

public class ServerConfig {
	
	public static int ACTIVITY_PARKED = 1;
	public static int ACTIVITY_DEPARKED = 0;
	
	public static double FLASE_POSITIVE = 0.1;
	public static double FLASE_NEGATIVE = 0.1;
	public static double PENETRATION_RATIO = 0.05;
	
	/*GCM parameters*/
	public static int k_m = 17; // Maximum number of edges to be displayed
	
	
	public static double velocity = 8.9408/4.0; 
	public static double velocityWalking = Double.POSITIVE_INFINITY;
	public static int beta = 60*60;
	public static int tau = 120;

}
