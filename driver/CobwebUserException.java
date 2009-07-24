package driver;

import java.util.logging.Level;
import java.util.logging.Logger;

import javax.swing.JOptionPane;

public class CobwebUserException extends RuntimeException {

	/**
	 *
	 */
	private static final long serialVersionUID = 4381700902598657728L;

	public CobwebUserException(String message) {
		super(message);
	}

	public CobwebUserException(String message, Throwable cause) {
		super(message, cause);
	}

	public CobwebUserException(Throwable cause) {
		super(cause);
	}

	public void notifyUser() {
		Logger.getLogger("COBWEB2").log(Level.CONFIG, "User exception", this);
		JOptionPane.showMessageDialog(null, this.getMessage(), "Error", JOptionPane.WARNING_MESSAGE);
	}
}
