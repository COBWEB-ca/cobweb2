package org.cobweb.cobweb2.ui.swing.config;

import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.LinkedList;
import java.util.List;
import java.util.regex.Pattern;

import javax.swing.AbstractListModel;
import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JComponent;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.ListSelectionModel;
import javax.swing.table.AbstractTableModel;
import javax.swing.table.TableColumnModel;

import org.cobweb.cobweb2.genetics.GeneticParams;
import org.cobweb.cobweb2.interconnect.Phenotype;
import org.cobweb.cobweb2.ui.swing.CobwebUserException;
import org.cobweb.swingutil.binding.EnumComboBoxModel;

public class GeneticConfigPage implements ConfigPage {

	private static class ListManipulator<T> extends AbstractListModel {
		private static final long serialVersionUID = 6521578944695127260L;

		List<T> items;

		public ListManipulator(List<T> list) {
			items = list;
		}

		public void addItem(T item) {
			items.add(item);
			fireIntervalAdded(this, items.size() - 1, items.size() - 1);
		}

		public Object getElementAt(int index) {
			return items.get(index);
		}

		public int getSize() {
			return items.size();
		}

		public T removeItem(T item) {
			int index = items.indexOf(item);
			fireIntervalRemoved(this, index, index);
			items.remove(item);
			return item;
		}
	}

	/**
	 * Default genes. <code>default.get(gene)[agent] = x;</code>
	 */
	List<int[]> defaults = new LinkedList<int[]>();

	private class GenesTableModel extends AbstractTableModel {

		private static final long serialVersionUID = 8849213073862759751L;

		public int getColumnCount() {
			return 1 + agentTypes;
		}

		public int getRowCount() {
			return phenosUsed.size();
		}

		public Object getValueAt(int rowIndex, int columnIndex) {
			if ( columnIndex == 0)
				return phenosUsed.get(rowIndex).toString();

			return Integer.toString(defaults.get(rowIndex)[columnIndex - 1], 2);
		}

		@Override
		public void setValueAt(Object value, int rowIndex, int columnIndex) {
			if (columnIndex < 1)
				throw new IllegalArgumentException("Cannot set that column");

			if (value instanceof String) {
				String s = (String) value;
				if (!geneticStringPatern.matcher(s).matches()) {
					throw new CobwebUserException("Please enter a binary string!");
				}
				int v = Integer.parseInt(s, 2);
				defaults.get(rowIndex)[columnIndex - 1] = v;
			}

		}

		@Override
		public boolean isCellEditable(int rowIndex, int columnIndex) {
			return columnIndex != 0;
		}

		@Override
		public String getColumnName(int column) {
			if (column == 0)
				return "Phenotype";
			return "Agent " + column;
		}
	}

	/** The list of mutable phenotypes shown on Genetic Algorithm tab. */
	private JList listAvailable;

	private JPanel myPanel;

	private static final Pattern geneticStringPatern = Pattern.compile("^[01]*$");

	private GeneticParams params;

	private int agentTypes;

	private class AddListener implements ActionListener {
		public void actionPerformed(ActionEvent e) {
			for (Object o : listAvailable.getSelectedValues()) {
				Phenotype p = (Phenotype) o;
				addGene(p);
			}
			modelSelected.fireTableDataChanged();
		}
	}

	private class RemoveListener implements ActionListener {
		public void actionPerformed(ActionEvent e) {
			int[] z = listSelected.getSelectedRows();
			for (int i = z.length - 1; i >= 0; i--) {
				int o = z[i];
				removeGene(phenosUsed.get(o));
			}
			modelSelected.fireTableDataChanged();
		}
	}

	public GeneticConfigPage(GeneticParams params, int agentTypes) {
		this.params = params;
		this.agentTypes = agentTypes;

		myPanel = new JPanel();
		myPanel.setLayout(new BoxLayout(myPanel, BoxLayout.X_AXIS));

		JComponent phenotypeScroller = setupPhenotypeList();
		myPanel.add(phenotypeScroller);

		JComponent phenoSelectedScroller = setupSelectedList();



		JButton addPheno = new JButton("Add ^");
		addPheno.addActionListener(new AddListener());
		JButton remPheno = new JButton("Remove <");
		remPheno.addActionListener(new RemoveListener());
		JPanel buttons = new JPanel();
		buttons.add(addPheno);
		buttons.add(remPheno);

		JPanel ga_combined_panel = new JPanel(new BorderLayout());

		JPanel meiosis_mode_panel = makeMeiosisConfig();
		JPanel updateConfig = makeUpdateConfig();

		ga_combined_panel.add(meiosis_mode_panel, BorderLayout.NORTH);
		ga_combined_panel.add(updateConfig, BorderLayout.SOUTH);
		GUI.makeGroupPanel(ga_combined_panel, "Tracking");

		JPanel gene_info_display = new JPanel();
		gene_info_display.setLayout(new BoxLayout(gene_info_display, BoxLayout.Y_AXIS));
		gene_info_display.add(phenoSelectedScroller);
		gene_info_display.add(buttons);
		gene_info_display.add(ga_combined_panel);


		myPanel.add(gene_info_display);

		GUI.makeGroupPanel(myPanel, "Genetic Algorithm Parameters");
	}

	public void removeGene(Phenotype phenotype) {
		phenosAvailable.addItem(phenotype);
		int i = phenosUsed.indexOf(phenotype);
		phenosUsed.remove(phenotype);
		defaults.remove(i);
	}

	public void addGene(Phenotype p) {
		phenosUsed.add(phenosAvailable.removeItem(p));
		int[] temp = new int[agentTypes];
		for (int i = 0; i < temp.length; i++) {
			temp[i] = 30;
		}
		defaults.add(temp);
	}

	private JTable listSelected;

	private JComponent setupSelectedList() {
		phenosUsed = new LinkedList<Phenotype>();

		modelSelected = new GenesTableModel();
		listSelected = new MixedValueJTable();

		listSelected.setModel(modelSelected);

		int j = 0;
		for (Phenotype p : params.phenotype)
			for (Phenotype p2 : new LinkedList<Phenotype>(phenoAvailable))
				if (p.equals(p2)) {
					addGene(p2);
					for (int i = 0; i < agentTypes; i++) {
						defaults.get(defaults.size() - 1)[i] = Integer.parseInt(params.geneValues[i][j], 2);
					}
					j++;
				}

		JScrollPane phenotypeScroller = new JScrollPane(listSelected);

		TableColumnModel agParamColModel = listSelected.getColumnModel();

		// Get the column at index pColumn, and set its preferred width.
		agParamColModel.getColumn(0).setPreferredWidth(200);

		GUI.colorHeaders(listSelected, true);

		GUI.makeGroupPanel(phenotypeScroller, "Selected Phenotypes");
		return phenotypeScroller;
	}

	public JPanel getPanel() {
		return myPanel;
	}

	private JPanel makeMeiosisConfig() {
		JComboBox meiosis_mode = new JComboBox(new EnumComboBoxModel(this.params.meiosisMode, "mode"));
		JPanel meiosis_mode_panel = new JPanel(new BorderLayout());
		meiosis_mode_panel.add(new JLabel("Mode of Meiosis"), BorderLayout.NORTH);
		meiosis_mode_panel.add(meiosis_mode, BorderLayout.CENTER);
		return meiosis_mode_panel;
	}

	private JPanel makeUpdateConfig() {
		JPanel gene_check_boxes = new JPanel(new BorderLayout());
		return gene_check_boxes;
	}

	private ListManipulator<Phenotype> phenosAvailable;
	private List<Phenotype> phenosUsed;

	private GenesTableModel modelSelected;

	List<Phenotype> phenoAvailable = new LinkedList<Phenotype>();

	private JScrollPane setupPhenotypeList() {
		//TODO fix this weird selection process
		phenoAvailable = new LinkedList<Phenotype>(new Phenotype().getPossibleValues());
		phenoAvailable.remove(0);
		phenosAvailable = new ListManipulator<Phenotype>(phenoAvailable);

		listAvailable = new JList(phenosAvailable);
		listAvailable.setSelectionMode(ListSelectionModel.MULTIPLE_INTERVAL_SELECTION);
		listAvailable.setLayoutOrientation(JList.VERTICAL_WRAP);
		listAvailable.setVisibleRowCount(-1);
		JScrollPane phenotypeScroller = new JScrollPane(listAvailable);
		phenotypeScroller.setPreferredSize(new Dimension(220, 500));

		GUI.makeGroupPanel(phenotypeScroller, "Agent Parameter Selection");
		return phenotypeScroller;
	}

	public void validateUI() throws IllegalArgumentException {
		params.geneCount = phenosUsed.size();
		params.phenotype = phenosUsed.toArray(new Phenotype[0]);

		params.geneLength = 8;
		params.geneValues = new String[agentTypes][params.geneCount];
		for (int g = 0; g < params.geneCount; g++) {
			for (int a = 0; a < agentTypes; a++) {
				params.geneValues[a][g] = Integer.toString(defaults.get(g)[a], 2);
			}
		}

	}

}