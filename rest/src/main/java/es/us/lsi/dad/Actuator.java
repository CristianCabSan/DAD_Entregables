package es.us.lsi.dad;

import java.sql.Timestamp;
import java.util.Objects;

public class Actuator {
	
	private Integer ID;
	private Integer boardID;
	private Boolean active;
	private String type;
	private Timestamp date;
	
	public Actuator() {
		super();
	}

	public Integer getID() {
		return ID;
	}

	public void setID(Integer iD) {
		ID = iD;
	}

	public Integer getBoardID() {
		return boardID;
	}

	public void setBoardID(Integer boardID) {
		this.boardID = boardID;
	}

	public Boolean getActive() {
		return active;
	}

	public void setActive(Boolean active) {
		this.active = active;
	}

	public String getType() {
		return type;
	}

	public void setType(String type) {
		this.type = type;
	}

	public Timestamp getDate() {
		return date;
	}

	public void setDate(Timestamp date) {
		this.date = date;
	}

	@Override
	public int hashCode() {
		return Objects.hash(ID, active, boardID, date, type);
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		Actuator other = (Actuator) obj;
		return Objects.equals(ID, other.ID) && Objects.equals(active, other.active)
				&& Objects.equals(boardID, other.boardID) && Objects.equals(date, other.date)
				&& Objects.equals(type, other.type);
	}

	@Override
	public String toString() {
		return "Actuator [ID=" + ID + ", boardID=" + boardID + ", active=" + active + ", type="
				+ type + ", date=" + date + "]";
	}
}
