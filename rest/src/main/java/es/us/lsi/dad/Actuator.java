package es.us.lsi.dad;

import java.sql.Timestamp;
import java.util.Objects;

public class Actuator {
	
	private Integer ID;
	private Integer boardID;
	private Integer groupID;
	private Double value;
	private String type;
	private Long date;
	
	public Actuator(Integer iD, Integer boardID, Integer groupID, Double value, String type, Long date) {
		super();
		ID = iD;
		this.boardID = boardID;
		this.groupID = groupID;
		this.value = value;
		this.type = type;
		this.date = date;
	}

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

	public Integer getGroupID() {
		return groupID;
	}


	public void setGroupID(Integer groupID) {
		this.groupID = groupID;
	}


	public void setDate(Long date) {
		this.date = date;
	}


	public Double getValue() {
		return value;
	}

	public void setValue(Double value) {
		this.value = value;
	}

	public String getType() {
		return type;
	}

	public void setType(String type) {
		this.type = type;
	}

	public long getDate() {
		return date;
	}

	public void setDate(long date) {
		this.date = date;
	}

	@Override
	public int hashCode() {
		return Objects.hash(ID, boardID, date, groupID, type, value);
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
		return Objects.equals(ID, other.ID) && Objects.equals(boardID, other.boardID)
				&& Objects.equals(date, other.date) && Objects.equals(groupID, other.groupID)
				&& Objects.equals(type, other.type) && Objects.equals(value, other.value);
	}

	@Override
	public String toString() {
		return "Actuator [ID=" + ID + ", boardID=" + boardID + ", groupID=" + groupID + ", value=" + value + ", type="
				+ type + ", date=" + date + "]";
	}
}
