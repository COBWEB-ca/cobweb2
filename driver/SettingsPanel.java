package driver;

import java.awt.LayoutManager;
import java.io.IOException;
import java.io.Writer;

import javax.swing.JPanel;

public abstract class SettingsPanel extends JPanel  {


	/**
	 *
	 */
	private static final long serialVersionUID = 7049471695197763906L;



	public SettingsPanel() {
		super();
	}

	public SettingsPanel(boolean isDoubleBuffered) {
		super(isDoubleBuffered);
	}

	public SettingsPanel(LayoutManager layout, boolean isDoubleBuffered) {
		super(layout, isDoubleBuffered);
	}

	public SettingsPanel(LayoutManager layout) {
		super(layout);
	}

	public abstract void writeXML(Writer out) throws IOException;

	public abstract void readFromParser(Parser p);
}
