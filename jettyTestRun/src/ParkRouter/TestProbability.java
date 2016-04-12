package ParkRouter;

import parkAttribs.StatisticMatrices;

public class TestProbability {
	
	public static void main(String[] arhs){
		
		double var = 2;
		double avail = 2.5;
		double probExp = 1 - StatisticMatrices.Phi(0.5, avail, Math.sqrt(var));
		
		System.out.println(probExp);
	}

}
