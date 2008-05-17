package driver;

import java.awt.*;
import java.awt.event.*;

public class MyScrollbar extends java.awt.Scrollbar implements
		java.awt.event.AdjustmentListener {
	private cobweb.UIInterface uiPipe;
	private Dimension d = new Dimension(70, 18); // $$$$$ you can generate any size you want.  Apr 3

	public MyScrollbar(cobweb.UIInterface theUI) {
		uiPipe = theUI;
		setOrientation(Scrollbar.HORIZONTAL);
		this.setValues(0, 1, 0, 100);
		this.setPreferredSize(d); // // $$$$$$ some computers may not show the scroll bar properly without this line.  Apr 3
		addAdjustmentListener(this);
	}

	public void adjustmentValueChanged(AdjustmentEvent e) {
		int val = getValue();
		uiPipe.slowDown(val);
	}

	public static final long serialVersionUID = 0xD5E78F1D65B18165L;
}