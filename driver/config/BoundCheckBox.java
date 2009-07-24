package driver.config;

import java.lang.reflect.Field;

import javax.swing.JCheckBox;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;

import cobweb.params.ConfDisplayName;

public class BoundCheckBox extends JCheckBox implements ChangeListener, FieldBoundComponent  {

	/**
	 *
	 */
	private static final long serialVersionUID = -4621056922233460755L;


	private final Object obj;
	private final Field field;
	private final String label;

	public BoundCheckBox(Object obj, String fieldName) {
		this.obj = obj;
		try {
			this.field = obj.getClass().getField(fieldName);
		} catch (NoSuchFieldException ex) {
			throw new RuntimeException(ex);
		}
		this.label = field.getAnnotation(ConfDisplayName.class).value();
		try {
			this.setSelected(field.getBoolean(obj));
		} catch (IllegalAccessException ex) {
			throw new RuntimeException(ex);
		}
		this.addChangeListener(this);
	}

	@Override
	public String getLabel() {
		return label;
	}

	public void stateChanged(ChangeEvent evt) {
		try {
			field.setBoolean(obj, isSelected());
		} catch (IllegalAccessException ex) {
			throw new RuntimeException(ex);
		}
	}
}
