package org.cobweb.cobweb2.ui.swing.stats;

import java.awt.Container;
import java.awt.Dimension;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;

import javax.swing.BoxLayout;
import javax.swing.JComponent;
import javax.swing.JFrame;
import javax.swing.JScrollPane;
import javax.swing.event.TableModelEvent;
import javax.swing.event.TableModelListener;

import org.cobweb.cobweb2.Simulation;
import org.cobweb.cobweb2.ui.GridStats;
import org.cobweb.cobweb2.ui.GridStats.RegionOptions;
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

	public static class RegionViewerOptions implements ParameterSerializable {
		@ConfDisplayName("Stats")
		public RegionOptions statsOptions = new RegionOptions();

		@ConfDisplayName("Fade background")
		public float fade = 0.8f;

		@ConfDisplayName("Bar graphs")
		public boolean graphs = false;

		private static final long serialVersionUID = 1L;
	}

	public RegionViewer(DisplayPanel panel) {
		super(panel);
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
		JFrame result = new JFrame("Regional Statistics");
		Container mainPanel = result.getContentPane();
		mainPanel.setLayout(new BoxLayout(mainPanel, BoxLayout.Y_AXIS));

		JComponent sp = createDisplayConfigPanel();
		sp.setPreferredSize(new Dimension(300, 200));
		mainPanel.add(sp);

		result.pack();

		return result;
	}

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
