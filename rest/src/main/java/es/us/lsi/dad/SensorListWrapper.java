package es.us.lsi.dad;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

public class SensorListWrapper {
	private List<Sensor> sensorList;

	public SensorListWrapper() {
		super();
	}

	public SensorListWrapper(Collection<Sensor> sensorList) {
		super();
		this.sensorList = new ArrayList<Sensor>(sensorList);
	}
	
	public SensorListWrapper(List<Sensor> sensorList) {
		super();
		this.sensorList = new ArrayList<Sensor>(sensorList);
	}

	public List<Sensor> getSensorList() {
		return sensorList;
	}

	public void setSensorList(List<Sensor> sensorList) {
		this.sensorList = sensorList;
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((sensorList == null) ? 0 : sensorList.hashCode());
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
		SensorListWrapper other = (SensorListWrapper) obj;
		if (sensorList == null) {
			if (other.sensorList != null)
				return false;
		} else if (!sensorList.equals(other.sensorList))
			return false;
		return true;
	}

	@Override
	public String toString() {
		return "SensorListWrapper [sensorList=" + sensorList + "]";
	}

}
