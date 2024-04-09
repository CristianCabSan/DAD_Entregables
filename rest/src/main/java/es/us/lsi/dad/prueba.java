package es.us.lsi.dad;

import java.util.Objects;

public class prueba{
	private String ID;

	public String getID() {
		return ID;
	}

	public void setID(String iD) {
		ID = iD;
	}

	public prueba(String iD) {
		super();
		ID = iD;
	}

	@Override
	public int hashCode() {
		return Objects.hash(ID);
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		prueba other = (prueba) obj;
		return Objects.equals(ID, other.ID);
	}

	@Override
	public String toString() {
		return "prueba [ID=" + ID + "]";
	}
	
	
}