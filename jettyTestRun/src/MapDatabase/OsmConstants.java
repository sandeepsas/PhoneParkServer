package MapDatabase;

/*
 * @Author Sandeep
 * 
 * Stores Constants for OSM parsing
 * */
public class OsmConstants {

	public static String GOOGLE_MAP_API_KEY = "AIzaSyAkBR5OFsUz1Np0Rg1bJSyb4ObuXYF4to4";

	/*Road type to MaxSpeed mapping */
	public static int roadTypeToSpeed(String type)
	{
		int speed=0;
		switch (type)
		{
		case "primary":
			speed =40;
			break;
		case "secondary":
			speed =40;
			break;
		case "tertiary":
			speed =35;
			break;
		case "primary_link":
			speed =35;
			break;
		case "secondary_link":
			speed =35;
			break;
		case "teritiary_link":
			speed =35;
			break;
		case "residential":
			speed =20;
			break;
		case "unclassified":
			speed =20;
			break;
		case "road":
			speed =20;
			break;
		case "living street":
			speed =20;
			break;
		case "motorway":
			speed =65;
			break;
		case "motorway_link":
			speed =35;
			break;
		case "trunk":
			speed =65;
			break;
		case "trunk_link":
			speed =35;
			break;
		default:
			speed =20;

		}
		return speed;
	}
	
	public static Pair<Double,Double> midPoint(double lat1,double lon1,double lat2,double lon2){

	    double dLon = Math.toRadians(lon2 - lon1);

	    //convert to radians
	    lat1 = Math.toRadians(lat1);
	    lat2 = Math.toRadians(lat2);
	    lon1 = Math.toRadians(lon1);

	    double Bx = Math.cos(lat2) * Math.cos(dLon);
	    double By = Math.cos(lat2) * Math.sin(dLon);
	    double lat3 = Math.atan2(Math.sin(lat1) + Math.sin(lat2), Math.sqrt((Math.cos(lat1) + Bx) * (Math.cos(lat1) + Bx) + By * By));
	    double lon3 = lon1 + Math.atan2(By, Math.cos(lat1) + Bx);

	    //print out in degrees
		return new Pair<Double,Double>(Math.toDegrees(lon3),Math.toDegrees(lat3));
	}
}
