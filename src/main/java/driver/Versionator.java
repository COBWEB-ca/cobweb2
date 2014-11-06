package driver;

import java.util.Properties;


public class Versionator {

	private static String version = null;

	public static String getVersion() {
		if (version != null)
			return version;

		// Try JAR file first
		String version = Versionator.class.getPackage().getImplementationVersion();

		if (version == null) {
			// Else look in the version text file
			try {
				ClassLoader loader = Versionator.class.getClassLoader();

				Properties properties = new Properties();
				properties.load(loader.getResourceAsStream("git.properties"));
				String description = properties.getProperty("git.commit.id.describe");

				if (description != null) {
					String dateType = description.endsWith("-modified") ? "git.build.time" : "git.commit.time";
					String date = properties.getProperty(dateType);
					version = description + " " + date;
				}
			} catch (Exception ex) {
				// nothing
			}
		}

		if (version == null)
			version = "Unknown Version";

		return version;
	}
}
