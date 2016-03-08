package Runner;
import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public class CSVfile {

  public static void main(String[] args) {

	CSVfile obj = new CSVfile();
	obj.run();

  }

  public List<String> run() {

	List<String> list = new ArrayList<String>();
	
	  
	String csvFile = "E:/Program_Workspaces/MyFirstWebService/JettDemo/src/UICStateMid.csv";
	BufferedReader br = null;
	String line = "";
	String cvsSplitBy = ",";

	try {

		br = new BufferedReader(new FileReader(csvFile));
		while ((line = br.readLine()) != null) {

			list.add(line);
		        // use comma as separator
//			String[] country = line.split(cvsSplitBy);
//
//			System.out.println("Country [code= " + country[4] 
//                                 + " , name=" + country[5] + "]");

		}

	} catch (FileNotFoundException e) {
		e.printStackTrace();
	} catch (IOException e) {
		e.printStackTrace();
	} finally {
		if (br != null) {
			try {
				br.close();
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
	}

	System.out.println("Done");
	return list;
  }

}