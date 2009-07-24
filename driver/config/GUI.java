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
import javax.swing.JCheckBox;
import javax.swing.JComponent;
import javax.swing.JFrame;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTabbedPane;
import javax.swing.JTable;
import javax.swing.LookAndFeel;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.TableColumnModel;

import cobweb.TypeColorEnumeration;
import cwcore.complexParams.FoodwebParams;
import driver.CobwebApplication;
import driver.CobwebUserException;
import driver.Parser;
import driver.SettingsPanel;

/**
 * Simulation configuration dialog
 *
 * @author time itself
 *
 */
public class GUI extends JFrame {

	private final class OkButtonListener implements ActionListener {
		public void actionPerformed(java.awt.event.ActionEvent evt) {

			/*
			 * this fragment of code is necessary to update the last cell of the table before saving it
			 */
			environmentPage.validateUI();
			updateTable(resourceParamTable);
			updateTable(agentParamTable);
			updateTable(foodTable);
			updateTable(tablePD);
			geneticPage.validateUI();


			/* write UI info to xml file */
			try {
				p.write(new FileOutputStream(datafile));
			} catch (java.io.IOException ex) {
				throw new CobwebUserException("Cannot write file! Make sure your file is not read-only.", ex);
			}

			/* create a new parser for the xml file */
			try {
				p = new Parser(datafile);
			} catch (FileNotFoundException ex) {
				throw new CobwebUserException("Cannot write file! Make sure your file is not read-only.", ex);
			}
			CA.openFile(p);
			if (!datafile.equals(CA.getCurrentFile())) {
				CA.setCurrentFile(datafile);
			} // $$$$$$ added on Mar 14
			frame.setVisible(false);
			frame.dispose(); // $$$$$$ added on Feb 28
			// $$$$$$ Added on Mar 14
			if (CA.getUI() != null) {
				if (CA.isInvokedByModify() == false) {
					CA.getUI().reset(); // reset tick
					// CA.refresh(CA.getUI());
					// if (CA.tickField != null && !CA.tickField.getText().equals("")) {CA.tickField.setText("");}
					// // $$$$$$ Mar 17
				}
				CA.getUI().setRunnable(true);
				//CA.getUI().refresh(10);
			}
		}
	}

	private final class SaveAsButtonListener implements java.awt.event.ActionListener {
		public void actionPerformed(java.awt.event.ActionEvent e) {
			try {
				environmentPage.validateUI();
				updateTable(resourceParamTable);
				updateTable(agentParamTable);
				updateTable(foodTable);
				updateTable(tablePD);
				geneticPage.validateUI();
			} catch (IllegalArgumentException ex) {
				throw new CobwebUserException("Parameter error: " + ex.getMessage(), ex);
			}

			openFileDialog();
		}
	}

	final String TickScheduler = "cobweb.TickScheduler";
	JCheckBox FoodWeb;

	// //////////////////////////////////////////////////////////////////////////////////////////////

	EnvironmentConfigPage environmentPage;
	GeneticConfigPage geneticPage;

	private JTable resourceParamTable;

	private JTable agentParamTable;

	private JTable foodTable;

	private JTable tablePD;

	private JTabbedPane tabbedPane;

	private JButton ok;

	private JButton save;

	public static JFrame frame;

	Parser p;

	private final CobwebApplication CA;

	private String datafile;

	public int numAgentTypes;

	public int numFoodTypes;

	SettingsPanel controllerPanel;

	public static final long serialVersionUID = 0xB9967684A8375BC0L;

	/**
	 * Create the GUI and show it. For thread safety, this method should be invoked from the event-dispatching thread.
	 */
	public static void createAndShowGUI(CobwebApplication ca, String filename) {
		if (ca.getUI() != null) {
			ca.getUI().setRunnable(false);
		}
		// Make sure we have nice window decorations.
		// JFrame.setDefaultLookAndFeelDecorated(true);

		// Create and set up the window.


		// Create and set up the content pane.

		frame = new GUI(ca, filename, ca.getUI() != null);
		frame.setVisible(true);

		// frame.validate();
	}

	Logger myLogger = Logger.getLogger("COBWEB2");

	public GUI() {
		super();
		CA = null;
	}

	// GUI Special Constructor
	public GUI(CobwebApplication ca, String filename, boolean allowKeep) {
		super("Test Data");
		setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);

		JPanel j = new JPanel();
		j.setLayout(new BoxLayout(j, BoxLayout.Y_AXIS));

		CA = ca;
		datafile = filename;
		tabbedPane = new JTabbedPane();

		/* Environment panel - composed of 4 panels */

		File f = new File(datafile);

		if (f.exists()) {
			try {
				p = new Parser(datafile);
			} catch (Exception ex) {
				myLogger.log(Level.WARNING, "Cannot open config file", ex);
				setDefault();
			}
		} else {
			setDefault();
		}

		numAgentTypes = p.getEnvParams().agentTypeCount;
		numFoodTypes = p.getEnvParams().agentTypeCount;

		tabbedPane = new JTabbedPane();

		environmentPage = new EnvironmentConfigPage(p.getEnvParams(), allowKeep);
		tabbedPane.addTab("Environment", environmentPage.getPanel());

		/* Resources panel */
		JComponent resourcePanel = setupResourcePanel();
		tabbedPane.addTab("Resources", resourcePanel);

		/* Agents' panel */
		JComponent agentPanel = setupAgentPanel();
		tabbedPane.addTab("Agents", agentPanel);

		JComponent foodPanel = setupFoodwebPanel();
		tabbedPane.addTab("Food Web", foodPanel);

		JComponent panelPD = setupPDpannel();
		tabbedPane.addTab("PD Options", panelPD);

//		geneticPage = new GeneticConfigPage(p.getGeneticParams(), p.getEnvParams().agentTypeCount);
//		JComponent panelGA = geneticPage.getPanel();
//		tabbedPane.addTab("Genetic Algorithm", panelGA);

		controllerPanel = new AIPanel();
		controllerPanel.bindToParser(p);
		tabbedPane.addTab("AI", controllerPanel);

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
		j.setPreferredSize(new Dimension(718, 513));

		getRootPane().setDefaultButton(ok);
		add(j);
		pack();
	}

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

	public Parser getParser() {
		return p;
	}

	static void makeGroupPanel(JComponent target, String title) {
		target.setBorder(BorderFactory.createTitledBorder(BorderFactory.createLineBorder(Color.blue), title));
	}


	// $$$$$$ This openFileDialog method is invoked by pressing the "Save" button
	public void openFileDialog() {
		FileDialog theDialog = new FileDialog(frame, "Choose a file to save state to", java.awt.FileDialog.SAVE);
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

					p = new Parser(theDialog.getDirectory() + theDialog.getFile());
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
		p = new Parser();
	}

	private JComponent setupAgentPanel() {
		JComponent agentPanel = new JPanel();
		agentPanel.setLayout(new BoxLayout(agentPanel, BoxLayout.X_AXIS));
		makeGroupPanel(agentPanel, "Agent Parameters");

		agentParamTable = new MixedValueJTable();
		agentParamTable.setModel(new ConfigTableModel(p.getAgentParams(), "Agent "));

		TableColumnModel agParamColModel = agentParamTable.getColumnModel();
		// Get the column at index pColumn, and set its preferred width.
		agParamColModel.getColumn(0).setPreferredWidth(200);

		colorHeaders(agentParamTable, true);

		JScrollPane agentScroll = new JScrollPane(agentParamTable);
		// Add the scroll pane to this panel.
		agentPanel.add(agentScroll);
		return agentPanel;
	}



	private JComponent setupFoodwebPanel() {
		JComponent foodPanel = new JPanel();
		// tabbedPane.addTab("Agents", panel3);

		foodTable = new MixedValueJTable();

		FoodwebParams[] foodweb = new FoodwebParams[p.getEnvParams().agentTypeCount];
		for (int i = 0; i < p.getEnvParams().agentTypeCount; i++) {
			foodweb[i] = p.getAgentParams()[i].foodweb;
		}
		foodTable.setModel(new ConfigTableModel(foodweb, "Agent "));

		colorHeaders(foodTable, true);

		// Create the scroll pane and add the table to it.
		JScrollPane foodScroll = new JScrollPane(foodTable);

		foodPanel.setLayout(new BoxLayout(foodPanel, BoxLayout.X_AXIS));
		makeGroupPanel(foodPanel, "Food Parameters");
		foodPanel.add(foodScroll);
		return foodPanel;
	}

	private JComponent setupPDpannel() {
		JComponent panelPD = new JPanel();

		tablePD = new JTable();
		tablePD.setModel(new ConfigTableModel(p.getEnvParams().pdParams, "Value"));

		JScrollPane scrollPanePD = new JScrollPane(tablePD);

		panelPD.add(scrollPanePD, BorderLayout.CENTER);

		panelPD.setLayout(new BoxLayout(panelPD, BoxLayout.X_AXIS));
		makeGroupPanel(panelPD, "Prisoner's Dilemma Parameters");
		return panelPD;
	}

	private JComponent setupResourcePanel() {

		JComponent resourcePanel = new JPanel();
		resourceParamTable = new MixedValueJTable();
		resourceParamTable.setModel(new ConfigTableModel(p.getFoodParams(), "Food "));

		TableColumnModel colModel = resourceParamTable.getColumnModel();
		colModel.getColumn(0).setPreferredWidth(120);
		System.out.println(colModel.getColumn(0).getHeaderValue());

		colorHeaders(resourceParamTable, true);

		JScrollPane resourceScroll = new JScrollPane(resourceParamTable);
		resourcePanel.setLayout(new BoxLayout(resourcePanel, BoxLayout.X_AXIS));
		makeGroupPanel(resourcePanel, "Resource Parameters");
		resourcePanel.add(resourceScroll);
		return resourcePanel;
	}

	public static void updateTable(JTable table) {
		int row = table.getEditingRow();
		int col = table.getEditingColumn();
		if (table.isEditing()) {
			table.getCellEditor(row, col).stopCellEditing();
		}
	}

}
