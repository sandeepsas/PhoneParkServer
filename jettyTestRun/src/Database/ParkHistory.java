package Database;

public class ParkHistory {
	
	int totalSamplesForStreet;
	int totalSamplesForDateTime;
	double avgParkEstForStreet;
	double avgParkEstForDateTime;
	int StreetID;
	
	ParkHistory(){
		
		 this.totalSamplesForStreet = 0;
		 this.totalSamplesForDateTime = 0;
		 this.avgParkEstForStreet = 0;
		 this.avgParkEstForDateTime = 0;
		 this.StreetID = 0;
		
		
	}
	
	public int getTotalSamplesForStreet() {
		return totalSamplesForStreet;
	}

	public void setTotalSamplesForStreet(int totalSamplesForStreet) {
		this.totalSamplesForStreet = totalSamplesForStreet;
	}

	public int getTotalSamplesForDateTime() {
		return totalSamplesForDateTime;
	}

	public void setTotalSamplesForDateTime(int totalSamplesForDateTime) {
		this.totalSamplesForDateTime = totalSamplesForDateTime;
	}

	public double getAvgParkEstForStreet() {
		return avgParkEstForStreet;
	}

	public void setAvgParkEstForStreet(double avgParkEstForStreet) {
		this.avgParkEstForStreet = avgParkEstForStreet;
	}

	public double getAvgParkEstForDateTime() {
		return avgParkEstForDateTime;
	}

	public void setAvgParkEstForDateTime(double avgParkEstForDateTime) {
		this.avgParkEstForDateTime = avgParkEstForDateTime;
	}

	public int getStreetID() {
		return StreetID;
	}

	public void setStreetID(int streetID) {
		StreetID = streetID;
	}

	ParkHistory(int StreetID, int totalSamplesForStreet,
			int totalSamplesForDateTime, double avgParkEstForStreet,
			double avgParkEstForDateTime){
		
		 this.totalSamplesForStreet = totalSamplesForStreet;
		 this.totalSamplesForDateTime = totalSamplesForDateTime;
		 this.avgParkEstForStreet = avgParkEstForStreet;
		 this.avgParkEstForDateTime = avgParkEstForDateTime;
		 this.StreetID = StreetID;
		
		
	}

}
