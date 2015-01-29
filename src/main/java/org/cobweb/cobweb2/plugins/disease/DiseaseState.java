package org.cobweb.cobweb2.plugins.disease;

import org.cobweb.cobweb2.plugins.AgentState;

public class DiseaseState implements AgentState {
	public boolean sick = false;
	public boolean vaccinated = false;
	public long sickStart = -1;
	public float vaccineEffectiveness;

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