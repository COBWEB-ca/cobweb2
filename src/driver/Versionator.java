package driver;

import java.io.IOException;
import java.io.InputStreamReader;
import java.net.URL;
import java.net.URLConnection;
import java.nio.CharBuffer;


public class Versionator {

	private static String version = null;

	public static String getVersion() {
		if (version != null)
			return version;

		// Try JAR file first
		String version = Versionator.class.getPackage().getImplementationVersion();

		if (version == null) {
			// Else look in the version text file
			ClassLoader loader = SimulationConfig.class.getClassLoader();
			URL resource= loader.getResource( "version_file.txt");

			try {
				URLConnection connection = resource.openConnection();

				CharBuffer b = CharBuffer.allocate(connection.getContentLength());
				InputStreamReader r = new InputStreamReader(connection.getInputStream());
				r.read(b);
				b.position(0);
				version = b.toString();
				r.close();
			} catch (IOException ex) {

			}
		}
		return version;
	}
}
