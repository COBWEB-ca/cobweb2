/*
 * $$$$$: Comments by Liang $$$$$$: Codes modified and/or added by Liang
 */

package driver.config;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.FileDialog;
import java.awt.FlowLayout;
import java.awt.event.ActionListener;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.swing.BorderFactory;
import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JComponent;
import javax.swing.JFrame;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JTabbedPane;
import javax.swing.JTable;
import javax.swing.LookAndFeel;
import javax.swing.table.DefaultTableCellRenderer;

import cobweb.TypeColorEnumeration;
import cwcore.ComplexAgentLearning;
import driver.CobwebApplication;
import driver.CobwebUserException;
import driver.SettingsPanel;
import driver.SimulationConfig;

/**
 * Simulation configuration dialog
 *
 * @author time itself
 *
 */
public class GUI extends JFrame {

	private final class OkButtonListener implements ActionListener {
		public void actionPerformed(java.awt.event.ActionEvent evt) {

			try {
				validateSettings();
			} catch (IllegalArgumentException ex) {
				throw new CobwebUserException("Parameter error: " + ex.getMessage(), ex);
			}

			/* write UI info to xml file */
			try {
				p.write(new FileOutputStream(datafile));
			} catch (java.io.IOException ex) {
				throw new CobwebUserException("Cannot write file! Make sure your file is not read-only.", ex);
			}

			/* create a new parser for the xml file */
			try {
				p = new SimulationConfig(datafile);
			} catch (FileNotFoundException ex) {
				throw new CobwebUserException("Cannot open file!", ex);
			}

			CA.openFile(p);
			if (!datafile.equals(CA.getCurrentFile())) {
				CA.setCurrentFile(datafile);
			}
			frame.setVisible(false);
			frame.dispose();

			if (CA.getUI() != null) {
				if (CA.isInvokedByModify() == false) {
					CA.getUI().reset();
				}
				CA.getUI().setRunnable(true);
			}
		}


	}

	protected void validateSettings() {
		/*
		 * this fragment of code is necessary to update the last cell of the table before saving it
		 */
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
	}

	private final class SaveAsButtonListener implements java.awt.event.ActionListener {
		public void actionPerformed(java.awt.event.ActionEvent e) {
			try {
				validateSettings();
			} catch (IllegalArgumentException ex) {
				throw new CobwebUserException("Parameter error: " + ex.getMessage(), ex);
			}

			openFileDialog();
		}
	}

	private static final String WINDOW_TITLE = "Simulation Settings";

	static void colorHeaders(JTable ft, boolean skipFirst) {
		TypeColorEnumeration tc = TypeColorEnumeration.getInstance();

		int firstCol = skipFirst ? 1 : 0;

		for (int col = firstCol; col < ft.getColumnCount(); col++) {
			DefaultTableCellRenderer r = new DefaultTableCellRenderer();
			r.setBackground(tc.getColor(col - firstCol, 0));
			ft.getColumnModel().getColumn(col).setHeaderRenderer(r);
			LookAndFeel.installBorder(ft.getTableHeader(), "TableHeader.cellBorder");
		}
	}
	static void makeGroupPanel(JComponent target, String title) {
		target.setBorder(BorderFactory.createTitledBorder(BorderFactory.createLineBorder(Color.blue), title));
	}

	// //////////////////////////////////////////////////////////////////////////////////////////////

	static void updateTable(JTable table) {
		int row = table.getEditingRow();
		int col = table.getEditingColumn();
		if (table.isEditing()) {
			table.getCellEditor(row, col).stopCellEditing();
		}
	}
	private EnvironmentConfigPage environmentPage;

	private ResourceConfigPage resourcePage;

	private GeneticConfigPage geneticPage;

	private JTabbedPane tabbedPane;

	private JButton ok;

	private JButton save;

	public static JFrame frame;

	private SimulationConfig p;

	private final CobwebApplication CA;

	private String datafile;

	SettingsPanel controllerPanel;
	public static final long serialVersionUID = 0xB9967684A8375BC0L;
	/**
	 * Create the GUI and show it. For thread safety, this method should be invoked from the event-dispatching thread.
	 */
	public static void createAndShowGUI(CobwebApplication ca, String filename, boolean allowModify) {
		if (ca.getUI() != null) {
			ca.getUI().setRunnable(false);
		}

		// Create and set up the content pane.

		frame = new GUI(ca, filename, allowModify && (ca.getUI() != null));
		frame.setVisible(true);

	}

	private Logger myLogger = Logger.getLogger("COBWEB2");

	private DiseaseConfigPage diseaseConfigPage;

	private TemperatureConfigPage tempPage;

	private AgentConfigPage agentPage;

	private ProductionConfigPage prodPage;

	private FoodwebConfigPage foodwebPage;

	private PDConfigPage pdPage;

	private LearningConfigPage learnPage;

	public GUI() {
		super();
		CA = null;
	}

	// GUI Special Constructor
	public GUI(CobwebApplication ca, String filename, boolean allowKeep) {
		super(WINDOW_TITLE);
		//setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);

		JPanel j = new JPanel();
		j.setLayout(new BoxLayout(j, BoxLayout.Y_AXIS));

		CA = ca;
		datafile = filename;
		tabbedPane = new JTabbedPane();

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

		tabbedPane = new JTabbedPane();

		environmentPage = new EnvironmentConfigPage(p.getEnvParams(), allowKeep);

		tabbedPane.addTab("Environment", environmentPage.getPanel());

		setupConfigPages();

		environmentPage.addAgentNumChangeListener(new AgentNumChangeListener() {

			@Override
			public void AgentNumChanged(int oldNum, int newNum) {
				p.SetAgentTypeCount(newNum);
				setupConfigPages();
			}
		});

		ok = new JButton("OK");
		ok.setMaximumSize(new Dimension(80, 20));
		ok.addActionListener(new OkButtonListener());

		save = new JButton("Save As...");
		save.setMaximumSize(new Dimension(80, 20));
		save.addActionListener(new SaveAsButtonListener());

		JPanel buttons = new JPanel();
		buttons.setLayout(new FlowLayout(FlowLayout.RIGHT));
		buttons.add(save);
		buttons.add(ok);

		// Add the tabbed pane to this panel.
		j.add(tabbedPane, BorderLayout.CENTER);
		j.add(buttons, BorderLayout.SOUTH);
		j.setPreferredSize(new Dimension(750, 513));

		getRootPane().setDefaultButton(ok);
		add(j);
		pack();
		File filePath;
		if (p.getFilename() == null ||
				(filePath = new File(p.getFilename())).getName() == null)
			filePath = new File(CobwebApplication.DEFAULT_DATA_FILE_NAME + CobwebApplication.CONFIG_FILE_EXTENSION);
		setTitle(WINDOW_TITLE + " - " + filePath.getName());
	}
	public SimulationConfig getParser() {
		return p;
	}


	// $$$$$$ This openFileDialog method is invoked by pressing the "Save" button
	public void openFileDialog() {
		FileDialog theDialog = new FileDialog(frame, "Choose a file to save state to", java.awt.FileDialog.SAVE);
		theDialog.setFile("*.xml");
		theDialog.setVisible(true);
		if (theDialog.getFile() != null) {
			// $$$$$$ Check if the saving filename is one of the names reserved by CobwebApplication. Feb 8
			// $$$$$$$$$$$$$$$$$$$$$$$$$$ Block silenced by Andy due to the annoyingness of this feature. May 7, 2008
			// String savingFileName;
			// savingFileName = theDialog.getFile();

			// Block silenced, see above.

			/*
			 * if ( (savingFileName.contains(CobwebApplication.INITIAL_OR_NEW_INPUT_FILE_NAME) != false) ||
			 * (savingFileName.contains(CobwebApplication.CURRENT_DATA_FILE_NAME) != false) //$$$$$ added for
			 * "Modify Current Data" || (savingFileName.contains(CobwebApplication.DEFAULT_DATA_FILE_NAME) != false)) {
			 * JOptionPane.showMessageDialog(GUI.this, "Save State: The filename\"" + savingFileName +
			 * "\" is reserved by Cobweb Application.\n" + "                       Please choose another file to save.",
			 * "Warning", JOptionPane.WARNING_MESSAGE); // $$$$$$ modified on Feb 22 openFileDialog(); } else {
			 */// $$$$$ If filename not reserved. Feb 8
			try {
				// $$$$$$ The following block added to handle a readonly file. Feb 22
				String savingFile = theDialog.getDirectory() + theDialog.getFile();
				File sf = new File(savingFile);
				if ((sf.isHidden() != false) || ((sf.exists() != false) && (sf.canWrite() == false))) {
					JOptionPane.showMessageDialog(
							GUI.frame, // $$$$$$ change from "this" to "GUI.frame". Feb 22
							"Caution:  File \"" + savingFile + "\" is NOT allowed to be written to.", "Warning",
							JOptionPane.WARNING_MESSAGE);
				} else {
					// $$$$$ The following block used to be the original code. Feb 22
					p.write(new FileOutputStream(theDialog.getDirectory() + theDialog.getFile()));

					p = new SimulationConfig(theDialog.getDirectory() + theDialog.getFile());
					CA.openFile(p);
					if (!datafile.equals(CA.getCurrentFile())) {
						CA.setCurrentFile(datafile);
					} // $$$$$$ added on Mar 14
					frame.setVisible(false);
					frame.dispose(); // $$$$$$ Feb 28
					// $$$$$$ Added on Mar 14
					if (CA.getUI() != null) {
						if (CA.isInvokedByModify() == false) {
							CA.getUI().reset(); // reset tick
							// CA.refresh(CA.getUI());
							// if (CA.tickField != null && !CA.tickField.getText().equals(""))
							// {CA.tickField.setText("");} // $$$$$$ Mar 17
						}
						CA.getUI().setRunnable(true);
						CA.getUI().refresh(true);
					}
				}
			} catch (IOException ex) {
				myLogger.log(Level.WARNING, "Cannot save config", ex);
				JOptionPane.showMessageDialog(CA, // $$$$$$ added on Apr 22
						"Save failed: " + ex.getMessage(), "Warning", JOptionPane.WARNING_MESSAGE);
			}
			// }
		}
	}

	private void setDefault() {
		p = new SimulationConfig();
	}

	private void setupConfigPages() {



		/* Resources panel */
		removeOldPage(resourcePage);
		resourcePage = new ResourceConfigPage(p.getFoodParams());
		tabbedPane.addTab("Resources", resourcePage.getPanel());

		/* Agents' panel */
		removeOldPage(agentPage);
		agentPage = new AgentConfigPage(p.getAgentParams());
		tabbedPane.addTab("Agents", agentPage.getPanel());

		/* Production panel */
		removeOldPage(prodPage);
		prodPage = new ProductionConfigPage(p.getProdParams());
		tabbedPane.addTab("Production", prodPage.getPanel());

		removeOldPage(foodwebPage);
		foodwebPage = new FoodwebConfigPage(p.getAgentParams());
		tabbedPane.addTab("Food Web", foodwebPage.getPanel());

		removeOldPage(pdPage);
		if (p.getEnvParams().prisDilemma) {
			pdPage = new PDConfigPage(p.getEnvParams().pdParams);
			tabbedPane.addTab("PD Options", pdPage.getPanel());
		}

		removeOldPage(geneticPage);
		geneticPage = new GeneticConfigPage(p.getGeneticParams(), p.getEnvParams().getAgentTypes());
		JComponent panelGA = geneticPage.getPanel();
		tabbedPane.addTab("Genetics", panelGA);

		if (controllerPanel != null) {
			tabbedPane.remove(controllerPanel);
		}
		controllerPanel = new AIPanel();
		controllerPanel.bindToParser(p);
		tabbedPane.addTab("AI", controllerPanel);

		removeOldPage(diseaseConfigPage);
		diseaseConfigPage = new DiseaseConfigPage(p.getDiseaseParams());
		tabbedPane.addTab("Disease", diseaseConfigPage.getPanel());

		removeOldPage(tempPage);
		tempPage = new TemperatureConfigPage(p.getTempParams());
		tabbedPane.addTab("Abiotic Factor", tempPage.getPanel());


		removeOldPage(learnPage);
		if (p.getEnvParams().agentName.equals(ComplexAgentLearning.class.getName())) {
			learnPage = new LearningConfigPage(p.getLearningParams().getLearningAgentParams());
			tabbedPane.addTab("Learning", learnPage.getPanel());
		}
	}

	private void removeOldPage(ConfigPage r) {
		if (r != null) {
			tabbedPane.remove(r.getPanel());
		}
	}

}
