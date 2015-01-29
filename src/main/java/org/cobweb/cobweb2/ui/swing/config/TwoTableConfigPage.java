package org.cobweb.cobweb2.ui.swing.config;

import java.awt.BorderLayout;
import java.awt.Dimension;
import java.lang.reflect.Array;

import javax.swing.JPanel;

import org.cobweb.cobweb2.plugins.PerAgentParams;
import org.cobweb.io.ParameterSerializable;
import org.cobweb.swingutil.ColorLookup;

/**
 * Config page for PerAgentParams<TperType> parameters that also have global properties
 * @param <Tmain> type of param container
 * @param <TperType> type of per-agent-type param object
 */
public class TwoTableConfigPage<Tmain extends PerAgentParams<TperType>, TperType extends ParameterSerializable> implements ConfigPage {

	private final TableConfigPage<Tmain> mainPage;
	private final TableConfigPage<TperType> perTypePage;
	private final JPanel myPanel;

	public TwoTableConfigPage(Class<Tmain> paramClass, Tmain params, String mainName, ColorLookup colors) {
		@SuppressWarnings("unchecked")
		Tmain[] mainArray = (Tmain[]) Array.newInstance(paramClass, 1);
		mainArray[0] = params;
		mainPage = new TableConfigPage<>(mainArray, mainName, "Value");
		perTypePage = new TableConfigPage<>(params.agentParams, "Agent Preferences", colors);

		JPanel mainPanel = mainPage.getPanel();
		mainPanel.setPreferredSize(new Dimension(200, 200));

		JPanel perTypePanel = perTypePage.getPanel();

		myPanel = new JPanel(new BorderLayout());
		myPanel.add(mainPanel, BorderLayout.NORTH);
		myPanel.add(perTypePanel);
	}

	@Override
	public JPanel getPanel() {
		return myPanel;
	}

	@Override
	public void validateUI() throws IllegalArgumentException {
		mainPage.validateUI();
		perTypePage.validateUI();
	}

}
