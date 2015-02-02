package org.cobweb.cobweb2.ui.swing;

import java.awt.BorderLayout;
import java.awt.Component;
import java.awt.FileDialog;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.io.File;
import java.io.FileOutputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.swing.AbstractAction;
import javax.swing.Action;
import javax.swing.ButtonGroup;
import javax.swing.JCheckBoxMenuItem;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JMenu;
import javax.swing.JMenuBar;
import javax.swing.JMenuItem;
import javax.swing.JOptionPane;
import javax.swing.JRadioButton;
import javax.swing.JSeparator;
import javax.swing.JTextField;
import javax.swing.WindowConstants;

import org.cobweb.cobweb2.Simulation;
import org.cobweb.cobweb2.SimulationConfig;
import org.cobweb.cobweb2.impl.ai.LinearWeightsController;
import org.cobweb.cobweb2.impl.ai.LinearWeightsControllerParams;
import org.cobweb.cobweb2.io.Cobweb2Serializer;
import org.cobweb.cobweb2.ui.PopulationSampler;
import org.cobweb.cobweb2.ui.SimulationInterface;
import org.cobweb.cobweb2.ui.ThreadSimulationRunner;
import org.cobweb.cobweb2.ui.UserInputException;
import org.cobweb.cobweb2.ui.ViewerClosedCallback;
import org.cobweb.cobweb2.ui.ViewerPlugin;
import org.cobweb.cobweb2.ui.swing.ai.LinearAIViewer;
import org.cobweb.cobweb2.ui.swing.config.DisplaySettings;
import org.cobweb.cobweb2.ui.swing.energy.EnergyEventViewer;
import org.cobweb.cobweb2.ui.swing.genetics.GAChartOutput;
import org.cobweb.cobweb2.ui.swing.production.ProductionViewer;
import org.cobweb.util.FileUtils;

/**
 * This class consists of methods to allow the user to use the Cobweb simulation
 * tool.  It implements all necessary methods defined by the UIClient class, and
 * makes use of the JFrame class.
 */
public class CobwebApplication extends JFrame {

	private static final String WINDOW_TITLE = "COBWEB 2";

	/** Filename of current simulation config */
	private String currentFile;

	private SimulatorUI simulatorUI;

	private JMenu foodMenu;

	private JMenu agentMenu;

	public static final String CONFIG_FILE_EXTENSION = ".xml";

	private static final String TEMPORARY_FILE_EXTENSION = ".cwtemp";

	static final String INITIAL_OR_NEW_INPUT_FILE_NAME = "initial_or_new_input_(reserved)" + CONFIG_FILE_EXTENSION;

	public static final String DEFAULT_DATA_FILE_NAME = "default_data_(reserved)";

	private static final String CURRENT_DATA_FILE_NAME = "current_data_(reserved)" + TEMPORARY_FILE_EXTENSION;

	public ThreadSimulationRunner simRunner = new ThreadSimulationRunner(new Simulation());

	private final Logger myLogger = Logger.getLogger("COBWEB2");

	private DisplaySettings displaySettings = new DisplaySettings();

	// constructor
	public CobwebApplication() {
		super(WINDOW_TITLE);
		setLayout(new BorderLayout());

		myLogger.info("Welcome to COBWEB 2");
		myLogger.info("JVM Memory: " + Runtime.getRuntime().maxMemory() / 1024 + "KB");

		setDefaultCloseOperation(WindowConstants.EXIT_ON_CLOSE);
		addWindowListener(new WindowAdapter() {
			@Override
			public void windowClosing(WindowEvent e) {
				quitApplication();
			}
		});

		setLayout(new BorderLayout());
		setSize(580, 650);

		JMenuBar myMenuBar = makeMenuBar();
		setJMenuBar(myMenuBar);

		// Center window on screen
		setLocationRelativeTo(null);

		openFile(new SimulationConfig());

		simulatorUI = new SimulatorUI(simRunner, displaySettings);
		add(simulatorUI, BorderLayout.CENTER);

		setVisible(true);
	}

	/**
	 * Opens an initial simulation settings file using the simulation settings
	 * window.  The user can modify the simulation settings and save the
	 * settings to a new file.  The method is invoked when the user selects
	 * "Create New Data" located under "File" in the main tool bar.
	 */
	private void createNewData() {
		String newInput = INITIAL_OR_NEW_INPUT_FILE_NAME;
		SimulationConfigEditor editor = SimulationConfigEditor.show(this, newInput, false, displaySettings);
		if (editor.isOK()) {
			openFile(editor.getConfig(), editor.isContinuation());
		}
	}

	public SimulationInterface getSimulation() {
		return simRunner.getSimulation();
	}

	/**
	 * Allows the user to select the log file to write to.
	 */
	private void logFileDialog() {
		FileDialog theDialog = new FileDialog(this,
				"Choose a file to save log to", FileDialog.SAVE);
		theDialog.setVisible(true);
		if (theDialog.getFile() != null) {
			try {
				simRunner.setLog(new FileWriter(theDialog.getDirectory() + theDialog.getFile(), false));
			} catch (IOException ex) {
				throw new UserInputException("Can't create log file!", ex);
			}
		}
	}

	/**
	 * Creates the main menu bar, which contains all options to allow the user
	 * to modify the simulation, save the simulation, etc.
	 *
	 * @return The menu bar object.
	 * @see #makeAgentFoodSelectMenu()
	 * @see #makeViewMenu()
	 */
	private JMenuBar makeMenuBar() {

		JMenu fileMenu = new JMenu("File");
		fileMenu.add(new JMenuItem(openSimulation));
		fileMenu.add(new JMenuItem(createNewData));
		fileMenu.add(new JMenuItem(modifySimulationFile));
		fileMenu.add(new JMenuItem(retrieveDefaultData));
		fileMenu.add(new JMenuItem(modifySimulation));
		fileMenu.add(new JSeparator());
		fileMenu.add(new JMenuItem(setDefaultData));

		fileMenu.add(new JSeparator());
		fileMenu.add(new JMenuItem(savePopulation));
		fileMenu.add(new JMenuItem(loadPopulation));

		fileMenu.add(new JSeparator());
		fileMenu.add(new JMenuItem(saveConfig));
		fileMenu.add(new JMenuItem(report));
		fileMenu.add(new JMenuItem(setLog));
		fileMenu.add(new JSeparator());
		fileMenu.add(new JMenuItem(quit));

		JMenu editMenu = new JMenu("Edit");
		// Sub-menus created dynamically in #makeAgentFoodSelectMenu()
		agentMenu = new JMenu("Select Agents");
		foodMenu = new JMenu("Select Food");
		editMenu.add(new JMenuItem(setObservationMode));
		editMenu.add(new JMenuItem(setModeStones));
		editMenu.add(agentMenu);
		editMenu.add(foodMenu);
		editMenu.add(new JSeparator());
		editMenu.add(new JMenuItem(removeAllStones));
		editMenu.add(new JMenuItem(removeAllFood));
		editMenu.add(new JMenuItem(removeAllAgents));
		editMenu.add(new JMenuItem(removeAllWaste));
		editMenu.add(new JMenuItem(removeAll));

		// Sub-menus created dynamically in #makeViewMenu()
		viewMenu = new JMenu("View");

		JMenu helpMenu = new JMenu("Help");
		helpMenu.add(new JMenuItem(openAbout));
		helpMenu.add(new JMenuItem(openCredits));

		JMenuBar myMenuBar = new JMenuBar();
		myMenuBar.add(fileMenu);
		myMenuBar.add(editMenu);
		myMenuBar.add(viewMenu);
		myMenuBar.add(helpMenu);
		return myMenuBar;
	}

	/**
	 * Copies the current simulation data being used to a temporary file, which
	 * can be modified and saved by the user.
	 */
	private void openCurrentData() {
		String currentData = CURRENT_DATA_FILE_NAME;
		File cf = new File(currentData);
		cf.deleteOnExit();
		try {
			Cobweb2Serializer serializer = new Cobweb2Serializer();
			FileOutputStream outStream = new FileOutputStream(cf);
			serializer.saveConfig(simRunner.getSimulation().simulationConfig, outStream);
			outStream.close();
		} catch (IOException ex) {
			throw new UserInputException("Cannot open config file", ex);
		}

		SimulationConfigEditor editor = SimulationConfigEditor.show(this, currentData, true, displaySettings);
		if (editor.isOK()) {
			openFile(editor.getConfig(), editor.isContinuation());
		}
	}

	/**
	 * Opens the simulation settings window with the current simulation file
	 * data.  The user can modify and save the file here.  If the user tries
	 * to overwrite data found in the default data file, a dialog box will be
	 * created to tell the user the proper way to create new default data.
	 */
	private void openCurrentFile() {
		if (CURRENT_DATA_FILE_NAME.equals(currentFile)) {
			throw new UserInputException("File not currently saved, use \"Modify Current Data\" instead");
		}
		SimulationConfigEditor editor = SimulationConfigEditor.show(this, currentFile, true, displaySettings);
		if (editor.isOK()) {
			openFile(editor.getConfig(), editor.isContinuation());
		}
	}

	public void openFile(SimulationConfig config) {
		openFile(config, false);
	}

	/**
	 * Load simulation config.
	 * @param config simulation configuration
	 * @param continuation load this as a continuation of the current simulation?
	 */
	private void openFile(SimulationConfig config, boolean continuation) {
		// TODO more organized way to deal with loading simulation configurations
		// TODO create new simRunner when starting new simulation, reuse when modifying
		if (simRunner.isRunning())
			simRunner.stop();

		if (!continuation) {
			simRunner.getSimulation().resetTime();
			simRunner.setLog(null);
		}

		simRunner.getSimulation().load(config);
		File file = new File(config.fileName);

		if (file.exists()) {
			currentFile = file.getName();

			if (file.isHidden() || !file.canWrite()) {
				JOptionPane.showMessageDialog(this,
						"Caution:  The initial data file \"" + currentFile
						+ "\" is NOT allowed to be modified.\n"
						+ "\n                  Any modification of this data file will be neither implemented nor saved.");
			}
		}

		updateDynamicUI();

		setTitle(WINDOW_TITLE + "  - " + file.getName());

		// simulatorUI null only when called from constructor.
		// TODO: create UI in such a way as to avoid this check
		if (simulatorUI != null) {
			simulatorUI.update(true);
		}
	}

	/**
	 *Opens an existing xml file, selected by the user through a dialog box,
	 *which contains all the information for a simulation environment.
	 */
	private void openFileDialog() {
		FileDialog theDialog = new FileDialog(CobwebApplication.this,
				"Open a State File", FileDialog.LOAD);
		theDialog.setFile("*.xml");
		theDialog.setVisible(true);
		String directory = theDialog.getDirectory();
		String file = theDialog.getFile();

		if (file != null && directory != null) {
			File of = new File(directory + file);
			if (of.exists()) {
				SimulationConfigEditor editor = SimulationConfigEditor.show(this, directory + file, true, displaySettings);
				if (editor.isOK()) {
					openFile(editor.getConfig(), editor.isContinuation());
				}
			} else {
				JOptionPane.showMessageDialog(
						this,
						"File \" " + directory + file + "\" could not be found!", "Warning",
						JOptionPane.WARNING_MESSAGE);
			}
		}
	}

	/**
	 * Exits the CobwebApplication.
	 */
	private void quitApplication() {
		simRunner.stop();
		System.exit(0);
	}

	/**
	 * Opens a dialog box for the user to select the file he/she would like
	 * to report to.
	 */
	private void reportDialog() {
		FileDialog theDialog = new FileDialog(this,
				"Choose a file to save report to", FileDialog.SAVE);
		theDialog.setVisible(true);
		if (theDialog.getFile() != null) {
			try {
				simRunner.report(new FileWriter(theDialog.getDirectory() + theDialog.getFile(), false));
			} catch (IOException ex) {
				throw new UserInputException("Can't create report file!", ex);
			}
		}
	}

	/**
	 * Loads the default files simulation settings for the current simulation.
	 * Uses the default file if available.  If not, then it will create a temporary
	 * default data file to use.
	 *
	 * <p> Used when the user selects "File" -> "Retrieve Default Data"
	 */
	private void retrieveDefaultData() {
		// Two fashions for retrieving default data:
		// The first fashion for retrieving default data -- using the file default_data_(reserved).xml if one is
		// provided.
		String defaultData = DEFAULT_DATA_FILE_NAME + CONFIG_FILE_EXTENSION;

		File df = new File(defaultData);
		boolean isTheFirstFashion = false;
		if (df.exists()) {
			if (df.canWrite()) {
				df.setReadOnly();
			}
			isTheFirstFashion = true;
		}

		String tempDefaultData = DEFAULT_DATA_FILE_NAME + TEMPORARY_FILE_EXTENSION;
		File tdf = new File(tempDefaultData);
		tdf.deleteOnExit();

		if (isTheFirstFashion) {
			try {
				FileUtils.copyFile(defaultData, tempDefaultData);
			} catch (IOException ex) {
				isTheFirstFashion = false;
			}
		}

		if (!isTheFirstFashion) {
			if (tdf.exists()) {
				tdf.delete(); // delete the potential default_data file created by last time pressing
				// "Retrieve Default Data" menu.
			}
		}

		SimulationConfigEditor editor = SimulationConfigEditor.show(this, tempDefaultData, false, displaySettings);
		if (editor.isOK()) {
			openFile(editor.getConfig(), editor.isContinuation());
		}
	}

	/**
	 * Saves the current data being used to savingFile.
	 *
	 * @param savingFile Contains the file path and name
	 * @see CobwebApplication#saveFileDialog()
	 */
	private void saveFile(String savingFile) {
		try {
			File sf = new File(savingFile);
			if (sf.isHidden() || (sf.exists() && !sf.canWrite())) {
				JOptionPane.showMessageDialog(
						this,
						"Caution:  File \"" + savingFile + "\" is NOT allowed to be written to.", "Warning",
						JOptionPane.WARNING_MESSAGE);
			} else {
				FileUtils.copyFile(currentFile, savingFile);
			}
		} catch (IOException ex) {
			throw new UserInputException("Save failed", ex);
		}
	}

	/**
	 * Opens the dialog box to allow the user to select the file to save
	 * the current data to.
	 */
	private void saveFileDialog() {
		FileDialog theDialog = new FileDialog(this,
				"Choose a file to save state to", FileDialog.SAVE);
		theDialog.setFile("*.xml");
		theDialog.setVisible(true);
		if (theDialog.getFile() != null) {
			saveFile(theDialog.getDirectory() + theDialog.getFile());
		}
	}

	public String getCurrentFile() {
		return currentFile;
	}

	/**
	 * Allows the user to select a new file to use as the default data file.
	 * The selected file is copied into the default data file if the default
	 * data file is writable or doesn�t exist.
	 */
	private void setDefaultData() {
		String defaultData = DEFAULT_DATA_FILE_NAME + CONFIG_FILE_EXTENSION;
		// prepare the file default_data_(reserved).xml to be writable
		File df = new File(defaultData);
		if (df.isHidden()) {
			JOptionPane.showMessageDialog(
					this,
					"Cannot set default data:  file \"" + defaultData + "\" is hidden.", "Warning",
					JOptionPane.WARNING_MESSAGE);
			return;
		}

		if (!df.exists() || df.canWrite()) {
			FileDialog setDialog = new FileDialog(this,
					"Set Default Data", FileDialog.LOAD);
			setDialog.setFile("*.xml");
			setDialog.setVisible(true);

			if (setDialog.getFile() != null) {
				String directory = setDialog.getDirectory();
				String file = setDialog.getFile();
				String chosenFile = directory + file;
				File f = new File(chosenFile);
				if (f.exists()) {
					try {
						FileUtils.copyFile(chosenFile, defaultData);
					} catch (IOException ex) {
						Logger.getLogger("COBWEB2").log(Level.WARNING, "Unable to set default data", ex);
						JOptionPane.showMessageDialog(setDialog, "Fail to set default data!\n"
								+ "\nPossible cause(s): " + ex.getMessage(), "Warning", JOptionPane.WARNING_MESSAGE);

					}
				} else {
					JOptionPane.showMessageDialog(this, "File \" " + chosenFile + "\" could not be found!", "Warning",
							JOptionPane.WARNING_MESSAGE);
				}
			}

		} else { // write permission failed to set
			JOptionPane.showMessageDialog(
					this,
					"Fail to set default data!\n"
							+ "\nPossible cause(s): Permission for the current folder may not be attained.", "Warning",
							JOptionPane.WARNING_MESSAGE);

			Logger.getLogger("COBWEB2").log(Level.WARNING, "Unable to set default data");

		}
		// Disallow write again to make sure the default data file would not be modified by outer calling.
		if (df.canWrite()) {
			df.setReadOnly();
		}
	}

	private void updateDynamicUI() {
		setupViewers();

		makeAgentFoodSelectMenu();

		makeViewMenu();

		if (simulatorUI != null)
			simulatorUI.simulationChanged();

		validate();
	}

	private List<ViewerPlugin> viewers = new LinkedList<ViewerPlugin>();

	private void setupViewers() {
		// TODO: don't kill viewers when modifying simulation
		for (ViewerPlugin viewer : viewers) {
			viewer.dispose();
		}
		viewers.clear();

		// TODO: ViewerPlugin.isCompatible(simulationConfig)
		if (simRunner.getSimulation().simulationConfig.getControllerName().equals(LinearWeightsController.class.getName())) {
			viewers.add(new LinearAIViewer((LinearWeightsControllerParams)simRunner.getSimulation().simulationConfig.controllerParams));
		}


		viewers.add(new LiveStats(simRunner));

		if (simRunner.getSimulation().simulationConfig.geneticParams.getGeneCount() != 0) {
			GAChartOutput gaViewer = new GAChartOutput(
					simRunner.getSimulation().geneticMutator.getTracker(),
					simRunner.getSimulation().simulationConfig.geneticParams,
					simRunner,
					displaySettings);
			viewers.add(gaViewer);
		}

		if (simulatorUI != null) {
			viewers.add(new ProductionViewer(simulatorUI.displayPanel));
			viewers.add(new EnergyEventViewer(simulatorUI.displayPanel, simRunner.getSimulation()));
		}
	}


	private void makeViewMenu() {
		viewMenu.removeAll();
		for (final ViewerPlugin viewer : viewers) {
			final JCheckBoxMenuItem box = new JCheckBoxMenuItem(viewer.getName(), false);

			box.addItemListener(new ItemListener() {

				@Override
				public void itemStateChanged(ItemEvent e) {
					if (e.getStateChange() == ItemEvent.SELECTED) {
						viewer.on();
					} else if (e.getStateChange() == ItemEvent.DESELECTED) {
						viewer.off();
					}
				}
			});
			ViewerClosedCallback onClosed = new ViewerClosedCallback() {
				@Override
				public void viewerClosed() {
					box.setSelected(false);
				}
			};
			viewer.setClosedCallback(onClosed);
			viewMenu.add(box);
		}
	}

	private void makeAgentFoodSelectMenu() {
		JMenuItem foodtype[] = new JMenuItem[simRunner.getSimulation().getAgentTypeCount()];
		JMenuItem agentype[] = new JMenuItem[simRunner.getSimulation().getAgentTypeCount()];
		foodMenu.removeAll();
		agentMenu.removeAll();
		for (int i = 0; i < simRunner.getSimulation().getAgentTypeCount(); i++) {
			foodtype[i] = new JMenuItem("Food Type " + (i + 1));
			foodtype[i].setActionCommand("Food Type " + (i + 1));
			foodtype[i].addActionListener(new FoodMouseActionListener(i));
			foodMenu.add(foodtype[i]);

			agentype[i] = new JMenuItem("Agent Type " + (i + 1));
			agentype[i].setActionCommand("Agent Type " + (i + 1));
			agentype[i].addActionListener(new AgentMouseActionListener(i));
			agentMenu.add(agentype[i]);
		}
	}


	private class FoodMouseActionListener implements ActionListener {

		private final int type;

		public FoodMouseActionListener(int type) {
			this.type = type;

		}

		@Override
		public void actionPerformed(ActionEvent e) {
			simulatorUI.displayPanel.setMouseMode(MouseMode.AddFood, type);
		}
	}

	private class AgentMouseActionListener implements ActionListener {

		private final int type;

		public AgentMouseActionListener(int type) {
			this.type = type;

		}

		@Override
		public void actionPerformed(ActionEvent e) {
			simulatorUI.displayPanel.setMouseMode(MouseMode.AddAgent, type);
		}

	}

	private JMenu viewMenu;

	private Action openSimulation = new AbstractAction("Open") {
		@Override
		public void actionPerformed(ActionEvent e) {
			pauseUI();
			openFileDialog();
		}
		private static final long serialVersionUID = 1L;
	};

	private Action setDefaultData = new AbstractAction("Set Default Data") {
		@Override
		public void actionPerformed(ActionEvent e) {
			setDefaultData();
		}
		private static final long serialVersionUID = 1L;
	};

	private Action openAbout = new AbstractAction("About") {
		@Override
		public void actionPerformed(ActionEvent e) {
			AboutDialog.show();
		}
		private static final long serialVersionUID = 1L;
	};

	private Action createNewData = new AbstractAction("Create New Data") {
		@Override
		public void actionPerformed(ActionEvent e) {
			pauseUI();
			createNewData();
		}
		private static final long serialVersionUID = 1L;
	};

	private Action openCredits = new AbstractAction("Credits") {
		@Override
		public void actionPerformed(ActionEvent e) {
			CreditsDialog.show();
		}
		private static final long serialVersionUID = 1L;
	};

	private Action loadPopulation = new AbstractAction("Insert Sample Population") {
		@Override
		public void actionPerformed(ActionEvent e) {

			ReplaceMergeCancel option = openInsertSamplePopReplaceDialog();

			if (option != ReplaceMergeCancel.CANCEL){
				//Select the XML file
				FileDialog theDialog = new FileDialog(CobwebApplication.this,
						"Choose a file to load", FileDialog.LOAD);
				theDialog.setFile("*.xml");
				theDialog.setVisible(true);
				if (theDialog.getFile() != null) {
					//Load the XML file
					PopulationSampler.insertPopulation(simRunner.getSimulation(), theDialog.getDirectory() + theDialog.getFile(), option == ReplaceMergeCancel.REPLACE);
					simulatorUI.update(true);
				}
			}
		}
		private static final long serialVersionUID = 1L;
	};

	private Action setLog = new AbstractAction("Log") {
		@Override
		public void actionPerformed(ActionEvent e) {
			pauseUI();
			logFileDialog();
		}
		private static final long serialVersionUID = 1L;
	};

	private Action modifySimulation = new AbstractAction("Modify Simulation") {
		@Override
		public void actionPerformed(ActionEvent e) {
			pauseUI();
			openCurrentData();
		}
		private static final long serialVersionUID = 1L;
	};

	private Action modifySimulationFile = new AbstractAction("Modify Simulation File") {
		@Override
		public void actionPerformed(ActionEvent e) {
			pauseUI();
			openCurrentFile();
		}
		private static final long serialVersionUID = 1L;
	};

	private Action setObservationMode = new AbstractAction("Observation Mode") {
		@Override
		public void actionPerformed(ActionEvent e) {
			simulatorUI.displayPanel.setMouseMode(MouseMode.Observe);
		}
		private static final long serialVersionUID = 1L;
	};

	private Action quit = new AbstractAction("Quit") {
		@Override
		public void actionPerformed(ActionEvent e) {
			quitApplication();
		}
		private static final long serialVersionUID = 1L;
	};

	private Action removeAll = new AbstractAction("Remove All") {
		@Override
		public void actionPerformed(ActionEvent e) {
			simRunner.getSimulation().theEnvironment.clearStones();
			simRunner.getSimulation().theEnvironment.clearFood();
			simRunner.getSimulation().theEnvironment.clearAgents();
			simRunner.getSimulation().theEnvironment.clearWaste();
			simulatorUI.update(true);
		}
		private static final long serialVersionUID = 1L;
	};

	private Action removeAllStones = new AbstractAction("Remove All Stones") {
		@Override
		public void actionPerformed(ActionEvent e) {
			simRunner.getSimulation().theEnvironment.clearStones();
			simulatorUI.update(true);
		}
		private static final long serialVersionUID = 1L;
	};

	private Action removeAllFood = new AbstractAction("Remove All Food") {
		@Override
		public void actionPerformed(ActionEvent e) {
			simRunner.getSimulation().theEnvironment.clearFood();
			simulatorUI.update(true);
		}
		private static final long serialVersionUID = 1L;
	};

	private Action removeAllAgents = new AbstractAction("Remove All Agents") {
		@Override
		public void actionPerformed(ActionEvent e) {
			simRunner.getSimulation().theEnvironment.clearAgents();
			simulatorUI.update(true);
		}
		private static final long serialVersionUID = 1L;
	};

	private Action removeAllWaste = new AbstractAction("Remove All Waste") {
		@Override
		public void actionPerformed(ActionEvent e) {
			simRunner.getSimulation().theEnvironment.clearWaste();
			simulatorUI.update(true);
		}
		private static final long serialVersionUID = 1L;
	};

	private Action report = new AbstractAction("Report") {
		@Override
		public void actionPerformed(ActionEvent e) {
			pauseUI();
			reportDialog();
		}
		private static final long serialVersionUID = 1L;
	};

	private Action retrieveDefaultData = new AbstractAction("Retrieve Default Data") {
		@Override
		public void actionPerformed(ActionEvent e) {
			pauseUI();
			retrieveDefaultData();
		}
		private static final long serialVersionUID = 1L;
	};

	private Action saveConfig = new AbstractAction("Save") {
		@Override
		public void actionPerformed(ActionEvent e) {
			pauseUI();
			saveFileDialog();
		}
		private static final long serialVersionUID = 1L;
	};

	private Action savePopulation = new AbstractAction("Save Sample Population") {
		@Override
		public void actionPerformed(ActionEvent e) {

			// open dialog to choose population size to be saved
			HashMap<String, Object> result = openSaveSamplePopOptionsDialog();
			if (result != null){
				String option = (String)result.get("option");
				int amount = ((Integer)result.get("amount")).intValue();

				if (option != null && amount != -1) {
					// Open file dialog box
					FileDialog theDialog = new FileDialog(CobwebApplication.this,
							"Choose a file to save state to", FileDialog.SAVE);
					theDialog.setFile("*.xml");
					theDialog.setVisible(true);
					if (theDialog.getFile() != null) {
						int saveNum;

						int agentCount = simRunner.getSimulation().theEnvironment.getAgentCount();
						if (option.equals("percentage")) {
							saveNum = agentCount * amount / 100;
						} else {
							saveNum = amount;
						}
						if (saveNum > agentCount)
							saveNum = agentCount;

						//Save population in the specified file.
						PopulationSampler.savePopulation(simRunner.getSimulation(), theDialog.getDirectory() + theDialog.getFile(), saveNum);
					}
				}
			}
		}
		private static final long serialVersionUID = 1L;
	};

	private Action setModeStones = new AbstractAction("Select Stones") {
		@Override
		public void actionPerformed(ActionEvent e) {
			simulatorUI.displayPanel.setMouseMode(MouseMode.AddStone);
		}
		private static final long serialVersionUID = 1L;
	};

	private enum ReplaceMergeCancel {
		CANCEL,
		REPLACE,
		MERGE
	}

	/**
	 * Opens a dialog box to allow the user to select the option of replacing the
	 * current population, or merge with the the current population.
	 *
	 * <p> Used when the user selects "File" -> "Insert Sample Population"
	 *
	 * @return The option selected by the user.
	 * @see #loadPopulation
	 */
	private static ReplaceMergeCancel openInsertSamplePopReplaceDialog() {
		JRadioButton b1 = new JRadioButton("Replace current population", true);
		JRadioButton b2 = new JRadioButton("Merge with current population");

		ButtonGroup group = new ButtonGroup();
		group.add(b1);
		group.add(b2);

		Object[] array = {
				new JLabel("Select an option:"),
				b1,
				b2
		};

		int res = JOptionPane.showConfirmDialog(null, array, "Select",
				JOptionPane.OK_CANCEL_OPTION);

		if (res == JOptionPane.CANCEL_OPTION || res == JOptionPane.CLOSED_OPTION)
			return ReplaceMergeCancel.CANCEL;

		if (b1.isSelected()) {
			return ReplaceMergeCancel.REPLACE;
		}
		else {
			return ReplaceMergeCancel.MERGE;
		}
	}

	/**
	 * Creates a hash that contains the information of whether the user selected
	 * to save as a population or an amount, and what percentage or amount.
	 *
	 * <p>Used when the user selects "File" -> "Save Sample Population"
	 *
	 * @return A hash of the options the user selected.
	 */
	private HashMap<String, Object> openSaveSamplePopOptionsDialog() {

		JRadioButton b1 = new JRadioButton("Save a percentage (%) between 1-100");


		int popNum = simRunner.getSimulation().theEnvironment.getAgentCount();

		JRadioButton b2 = new JRadioButton("Save an amount (between 1-"+ popNum + ")");
		b1.setSelected(true);

		ButtonGroup group = new ButtonGroup();
		group.add(b1);
		group.add(b2);

		JTextField amountField = new JTextField(30);

		Object[] array = {
				new JLabel("Select an option:"),
				b1,
				b2,
				new JLabel("Enter the number for the selected option:"),
				amountField
		};

		int res = JOptionPane.showConfirmDialog(null, array, "Select",
				JOptionPane.OK_CANCEL_OPTION);

		if (res == -1 || res == 2)
			return null;

		int amount = -1;

		HashMap<String, Object> result = new HashMap<String, Object>();

		try {
			amount = Integer.parseInt(amountField.getText());
			if (amount < 1)
				throw new IllegalArgumentException("Amount must be 1 or greater");
		} catch (IllegalArgumentException e) {
			JOptionPane.showMessageDialog((Component)null, "Invalid input.");
			return null;
		}

		result.put("amount", new Integer(amount));

		if (b1.isSelected()) {
			result.put("option", "percentage");
		}
		else if ( b2.isSelected()) {
			result.put("option", "amount");
		}

		return result;


	}

	private void pauseUI() {
		simRunner.stop();
	}

	private static final long serialVersionUID = 2112476687880153089L;
}
