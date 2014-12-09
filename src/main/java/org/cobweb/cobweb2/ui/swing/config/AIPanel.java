/**
 *
 */
package org.cobweb.cobweb2.ui.swing.config;

import java.awt.CardLayout;
import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;

import javax.swing.BoxLayout;
import javax.swing.JComboBox;
import javax.swing.JPanel;

import org.cobweb.cobweb2.SimulationConfig;
import org.cobweb.cobweb2.ai.GeneticController;
import org.cobweb.cobweb2.ai.LinearWeightsController;

class AIPanel extends SettingsPanel {

	private static final long serialVersionUID = 6045306756522429063L;

	static final String[] AI_LIST = { GeneticController.class.getName(), LinearWeightsController.class.getName() };

	CardLayout cardSwitch = new CardLayout();
	JPanel inner = new JPanel();

	JComboBox aiSwitch;

	SettingsPanel[] tabs;

	private SimulationConfig parser;

	public AIPanel() {
		// Nothing
	}

	@Override
	public void bindToParser(SimulationConfig p) {
		parser = p;
		setLayout(new BoxLayout(this, BoxLayout.Y_AXIS));
		inner.setLayout(cardSwitch);

		tabs = new SettingsPanel[AI_LIST.length];

		SettingsPanel genPanel = new GeneticAIPanel();
		inner.add(genPanel, AI_LIST[0]);
		tabs[0] = genPanel;

		SettingsPanel lWpanel = new LinearAIPanel();
		inner.add(lWpanel, AI_LIST[1]);
		tabs[1] = lWpanel;

		aiSwitch = new JComboBox(AI_LIST);
		aiSwitch.setEditable(false);
		aiSwitch.addItemListener(new ItemListener() {
			@Override
			public void itemStateChanged(ItemEvent e) {
				cardSwitch.show(inner, (String) e.getItem());
				tabs[aiSwitch.getSelectedIndex()].bindToParser(parser);
			}
		});

		add(aiSwitch);
		add(inner);

		aiSwitch.setSelectedItem(p.getEnvParams().controllerName);
		tabs[aiSwitch.getSelectedIndex()].bindToParser(parser);
	}

}