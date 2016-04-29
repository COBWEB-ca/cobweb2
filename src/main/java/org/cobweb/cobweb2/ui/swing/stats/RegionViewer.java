package org.cobweb.cobweb2.ui.swing.stats;

import java.awt.BorderLayout;
import java.awt.Container;
import java.awt.Dimension;
import java.awt.FileDialog;
import java.awt.event.ActionEvent;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;

import javax.swing.AbstractAction;
import javax.swing.JButton;
import javax.swing.JComponent;
import javax.swing.JFrame;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.event.TableModelEvent;
import javax.swing.event.TableModelListener;

import org.cobweb.cobweb2.Simulation;
import org.cobweb.cobweb2.ui.GridStats;
import org.cobweb.cobweb2.ui.GridStats.RegionOptions;
import org.cobweb.cobweb2.ui.RegionStatsReporter;
import org.cobweb.cobweb2.ui.SimulationRunner;
import org.cobweb.cobweb2.ui.UserInputException;
import org.cobweb.cobweb2.ui.swing.DisplayOverlay;
import org.cobweb.cobweb2.ui.swing.DisplayPanel;
import org.cobweb.cobweb2.ui.swing.OverlayGenerator;
import org.cobweb.cobweb2.ui.swing.OverlayPluginViewer;
import org.cobweb.cobweb2.ui.swing.config.ConfigTableModel;
import org.cobweb.cobweb2.ui.swing.config.MixedValueJTable;
import org.cobweb.io.ConfDisplayName;
import org.cobweb.io.ParameterSerializable;


public class RegionViewer extends OverlayPluginViewer<RegionViewer> implements OverlayGenerator {

	private final RegionViewerOptions viewerOptions = new RegionViewerOptions();

	private JFrame configFrame;

	private SimulationRunner simRunner;

	private Simulation simulation;

	public static class RegionViewerOptions implements ParameterSerializable {
		@ConfDisplayName("Stats")
		public RegionOptions statsOptions = new RegionOptions();

		@ConfDisplayName("Fade background")
		public float fade = 0.8f;

		@ConfDisplayName("Bar graphs")
		public boolean graphs = false;

		@ConfDisplayName("Graph max per-cell")
		public boolean graphMaxCell = true;

		private static final long serialVersionUID = 1L;
	}

	public RegionViewer(DisplayPanel panel, SimulationRunner simRunner) {
		super(panel);
		this.simRunner = simRunner;
		this.simulation = (Simulation) simRunner.getSimulation();
	}

	@Override
	public String getName() {
		return "Regional Stats";
	}

	@Override
	public DisplayOverlay getDrawInfo(Simulation sim) {
		return new RegionOverlay(new GridStats(sim, viewerOptions.statsOptions), viewerOptions);
	}

	@Override
	protected RegionViewer createOverlay() {
		return this;
	}

	@Override
	public void on() {
		super.on();

		if (configFrame == null) {
			configFrame = createConfigFrame();
			configFrame.addWindowListener(new WindowAdapter() {
				@Override
				public void windowClosing(WindowEvent e) {
					off();
					onClosed.viewerClosed();
				}
			});
			configFrame.setVisible(true);
		}
	}

	@Override
	public void off() {
		super.off();

		if (configFrame != null) {
			configFrame.dispose();
			configFrame = null;
		}
	}


	private JFrame createConfigFrame() {
		final JFrame result = new JFrame("Regional Statistics");
		Container mainPanel = result.getContentPane();
		mainPanel.setLayout(new BorderLayout());

		JComponent sp = createDisplayConfigPanel();
		sp.setPreferredSize(new Dimension(300, 200));
		mainPanel.add(sp);

		JButton saveButton = new JButton(new AbstractAction("Save Data") {
			@Override
			public void actionPerformed(ActionEvent arg0) {
				FileDialog theDialog = new FileDialog(result,
						"Choose a file to save regional stats to", FileDialog.SAVE);
				theDialog.setVisible(true);
				if (theDialog.getFile() != null) {
					try (PrintWriter writer = new PrintWriter(theDialog.getDirectory() + theDialog.getFile())) {

						GridStats stats = new GridStats(simulation, viewerOptions.statsOptions);
						RegionStatsReporter.report(writer, stats);
					} catch (IOException ex) {
						throw new UserInputException("Can't create report file!", ex);
					}
				}
			}
			private static final long serialVersionUID = 1L;
		});

		JButton logButton = new JButton(new AbstractAction("Log Counts") {
			@Override
			public void actionPerformed(ActionEvent arg0) {
				FileDialog theDialog = new FileDialog(result,
						"Choose a file to log regional stats to", FileDialog.SAVE);
				theDialog.setVisible(true);
				if (logger != null) {
					simRunner.removeUIComponent(logger);
					logger.dispose();
					logger = null;
				}

				if (theDialog.getFile() != null) {
					try {
						logger = new RegionLogger(new FileWriter(theDialog.getDirectory() + theDialog.getFile()), simulation, viewerOptions.statsOptions);
						simRunner.addUIComponent(logger);
					} catch (IOException ex) {
						throw new UserInputException("Can't create report file!", ex);
					}
				}
			}
			private static final long serialVersionUID = 1L;
		});

		JPanel buttonPanel = new JPanel();
		buttonPanel.add(saveButton);
		buttonPanel.add(logButton);
		mainPanel.add(buttonPanel, BorderLayout.SOUTH);

		result.pack();

		return result;
	}

	RegionLogger logger = null;

	private JComponent createDisplayConfigPanel() {
		ConfigTableModel model = new ConfigTableModel(new ParameterSerializable[] { viewerOptions }, "Value");
		model.addTableModelListener(new TableModelListener() {
			@Override
			public void tableChanged(TableModelEvent e) {
				panel.refresh(true);
			}
		});
		MixedValueJTable paramTable = new MixedValueJTable(model);
		JScrollPane sp = new JScrollPane(paramTable);
		return sp;
	}

}
