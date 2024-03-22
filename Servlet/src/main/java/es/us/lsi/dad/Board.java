package es.us.lsi.dad;

import java.sql.Timestamp;
import java.util.List;
import java.util.Objects;

public class Board {
	private Integer ID;
	private List<Integer> assignedSensors;
	private List<Integer> assignedActuators;
	private Timestamp date;
	
	public Integer getID() {
		return ID;
	}
	
	public void setID(Integer iD) {
		ID = iD;
	}
	
	public List<Integer> getAssignedSensors() {
		return assignedSensors;
	}
	
	public void setAssignedSensors(List<Integer> assignedSensors) {
		this.assignedSensors = assignedSensors;
	}
	
	public List<Integer> getAssignedActuators() {
		return assignedActuators;
	}
	
	public void setAssignedActuators(List<Integer> assignedActuators) {
		this.assignedActuators = assignedActuators;
	}
	
	

	public Timestamp getDate() {
		return date;
	}

	public void setDate(Timestamp date) {
		this.date = date;
	}

	@Override
	public int hashCode() {
		return Objects.hash(ID, assignedActuators, assignedSensors, date);
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		Board other = (Board) obj;
		return Objects.equals(ID, other.ID) && Objects.equals(assignedActuators, other.assignedActuators)
				&& Objects.equals(assignedSensors, other.assignedSensors) && Objects.equals(date, other.date);
	}

	@Override
	public String toString() {
		return "Board [ID=" + ID + ", assignedSensors=" + assignedSensors + ", assignedActuators=" + assignedActuators
				+ ", date=" + date + "]";
	}
}
