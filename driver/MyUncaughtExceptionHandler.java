/**
 *
 */
package driver;

import java.lang.Thread.UncaughtExceptionHandler;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.swing.JOptionPane;

/**
 * @author Igor
 *
 */
public class MyUncaughtExceptionHandler implements UncaughtExceptionHandler {

	private static final String newLine = System.getProperty("line.separator");
	private static final Logger logger = Logger.getLogger("Cobweb2");

	/* (non-Javadoc)
	 * @see java.lang.Thread.UncaughtExceptionHandler#uncaughtException(java.lang.Thread, java.lang.Throwable)
	 */
	public void uncaughtException(Thread thread, Throwable ex) {
		if (thread.getName().contains("AWT-EventQueue")) {
			return;
		}
		logger.log(Level.SEVERE, "Uncaught Exception in thread " + thread.getName(), ex);

		StringBuilder sb = new StringBuilder();

		sb.append("Exception in thread " + thread.getName() + " " + ex.getClass().getName() + newLine);
		for (StackTraceElement s : ex.getStackTrace()) {
			sb.append("\tat " + s.getClassName() + "(" + s.getFileName() + ":" + s.getLineNumber() + ")" + newLine);
		}
		JOptionPane.showMessageDialog(null, sb.toString());
	}

}
