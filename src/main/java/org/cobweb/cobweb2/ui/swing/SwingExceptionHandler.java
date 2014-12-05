package org.cobweb.cobweb2.ui.swing;

import javax.swing.JOptionPane;

import org.cobweb.cobweb2.ui.LoggingExceptionHandler;
import org.cobweb.cobweb2.ui.UserInputException;

/**
 * Shows pop-ups for UserInputException and uncaught exceptions.
 */
public class SwingExceptionHandler extends LoggingExceptionHandler {

	@Override
	protected void notificationUncaughtException(String errorText) {
		JOptionPane.showMessageDialog(null, "Oh no! You crashed COBWEB!\n" + errorText
				, "Unexpected Error", JOptionPane.ERROR_MESSAGE);
	}

	@Override
	public void notificationUserInputException(UserInputException ex) {
		JOptionPane.showMessageDialog(null, ex.getMessage(), "Error", JOptionPane.WARNING_MESSAGE);
	}

}
