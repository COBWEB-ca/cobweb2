package driver;

import java.awt.Color;
import java.awt.Dimension;
import java.awt.Scrollbar;
import java.awt.event.AdjustmentEvent;

import javax.swing.JScrollBar;

public class SpeedBar extends JScrollBar implements
		java.awt.event.AdjustmentListener {
	private final cobweb.UIInterface uiPipe;
	private final Dimension d = new Dimension(70, 18); // $$$$$ you can generate any size you want.  Apr 3

	private final Color original;

	private static final int SCROLLBAR_TICKS = 11;

	public SpeedBar(cobweb.UIInterface theUI) {
		uiPipe = theUI;
		setOrientation(Scrollbar.HORIZONTAL);
		addAdjustmentListener(this);
		this.setValues(SCROLLBAR_TICKS - 1, 0, 0, SCROLLBAR_TICKS);
		this.setPreferredSize(d); // // $$$$$$ some computers may not show the scroll bar properly without this line.  Apr 3
		original = this.getBackground();
	}

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
		uiPipe.slowDown(delay);
	}

	public static final long serialVersionUID = 0xD5E78F1D65B18165L;
}