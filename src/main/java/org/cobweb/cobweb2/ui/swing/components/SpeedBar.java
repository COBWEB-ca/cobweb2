package org.cobweb.cobweb2.ui.swing.components;

import java.awt.Color;
import java.awt.Dimension;
import java.awt.Scrollbar;
import java.awt.event.AdjustmentEvent;

import javax.swing.JScrollBar;

import org.cobweb.cobweb2.ui.ThreadSimulationRunner;

public class SpeedBar extends JScrollBar implements
java.awt.event.AdjustmentListener {
	private final ThreadSimulationRunner scheduler;
	private final Dimension d = new Dimension(70, 18); // $$$$$ you can generate any size you want.  Apr 3

	private final Color original;

	private static final int SCROLLBAR_TICKS = 11;

	public SpeedBar(ThreadSimulationRunner scheduler) {
		this.scheduler = scheduler;
		setOrientation(Scrollbar.HORIZONTAL);
		addAdjustmentListener(this);
		this.setValues(SCROLLBAR_TICKS - 1, 0, 0, SCROLLBAR_TICKS);
		this.setPreferredSize(d); // // $$$$$$ some computers may not show the scroll bar properly without this line.  Apr 3
		original = this.getBackground();
	}

	@Override
	public void adjustmentValueChanged(AdjustmentEvent e) {
		int delay = 0;
		int d1 = SCROLLBAR_TICKS - getValue();
		if (d1 != 0) {
			delay = 1 << (d1 - 1);
		}
		if (delay == 0) {
			this.setBackground(Color.yellow);
		} else {
			this.setBackground(original);
		}
		scheduler.setDelay(delay);
	}

	public static final long serialVersionUID = 0xD5E78F1D65B18165L;
}