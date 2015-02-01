package org.cobweb.cobweb2.ui.swing.config;

import java.awt.BorderLayout;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.event.ActionEvent;
import java.util.Arrays;
import java.util.List;

import javax.swing.AbstractAction;
import javax.swing.Action;
import javax.swing.BoxLayout;
import javax.swing.DefaultListCellRenderer;
import javax.swing.JButton;
import javax.swing.JComponent;
import javax.swing.JList;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.ListModel;
import javax.swing.ListSelectionModel;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;

import org.cobweb.cobweb2.impl.ComplexEnvironmentParams;
import org.cobweb.cobweb2.plugins.abiotic.AbioticFactor;
import org.cobweb.cobweb2.plugins.abiotic.AbioticParams;
import org.cobweb.cobweb2.plugins.abiotic.Bands;
import org.cobweb.cobweb2.plugins.abiotic.HorizontalBands;
import org.cobweb.cobweb2.plugins.abiotic.VerticalBands;
import org.cobweb.cobweb2.ui.swing.ConfigRefresher;


public class AbioticFactorConfigPage implements ConfigPage {

	private AbioticParams params;

	private JPanel myPanel;

	private JPanel factorConfigPanel;

	private ComplexEnvironmentParams envParams;


	private static final List<AbioticFactor> PATTERNS = Arrays.asList(
			(AbioticFactor) new HorizontalBands(),
			(AbioticFactor) new VerticalBands()
			);

	private ConfigRefresher refresher;

	public AbioticFactorConfigPage(AbioticParams abioticParams, ComplexEnvironmentParams environmentParams, ConfigRefresher simulationConfigEditor) {
		this.params = abioticParams;
		this.envParams = environmentParams;
		this.refresher = simulationConfigEditor;

		myPanel = new JPanel();
		myPanel.setLayout(new BorderLayout());

		JPanel left = new JPanel();
		left.setLayout(new BoxLayout(left, BoxLayout.Y_AXIS));

		JComponent optionPanel = setupAvailable();

		JComponent selectedPanel = setupSelectedList();

		JButton addPheno = new JButton(addFactor);
		JButton remPheno = new JButton(removeFactor);
		JPanel buttons = new JPanel();
		buttons.add(addPheno);
		buttons.add(remPheno);

		left.add(optionPanel);
		left.add(buttons);
		left.add(selectedPanel);

		myPanel.add(left, BorderLayout.WEST);

		factorConfigPanel = new JPanel(new BorderLayout());

		myPanel.add(factorConfigPanel);

		Util.makeGroupPanel(myPanel, "Abiotic Factors");
	}

	private ListModel<AbioticFactor> modelOptions;

	private ListManipulator<AbioticFactor> modelSelected;

	private JList<AbioticFactor> listSelected;

	private JList<AbioticFactor> listOptions;

	private JScrollPane setupAvailable() {
		modelOptions = new ListManipulator<>(PATTERNS);

		listOptions = new JList<AbioticFactor>(modelOptions);
		listOptions.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
		listOptions.setLayoutOrientation(JList.VERTICAL);
		listOptions.setVisibleRowCount(-1);
		listOptions.setCellRenderer(new FactorNameRenderer());
		JScrollPane scroller = new JScrollPane(listOptions);
		scroller.setPreferredSize(new Dimension(240, 500));

		Util.makeGroupPanel(scroller, "Available Patterns");
		return scroller;
	}

	private JComponent setupSelectedList() {
		modelSelected = new ListManipulator<>(params.factors);

		listSelected = new JList<AbioticFactor>(modelSelected);
		listSelected.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
		listSelected.setLayoutOrientation(JList.VERTICAL);
		listSelected.setVisibleRowCount(-1);
		listSelected.setCellRenderer(new ActiveFactorRenderer());
		listSelected.addListSelectionListener(new EditorSelector());
		JScrollPane scroller = new JScrollPane(listSelected);
		scroller.setPreferredSize(new Dimension(240, 500));

		Util.makeGroupPanel(scroller, "Active Factors");
		return scroller;
	}

	private class EditorSelector implements ListSelectionListener {

		@Override
		public void valueChanged(ListSelectionEvent e) {
			int index = listSelected.getSelectedIndex();
			editFactor(index);
		}
	}

	int lastEditorIndex = -1;

	private void refreshEditor() {
		editFactor(lastEditorIndex);
	}

	private void editFactor(int index) {
		factorConfigPanel.removeAll();

		lastEditorIndex = index;

		if (index < 0)
			return;

		AbioticFactor selectedFactor = params.factors.get(index);

		String editorName = describeActiveFactor(selectedFactor, index);
		JPanel editorPanel = null;
		if (selectedFactor instanceof Bands) {
			Bands bands = (Bands) selectedFactor;
			editorPanel = getBandsEditor(editorName, bands);
		}
		if (editorPanel != null) {
			factorConfigPanel.add(editorPanel);
		}
		factorConfigPanel.validate();
		factorConfigPanel.repaint();
	}

	private JPanel getBandsEditor(String editorName, final Bands factor) {
		TableConfigPage<Bands> editor = new TableConfigPage<>(
				new Bands[] { factor },
				null,
				"Value");
		final JPanel table = editor.getPanel();

		JPanel group = new JPanel(new BorderLayout());

		group.add(table);

		JPanel buttons = new JPanel();

		Action addBand = new AbstractAction("Add Band") {
			@Override
			public void actionPerformed(ActionEvent e) {
				factor.bands.add(0f);
				refreshEditor();
			}
			private static final long serialVersionUID = 1L;
		};
		Action removeBand = new AbstractAction("Remove Band") {
			@Override
			public void actionPerformed(ActionEvent e) {
				if (factor.bands.size() <= 1)
					return;
				factor.bands.remove(factor.bands.size()-1);
				refreshEditor();
			}
			private static final long serialVersionUID = 1L;
		};

		JButton addButton = new JButton(addBand);
		JButton removeButton = new JButton(removeBand);
		buttons.add(addButton);
		buttons.add(removeButton);

		group.add(buttons, BorderLayout.SOUTH);

		Util.makeGroupPanel(group, editorName);
		return group;
	}


	private class FactorNameRenderer extends DefaultListCellRenderer {
		@Override
		public Component getListCellRendererComponent(JList<?> list, Object value, int index, boolean isSelected, boolean cellHasFocus) {
			super.getListCellRendererComponent(list, value, index, isSelected, cellHasFocus);
			if (value instanceof AbioticFactor) {
				setText(((AbioticFactor)value).getName());
			}
			return this;
		}
		private static final long serialVersionUID = 1L;
	}

	private class ActiveFactorRenderer extends DefaultListCellRenderer {
		@Override
		public Component getListCellRendererComponent(JList<?> list, Object value, int index, boolean isSelected, boolean cellHasFocus) {
			super.getListCellRendererComponent(list, value, index, isSelected, cellHasFocus);
			if (value instanceof AbioticFactor) {
				setText(describeActiveFactor((AbioticFactor) value, index));
			}
			return this;
		}
		private static final long serialVersionUID = 1L;
	}

	private static String describeActiveFactor(AbioticFactor value, int index) {
		return "Factor " + (index + 1) + ": " + value.getName();
	}

	private Action addFactor = new AbstractAction("Add") {
		@Override
		public void actionPerformed(ActionEvent e) {
			AbioticFactor selectedValue = listOptions.getSelectedValue();
			if (selectedValue == null)
				return;

			modelSelected.addItem(selectedValue.copy());

			refresher.refreshConfig();
		}
		private static final long serialVersionUID = 1L;
	};

	private Action removeFactor = new AbstractAction("Remove") {
		@Override
		public void actionPerformed(ActionEvent e) {
			int index = listSelected.getSelectedIndex();
			if (index < 0)
				return;

			modelSelected.removeAt(index);

			refresher.refreshConfig();
		}
		private static final long serialVersionUID = 1L;
	};



	@Override
	public JPanel getPanel() {
		return myPanel;
	}

	@Override
	public void validateUI() throws IllegalArgumentException {
		// TODO Auto-generated method stub

	}

}
