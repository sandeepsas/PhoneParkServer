package Runner;

import Database.LoadHPP;

public class pullClass {

	public static void main(String[] args) {
		
		String result = LoadHPP.pullDataFromHPP(1,5);
		System.out.println(result);

	}

}
