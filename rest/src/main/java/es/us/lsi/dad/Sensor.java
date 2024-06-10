package es.us.lsi.dad;

import java.util.Objects;

public class Sensor {
	
	private Integer ID;
	private Integer boardID;
	private Integer groupID;
	public Integer getGroupID() {
		return groupID;
	}

	public void setGroupID(Integer groupID) {
		this.groupID = groupID;
	}

	private Integer value;
	private String type;
	private Long date;
	
	public Sensor() {
		super();
	}
	
	public Sensor(Integer iD, Integer boardID, Integer groupID, Integer value, String type, Long date) {
		super();
		ID = iD;
		this.boardID = boardID;
		this.groupID = groupID;
		this.value = value;
		this.type = type;
		this.date = date;
	}


	public Long getDate() {
		return date;
	}

	public void setDate(Long date) {
		this.date = date;
	}

	public String getType() {
		return type;
	}

	public void setType(String type) {
		this.type = type;
	}

	public Integer getValue() {
		return value;
	}

	public void setValue(Integer value) {
		this.value = value;
	}

	public Integer getBoardID() {
		return boardID;
	}

	public void setBoardID(Integer boardID) {
		this.boardID = boardID;
	}

	public Integer getID() {
		return ID;
	}

	public void setID(Integer iD) {
		ID = iD;
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
		Sensor other = (Sensor) obj;
		return Objects.equals(ID, other.ID) && Objects.equals(boardID, other.boardID)
				&& Objects.equals(date, other.date) && Objects.equals(groupID, other.groupID)
				&& Objects.equals(type, other.type) && Objects.equals(value, other.value);
	}

	@Override
	public String toString() {
		return "Sensor [ID=" + ID + ", boardID=" + boardID + ", groupID=" + groupID + ", value=" + value + ", type="
				+ type + ", date=" + date + "]";
	}
}
