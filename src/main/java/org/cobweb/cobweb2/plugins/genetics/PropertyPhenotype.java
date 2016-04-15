package org.cobweb.cobweb2.plugins.genetics;

import org.cobweb.cobweb2.core.Agent;
import org.cobweb.cobweb2.core.Mutatable;
import org.cobweb.cobweb2.core.Phenotype;
import org.cobweb.cobweb2.ui.config.PropertyAccessor;
import org.cobweb.io.ConfDisplayName;

public abstract class PropertyPhenotype extends Phenotype {

	private PropertyAccessor propertyAccessor;

	public PropertyPhenotype(PropertyAccessor propertyAccessor) {
		super();
		if (propertyAccessor != null &&(
				propertyAccessor.getAnnotationSource().getAnnotation(Mutatable.class) == null ||
				propertyAccessor.getAnnotationSource().getAnnotation(ConfDisplayName.class) == null)) {
			throw new IllegalArgumentException("Property must be labeled as @Mutatable and have a @ConfDisplayName");
		}

		this.propertyAccessor = propertyAccessor;
	}

	@Override
	public int hashCode() {
		return propertyAccessor.hashCode();
	}

	@Override
	public boolean equals(Object obj) {
		if (obj instanceof PropertyPhenotype) {
			PropertyPhenotype o = (PropertyPhenotype) obj;
			return propertyAccessor.equals(o.propertyAccessor);
		}
		return false;
	}

	@Override
	public String getIdentifier() {
		return propertyAccessor.getXmlName();
	}

	@Override
	public String getName() {
		return propertyAccessor.getName();
	}

	@Override
	public void modifyValue(Agent a, float m, float b) {
		if (rootAccessor(a) == null)
			return;

		float value = getValue(a);
		float newValue = value * m + b;
		setValue(a, newValue);
	}

	@Override
	public float getValue(Agent a) {
		return propertyAccessor.getAsFloat(rootAccessor(a));
	}

	@Override
	public void setValue(Agent a, float value) {
		propertyAccessor.setAsFloat(rootAccessor(a), value);
	}

	protected abstract Object rootAccessor(Agent a);

	private static final long serialVersionUID = 1L;
}
