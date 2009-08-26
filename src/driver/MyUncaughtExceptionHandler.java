/**
 *
 */
package driver;

import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.lang.Thread.UncaughtExceptionHandler;
import java.util.logging.Handler;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.logging.SimpleFormatter;
import java.util.logging.StreamHandler;

import javax.swing.JOptionPane;

/**
 * @author Igor
 *
 */
public class MyUncaughtExceptionHandler implements UncaughtExceptionHandler {

	private static final String newLine = System.getProperty("line.separator");
	private static final Logger logger = Logger.getLogger("COBWEB2");

	Handler fh;

	public MyUncaughtExceptionHandler() {

	}

	private void exceptionToString(Throwable ex, StringBuilder sb, String indent) {
		sb.append(ex.toString() + newLine);
		for (StackTraceElement s : ex.getStackTrace()) {
			if (s.getClassName().startsWith("java"))
				continue;
			sb.append(indent + "  at " + s.getClassName() + "." + s.getMethodName() + "(" + s.getFileName() + ":" + s.getLineNumber() + ")" + newLine);
		}
		if (ex.getCause() != null) {
			sb.append(indent + "Caused by: " + newLine);
			exceptionToString(ex.getCause(), sb, indent + "  ");
		}
	}

	/* (non-Javadoc)
	 * @see java.lang.Thread.UncaughtExceptionHandler#uncaughtException(java.lang.Thread, java.lang.Throwable)
	 */
	public void uncaughtException(Thread thread, Throwable ex) {
		if (ex instanceof CobwebUserException) {
			((CobwebUserException) ex).notifyUser();
			return;
		}

		if (fh == null) {
			try {
				fh = new StreamHandler(new FileOutputStream("cobweb_errors.log", true), new SimpleFormatter());
				fh.setLevel(Level.WARNING);
				logger.addHandler(fh);
			} catch (FileNotFoundException exi) {
				logger.log(Level.SEVERE, "Cannot open file log!", exi);
			}
		}

		logger.log(Level.SEVERE, "Uncaught Exception in thread " + thread.getName(), ex);
		for (int depth = 0; depth < 4; depth++) {
			if (depth >= ex.getStackTrace().length)
				break;
			if (ex.getStackTrace()[depth].getClassName().equals("org.jfree.data.xy.DefaultXYDataset"))
				return;
		}

		StringBuilder sb = new StringBuilder();

		sb.append("Exception in thread " + thread.getName() + newLine);
		exceptionToString(ex, sb, "");

		JOptionPane.showMessageDialog(null, "Oh no! You crashed COBWEB!" + newLine + sb.toString()
				, "Unexpected Error", JOptionPane.ERROR_MESSAGE);
	}

}
