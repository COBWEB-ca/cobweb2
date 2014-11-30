package org.cobweb.cobweb2.ui.swing.config;

import javax.swing.JPanel;

public interface ConfigPage {

	public abstract JPanel getPanel();

	public abstract void validateUI() throws IllegalArgumentException;
}