/**
 *
 */
package org.cobweb.cobweb2.ui.swing.applet;

import java.util.Map;

import javax.swing.JComboBox;

/**
 *
 */
public class ExperimentSelector extends JComboBox {

	/**
	 *
	 */
	private static final long serialVersionUID = 8328713697383538804L;

	public ExperimentSelector(Map<String, String> experements) {
		super(experements.keySet().toArray());
		this.setEditable(false);

		this.setMaximumRowCount(5);

	}

}
