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
					if (description.endsWith("-modified")) {
						version = String.format("%1$s (%2$s) %3$s",
								description,
								properties.getProperty("git.branch"),
								properties.getProperty("git.build.time"));
					}
					else {
						version = String.format("%1$s %2$s",
								description,
								properties.getProperty("git.commit.time"));
					}
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
