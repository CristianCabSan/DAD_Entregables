package es.us.lsi.dad;

import java.sql.Timestamp;
import java.util.Objects;

public class Sensor {
	
	private Integer ID;
	private Integer boardID;
	private Double value;
	private String type;
	private Timestamp date;
	
	public Sensor() {
		super();
	}

	public Sensor(Integer ID, Integer boardID, Double value, String type, Timestamp date) {
		super();
		this.setID(ID);
		this.setBoardID(boardID);
		this.setValue(value);
		this.setType(type);
		this.setDate(date);
	}

	public Timestamp getDate() {
		return date;
	}

	public void setDate(Timestamp date) {
		this.date = date;
	}

	public String getType() {
		return type;
	}

	public void setType(String type) {
		this.type = type;
	}

	public Double getValue() {
		return value;
	}

	public void setValue(Double value) {
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
		return Objects.hash(ID, boardID, date, type, value);
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
				&& Objects.equals(date, other.date)	
				&& Objects.equals(type, other.type) && Objects.equals(value, other.value);
	}

	@Override
	public String toString() {
		return "Sensor [ID=" + ID + ", boardID=" + boardID + ", value=" + value + ", type=" + type
				+ ", date=" + date + "]";
	}
}
