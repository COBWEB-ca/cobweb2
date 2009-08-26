/**
 *
 */
package driver.applet;

import java.util.Map;

import javax.swing.JComboBox;

/**
 * @author igor
 *
 */
public class ExperementSelector extends JComboBox {

	/**
	 *
	 */
	private static final long serialVersionUID = 8328713697383538804L;

	Map <String, String> experements;

	public ExperementSelector(Map<String, String> experements) {
		super(experements.keySet().toArray());
		this.experements = experements;
		this.setEditable(false);

		this.setMaximumRowCount(5);

	}

}
