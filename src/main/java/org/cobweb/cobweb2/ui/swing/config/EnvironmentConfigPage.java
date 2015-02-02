/**
 *
 */
package org.cobweb.cobweb2.ui.swing.config;

import java.awt.BorderLayout;
import java.awt.GridLayout;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.text.NumberFormat;

import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.SpringLayout;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;

import org.cobweb.cobweb2.SimulationConfig;
import org.cobweb.cobweb2.impl.ComplexAgent;
import org.cobweb.cobweb2.impl.ComplexEnvironment;
import org.cobweb.cobweb2.impl.ComplexEnvironmentParams;
import org.cobweb.cobweb2.impl.learning.ComplexAgentLearning;
import org.cobweb.cobweb2.impl.learning.ComplexEnvironmentLearning;
import org.cobweb.cobweb2.ui.config.FieldPropertyAccessor;
import org.cobweb.cobweb2.ui.config.SetterPropertyAccessor;
import org.cobweb.cobweb2.ui.swing.ConfigRefresher;
import org.cobweb.swingutil.SpringUtilities;
import org.cobweb.swingutil.binding.BoundCheckBox;
import org.cobweb.swingutil.binding.BoundJFormattedTextField;
/**
 * @author Igor
 *
 */
public class EnvironmentConfigPage implements ConfigPage {
	private JPanel thePanel;

	private SimulationConfig params;

	private ConfigRefresher refresher;

	public EnvironmentConfigPage(SimulationConfig theParams, boolean allowKeep, ConfigRefresher refresher) throws NoSuchFieldException, NoSuchMethodException {
		this.params = theParams;
		this.refresher = refresher;
		thePanel = new JPanel(new SpringLayout());

		JPanel panel11 = makeEnvironmentPanel();
		thePanel.add(panel11);

		JPanel panel12 = makeTransitionPanel(allowKeep);
		thePanel.add(panel12);

		JPanel panel14 = makeRandomPanel();

		thePanel.add(panel14);

		SpringUtilities.makeCompactGrid(thePanel, 1, 3, 0, 0, 0, 0, 0, 0);
	}

	private JPanel makeEnvironmentPanel() throws NoSuchFieldException, NoSuchMethodException {
		JPanel panel11 = new JPanel();
		Util.makeGroupPanel(panel11, "Environment Settings");
		JPanel fieldPane = new JPanel();

		BoundJFormattedTextField Width = new BoundJFormattedTextField(params.envParams,
				new FieldPropertyAccessor(ComplexEnvironmentParams.class.getField("width")),
				NumberFormat.getIntegerInstance());
		fieldPane.add(new JLabel(Width.getLabelText()));
		fieldPane.add(Width);

		BoundJFormattedTextField Height = new BoundJFormattedTextField(params.envParams,
				new FieldPropertyAccessor(ComplexEnvironmentParams.class.getField("height")),
				NumberFormat.getIntegerInstance());
		fieldPane.add(new JLabel(Height.getLabelText()));
		fieldPane.add(Height);

		BoundCheckBox wrap = new BoundCheckBox(params.envParams,
				new FieldPropertyAccessor(ComplexEnvironmentParams.class.getField("wrapMap")));
		fieldPane.add(new JLabel(wrap.getLabelText()));
		fieldPane.add(wrap);


		BoundJFormattedTextField AgentNum = new BoundJFormattedTextField(params,
				new SetterPropertyAccessor(SimulationConfig.class.getMethod("setAgentTypes", int.class)),
				NumberFormat.getIntegerInstance());
		AgentNum.addPropertyChangeListener("value", new PropertyChangeListener() {

			@Override
			public void propertyChange(PropertyChangeEvent evt) {
				refresher.refreshConfig();
			}
		});

		fieldPane.add(new JLabel(AgentNum.getLabelText()));
		fieldPane.add(AgentNum);

		final JCheckBox LearningAgents = new JCheckBox();
		fieldPane.add(new JLabel("Learning Agents"));
		fieldPane.add(LearningAgents);
		LearningAgents.addChangeListener(new ChangeListener() {

			@Override
			public void stateChanged(ChangeEvent e) {
				if (LearningAgents.isSelected()) {
					params.agentName = ComplexAgentLearning.class.getName();
					params.environmentName = ComplexEnvironmentLearning.class.getName();
				} else {
					params.agentName = ComplexAgent.class.getName();
					params.environmentName = ComplexEnvironment.class.getName();
				}

				refresher.refreshConfig();
			}
		});
		LearningAgents.setSelected(params.agentName.equals(ComplexAgentLearning.class.getName()));


		panel11.add(fieldPane, BorderLayout.CENTER);

		makeOptionsTable(fieldPane, 5);
		return panel11;
	}

	private JPanel makeTransitionPanel(boolean allowKeep) throws NoSuchFieldException {
		JPanel fieldPane;
		JPanel panel12 = new JPanel();
		Util.makeGroupPanel(panel12, "Environment Transition Settings");

		fieldPane = new JPanel();


		BoundCheckBox keepOldAgents = new BoundCheckBox(params, new FieldPropertyAccessor(SimulationConfig.class.getField("keepOldAgents")));
		fieldPane.add(new JLabel(keepOldAgents.getLabelText()));
		fieldPane.add(keepOldAgents);

		BoundCheckBox spawnNewAgents = new BoundCheckBox(params, new FieldPropertyAccessor(SimulationConfig.class.getField("spawnNewAgents")));
		fieldPane.add(new JLabel(spawnNewAgents.getLabelText()));
		fieldPane.add(spawnNewAgents);

		BoundCheckBox keepOldArray = new BoundCheckBox(params, new FieldPropertyAccessor(SimulationConfig.class.getField("keepOldArray")));
		fieldPane.add(new JLabel(keepOldArray.getLabelText()));
		fieldPane.add(keepOldArray);

		BoundCheckBox keepOldWaste = new BoundCheckBox(params, new FieldPropertyAccessor(SimulationConfig.class.getField("keepOldDrops")));
		fieldPane.add(new JLabel(keepOldWaste.getLabelText()));
		fieldPane.add(keepOldWaste);

		BoundCheckBox keepOldPackets = new BoundCheckBox(params, new FieldPropertyAccessor(SimulationConfig.class.getField("keepOldPackets")));
		fieldPane.add(new JLabel(keepOldPackets.getLabelText()));
		fieldPane.add(keepOldPackets);
		makeOptionsTable(fieldPane, 5);

		panel12.add(fieldPane);


		if (!allowKeep) {
			keepOldAgents.setEnabled(false);
			keepOldArray.setEnabled(false);
			keepOldPackets.setEnabled(false);
			keepOldWaste.setEnabled(false);
			keepOldAgents.setSelected(false);
			keepOldArray.setSelected(false);
			keepOldPackets.setSelected(false);
			keepOldWaste.setSelected(false);
		}

		return panel12;
	}

	private JPanel makeRandomPanel() throws NoSuchFieldException {
		JPanel fieldPane;
		JPanel panel14 = new JPanel();
		Util.makeGroupPanel(panel14, "Random Variables");
		fieldPane = new JPanel(new GridLayout(3, 1));

		BoundJFormattedTextField initialStones = new BoundJFormattedTextField(params.envParams,
				new FieldPropertyAccessor(ComplexEnvironmentParams.class.getField("initialStones")),
				NumberFormat.getIntegerInstance());
		fieldPane.add(new JLabel(initialStones.getLabelText()));
		fieldPane.add(initialStones);

		BoundJFormattedTextField randomSeed = new BoundJFormattedTextField(params, new FieldPropertyAccessor(SimulationConfig.class.getField("randomSeed")), NumberFormat.getIntegerInstance());
		JButton makeRandom = new JButton("Generate");
		makeRandom.addActionListener(new SeedRandomListener(randomSeed));
		fieldPane.add(new JLabel(randomSeed.getLabelText()));
		fieldPane.add(randomSeed);

		fieldPane.add(new JPanel());
		fieldPane.add(makeRandom);

		panel14.add(fieldPane, BorderLayout.EAST);
		makeOptionsTable(fieldPane, 3);
		return panel14;
	}

	/* (non-Javadoc)
	 * @see driver.config.ConfigPage#getPanel()
	 */
	@Override
	public JPanel getPanel() {
		return thePanel;
	}

	private static void makeOptionsTable(JPanel fieldPane, int items) {
		fieldPane.setLayout(new SpringLayout());
		SpringUtilities.makeCompactGrid(fieldPane, items, 2, 0, 0, 16, 0, 50, 0);
	}

	@Override
	public void validateUI() throws IllegalArgumentException {
		// Nothing
	}


}
