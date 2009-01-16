package driver;

import java.awt.Dimension;
import java.awt.Scrollbar;
import java.awt.event.AdjustmentEvent;

import javax.swing.JScrollBar;

public class MyScrollbar extends JScrollBar implements
		java.awt.event.AdjustmentListener {
	private final cobweb.UIInterface uiPipe;
	private final Dimension d = new Dimension(70, 18); // $$$$$ you can generate any size you want.  Apr 3

	private static final int SCROLLBAR_TICKS = 10;

	public MyScrollbar(cobweb.UIInterface theUI) {
		uiPipe = theUI;
		setOrientation(Scrollbar.HORIZONTAL);
		this.setValues(SCROLLBAR_TICKS, 0, 0, SCROLLBAR_TICKS);
		this.setPreferredSize(d); // // $$$$$$ some computers may not show the scroll bar properly without this line.  Apr 3
		addAdjustmentListener(this);
	}

	public void adjustmentValueChanged(AdjustmentEvent e) {
		int delay = (SCROLLBAR_TICKS - getValue()) * SCROLLBAR_TICKS;
		uiPipe.slowDown(delay);
	}

	public static final long serialVersionUID = 0xD5E78F1D65B18165L;
}