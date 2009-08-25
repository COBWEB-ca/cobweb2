/**
 *
 */
package driver.config;

import java.awt.BorderLayout;
import java.awt.GridLayout;
import java.text.NumberFormat;

import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.SpringLayout;

import cwcore.complexParams.ComplexEnvironmentParams;
import driver.SpringUtilities;
/**
 * @author Igor
 *
 */
public class EnvironmentConfigPage implements ConfigPage {



	/**
	 *
	 */
	private static final long serialVersionUID = 7042537975378723377L;

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
	public BoundJFormattedTextField randomSeed;
	public BoundJFormattedTextField initialStones;
	public BoundJFormattedTextField memory_size;
	public BoundCheckBox flexibility;
	public BoundJFormattedTextField numColor;
	public BoundJFormattedTextField colorSelectSize;
	public BoundJFormattedTextField reColorTimeStep;
	public BoundJFormattedTextField colorizerMode;
	public BoundJFormattedTextField maxFoodChance;

	JPanel thePanel;

	ComplexEnvironmentParams params;

	/**
	 *
	 */
	public EnvironmentConfigPage(ComplexEnvironmentParams theParams, boolean allowKeep) {
		this.params = theParams;
		thePanel = new JPanel(new SpringLayout());

		/* Environment Settings */
		JPanel panel11 = new JPanel();
		GUI.makeGroupPanel(panel11, "Grid Settings");
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

		panel11.add(fieldPane, BorderLayout.CENTER);

		makeOptionsTable(fieldPane, 3);

		thePanel.add(panel11);

		JPanel panel15 = new JPanel();
		GUI.makeGroupPanel(panel15, "Prisoner's Dilemma Options");

		fieldPane = new JPanel(new GridLayout(3, 1));

		PrisDilemma = new BoundCheckBox(params, "prisDilemma");
		fieldPane.add(new JLabel(PrisDilemma.getLabelText()));
		fieldPane.add(PrisDilemma);

		memory_size = new BoundJFormattedTextField(params, "pdMemorySize", NumberFormat.getIntegerInstance());
		fieldPane.add(new JLabel(memory_size.getLabelText()));
		fieldPane.add(memory_size);

		makeOptionsTable(fieldPane, 2);

		panel15.add(fieldPane);
		thePanel.add(panel15, "WEST");

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

		/* Options */
		JPanel panel13 = new JPanel();
		String title = "Colour Settings";
		GUI.makeGroupPanel(panel13, title);

		fieldPane = new JPanel(new GridLayout(1, 1));

//		fieldPane.add(new JLabel("No. of Colors"));
//		fieldPane.add(numColor);
//		fieldPane.add(new JLabel("Color Select Size"));
//		fieldPane.add(colorSelectSize);
//		fieldPane.add(new JLabel("Recolor Time Step"));
//		fieldPane.add(reColorTimeStep);
//		fieldPane.add(new JLabel("Colorizer Mode"));
//		fieldPane.add(colorizerMode);
//		fieldPane.add(new JLabel("Color Coded Agents"));
//		fieldPane.add(ColorCodedAgents);

		fieldPane.add(new JLabel("Not currently used"));
		panel13.add(fieldPane);
		//makeOptionsTable(fieldPane, 5);

		thePanel.add(panel13);

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

		panel16.add(fieldPane, BorderLayout.EAST);
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

		SpringUtilities.makeCompactGrid(thePanel, 3, 2, 0, 0, 0, 0, 0, 0);
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
