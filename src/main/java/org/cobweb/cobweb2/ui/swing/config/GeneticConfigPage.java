package org.cobweb.cobweb2.ui.swing.config;

import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.event.ActionEvent;
import java.util.ArrayList;
import java.util.List;

import javax.swing.AbstractAction;
import javax.swing.Action;
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

import org.cobweb.cobweb2.core.Phenotype;
import org.cobweb.cobweb2.plugins.genetics.GeneticParams;
import org.cobweb.cobweb2.plugins.genetics.MeiosisMode;
import org.cobweb.cobweb2.ui.UserInputException;
import org.cobweb.cobweb2.ui.config.FieldPropertyAccessor;
import org.cobweb.io.ChoiceCatalog;
import org.cobweb.swingutil.ColorLookup;
import org.cobweb.swingutil.binding.EnumComboBoxModel;
import org.cobweb.util.ArrayUtilities;

public class GeneticConfigPage implements ConfigPage {

	private static class GenesTableModel extends AbstractTableModel {

		private final GeneticParams params;

		public GenesTableModel(GeneticParams params) {
			this.params = params;
		}

		private static final long serialVersionUID = 8849213073862759751L;

		@Override
		public int getColumnCount() {
			return 1 + params.agentParams.length;
		}

		@Override
		public int getRowCount() {
			return params.phenotype.length;
		}

		@Override
		public Object getValueAt(int rowIndex, int columnIndex) {
			if (columnIndex == 0)
				return params.phenotype[rowIndex].getName();

			byte gene = params.agentParams[columnIndex - 1].genes[rowIndex];
			return String.format("%8s", Integer.toBinaryString(gene)).replace(' ', '0');
		}

		@Override
		public void setValueAt(Object value, int rowIndex, int columnIndex) {
			if (columnIndex < 1)
				throw new IllegalArgumentException("Cannot set that column");

			if (value instanceof String) {
				String s = (String) value;
				try {
					byte v = Byte.parseByte(s, 2);
					params.agentParams[columnIndex - 1].genes[rowIndex] = v;
				} catch (NumberFormatException ex) {
					throw new UserInputException("Please enter a binary string " + params.geneLength + " digits long!", ex);
				}
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
	private JList<Phenotype> listAvailable;

	private JPanel myPanel;

	private GeneticParams params;

	private final ChoiceCatalog choiceCatalog;

	private Action addGene = new AbstractAction("Add ^") {
		@Override
		public void actionPerformed(ActionEvent e) {
			for (Object o : listAvailable.getSelectedValuesList()) {
				Phenotype p = (Phenotype) o;
				addGene(p);
			}
			modelSelected.fireTableDataChanged();
		}
		private static final long serialVersionUID = 1L;
	};

	private Action removeGene = new AbstractAction("Remove <") {
		@Override
		public void actionPerformed(ActionEvent e) {
			int[] selectedRows = listSelected.getSelectedRows();
			for (int i = selectedRows.length - 1; i >= 0; i--) {
				removeGene(selectedRows[i]);
			}
			modelSelected.fireTableDataChanged();
		}
		private static final long serialVersionUID = 1L;
	};

	public GeneticConfigPage(GeneticParams params, ChoiceCatalog choiceCatalog, ColorLookup agentColors) {
		this.params = params;
		this.choiceCatalog = choiceCatalog;

		myPanel = new JPanel();
		myPanel.setLayout(new BoxLayout(myPanel, BoxLayout.X_AXIS));

		JComponent phenotypeScroller = setupPhenotypeList();
		myPanel.add(phenotypeScroller);

		JComponent phenoSelectedScroller = setupSelectedList(agentColors);

		JButton addPheno = new JButton(addGene);
		JButton remPheno = new JButton(removeGene);
		JPanel buttons = new JPanel();
		buttons.add(addPheno);
		buttons.add(remPheno);

		JPanel ga_combined_panel = new JPanel(new BorderLayout());

		JPanel meiosis_mode_panel;
		try {
			meiosis_mode_panel = makeMeiosisConfig();
		} catch (NoSuchFieldException ex) {
			throw new RuntimeException(ex);
		}

		ga_combined_panel.add(meiosis_mode_panel, BorderLayout.NORTH);
		Util.makeGroupPanel(ga_combined_panel, "Tracking");

		JPanel gene_info_display = new JPanel();
		gene_info_display.setLayout(new BoxLayout(gene_info_display, BoxLayout.Y_AXIS));
		gene_info_display.add(phenoSelectedScroller);
		gene_info_display.add(buttons);
		gene_info_display.add(ga_combined_panel);


		myPanel.add(gene_info_display);

		Util.makeGroupPanel(myPanel, "Genetic Algorithm Parameters");
	}

	private void removeGene(int index) {
		List<Phenotype> phenos = ArrayUtilities.modifiableList(params.phenotype);
		Phenotype phenotype = phenos.remove(index);
		phenosAvailable.addItem(phenotype);
		params.phenotype = phenos.toArray(new Phenotype[0]);
		params.resizeGenes();
	}

	private void addGene(Phenotype p) {
		phenosAvailable.removeItem(p);
		List<Phenotype> phenos = ArrayUtilities.modifiableList(params.phenotype);
		phenos.add(p);

		params.phenotype = phenos.toArray(new Phenotype[0]);
		params.resizeGenes();
	}

	private JTable listSelected;

	private JComponent setupSelectedList(ColorLookup agentColors) {
		listSelected = new JTable();
		modelSelected = new GenesTableModel(params);
		listSelected.setModel(modelSelected);

		JScrollPane phenotypeScroller = new JScrollPane(listSelected);

		TableColumnModel agParamColModel = listSelected.getColumnModel();

		// Get the column at index pColumn, and set its preferred width.
		agParamColModel.getColumn(0).setPreferredWidth(200);

		Util.colorHeaders(listSelected, true, agentColors);

		Util.makeGroupPanel(phenotypeScroller, "Selected Phenotypes");
		return phenotypeScroller;
	}

	@Override
	public JPanel getPanel() {
		return myPanel;
	}

	private JPanel makeMeiosisConfig() throws NoSuchFieldException {
		JComboBox<MeiosisMode> meiosis_mode = new JComboBox<MeiosisMode>(
				new EnumComboBoxModel<MeiosisMode>(this.params,
						new FieldPropertyAccessor(GeneticParams.class.getField("meiosisMode"))));
		JPanel meiosis_mode_panel = new JPanel(new BorderLayout());
		meiosis_mode_panel.add(new JLabel("Mode of Meiosis"), BorderLayout.NORTH);
		meiosis_mode_panel.add(meiosis_mode, BorderLayout.CENTER);
		return meiosis_mode_panel;
	}

	private ListManipulator<Phenotype> phenosAvailable;

	private GenesTableModel modelSelected;

	private JScrollPane setupPhenotypeList() {
		phenosAvailable = new ListManipulator<Phenotype>(
				new ArrayList<Phenotype>(choiceCatalog.getChoices(Phenotype.class)));

		for (Phenotype p : params.phenotype) {
			phenosAvailable.removeItem(p);
		}

		listAvailable = new JList<Phenotype>(phenosAvailable);
		listAvailable.setSelectionMode(ListSelectionModel.MULTIPLE_INTERVAL_SELECTION);
		listAvailable.setLayoutOrientation(JList.VERTICAL);
		listAvailable.setVisibleRowCount(-1);
		JScrollPane phenotypeScroller = new JScrollPane(listAvailable);
		phenotypeScroller.setPreferredSize(new Dimension(240, 500));

		Util.makeGroupPanel(phenotypeScroller, "Agent Parameter Selection");
		return phenotypeScroller;
	}

	@Override
	public void validateUI() throws IllegalArgumentException {
		Util.updateTable(listSelected);
	}

}
