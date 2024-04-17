package es.us.lsi.dad;

import java.sql.Timestamp;
import java.util.Objects;

public class Actuator {
	
	private Integer ID;
	private Integer boardID;
	private Double value;
	private String type;
	private Long date;
	
	public Actuator(Integer ID, Integer boardID, Double value, String type, Long date) {
		super();
		this.setID(ID);
		this.setBoardID(boardID);
		this.setValue(value);
		this.setType(type);
		this.setDate(date);
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
		return Objects.hash(ID, value, boardID, date, type);
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
		return Objects.equals(ID, other.ID) && Objects.equals(value, other.value)
				&& Objects.equals(boardID, other.boardID) && Objects.equals(date, other.date)
				&& Objects.equals(type, other.type);
	}

	@Override
	public String toString() {
		return "Actuator [ID=" + ID + ", boardID=" + boardID + ", value=" + value + ", type="
				+ type + ", date=" + date + "]";
	}
}
