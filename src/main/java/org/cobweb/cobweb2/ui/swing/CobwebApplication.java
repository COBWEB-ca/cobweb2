package org.cobweb.cobweb2.ui.swing;

import java.awt.BorderLayout;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.FileDialog;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.FocusAdapter;
import java.awt.event.FocusEvent;
import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.io.File;
import java.io.FileOutputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.text.NumberFormat;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.swing.AbstractAction;
import javax.swing.Action;
import javax.swing.ButtonGroup;
import javax.swing.JButton;
import javax.swing.JCheckBoxMenuItem;
import javax.swing.JDialog;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JMenu;
import javax.swing.JMenuBar;
import javax.swing.JMenuItem;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JRadioButton;
import javax.swing.JSeparator;
import javax.swing.JTextField;
import javax.swing.WindowConstants;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;

import org.cobweb.cobweb2.Simulation;
import org.cobweb.cobweb2.SimulationConfig;
import org.cobweb.cobweb2.ai.LinearWeightsController;
import org.cobweb.cobweb2.core.SimulationInterface;
import org.cobweb.cobweb2.ui.SimulationRunner;
import org.cobweb.cobweb2.ui.ThreadSimulationRunner;
import org.cobweb.cobweb2.ui.UpdatableUI;
import org.cobweb.cobweb2.ui.UserInputException;
import org.cobweb.cobweb2.ui.ViewerClosedCallback;
import org.cobweb.cobweb2.ui.ViewerPlugin;
import org.cobweb.cobweb2.ui.swing.ai.LinearAIViewer;
import org.cobweb.cobweb2.ui.swing.components.PauseButton;
import org.cobweb.cobweb2.ui.swing.components.SpeedBar;
import org.cobweb.cobweb2.ui.swing.components.StepButton;
import org.cobweb.cobweb2.ui.swing.config.GUI;
import org.cobweb.cobweb2.ui.swing.genetics.GAChartOutput;
import org.cobweb.cobweb2.ui.swing.production.ProductionViewer;
import org.cobweb.util.FileUtils;
import org.cobweb.util.Versionator;

/**
 * This class consists of methods to allow the user to use the Cobweb simulation
 * tool.  It implements all necessary methods defined by the UIClient class, and
 * makes use of the JFrame class.
 *
 * @author Liang
 *
 */
public class CobwebApplication extends JFrame implements UpdatableUI, SimulationRunner {

	private static final String WINDOW_TITLE = "COBWEB 2";

	private static final long serialVersionUID = 2112476687880153089L;

	public static final String GREETINGS = "Welcome to COBWEB 2";

	/** Filename of current simulation config */
	String currentFile;

	private DisplayPanel displayPanel;

	private PauseButton pauseButton;

	private StepButton stepButton;

	public JTextField tickField;

	private JMenu foodMenu;

	private JMenu agentMenu;

	/**
	 * The value is determined by whether a "Test Data" window is invoked by one of "Modify This File"
	 * and "Modify Current Data" or by one of "Open", "Create New Data" and "Retrieve Default Data". Mar 14
	 */
	private boolean invokedByModify;

	public static final String CONFIG_FILE_EXTENSION = ".xml";

	public static final String TEMPORARY_FILE_EXTENSION = ".cwtemp";

	static final String INITIAL_OR_NEW_INPUT_FILE_NAME = "initial_or_new_input_(reserved)" + CONFIG_FILE_EXTENSION;

	public static final String DEFAULT_DATA_FILE_NAME = "default_data_(reserved)";

	public static final String CURRENT_DATA_FILE_NAME = "current_data_(reserved)" + TEMPORARY_FILE_EXTENSION;

	private JPanel mainPanel;
	private JLabel tickDisplay;

	private JPanel controls;

	public ThreadSimulationRunner simRunner = new ThreadSimulationRunner(new Simulation());

	@Override
	public void step() {
		simRunner.step();
	}

	@Override
	public void stop() {
		simRunner.stop();
	}

	@Override
	public boolean isRunning() {
		return simRunner.isRunning();
	}

	@Override
	public void run() {
		simRunner.run();
	}

	@Override
	public void addUIComponent(UpdatableUI ui) {
		simRunner.addUIComponent(ui);
	}

	@Override
	public void removeUIComponent(UpdatableUI ui) {
		simRunner.removeUIComponent(ui);
	}

	protected Logger myLogger = Logger.getLogger("COBWEB2");

	// constructor
	public CobwebApplication() {
		super(WINDOW_TITLE);

		myLogger.info(GREETINGS);
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

		setVisible(true);
	}

	/**
	 * Creates the about dialog box, which contains information pertaining
	 * to the Cobweb version being used, and the date it was last modified.
	 */
	public void aboutDialog() {
		final javax.swing.JDialog whatDialog = new javax.swing.JDialog(GUI.frame,
				"About Cobweb", true);
		whatDialog.setDefaultCloseOperation(WindowConstants.DISPOSE_ON_CLOSE);

		JPanel info = new JPanel();
		info.setAlignmentX(CENTER_ALIGNMENT);
		info.add(new JLabel("<html><center>COBWEB2 2003/2011<br/>version: <br/>"
				+  Versionator.getVersion().replace(" ", "<br/>")
				+ "</center></html>"));

		JPanel term = new JPanel();
		JButton close = new JButton("Close");
		term.add(close);

		close.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent evt) {
				whatDialog.setVisible(false);
			}
		});

		whatDialog.setLayout(new BorderLayout());
		whatDialog.add(info, BorderLayout.CENTER);
		whatDialog.add(term, BorderLayout.SOUTH);
		whatDialog.setSize(300, 150);
		//whatDialog.pack();
		whatDialog.setVisible(true);
	}

	/**
	 * Opens an initial simulation settings file using the simulation settings
	 * window.  The user can modify the simulation settings and save the
	 * settings to a new file.  The method is invoked when the user selects
	 * "Create New Data" located under "File" in the main tool bar.
	 */
	public void createNewData() {
		String newInput = INITIAL_OR_NEW_INPUT_FILE_NAME;
		GUI.createAndShowGUI(this, newInput, false);
		if (simRunner.getSimulation() == null) {
			setCurrentFile(newInput);
		}
		File inf = new File(newInput);
		if (inf.isHidden() != false || ((inf.exists() != false) && (inf.canWrite() == false))) {
			JOptionPane
			.showMessageDialog(
					GUI.frame,
					"Caution:  The new data file \""
							+ newInput
							+ "\" is NOT allowed to be modified.\n"
							+ "\n                  Any modification of this data file will be neither implemented nor saved.");
		}
	}

	/**
	 * Creates a dialog box with contact information about a specified person
	 * in the credits menu.
	 *
	 * @param parentDialog The credits dialog box that invoked the creation of this dialog box
	 * @param S The contact information that will be shown in the dialog box.
	 * @param length The length of the dialog box in pixels
	 * @param width The width of the dialog box in pixels
	 * @see CobwebApplication#creditsDialog()
	 */
	private void creditDialog(JDialog parentDialog, String[] S, int length, int width) {

		final javax.swing.JDialog creditDialog = new javax.swing.JDialog(parentDialog,
				"Click on Close to continue", true);

		JPanel credit = new JPanel();
		for (int i = 0; i < S.length; ++i) {
			credit.add(new JLabel(S[i]), "Center");
		}

		JPanel term = new JPanel();
		JButton close = new JButton("Close");
		term.add(close);
		close.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent evt) {
				creditDialog.setVisible(false);
			}
		});

		creditDialog.add(credit, "Center");
		creditDialog.add(term, "South");

		creditDialog.setSize(length, width);
		creditDialog.setVisible(true);

	}

	/**
	 * The credits dialog box that is created when the user selects "Credits"
	 * located under "Help" in the main tool bar.  It contains a list of
	 * buttons for important people that can be contacted for more information
	 * about Cobweb.  The information can be accessed by clicking on the buttons.
	 */
	public void creditsDialog() {
		final javax.swing.JDialog theDialog = new javax.swing.JDialog(GUI.frame, "Credits", true);
		theDialog.setDefaultCloseOperation(WindowConstants.DISPOSE_ON_CLOSE);

		JPanel credit = new JPanel();
		JButton brad = new JButton("Brad Bass, PhD");
		JButton jeff = new JButton("Jeff Hill");
		JButton jin = new JButton("Jin Soo Kang");
		credit.add(new JLabel("Coordinator"));
		credit.add(brad);
		credit.add(new JLabel("_______________"));
		credit.add(new JLabel("Programmers"));
		credit.add(jeff);
		credit.add(jin);

		JPanel term = new JPanel();
		JButton close = new JButton("Close");
		term.add(close);

		brad.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent evt) {
				String[] S = { "Brad Bass, PhD", "Adaptations and Impacts Research Group",
						"Environment Canada at Univ of Toronto", "Inst. for Environmental Studies",
						"33 Willcocks Street", "Toronto, Ont M5S 3E8 CANADA",
						"TEL: (416) 978-6285  FAX: (416) 978-3884", "brad.bass@ec.gc.ca" };
				creditDialog(theDialog, S, 300, 300);
			}
		});

		jeff.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent evt) {
				String[] S = { "Main Structural Programming By", "", "Jeff Hill", "oni1@home.com" };

				creditDialog(theDialog, S, 250, 150);
			}
		});

		jin.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent evt) {
				String[] S = { "Update & Additional Programming By", "", "Jin Soo Kang",
						"Undergraduate, Computer Science", "University of Toronto", "jin.kang@utoronto.ca",
				"[2000 - 2001]" };

				creditDialog(theDialog, S, 300, 250);
			}
		});

		close.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent evt) {
				theDialog.setVisible(false);
			}
		});

		theDialog.add(credit, "Center");
		theDialog.add(term, "South");
		theDialog.setSize(150, 265);
		theDialog.setVisible(true);

	}

	/**
	 * Returns the user interface pipe being used.
	 *
	 * @return User Interface Pipe
	 */
	@Override
	public SimulationInterface getSimulation() {
		return simRunner.getSimulation();
	}

	public boolean isInvokedByModify() {
		return invokedByModify;
	}

	@Override
	public boolean isReadyToUpdate() {
		return displayPanel != null && displayPanel.isReadyToRefresh();
	}

	/**
	 * Allows the user to select the log file to write to.
	 */
	public void logFileDialog() {
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
			FileOutputStream outStream = new FileOutputStream(cf);
			simRunner.getSimulation().simulationConfig.write(outStream);
			outStream.close();
		} catch (Exception ex) {
			throw new UserInputException("Cannot open config file", ex);
		}
		GUI.createAndShowGUI(this, currentData, true);
	}

	/**
	 * Opens the simulation settings window with the current simulation file
	 * data.  The user can modify and save the file here.  If the user tries
	 * to overwrite data found in the default data file, a dialog box will be
	 * created to tell the user the proper way to create new default data.
	 */
	public void openCurrentFile() {
		if (CURRENT_DATA_FILE_NAME.equals(currentFile)) {
			throw new UserInputException("File not currently saved, use \"Modify Current Data\" instead");
		}
		GUI.createAndShowGUI(this, currentFile, true);
	}

	// TODO more organized way to deal with loading simulation configurations
	// TODO create new simRunner when starting new simulation, reuse when modifying
	public void openFile(SimulationConfig p) {
		if (simRunner.isRunning())
			simRunner.stop();
		simRunner.getSimulation().load(p);
		File file = new File(p.getFilename());

		if (file.exists()) {
			currentFile = p.getFilename();
		}

		if (!isInvokedByModify()) {
			simRunner.getSimulation().resetTime();
			simRunner.setLog(null);
		}

		UIsettings();

		displayPanel.setSimulation(simRunner.getSimulation());

		File f = new File(p.getFilename());
		setTitle(WINDOW_TITLE + "  - " + f.getName());
		update(true);
	}

	/**
	 *Opens an existing xml file, selected by the user through a dialog box,
	 *which contains all the information for a simulation environment.
	 */
	public void openFileDialog() {
		FileDialog theDialog = new FileDialog(GUI.frame,
				"Open a State File", FileDialog.LOAD);
		theDialog.setFile("*.xml");
		theDialog.setVisible(true);
		String directory = theDialog.getDirectory();
		String file = theDialog.getFile();

		if (file != null && directory != null) {
			File of = new File(directory + file);
			if (of.exists() != false) {
				setCurrentFile(directory + file);

				if (GUI.frame != null && GUI.frame.isVisible() == true) {
					GUI.frame.dispose();
				}
				GUI.createAndShowGUI(this, currentFile, true);

			} else {
				JOptionPane.showMessageDialog(
						this,
						"File \" " + directory + file + "\" could not be found!", "Warning",
						JOptionPane.WARNING_MESSAGE);
				if (simRunner.getSimulation() == null) {
					GUI.frame.toFront();
				}
			}
		}
	}

	/**
	 * Exits the CobwebApplication.
	 */
	public void quitApplication() {
		simRunner.stop();
		System.exit(0);
	}

	@Override
	public void update(boolean wait) {
		tickDisplay.setText("Tick: " + NumberFormat.getIntegerInstance().format(
				simRunner.getSimulation().getTime()));
		if (displayPanel != null) {
			displayPanel.refresh(wait);
		}
	}

	/**
	 * Opens a dialog box for the user to select the file he/she would like
	 * to report to.
	 */
	public void reportDialog() {
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
		if (df.exists() != false) {
			if (df.canWrite() != false) {
				df.setReadOnly();
			}
			isTheFirstFashion = true;
		}

		String tempDefaultData = DEFAULT_DATA_FILE_NAME + TEMPORARY_FILE_EXTENSION;
		File tdf = new File(tempDefaultData);
		tdf.deleteOnExit();

		if (isTheFirstFashion != false) {
			try {
				FileUtils.copyFile(defaultData, tempDefaultData);
			} catch (Exception ex) {
				isTheFirstFashion = false;
			}
		}

		if (isTheFirstFashion == false) {
			if (tdf.exists() != false) {
				tdf.delete(); // delete the potential default_data file created by last time pressing
				// "Retrieve Default Data" menu.
			}
		}

		GUI.createAndShowGUI(this, tempDefaultData, false);
		if (simRunner.getSimulation() == null) {
			setCurrentFile(tempDefaultData);
		}
	}

	/**
	 * Saves the current data being used to savingFile.
	 *
	 * @param savingFile Contains the file path and name
	 * @see CobwebApplication#saveFileDialog()
	 */
	public void saveFile(String savingFile) {
		try {
			File sf = new File(savingFile);
			if ((sf.isHidden() != false) || ((sf.exists() != false) && (sf.canWrite() == false))) {
				JOptionPane.showMessageDialog(
						GUI.frame,
						"Caution:  File \"" + savingFile + "\" is NOT allowed to be written to.", "Warning",
						JOptionPane.WARNING_MESSAGE);
			} else {
				FileUtils.copyFile(currentFile, savingFile);
			}
		} catch (Exception ex) {
			throw new UserInputException("Save failed", ex);
		}
	}

	/**
	 * Opens the dialog box to allow the user to select the file to save
	 * the current data to.
	 */
	public void saveFileDialog() {
		FileDialog theDialog = new FileDialog(GUI.frame,
				"Choose a file to save state to", FileDialog.SAVE);
		theDialog.setFile("*.xml");
		theDialog.setVisible(true);
		if (theDialog.getFile() != null) {
			saveFile(theDialog.getDirectory() + theDialog.getFile());
		}
	}

	/**
	 * Sets the current file as input.
	 *
	 * @param input Name of the new current file.
	 */
	public void setCurrentFile(String input) {
		currentFile = input;
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
		if (df.isHidden() != false) {
			JOptionPane.showMessageDialog(
					this,
					"Cannot set default data:  file \"" + defaultData + "\" is hidden.", "Warning",
					JOptionPane.WARNING_MESSAGE);
			if (simRunner.getSimulation() == null) {
				GUI.frame.toFront();
			}
			return;
		}

		if ((df.exists() == false) || (df.canWrite() == true)) {
			FileDialog setDialog = new FileDialog(GUI.frame,
					"Set Default Data", FileDialog.LOAD);
			setDialog.setFile("*.xml");
			setDialog.setVisible(true);

			if (setDialog.getFile() != null) {
				String directory = setDialog.getDirectory();
				String file = setDialog.getFile();
				String chosenFile = directory + file;
				File f = new File(chosenFile);
				if (f.exists() != false) {
					try {
						FileUtils.copyFile(chosenFile, defaultData);
					} catch (Exception ex) {
						Logger.getLogger("COBWEB2").log(Level.WARNING, "Unable to set default data", ex);
						JOptionPane.showMessageDialog(setDialog, "Fail to set default data!\n"
								+ "\nPossible cause(s): " + ex.getMessage(), "Warning", JOptionPane.WARNING_MESSAGE);

					}
				} else {
					if (simRunner.getSimulation() != null && GUI.frame != null && GUI.frame.isVisible() == true) {
						GUI.frame.dispose();
					}
					JOptionPane.showMessageDialog(this, "File \" " + chosenFile + "\" could not be found!", "Warning",
							JOptionPane.WARNING_MESSAGE);
					if (simRunner.getSimulation() == null) {
						GUI.frame.toFront();
					}
				}
			}

		} else { // write permission failed to set
			JOptionPane.showMessageDialog(
					this,
					"Fail to set default data!\n"
							+ "\nPossible cause(s): Permission for the current folder may not be attained.", "Warning",
							JOptionPane.WARNING_MESSAGE);

			Logger.getLogger("COBWEB2").log(Level.WARNING, "Unable to set default data");

			if (simRunner.getSimulation() == null) {
				GUI.frame.toFront();
			}
		}
		// Disallow write again to make sure the default data file would not be modified by outer calling.
		if (df.canWrite() != false) {
			df.setReadOnly();
		}
	}

	public void setInvokedByModify(boolean b) {
		invokedByModify = b;
	}

	public void UIsettings() {

		createDefaultUI();

		makeAgentFoodSelectMenu();

		makeViewMenu();

		simRunner.addUIComponent(this);

		validate();
	} // end of UISettings

	private List<ViewerPlugin> viewers = new LinkedList<ViewerPlugin>();

	private void setupViewers() {
		for (ViewerPlugin viewer : viewers) {
			viewer.dispose();
		}
		viewers.clear();
		if (simRunner.getSimulation().simulationConfig.getEnvParams().controllerName.equals(LinearWeightsController.class.getName())) {
			viewers.add(new LinearAIViewer());
		}

		viewers.add(new ProductionViewer(simRunner.getSimulation().theEnvironment, simRunner));

		viewers.add(new LiveStats(simRunner));

		if (simRunner.getSimulation().simulationConfig.getGeneticParams().geneCount != 0) {
			GAChartOutput gaViewer = new GAChartOutput(
					simRunner.getSimulation().geneticMutator.getTracker(),
					simRunner.getSimulation().simulationConfig.getGeneticParams(),
					simRunner);
			viewers.add(gaViewer);
		}
	}


	private void makeViewMenu() {
		viewMenu.removeAll();

		setupViewers();

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

	protected void createDefaultUI() {
		if (mainPanel == null) {
			mainPanel = new JPanel();
			mainPanel.setLayout(new BorderLayout());
			add(mainPanel);
		}

		if (displayPanel == null) {
			displayPanel = new DisplayPanel(simRunner.getSimulation());
		} else {
			displayPanel.setSimulation(simRunner.getSimulation());
		}

		mainPanel.add(displayPanel, BorderLayout.CENTER);
		if (controls == null) {
			controls = new JPanel();
			// controls.setLayout(new BoxLayout(controls, BoxLayout.X_AXIS));
			mainPanel.add(controls, BorderLayout.NORTH);
		}
		if (tickDisplay == null) {
			tickDisplay = new JLabel();
			tickDisplay.setPreferredSize(new Dimension(90, 20));
			controls.add(tickDisplay);
		}
		if (tickField == null) {
			controls.add(new JLabel("Stop at"));
			tickField = new JTextField(8);
			tickField.setPreferredSize(new Dimension(40, 20));

			tickField.addFocusListener(new FocusAdapter() {
				@Override
				public void focusGained(FocusEvent e) {
					tickField.repaint();
				}

				@Override
				public void focusLost(FocusEvent e) {
					tickField.repaint();
				}
			});

			tickField.getDocument().addDocumentListener(new DocumentListener() {

				@Override
				public void removeUpdate(DocumentEvent e) {
					update();
				}

				@Override
				public void insertUpdate(DocumentEvent e) {
					update();
				}

				@Override
				public void changedUpdate(DocumentEvent e) {
					update();
				}

				private void update() {
					try {
						int time = Integer.parseInt(tickField.getText());
						simRunner.setAutoStopTime(time);
					} catch(NumberFormatException ex) {
						// Nothing
					}
				}
			});
			controls.add(tickField);
		}

		if (pauseButton == null) {
			pauseButton = new PauseButton(simRunner);
			controls.add(pauseButton);
			stepButton = new StepButton(simRunner);
			controls.add(stepButton);
			controls.add(new JLabel("   Adjust Speed:"));
			SpeedBar sb = new SpeedBar(simRunner);
			controls.add(sb);
		} else {
			pauseButton.setScheduler(simRunner);
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
			displayPanel.setMouseMode(MouseMode.AddFood, type);
		}
	}

	private class AgentMouseActionListener implements ActionListener {

		private final int type;

		public AgentMouseActionListener(int type) {
			this.type = type;

		}

		@Override
		public void actionPerformed(ActionEvent e) {
			displayPanel.setMouseMode(MouseMode.AddAgent, type);
		}

	}

	private JMenu viewMenu;

	private Action openSimulation = new AbstractAction("Open") {
		@Override
		public void actionPerformed(ActionEvent e) {
			pauseUI();
			disposeGUIframe();
			setInvokedByModify(false);
			openFileDialog();
		}
		private static final long serialVersionUID = 1L;
	};

	private Action setDefaultData = new AbstractAction("Set Default Data") {
		@Override
		public void actionPerformed(ActionEvent e) {
			disposeGUIframe();
			setDefaultData();
		}
		private static final long serialVersionUID = 1L;
	};

	/**
	 * If a "Test Data" window is open (visible), dispose it (when hitting a
	 * menu).
	 */
	private void disposeGUIframe() {
		if (simRunner.getSimulation() != null && GUI.frame != null && GUI.frame.isVisible()) {
			GUI.frame.dispose();
		}
	}

	private Action openAbout = new AbstractAction("About") {
		@Override
		public void actionPerformed(ActionEvent e) {
			disposeGUIframe();
			aboutDialog();
		}
		private static final long serialVersionUID = 1L;
	};

	private Action createNewData = new AbstractAction("Create New Data") {
		@Override
		public void actionPerformed(ActionEvent e) {
			pauseUI();
			if (GUI.frame != null && GUI.frame.isVisible()) {
				GUI.frame.dispose();
			}
			setInvokedByModify(false);
			createNewData();
		}
		private static final long serialVersionUID = 1L;
	};

	private Action openCredits = new AbstractAction("Credits") {
		@Override
		public void actionPerformed(ActionEvent e) {
			disposeGUIframe();
			creditsDialog();
		}
		private static final long serialVersionUID = 1L;
	};

	private Action loadPopulation = new AbstractAction("Insert Sample Population") {
		@Override
		public void actionPerformed(ActionEvent e) {
			disposeGUIframe();
			if (simRunner.getSimulation() != null) {

				ReplaceMergeCancel option = openInsertSamplePopReplaceDialog();

				if (option != ReplaceMergeCancel.CANCEL){
					//Select the XML file
					FileDialog theDialog = new FileDialog(GUI.frame,
							"Choose a file to load", FileDialog.LOAD);
					theDialog.setFile("*.xml");
					theDialog.setVisible(true);
					if (theDialog.getFile() != null) {
						//Load the XML file
						simRunner.getSimulation().theEnvironment.insertPopulation(theDialog.getDirectory() + theDialog.getFile(), option == ReplaceMergeCancel.REPLACE);
					}
				}

			}
		}
		private static final long serialVersionUID = 1L;
	};

	private Action setLog = new AbstractAction("Log") {
		@Override
		public void actionPerformed(ActionEvent e) {
			pauseUI();
			disposeGUIframe();

			if (simRunner.getSimulation() == null) {
				JOptionPane.showMessageDialog(GUI.frame,
						"To create a log file, please press \"OK\" to launch the Cobweb Application first.");
			} else {
				logFileDialog();
			}

		}
		private static final long serialVersionUID = 1L;
	};

	/**
	 * @see CobwebApplication#openCurrentData()
	 */
	private Action modifySimulation = new AbstractAction("Modify Simulation") {
		@Override
		public void actionPerformed(ActionEvent e) {
			pauseUI();
			if (GUI.frame != null && GUI.frame.isVisible()) {
				GUI.frame.dispose();
			}
			setInvokedByModify(true);
			openCurrentData();
		}
		private static final long serialVersionUID = 1L;
	};

	/**
	 * @see CobwebApplication#openCurrentFile()
	 */

	private Action modifySimulationFile = new AbstractAction("Modify Simulation File") {
		@Override
		public void actionPerformed(ActionEvent e) {
			pauseUI();
			if (GUI.frame != null && GUI.frame.isVisible()) {
				GUI.frame.dispose();
			}
			setInvokedByModify(true);
			openCurrentFile();
		}
		private static final long serialVersionUID = 1L;
	};


	private Action setObservationMode = new AbstractAction("Observation Mode") {
		@Override
		public void actionPerformed(ActionEvent e) {
			disposeGUIframe();
			displayPanel.setMouseMode(MouseMode.Observe);
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
			disposeGUIframe();
			if (simRunner.getSimulation() != null) {
				simRunner.getSimulation().theEnvironment.clearStones();
				simRunner.getSimulation().theEnvironment.clearFood();
				simRunner.getSimulation().theEnvironment.clearAgents();
				simRunner.getSimulation().theEnvironment.clearWaste();
				update(true);
			}
		}
		private static final long serialVersionUID = 1L;
	};

	private Action removeAllStones = new AbstractAction("Remove All Stones") {
		@Override
		public void actionPerformed(ActionEvent e) {
			disposeGUIframe();
			if (simRunner.getSimulation() != null) {
				simRunner.getSimulation().theEnvironment.clearStones();
				update(true);
			}
		}
		private static final long serialVersionUID = 1L;
	};

	private Action removeAllFood = new AbstractAction("Remove All Food") {
		@Override
		public void actionPerformed(ActionEvent e) {
			disposeGUIframe();
			if (simRunner.getSimulation() != null) {
				simRunner.getSimulation().theEnvironment.clearFood();
				update(true);
			}
		}
		private static final long serialVersionUID = 1L;
	};

	private Action removeAllAgents = new AbstractAction("Remove All Agents") {
		@Override
		public void actionPerformed(ActionEvent e) {
			disposeGUIframe();
			if (simRunner.getSimulation() != null) {
				simRunner.getSimulation().theEnvironment.clearAgents();
				update(true);
			}
		}
		private static final long serialVersionUID = 1L;
	};

	private Action removeAllWaste = new AbstractAction("Remove All Waste") {
		@Override
		public void actionPerformed(ActionEvent e) {
			disposeGUIframe();
			if (simRunner.getSimulation() != null) {
				simRunner.getSimulation().theEnvironment.clearWaste();
				update(true);
			}
		}
		private static final long serialVersionUID = 1L;
	};

	private Action report = new AbstractAction("Report") {
		@Override
		public void actionPerformed(ActionEvent e) {
			pauseUI();
			disposeGUIframe();
			if (simRunner.getSimulation() == null) {
				JOptionPane.showMessageDialog(GUI.frame,
						"To create a report file, please press \"OK\" to launch the Cobweb Application first.");
			} else {
				reportDialog();
			}
		}
		private static final long serialVersionUID = 1L;
	};

	private Action retrieveDefaultData = new AbstractAction("Retrieve Default Data") {
		@Override
		public void actionPerformed(ActionEvent e) {
			pauseUI();
			if (GUI.frame != null && GUI.frame.isVisible()) {
				GUI.frame.dispose();
			}
			setInvokedByModify(false);
			retrieveDefaultData();
		}
		private static final long serialVersionUID = 1L;
	};

	private Action saveConfig = new AbstractAction("Save") {
		@Override
		public void actionPerformed(ActionEvent e) {
			pauseUI();
			disposeGUIframe();
			if (GUI.frame == null || !GUI.frame.isVisible()) {
				GUI.createAndShowGUI(CobwebApplication.this, getCurrentFile(), true);
			}
			saveFileDialog();
			if (GUI.frame != null && simRunner.getSimulation() != null) {
				GUI.frame.dispose();
			}
		}
		private static final long serialVersionUID = 1L;
	};

	private Action savePopulation = new AbstractAction("Save Sample Population") {
		@Override
		public void actionPerformed(ActionEvent e) {

			disposeGUIframe();
			if (simRunner.getSimulation() != null) {

				// open dialog to choose population size to be saved
				HashMap<String, Object> result = openSaveSamplePopOptionsDialog();
				if (result != null){
					String option = (String)result.get("option");
					int amount = (Integer)result.get("amount");

					if (option != null && amount != -1) {
						// Open file dialog box
						FileDialog theDialog = new FileDialog(GUI.frame,
								"Choose a file to save state to", FileDialog.SAVE);
						theDialog.setFile("*.xml");
						theDialog.setVisible(true);
						if (theDialog.getFile() != null) {

							//Save population in the specified file.
							simRunner.getSimulation().theEnvironment.savePopulation(theDialog.getDirectory() + theDialog.getFile(), option, amount);
						}
					}
				}
			}
		}
		private static final long serialVersionUID = 1L;
	};

	private Action setModeStones = new AbstractAction("Select Stones") {
		@Override
		public void actionPerformed(ActionEvent e) {
			disposeGUIframe();
			displayPanel.setMouseMode(MouseMode.AddStone);
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
	private ReplaceMergeCancel openInsertSamplePopReplaceDialog() {
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

		JTextField amount = new JTextField(30);

		Object[] array = {
				new JLabel("Select an option:"),
				b1,
				b2,
				new JLabel("Enter the number for the selected option:"),
				amount
		};

		int res = JOptionPane.showConfirmDialog(null, array, "Select",
				JOptionPane.OK_CANCEL_OPTION);

		if (res == -1 || res == 2)
			return null;

		int am = -1;

		HashMap<String, Object> result = new HashMap<String, Object>();

		try {
			am = Integer.parseInt(amount.getText());
			if (am < 1)
				throw new Exception();
		} catch (Exception e) {
			JOptionPane.showMessageDialog((Component)null, "Invalid input.");
			return null;

		}

		result.put("amount", am);

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
		pauseButton.repaint();
	}

	@Override
	public void onStopped() {
		pauseButton.repaint();
		update(true);
	}

	@Override
	public void onStarted() {
		pauseButton.repaint();
	}

} // CobwebApplication
