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
import org.cobweb.swingutil.ColorLookup;

class AIPanel extends SettingsPanel {

	private static final long serialVersionUID = 6045306756522429063L;

	static final String[] AI_LIST = { GeneticController.class.getSimpleName(), LinearWeightsController.class.getSimpleName() };

	private CardLayout cardSwitch = new CardLayout();
	private JPanel inner = new JPanel();

	private SettingsPanel[] tabs;

	private SimulationConfig parser;

	private ColorLookup agentColors;

	public AIPanel(ColorLookup agentColors) {
		this.agentColors = agentColors;

		setLayout(new BoxLayout(this, BoxLayout.Y_AXIS));
		inner.setLayout(cardSwitch);

		tabs = new SettingsPanel[AI_LIST.length];
	}

	@Override
	public void bindToParser(SimulationConfig p) {
		parser = p;

		SettingsPanel genPanel = new GeneticAIPanel(agentColors);
		inner.add(genPanel, AI_LIST[0]);
		tabs[0] = genPanel;

		SettingsPanel lWpanel = new LinearAIPanel();
		inner.add(lWpanel, AI_LIST[1]);
		tabs[1] = lWpanel;

		final JComboBox aiSwitch = new JComboBox(AI_LIST);
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