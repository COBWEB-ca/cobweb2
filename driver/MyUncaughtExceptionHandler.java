/**
 *
 */
package driver;

import java.io.FileWriter;
import java.io.IOException;
import java.lang.Thread.UncaughtExceptionHandler;
import java.util.Calendar;
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

		sb.append("Exception in thread " + thread.getName() + newLine);
		exceptionToString(ex, sb);

		try {
			FileWriter fw = new FileWriter("cobweb_errors.log", true);
			fw.write(Calendar.getInstance().toString() + newLine);
			fw.write(sb.toString());
			fw.write(newLine);
			fw.close();
		} catch (IOException ex1) {
			// TODO Auto-generated catch block
			ex1.printStackTrace();
		}


		JOptionPane.showMessageDialog(null, "Oh no! You crashed COBWEB!" + newLine + sb.toString());
	}

	private void exceptionToString(Throwable ex, StringBuilder sb) {
		sb.append(ex.toString() + newLine);
		for (StackTraceElement s : ex.getStackTrace()) {
			sb.append("\tat " + s.getClassName() + "." + s.getMethodName() + "(" + s.getFileName() + ":" + s.getLineNumber() + ")" + newLine);
		}
		if (ex.getCause() != null) {
			sb.append("Caused by: " + newLine);
			exceptionToString(ex.getCause(), sb);
		}
	}

}
