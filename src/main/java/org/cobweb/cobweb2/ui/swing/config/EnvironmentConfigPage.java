/**
 *
 */
package org.cobweb.cobweb2.ui.swing.config;

import java.awt.BorderLayout;
import java.awt.GridLayout;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.text.NumberFormat;
import java.util.LinkedList;
import java.util.List;

import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.SpringLayout;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;

import org.cobweb.cobweb2.core.ComplexAgent;
import org.cobweb.cobweb2.core.ComplexEnvironment;
import org.cobweb.cobweb2.core.params.ComplexEnvironmentParams;
import org.cobweb.cobweb2.eventlearning.ComplexAgentLearning;
import org.cobweb.cobweb2.eventlearning.ComplexEnvironmentLearning;
import org.cobweb.swingutil.SpringUtilities;
import org.cobweb.swingutil.binding.BoundCheckBox;
import org.cobweb.swingutil.binding.BoundJFormattedTextField;
import org.cobweb.swingutil.binding.LabeledJFormattedTextField;
/**
 * @author Igor
 *
 */
public class EnvironmentConfigPage implements ConfigPage {

	public BoundJFormattedTextField Width;
	public BoundJFormattedTextField Height;
	public BoundCheckBox wrap;

	public BoundCheckBox keepOldAgents;
	public BoundCheckBox spawnNewAgents;
	public BoundCheckBox keepOldArray;
	public BoundCheckBox dropNewFood;
	public BoundCheckBox keepOldWaste;
	public BoundCheckBox keepOldPackets;
	public BoundCheckBox PrisDilemma;
	public JCheckBox LearningAgents;
	public BoundJFormattedTextField randomSeed;
	public BoundJFormattedTextField initialStones;
	public BoundJFormattedTextField maxFoodChance;

	JPanel thePanel;

	ComplexEnvironmentParams params;

	private int oldAgentNum;

	private List<AgentNumChangeListener> numChangeListeners = new LinkedList<AgentNumChangeListener>();

	public void addAgentNumChangeListener(AgentNumChangeListener listener) {
		numChangeListeners.add(listener);
	}

	public void removeAgentNumChangeListener(AgentNumChangeListener listener) {
		numChangeListeners.remove(listener);
	}

	/**
	 *
	 */
	public EnvironmentConfigPage(ComplexEnvironmentParams theParams, boolean allowKeep) {
		this.params = theParams;
		thePanel = new JPanel(new SpringLayout());

		/* Environment Settings */
		JPanel panel11 = new JPanel();
		GUI.makeGroupPanel(panel11, "Environment Settings");
		JPanel fieldPane = new JPanel();

		Width = new BoundJFormattedTextField(params, "width", NumberFormat.getIntegerInstance());
		fieldPane.add(new JLabel(Width.getLabelText()));
		fieldPane.add(Width);

		Height = new BoundJFormattedTextField(params, "height", NumberFormat.getIntegerInstance());
		fieldPane.add(new JLabel(Height.getLabelText()));
		fieldPane.add(Height);

		wrap = new BoundCheckBox(params, "wrapMap");
		fieldPane.add(new JLabel(wrap.getLabelText()));
		fieldPane.add(wrap);


		oldAgentNum = params.agentTypeCount;

		LabeledJFormattedTextField AgentNum = new LabeledJFormattedTextField(params, "agentTypeCount", NumberFormat.getIntegerInstance());
		AgentNum.setValue(theParams.agentTypeCount);
		AgentNum.addPropertyChangeListener("value", new PropertyChangeListener() {

			@Override
			public void propertyChange(PropertyChangeEvent evt) {
				Object nv = evt.getNewValue();
				if (!(nv instanceof Long)) {
					return;
				}

				int newAgentNum = ((Long)evt.getNewValue()).intValue();

				if (newAgentNum == oldAgentNum)
					return;

				for (AgentNumChangeListener x : numChangeListeners) {
					x.AgentNumChanged(oldAgentNum, newAgentNum);
				}
			}
		});

		fieldPane.add(new JLabel(AgentNum.getLabelText()));
		fieldPane.add(AgentNum);



		PrisDilemma = new BoundCheckBox(params, "prisDilemma");
		fieldPane.add(new JLabel(PrisDilemma.getLabelText()));
		fieldPane.add(PrisDilemma);
		PrisDilemma.addChangeListener(new ChangeListener() {

			@Override
			public void stateChanged(ChangeEvent e) {
				for (AgentNumChangeListener x : numChangeListeners) {
					x.AgentNumChanged(oldAgentNum, params.agentTypeCount);
				}
			}
		});

		LearningAgents = new JCheckBox();
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

				for (AgentNumChangeListener x : numChangeListeners) {
					x.AgentNumChanged(oldAgentNum, params.agentTypeCount);
				}
			}
		});
		LearningAgents.setSelected(params.agentName.equals(ComplexAgentLearning.class.getName()));


		panel11.add(fieldPane, BorderLayout.CENTER);

		makeOptionsTable(fieldPane, 6);

		thePanel.add(panel11);

		/* Colour Settings */
		JPanel panel12 = new JPanel();
		GUI.makeGroupPanel(panel12, "Environment Transition Settings");

		fieldPane = new JPanel();


		keepOldAgents = new BoundCheckBox(params, "keepOldAgents");
		fieldPane.add(new JLabel(keepOldAgents.getLabelText()));
		fieldPane.add(keepOldAgents);

		spawnNewAgents = new BoundCheckBox(params, "spawnNewAgents");
		fieldPane.add(new JLabel(spawnNewAgents.getLabelText()));
		fieldPane.add(spawnNewAgents);

		keepOldArray = new BoundCheckBox(params, "keepOldArray");
		fieldPane.add(new JLabel(keepOldArray.getLabelText()));
		fieldPane.add(keepOldArray);

		keepOldWaste = new BoundCheckBox(params, "keepOldWaste");
		fieldPane.add(new JLabel(keepOldWaste.getLabelText()));
		fieldPane.add(keepOldWaste);

		keepOldPackets = new BoundCheckBox(params, "keepOldPackets");
		fieldPane.add(new JLabel(keepOldPackets.getLabelText()));
		fieldPane.add(keepOldPackets);
		makeOptionsTable(fieldPane, 5);

		panel12.add(fieldPane);
		thePanel.add(panel12);


		/* Random variables */
		JPanel panel14 = new JPanel();
		GUI.makeGroupPanel(panel14, "Random Variables");
		fieldPane = new JPanel(new GridLayout(3, 1));

		initialStones = new BoundJFormattedTextField(params, "initialStones", NumberFormat.getIntegerInstance());
		fieldPane.add(new JLabel(initialStones.getLabelText()));
		fieldPane.add(initialStones);

		randomSeed = new BoundJFormattedTextField(params, "randomSeed", NumberFormat.getIntegerInstance());
		JButton makeRandom = new JButton("Generate");
		makeRandom.addActionListener(new SeedRandomListener(randomSeed));
		fieldPane.add(new JLabel(randomSeed.getLabelText()));
		fieldPane.add(randomSeed);

		fieldPane.add(new JPanel());
		fieldPane.add(makeRandom);

		panel14.add(fieldPane, BorderLayout.EAST);
		makeOptionsTable(fieldPane, 3);

		thePanel.add(panel14);

		JPanel panel16 = new JPanel();
		GUI.makeGroupPanel(panel16, "General Food Variables");

		fieldPane = new JPanel(new GridLayout(2, 1));

		dropNewFood = new BoundCheckBox(params, "dropNewFood");
		fieldPane.add(new JLabel(dropNewFood.getLabelText()));
		fieldPane.add(dropNewFood);

		maxFoodChance = new BoundJFormattedTextField(params, "likeFoodProb", NumberFormat.getInstance());
		fieldPane.add(new JLabel(maxFoodChance.getLabelText()));
		fieldPane.add(maxFoodChance);

		panel16.add(fieldPane, BorderLayout.WEST);
		makeOptionsTable(fieldPane, 2);

		thePanel.add(panel16);

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

		SpringUtilities.makeCompactGrid(thePanel, 2, 2, 0, 0, 0, 0, 0, 0);
	}

	/* (non-Javadoc)
	 * @see driver.config.ConfigPage#getPanel()
	 */
	public JPanel getPanel() {
		return thePanel;
	}

	private void makeOptionsTable(JPanel fieldPane, int items) {
		fieldPane.setLayout(new SpringLayout());
		SpringUtilities.makeCompactGrid(fieldPane, items, 2, 0, 0, 16, 0, 50, 0);
	}

	public void validateUI() throws IllegalArgumentException {
		// Nothing
	}


}
