package supportPack;

public class ReportPack {
	
	public static long reportGen16digit() {
	    while (true) {
	        long numb = (long)(Math.random() * 100000000 * 1000000); // had to use this as int's are to small for a 13 digit number.
	        if (String.valueOf(numb).length() == 16)
	            return numb;
	    }
	}
	public static long reportGenerator()
	{
	  return (long)(Math.random()*100000 + 3333300000L);
	}

}
