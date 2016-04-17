package org.cobweb.cobweb2.plugins.genetics;

import org.cobweb.cobweb2.core.Agent;
import org.cobweb.cobweb2.core.Phenotype;
import org.cobweb.cobweb2.ui.config.PropertyAccessor;
import org.cobweb.io.ConfDisplayName;
import org.cobweb.util.MutatableField;

public abstract class PropertyPhenotype extends Phenotype {

	private PropertyAccessor propertyAccessor;

	public PropertyPhenotype(PropertyAccessor propertyAccessor) {
		super();
		if (propertyAccessor != null &&(
				propertyAccessor.getAnnotationSource().getAnnotation(ConfDisplayName.class) == null)) {
			throw new IllegalArgumentException("Property must be have a @ConfDisplayName");
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
		return propertyAccessor.getIdentifier();
	}

	@Override
	public String getName() {
		return propertyAccessor.getName();
	}

	@Override
	public void modifyValue(Object cause, Agent a, float m) {
		if (rootAccessor(a) == null)
			return;

		MutatableField field = (MutatableField) propertyAccessor.get(rootAccessor(a));
		field.setMultiplier(cause, m);
	}

	@Override
	public void unmodifyValue(Object cause, Agent a) {
		if (rootAccessor(a) == null)
			return;

		MutatableField field = (MutatableField) propertyAccessor.get(rootAccessor(a));
		field.removeMultiplier(cause);
	}

	protected abstract Object rootAccessor(Agent a);

	private static final long serialVersionUID = 1L;
}
