package driver;

import java.io.File;
import java.io.IOException;

import javax.swing.JOptionPane;

import cobweb.LocalUIInterface;
import cobweb.LocalUIInterface.TickEventListener;
import cobweb.UIInterface;
import cobweb.UIInterface.UIClient;
import driver.config.GUI;


public class CobwebApplicationRunner {

	private static final class NullDisplayApplication implements UIClient {

		@Override
		public void refresh(boolean wait) {
			return;
		}

		@Override
		public boolean isReadyToRefresh() {
			return true;
		}

		@Override
		public void setCurrentFile(String input) {
			return;
		}

		@Override
		public void fileOpened(SimulationConfig conf) {
			return;
		}

		@Override
		public void setSimulation(UIInterface simulation) {
			return;
		}
	}

	static UIInterface simulation;

	public static void main(String[] args) {

		// Process Arguments`

		String inputFileName = "";
		String logFileName = "";
		boolean autostart = false;
		boolean visible = true;
		int finalstep = 0;

		if (args.length > 0) {
			for (int arg_pos = 0; arg_pos < args.length; ++arg_pos){
				if (args[arg_pos].equalsIgnoreCase("--help")){
					System.out.println("Syntax: " + CobwebApplicationRunner.Syntax);
					System.exit(0);
				} else if (args[arg_pos].equalsIgnoreCase("-autorun")){
					autostart = true;
					try{
						finalstep = Integer.parseInt(args[++arg_pos]);
					} catch (NumberFormatException numexception){
						System.out.println("-autorun argument must be integer");
						System.exit(1);
					}
					if (finalstep < -1) { 
						System.out.println("-autorun argument must >= -1");
						System.exit(1);
					}
				} else if (args[arg_pos].equalsIgnoreCase("-hide")){
					visible=false;
				} else if (args[arg_pos].equalsIgnoreCase("-open")){
					if (args.length - arg_pos == 1) {
						System.out.println("No value attached to '-open' argument,\n" +
								"Correct Syntax is: " + CobwebApplicationRunner.Syntax);
						System.exit(1);
					} else {
						inputFileName = args[++arg_pos];
					}
				} else if (args[arg_pos].equalsIgnoreCase("-log")){
					if (args.length - arg_pos == 1) {
						System.out.println("No value attached to '-log' argument,\n" +
								"Correct Syntax is: " + CobwebApplicationRunner.Syntax);
						System.exit(1);
					} else {
						logFileName = args[++arg_pos];
					}
				} else {
					inputFileName = args[arg_pos];
				}
			}
		}

		if (!inputFileName.equals("") && ! new File(inputFileName).exists()){
			System.out.println("Invalid settings file value: '" + inputFileName + "' does not exist" );
			System.exit(1);
		}
		if (!logFileName.equals("") && new File(logFileName).exists()){
			System.out.println("WARNING: log '" + logFileName + "' already exists, overwriting it!" );
		}

		//Create CobwebApplication and threads; this is not done earlier so 
		// that argument errors will result in quick exits.

		boolean isDebug = java.lang.management.ManagementFactory.getRuntimeMXBean().getInputArguments().toString().indexOf(
		"-agentlib:jdwp") > 0;

		if (!isDebug) {
			MyUncaughtExceptionHandler handler = new MyUncaughtExceptionHandler();
			Thread.setDefaultUncaughtExceptionHandler(handler);
		}

		UIClient CA = null;

		if (visible) {
			CA = new CobwebApplication();
		} else {
			CA = new NullDisplayApplication();
		}

		simulation = new LocalUIInterface(CA);
		CA.setSimulation(simulation);

		//Set up inputFile

		if (inputFileName.equals("")) {
			if (!visible) {
				System.err.println("Please specify an input file name when running with the -hide option");
				return;
			}

			String tempdir = System.getProperty("java.io.tmpdir");
			String sep = System.getProperty("file.separator");
			if (!tempdir.endsWith(sep))
				tempdir = tempdir + sep;

			inputFileName = CobwebApplication.INITIAL_OR_NEW_INPUT_FILE_NAME + CobwebApplication.CONFIG_FILE_EXTENSION;
			File testFile = new File(inputFileName);
			if (! (testFile.exists() && testFile.canWrite()))
				inputFileName = tempdir + CobwebApplication.INITIAL_OR_NEW_INPUT_FILE_NAME + CobwebApplication.CONFIG_FILE_EXTENSION;

		}
		CA.setCurrentFile(inputFileName); // $$$$$$ added on Mar 14

		SimulationConfig defaultconf;
		try {
			defaultconf = new SimulationConfig(inputFileName);
			simulation.load(defaultconf);
		} catch (Exception ex) {
			// CA.setEnabled(false); // $$$$$$ to make sure the "Cobweb Application" frame disables when ever the
			// "Test Data" window showing. Feb 28
			// $$$$$ a file named as the above name will be automatically created or modified when everytime running the
			// $$$$$ following code. Please refer to GUI.GUI.close.addActionListener, "/* write UI info to xml file */". Jan
			// 24
			if (visible && CA instanceof CobwebApplication) {
				GUI.createAndShowGUI((CobwebApplication)CA, inputFileName);
				System.out.println("Exception with SimulationConfig");
			}
		}




		// $$$$$$ Added to check if the new data file is hidden. Feb 22
		File inf = new File(inputFileName);
		if (inf.isHidden() || (inf.exists() && !inf.canWrite())) {
			JOptionPane.showMessageDialog(GUI.frame, "Caution:  The initial data file \"" + inputFileName
					+ "\" is NOT allowed to be modified.\n"
					+ "\n                  Any modification of this data file will be neither implemented nor saved.");
		}

		if (logFileName != ""){
			try {
				simulation.log(logFileName);
			} catch (IOException ex) {
				System.err.println("couldn't open log file!");
			}
		}

		if (autostart) {
			System.out.println(String.format("Running '%1$s' for %2$d steps with log %3$s..."
					, inputFileName, finalstep, logFileName));
			simulation.slowDown(0);
			simulation.resume();

			final class AutoStopTickListener implements TickEventListener {
				private long stopTick;

				private long increment;

				public AutoStopTickListener(long stopTick) {
					this.stopTick = stopTick;

					increment = stopTick / 10;
					if (increment > 2000)
						increment = 2000;
				}

				@Override
				public void TickPerformed(long currentTick) {
					if (currentTick % increment == 0) {
						System.out.print(100 * currentTick / stopTick + "% ");

					}
					if (currentTick > stopTick) {
						simulation.pause();

						simulation.killScheduler();
						System.out.println("Done!");
						System.exit(0);
					}
				}
			}

			simulation.AddTickEventListener(new AutoStopTickListener(finalstep));
		}
	}

	public static final String Syntax = "cobweb2 [--help] [-hide] [-autorun finalstep] [-log LogFile.tsv] [[-open] SettingsFile.xml]";



}
