package driver.config;

import javax.swing.JPanel;

public interface ConfigPage {

	public abstract JPanel getPanel();

	public abstract void validateUI() throws IllegalArgumentException;
}