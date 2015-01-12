package org.cobweb.cobweb2.ui.swing.config;

import java.awt.BorderLayout;
import java.awt.Dialog;
import java.awt.Dimension;
import java.awt.FileDialog;
import java.awt.FlowLayout;
import java.awt.Window;
import java.awt.event.ActionListener;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JComponent;
import javax.swing.JDialog;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JTabbedPane;

import org.cobweb.cobweb2.SimulationConfig;
import org.cobweb.cobweb2.eventlearning.ComplexAgentLearning;
import org.cobweb.cobweb2.ui.UserInputException;
import org.cobweb.cobweb2.ui.swing.CobwebApplication;
import org.cobweb.cobweb2.ui.swing.DisplaySettings;

/**
 * Simulation configuration dialog
 *
 * @author time itself
 *
 */
public class SimulationConfigEditor {

	private final class OkButtonListener implements ActionListener {
		@Override
		public void actionPerformed(java.awt.event.ActionEvent evt) {

			validateSettings();

			/* write UI info to xml file */
			try {
				p.write(new FileOutputStream(datafile));
			} catch (java.io.IOException ex) {
				throw new UserInputException("Cannot write file! Make sure your file is not read-only.", ex);
			}

			/* create a new parser for the xml file */
			try {
				p = new SimulationConfig(datafile);
			} catch (FileNotFoundException ex) {
				throw new UserInputException("Cannot open file!", ex);
			}

			closeEditor();
		}


	}

	private void validateSettings() {
		try {
			environmentPage.validateUI();
			resourcePage.validateUI();
			agentPage.validateUI();
			foodwebPage.validateUI();
			if (pdPage != null)
				pdPage.validateUI();
			geneticPage.validateUI();
			diseaseConfigPage.validateUI();
			tempPage.validateUI();
			if (learnPage != null)
				learnPage.validateUI();
		} catch (IllegalArgumentException ex) {
			throw new UserInputException("Parameter error: " + ex.getMessage(), ex);
		}
	}

	private final class SaveAsButtonListener implements java.awt.event.ActionListener {
		@Override
		public void actionPerformed(java.awt.event.ActionEvent e) {
			validateSettings();

			if (saveAsDialog()) {
				closeEditor();
			}
		}
	}

	private static final String WINDOW_TITLE = "Simulation Settings";



	// //////////////////////////////////////////////////////////////////////////////////////////////

	private EnvironmentConfigPage environmentPage;

	private ResourceConfigPage resourcePage;

	private GeneticConfigPage geneticPage;

	private final JTabbedPane tabbedPane;

	private final JDialog dialog;

	private SimulationConfig p;

	private boolean modifyExisting;

	private boolean ok;

	private String datafile;

	private SettingsPanel controllerPanel;
	public static final long serialVersionUID = 0xB9967684A8375BC0L;
	/**
	 * Create the SimulationConfigEditor and show it. For thread safety, this method should be invoked from the event-dispatching thread.
	 */
	public static SimulationConfigEditor show(Window parent, String filename, boolean allowModify, DisplaySettings displaySettings) {
		// Create and set up the content pane.
		SimulationConfigEditor configEditor = new SimulationConfigEditor(parent, filename, allowModify, displaySettings);
		configEditor.show();
		return configEditor;
	}

	private void show() {
		dialog.setVisible(true);
	}

	private Logger myLogger = Logger.getLogger("COBWEB2");

	private DiseaseConfigPage diseaseConfigPage;

	private TemperatureConfigPage tempPage;

	private AgentConfigPage agentPage;

	private ProductionConfigPage prodPage;

	private FoodwebConfigPage foodwebPage;

	private PDConfigPage pdPage;

	private LearningConfigPage learnPage;



	private DisplaySettings displaySettings;

	// SimulationConfigEditor Special Constructor
	public SimulationConfigEditor(Window parent, String filename, boolean allowModify, DisplaySettings dispSettings) {
		this.displaySettings = dispSettings;
		dialog = new JDialog(parent, WINDOW_TITLE, Dialog.DEFAULT_MODALITY_TYPE);

		JPanel j = new JPanel();
		j.setLayout(new BoxLayout(j, BoxLayout.Y_AXIS));

		datafile = filename;
		tabbedPane = new JTabbedPane();
		modifyExisting = allowModify;

		/* Environment panel - composed of 4 panels */

		File f = new File(datafile);

		if (f.exists()) {
			try {
				p = new SimulationConfig(datafile);
			} catch (Exception ex) {
				myLogger.log(Level.WARNING, "Cannot open config file", ex);
				setDefault();
			}
		} else {
			setDefault();
		}

		environmentPage = new EnvironmentConfigPage(p.getEnvParams(), allowModify);

		tabbedPane.addTab("Environment", environmentPage.getPanel());

		setupConfigPages();

		environmentPage.addAgentNumChangeListener(new AgentNumChangeListener() {

			@Override
			public void AgentNumChanged(int oldNum, int newNum) {
				p.SetAgentTypeCount(newNum);
				setupConfigPages();
			}
		});

		JButton okButton = new JButton("OK");
		okButton.setMaximumSize(new Dimension(80, 20));
		okButton.addActionListener(new OkButtonListener());

		JButton saveButton = new JButton("Save As...");
		saveButton.setMaximumSize(new Dimension(80, 20));
		saveButton.addActionListener(new SaveAsButtonListener());

		JPanel buttons = new JPanel();
		buttons.setLayout(new FlowLayout(FlowLayout.RIGHT));
		buttons.add(saveButton);
		buttons.add(okButton);

		// Add the tabbed pane to this panel.
		j.add(tabbedPane, BorderLayout.CENTER);
		j.add(buttons, BorderLayout.SOUTH);
		j.setPreferredSize(new Dimension(750, 513));

		dialog.getRootPane().setDefaultButton(okButton);
		dialog.add(j);
		dialog.pack();
		File filePath;
		if (p.getFilename() == null ||
				(filePath = new File(p.getFilename())).getName() == null)
			filePath = new File(CobwebApplication.DEFAULT_DATA_FILE_NAME + CobwebApplication.CONFIG_FILE_EXTENSION);
		dialog.setTitle(WINDOW_TITLE + " - " + filePath.getName());
	}

	public SimulationConfig getConfig() {
		return p;
	}

	public boolean isContinuation() {
		return modifyExisting && p.isContinuation();
	}

	public boolean isOK() {
		return ok;
	}

	/**
	 * This openFileDialog method is invoked by pressing the "Save" button
	 */
	public boolean saveAsDialog() {
		FileDialog theDialog = new FileDialog(dialog, "Choose a file to save state to", java.awt.FileDialog.SAVE);
		theDialog.setFile("*.xml");
		theDialog.setVisible(true);
		if (theDialog.getFile() != null) {
			try {
				// Handle a readonly file
				String savingFile = theDialog.getDirectory() + theDialog.getFile();
				File sf = new File(savingFile);
				if (sf.isHidden() || (sf.exists() && !sf.canWrite())) {
					JOptionPane.showMessageDialog(
							dialog,
							"Caution:  File \"" + savingFile + "\" is NOT allowed to be written to.", "Warning",
							JOptionPane.WARNING_MESSAGE);
				} else {
					FileOutputStream configStream = new FileOutputStream(theDialog.getDirectory() + theDialog.getFile());
					p.write(configStream);
					configStream.close();

					p = new SimulationConfig(theDialog.getDirectory() + theDialog.getFile());

					return true;
				}
			} catch (IOException ex) {
				myLogger.log(Level.WARNING, "Cannot save config", ex);
				JOptionPane.showMessageDialog(dialog,
						"Save failed: " + ex.getMessage(), "Warning", JOptionPane.WARNING_MESSAGE);
			}
		}
		return false;
	}

	private void setDefault() {
		p = new SimulationConfig();
	}

	private void setupConfigPages() {

		/* Resources panel */
		removeOldPage(resourcePage);
		resourcePage = new ResourceConfigPage(p.getFoodParams(), displaySettings.agentColor);
		tabbedPane.addTab("Resources", resourcePage.getPanel());

		/* Agents' panel */
		removeOldPage(agentPage);
		agentPage = new AgentConfigPage(p.getAgentParams(), displaySettings.agentColor);
		tabbedPane.addTab("Agents", agentPage.getPanel());

		/* Production panel */
		removeOldPage(prodPage);
		prodPage = new ProductionConfigPage(p.getProdParams(), displaySettings.agentColor);
		tabbedPane.addTab("Production", prodPage.getPanel());

		removeOldPage(foodwebPage);
		foodwebPage = new FoodwebConfigPage(p.getAgentParams(), displaySettings.agentColor);
		tabbedPane.addTab("Food Web", foodwebPage.getPanel());

		removeOldPage(pdPage);
		if (p.getEnvParams().prisDilemma) {
			pdPage = new PDConfigPage(p.getEnvParams().pdParams);
			tabbedPane.addTab("PD Options", pdPage.getPanel());
		}

		removeOldPage(geneticPage);
		geneticPage = new GeneticConfigPage(p.getGeneticParams(), p.getEnvParams().getAgentTypes(), displaySettings.agentColor);
		JComponent panelGA = geneticPage.getPanel();
		tabbedPane.addTab("Genetics", panelGA);

		if (controllerPanel != null) {
			tabbedPane.remove(controllerPanel);
		}
		controllerPanel = new AIPanel(displaySettings.agentColor);
		controllerPanel.bindToParser(p);
		tabbedPane.addTab("AI", controllerPanel);

		removeOldPage(diseaseConfigPage);
		diseaseConfigPage = new DiseaseConfigPage(p.getDiseaseParams(), displaySettings.agentColor);
		tabbedPane.addTab("Disease", diseaseConfigPage.getPanel());

		removeOldPage(tempPage);
		tempPage = new TemperatureConfigPage(p.getTempParams(), displaySettings.agentColor);
		tabbedPane.addTab("Abiotic Factor", tempPage.getPanel());


		removeOldPage(learnPage);
		if (p.getEnvParams().agentName.equals(ComplexAgentLearning.class.getName())) {
			learnPage = new LearningConfigPage(p.getLearningParams().getLearningAgentParams(), displaySettings.agentColor);
			tabbedPane.addTab("Learning", learnPage.getPanel());
		}
	}

	private void removeOldPage(ConfigPage r) {
		if (r != null) {
			tabbedPane.remove(r.getPanel());
		}
	}

	private void closeEditor() {
		ok = true;
		dialog.setVisible(false);
		dialog.dispose();
	}

}
