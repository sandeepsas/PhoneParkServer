package Database;


public class PsstData {

	int recordID;
	int streetBlockID;
	int day;
	int availableSpaces;
	long  timeStamp;

	public PsstData(int recordID,
			int streetBlockID,
			int day,
			int availableSpaces,
			long  timeStamp){
		
		
		this.recordID = recordID;
		this.streetBlockID = streetBlockID;
		this.day = day;
		this.availableSpaces = availableSpaces;
		this.timeStamp = timeStamp;

	}

	public int getRecordID() {
		return recordID;
	}

	public void setRecordID(int recordID) {
		this.recordID = recordID;
	}

	public int getStreetBlockID() {
		return streetBlockID;
	}

	public void setStreetBlockID(int streetBlockID) {
		this.streetBlockID = streetBlockID;
	}

	public int getAvailableSpaces() {
		return availableSpaces;
	}

	public void setAvailableSpaces(int availableSpaces) {
		this.availableSpaces = availableSpaces;
	}

	public long getTimeStamp() {
		return timeStamp;
	}

	public void setTimeStamp(long timeStamp) {
		this.timeStamp = timeStamp;
	}

}
