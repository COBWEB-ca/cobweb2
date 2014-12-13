package org.cobweb.cobweb2.interconnect;

import java.lang.reflect.Field;
import java.util.Collection;
import java.util.LinkedList;

import org.cobweb.cobweb2.core.params.ComplexAgentParams;
import org.cobweb.cobweb2.io.CobwebSelectionParam;
import org.cobweb.io.ConfDisplayName;
import org.cobweb.io.ConfXMLTag;
import org.w3c.dom.Document;
import org.w3c.dom.Node;

public class Phenotype implements CobwebSelectionParam<Phenotype> {

	private static final long serialVersionUID = -6142169580857190598L;

	public Field field = null;

	@Deprecated //FIXME static!
	private static Collection<Field> bindableFields = new LinkedList<Field>() {
		private static final long serialVersionUID = -6369342528741543712L;
		{
			for (Field f: ComplexAgentParams.class.getFields()) {
				if (f.getAnnotation(GeneMutatable.class) == null)
					continue;
				this.add(f);
			}
		}
	};

	@Deprecated //FIXME static!
	private static Collection<Phenotype> bindables = new LinkedList<Phenotype>() {
		private static final long serialVersionUID = -6369342528741543712L;
		{
			this.add(new Phenotype());
			for (Field f: bindableFields) {
				this.add(new Phenotype(f));
			}
		}
	};

	public Phenotype(){
		// Nothing
	}

	@Override
	public boolean equals(Object obj) {
		if (obj instanceof Phenotype) {
			Phenotype p = (Phenotype) obj;
			if (p.field == null && this.field == null) return true;
			if (p.field == null || this.field == null) return false;
			return p.field.equals(this.field);
		}
		return false;
	}

	@Override
	public int hashCode() {
		return field.hashCode();
	}

	/**
	 *
	 * @param f field to modify
	 */
	public Phenotype(Field f) {
		if (f != null &&
				(f.getAnnotation(GeneMutatable.class) == null || f.getAnnotation(ConfDisplayName.class) == null))
			throw new IllegalArgumentException("Field must be labeled as @GeneMutatable and have a @ConfDisplayName");
		this.field = f;
	}

	@Override
	public void loadConfig(Node root) throws IllegalArgumentException {
		String value = root.getTextContent();
		for (Field f : bindableFields) {
			if (value.equals("None")) {
				this.field = null;
				return;
			}
			if (f.getAnnotation(ConfXMLTag.class).value().equals(value)) {
				this.field = f;
				return;
			}
			if (f.getAnnotation(ConfDisplayName.class).value().equals(value)) {
				this.field = f;
				return;
			}
		}
		throw new IllegalArgumentException("Cannot match Phenotype '" + value + "' to any field");
	}

	@Override
	public String toString() {
		if (field == null)
			return "[Not Bound]";

		return field.getAnnotation(ConfDisplayName.class).value();
	}

	@Override
	public void saveConfig(Node root, Document document) {
		String value;
		if (field == null) {
			value = "None";
		} else {
			value = field.getAnnotation(ConfXMLTag.class).value();
		}
		root.setTextContent(value);
	}

	@Override
	public Collection<Phenotype> getPossibleValues() {
		return bindables;
	}

	@Override
	public void setValue(Phenotype value) {
		Phenotype p = value;
		this.field = p.field;
	}

}