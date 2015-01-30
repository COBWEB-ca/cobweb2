package org.cobweb.cobweb2.plugins.disease;

import org.cobweb.cobweb2.plugins.AgentState;
import org.cobweb.io.ConfXMLTag;

public class DiseaseState implements AgentState {

	@ConfXMLTag("sick")
	public boolean sick = false;

	@ConfXMLTag("vaccinated")
	public boolean vaccinated = false;

	@ConfXMLTag("sickStart")
	public long sickStart = -1;

	@ConfXMLTag("vaccineEffectiveness")
	public float vaccineEffectiveness = 1;


	public DiseaseState() {
	}

	public DiseaseState(boolean sick, boolean vaccinated, long sickStart) {
		this.sick = sick;
		this.vaccinated = vaccinated;
		this.sickStart = sickStart;
	}

	public DiseaseState(boolean sick, boolean vaccinated, float vaccineEffectiveness) {
		this.sick = sick;
		this.vaccinated = vaccinated;
		this.vaccineEffectiveness = vaccineEffectiveness;
	}

	@Override
	public boolean isTransient() {
		return false;
	}
	private static final long serialVersionUID = 1L;
}