/**
 * 
 */
package driver.config;

import java.text.NumberFormat;

import javax.swing.JButton;
import javax.swing.JLabel;

import cwcore.GeneticController;
import cwcore.GeneticControllerParams;
import cwcore.complexParams.ComplexEnvironmentParams;
import driver.Parser;
import driver.SettingsPanel;

final class GeneticAIPanel extends SettingsPanel {
	private static final long serialVersionUID = 1139521733160862828L;
	private GeneticControllerParams params;
	private BoundJFormattedTextField seed;

	@Override
	public void bindToParser(Parser p) {
		ComplexEnvironmentParams ep = p.getEnvParams();
		if (!(ep.controllerParams instanceof GeneticControllerParams)) {
			p.getEnvParams().controllerName = GeneticController.class.getName();
			if (params == null)
				params = new GeneticControllerParams();

			p.getEnvParams().controllerParams = params;

		} else {
			params = (GeneticControllerParams) ep.controllerParams;
		}
		updateBoxes();
	}

	private void updateBoxes() {
		seed = new BoundJFormattedTextField(params, "randomSeed", NumberFormat
				.getIntegerInstance());
		this.removeAll();
		seed.setColumns(5);
		this.add(new JLabel(seed.getLabelText()));
		this.add(seed);
		JButton makeRandom = new JButton("Generate");
		makeRandom.addActionListener(new SeedRandomListener(seed));
		add(makeRandom);
	}
}