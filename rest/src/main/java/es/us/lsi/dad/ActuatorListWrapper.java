package es.us.lsi.dad;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

public class ActuatorListWrapper {
	private List<Actuator> actuatorList;

	public ActuatorListWrapper() {
		super();
	}

	public ActuatorListWrapper(Collection<Actuator> actuatorList) {
		super();
		this.actuatorList = new ArrayList<Actuator>(actuatorList);
	}
	
	public ActuatorListWrapper(List<Actuator> actuatorList) {
		super();
		this.actuatorList = new ArrayList<Actuator>(actuatorList);
	}

	public List<Actuator> getActuatorList() {
		return actuatorList;
	}

	public void setActuatorList(List<Actuator> actuatorList) {
		this.actuatorList = actuatorList;
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((actuatorList == null) ? 0 : actuatorList.hashCode());
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
		ActuatorListWrapper other = (ActuatorListWrapper) obj;
		if (actuatorList == null) {
			if (other.actuatorList != null)
				return false;
		} else if (!actuatorList.equals(other.actuatorList))
			return false;
		return true;
	}

	@Override
	public String toString() {
		return "ActuatorListWrapper [actuatorList=" + actuatorList + "]";
	}

}
