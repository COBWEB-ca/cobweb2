package driver.config;

import ga.GeneticParams;
import ga.GeneticParams.Phenotype;

import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.event.ActionListener;
import java.lang.reflect.Field;
import java.text.NumberFormat;
import java.util.regex.Pattern;

import javax.swing.BoxLayout;
import javax.swing.DefaultListModel;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JComponent;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.ListSelectionModel;

import cobweb.params.ConfDisplayName;

public class GeneticConfigPage implements ConfigPage {
	private class myActionListener implements ActionListener {
		public void actionPerformed(java.awt.event.ActionEvent e) {
			if (e.getSource().equals(link_gene_1)) {
			} else if (e.getSource().equals(link_gene_2)) {
			} else if (e.getSource().equals(link_gene_3)) {
			} else if (e.getSource().equals(meiosis_mode)) {
			}
		}
	}

	/** The list of mutable phenotypes shown on Genetic Algorithm tab. */
	private JList mutable_phenotypes;
	
	/** The TextFields and Buttons of the Genetic Algorithm tab. */
	private JButton link_gene_1 = new JButton("Link to Gene 1");
	private JButton link_gene_2 = new JButton("Link to Gene 2");
	private JButton link_gene_3 = new JButton("Link to Gene 3");

	private JComboBox meiosis_mode;
	
	/** The TextFields that store the genetic bits of the agents. */
	private JTable genetic_table;

	/** Controls whether or not the distribution of gene value of an agent type is tracked and output. */
	private BoundCheckBox track_gene_value_distribution;

	/** The number of chart updates per time step. */
	private BoundJFormattedTextField chart_update_frequency;

	private JPanel myPanel;

	static final Pattern geneticStringPatern = Pattern.compile("^[01]{8}$");

	private GeneticParams params;

	private int agentTypes;

	public GeneticConfigPage(GeneticParams params, int agentTypes) {
		myPanel = new JPanel();
		this.params = params;
		this.agentTypes = agentTypes;

		myPanel.setLayout(new BoxLayout(myPanel, BoxLayout.Y_AXIS));
		JComponent phenotypeScroller = setupPhenotypeList();

		
		JPanel gene_1 = new JPanel();
		gene_1.add(link_gene_1);

		JPanel gene_2 = new JPanel();
		gene_2.add(link_gene_2);

		JPanel gene_3 = new JPanel();
		gene_3.add(link_gene_3);
		
		JPanel gene_info_display = new JPanel(new BorderLayout());
		gene_info_display.add(gene_1, BorderLayout.WEST);
		gene_info_display.add(gene_2, BorderLayout.CENTER);
		gene_info_display.add(gene_3, BorderLayout.EAST);
		
		
		meiosis_mode = new JComboBox(new EnumComboBoxModel(this.params.meiosisMode, "mode"));

		JPanel meiosis_mode_panel = new JPanel(new BorderLayout());
		meiosis_mode_panel.add(new JLabel("Mode of Meiosis"), BorderLayout.NORTH);
		meiosis_mode_panel.add(meiosis_mode, BorderLayout.CENTER);

		track_gene_value_distribution = new BoundCheckBox(this.params, "trackValues");
		track_gene_value_distribution.setText(track_gene_value_distribution.getLabel());
		chart_update_frequency = new BoundJFormattedTextField(this.params, "updateFrequency", NumberFormat.getIntegerInstance());
		chart_update_frequency.setColumns(4);
		// Checkboxes and TextAreas
		JPanel chart_update_frequency_panel = new JPanel();
		chart_update_frequency_panel.add(new JLabel(chart_update_frequency.getLabel()));
		chart_update_frequency_panel.add(chart_update_frequency);
		JPanel gene_check_boxes = new JPanel(new BorderLayout());
		gene_check_boxes.add(track_gene_value_distribution, BorderLayout.CENTER);
		gene_check_boxes.add(chart_update_frequency_panel, BorderLayout.SOUTH);

		// Combine Checkboxes and Dropdown menu
		JPanel ga_combined_panel = new JPanel(new BorderLayout());
		ga_combined_panel.add(meiosis_mode_panel, BorderLayout.EAST);
		ga_combined_panel.add(gene_check_boxes, BorderLayout.WEST);

		gene_info_display.add(ga_combined_panel, BorderLayout.SOUTH);

//		genetic_table.setPreferredScrollableViewportSize(new Dimension(150, 160));
//		genetic_table.setAutoResizeMode(JTable.AUTO_RESIZE_ALL_COLUMNS);

		GUI.makeGroupPanel(phenotypeScroller, "Agent Parameter Selection");

		myPanel.add(phenotypeScroller);

//		JScrollPane geneScroll = new JScrollPane(genetic_table);

//		GUI.makeGroupPanel(geneScroll, "Gene Bindings");
//
//		myPanel.add(geneScroll);

		myPanel.add(gene_info_display);

		/** Listeners of JButtons, JComboBoxes, and JCheckBoxes */
		ActionListener listener = new myActionListener();
		link_gene_1.addActionListener(listener);
		link_gene_2.addActionListener(listener);
		link_gene_3.addActionListener(listener);

		GUI.makeGroupPanel(myPanel, "Genetic Algorithm Parameters");
	}

	private JScrollPane setupPhenotypeList() {
		DefaultListModel mutable_list_model = new DefaultListModel();
		for (Field element : Phenotype.getBindables()) {
			if (element == null) {
				mutable_list_model.addElement("[No Phenotype]");
			} else {
				mutable_list_model.addElement(element.getAnnotation(ConfDisplayName.class).value());
			}
		}

		mutable_phenotypes = new JList(mutable_list_model);
		mutable_phenotypes.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
		mutable_phenotypes.setLayoutOrientation(JList.VERTICAL_WRAP);
		mutable_phenotypes.setVisibleRowCount(-1);
		JScrollPane phenotypeScroller = new JScrollPane(mutable_phenotypes);
		phenotypeScroller.setPreferredSize(new Dimension(150, 200));
		return phenotypeScroller;
	}

	public JPanel getPanel() {
		return myPanel;
	}

	public void validateUI() throws IllegalArgumentException {
		GUI.updateTable(genetic_table);

	}

}