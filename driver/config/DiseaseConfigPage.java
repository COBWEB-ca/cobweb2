/**
 * 
 */
package driver.config;

import java.awt.BorderLayout;

import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTable;

import cwcore.complexParams.AgentFoodCountable;

import disease.DiseaseParams;

/**
 *
 */
public class DiseaseConfigPage implements ConfigPage {

	JPanel myPanel;
	
	DiseaseParams[] params;

	public DiseaseConfigPage(DiseaseParams[] params, AgentFoodCountable env) {
		this.params = params;
		
		ConfigTableModel ctm = new ConfigTableModel(params, "Agent");
		JTable tab = new MixedValueJTable();
		tab.setRowHeight(20);
		tab.setModel(ctm);
		JScrollPane sp = new JScrollPane(tab);
		
		GUI.makeGroupPanel(sp, "Disease Parameters");
		GUI.colorHeaders(tab, true);
		tab.getColumnModel().getColumn(0).setPreferredWidth(150);
		
		myPanel = new JPanel(new BorderLayout());
		myPanel.add(sp);
		
	}
	
	public JPanel getPanel() {
		return myPanel;
	}

	public void validateUI() throws IllegalArgumentException {
	}

}
