/**
 *
 */
package org.cobweb.cobweb2.ui.swing.config;

import java.awt.CardLayout;
import java.awt.Dialog;
import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;

import javax.swing.BoxLayout;
import javax.swing.JComboBox;
import javax.swing.JPanel;

import org.cobweb.cobweb2.SimulationConfig;
import org.cobweb.cobweb2.impl.ai.GeneticController;
import org.cobweb.cobweb2.impl.ai.LinearWeightsController;
import org.cobweb.swingutil.ColorLookup;

public class AIPanel extends SettingsPanel {

	private static final long serialVersionUID = 6045306756522429063L;

	private static final String[] AI_LIST = { GeneticController.class.getSimpleName(), LinearWeightsController.class.getSimpleName() };

	private CardLayout cardSwitch = new CardLayout();
	private JPanel inner = new JPanel();

	private SettingsPanel[] tabs;

	private SimulationConfig parser;

	private ColorLookup agentColors;

	private Dialog parentWindow;

	public AIPanel(ColorLookup agentColors, Dialog parentWindow) {
		this.agentColors = agentColors;
		this.parentWindow = parentWindow;

		setLayout(new BoxLayout(this, BoxLayout.Y_AXIS));
		inner.setLayout(cardSwitch);

		tabs = new SettingsPanel[AI_LIST.length];
	}

	@Override
	public void bindToParser(SimulationConfig p) {
		parser = p;

		SettingsPanel genPanel = new GeneticAIPanel(agentColors, parentWindow);
		inner.add(genPanel, AI_LIST[0]);
		tabs[0] = genPanel;

		SettingsPanel lWpanel = new LinearAIPanel(agentColors);
		inner.add(lWpanel, AI_LIST[1]);
		tabs[1] = lWpanel;

		final JComboBox<String> aiSwitch = new JComboBox<String>(AI_LIST);
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

		aiSwitch.setSelectedItem(getSimpleName(p.getControllerName()));
		tabs[aiSwitch.getSelectedIndex()].bindToParser(parser);
	}

	private static String getSimpleName(String qualifiedName) {
		int lastDot = qualifiedName.lastIndexOf('.');
		String name = qualifiedName.substring(lastDot + 1);
		return name;
	}

}
