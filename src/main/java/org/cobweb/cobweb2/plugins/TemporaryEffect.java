package org.cobweb.cobweb2.plugins;

import org.cobweb.cobweb2.core.Agent;
import org.cobweb.cobweb2.core.Phenotype;


public class TemporaryEffect {

	public Phenotype parameter;
	public float factor;
	public int duration;

	public long startTime;

	public Agent target;

	public Object source;

	public TemporaryEffect(Agent target, TempEffectParam effect, Object effectSource) {
		this.target = target;
		this.parameter = effect.parameter;
		this.factor = effect.factor.getValue();
		this.duration = effect.duration.getValue();
		this.source = effectSource;
	}

	public void apply() {
		parameter.modifyValue(source, target, factor);
	}

	public boolean updateIsAlive(long time) {
		if (time - startTime > duration) {
			parameter.unmodifyValue(source, target);
			return false;
		}
		return true;
	}

}
