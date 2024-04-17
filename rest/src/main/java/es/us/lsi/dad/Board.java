package es.us.lsi.dad;

import java.util.Objects;

public class Board {
	private Integer ID;
	private Long date;
	
	public Board(Integer iD, Long date) {
		super();
		ID = iD;
		this.date = date;
	}

	public Integer getID() {
		return ID;
	}
	
	public void setID(Integer iD) {
		ID = iD;
	}

	public long getDate() {
		return date;
	}

	public void setDate(long date) {
		this.date = date;
	}

	@Override
	public int hashCode() {
		return Objects.hash(ID, date);
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
		return Objects.equals(ID, other.ID) && Objects.equals(date, other.date);
	}

	@Override
	public String toString() {
		return "Board [ID=" + ID + ", date=" + date + "]";
	}
}
