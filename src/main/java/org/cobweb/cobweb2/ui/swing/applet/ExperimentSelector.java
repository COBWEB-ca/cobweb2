/**
 *
 */
package org.cobweb.cobweb2.ui.swing.applet;

import java.util.Map;

import javax.swing.JComboBox;

/**
 *
 */
public class ExperimentSelector extends JComboBox<String> {

	/**
	 *
	 */
	private static final long serialVersionUID = 8328713697383538804L;

	public ExperimentSelector(Map<String, String> experements) {
		super(experements.keySet().toArray(new String[0]));
		this.setEditable(false);

		this.setMaximumRowCount(5);

	}

}
