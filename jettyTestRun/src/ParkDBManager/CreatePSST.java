/**
 * DO NOT RUN THIS FILE
 */
/*package ParkDBManager;

import java.sql.Timestamp;
import java.util.Random;

import Database.LoadPST;
import Database.UpdatePSST;
import MapDatabase.Pair;

*//**
 * @author Sandeep
 *
 *//*
public class CreatePSST {

	*//**
	 * @param args
	 *//*
	public static void main(String[] args) {

		Update Parking snap shot table PSST
		UpdatePSST uPsst = new UpdatePSST();

		Load PST database
		LoadPST pst = new LoadPST();
		for(int i=0;i<10000;i++){
			int StreetBlockID = randMinMax(1,2855);
			Fetch the parking availability values from PST database for the street Block
			Pair<Integer, Integer>  rsPST = pst.fetchRecord(StreetBlockID);
			int new_availability = 0;
			if(rsPST.getL()>0){
				new_availability =  randMinMax(0,rsPST.getL());
			}

			System.out.println(i+"->"+StreetBlockID+","+rsPST.getL()+","+new_availability+","+randTimeStamp());

			uPsst.updateWithTime(StreetBlockID,rsPST.getL(),new_availability,randTimeStamp());
		}

	}

	public static int randMinMax(int Low,int High){
		Random r = new Random();
		return (r.nextInt(High-Low) + Low);
	}

	public static Timestamp randTimeStamp(){
		long offset = Timestamp.valueOf("2016-01-01 00:00:00.0000").getTime();
		long end = Timestamp.valueOf("2016-03-31 00:00:00.0000").getTime();
		long diff = end - offset + 1;
		Timestamp rand = new Timestamp(offset + (long)(Math.random() * diff));
		return rand;
	}

}
*/