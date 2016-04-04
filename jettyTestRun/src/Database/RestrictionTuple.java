package Database;

public class RestrictionTuple {
	int streetID;
	int startTime;
	int endTime;
	public RestrictionTuple(int streetID, int startTime, int endTime){
		this.streetID = streetID;
		this.startTime = startTime;
		this.endTime = endTime;
	}
	public int getStreetID() {
		return streetID;
	}
	public void setStreetID(int streetID) {
		this.streetID = streetID;
	}
	public int getStartTime() {
		return startTime;
	}
	public void setStartTime(int startTime) {
		this.startTime = startTime;
	}
	public int getEndTime() {
		return endTime;
	}
	public void setEndTime(int endTime) {
		this.endTime = endTime;
	}
	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + startTime;
		result = prime * result + streetID;
		return result;
	}
	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		RestrictionTuple other = (RestrictionTuple) obj;
		if (startTime != other.startTime)
			return false;
		if (streetID != other.streetID)
			return false;
		return true;
	}
}
