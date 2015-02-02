package org.cobweb.cobweb2.ui.config;

import java.lang.reflect.AnnotatedElement;



public abstract class PropertyAccessorBase implements PropertyAccessor {

	protected final PropertyAccessor parent;

	protected PropertyAccessorBase() {
		this(null);
	}

	protected PropertyAccessorBase(PropertyAccessor parent) {
		this.parent = parent;
	}

	@Override
	public String getName() {
		String res = thisGetName();
		if (parent != null)
			res = parent.getName() + " " + res;

		return res;
	}

	protected abstract String thisGetName();

	@Override
	public String toString() {
		String res = thisToString();
		if (parent != null)
			res = parent.toString() + res;
		return res;
	}

	protected abstract String thisToString();

	@Override
	public Object get(Object object) {
		if (parent != null)
			object = parent.get(object);
		return thisGetValue(object);
	}

	protected abstract Object thisGetValue(Object object);

	@Override
	public void set(Object object, Object value) {
		if (parent != null)
			object = parent.get(object);
		thisSetValue(object, value);
	}

	protected abstract void thisSetValue(Object object, Object value);

	@Override
	public AnnotatedElement getAnnotationSource() {
		if (parent == null)
			throw new IllegalArgumentException("No annotation source!");
		else
			return parent.getAnnotationSource();
	}
}
