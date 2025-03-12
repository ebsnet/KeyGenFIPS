package de.ebsnet.keygen;

import java.io.IOException;
import java.util.Properties;
import picocli.CommandLine;

/**
 * Provider for the tool version. The version is injected into {@code version.properties} at build
 * time.
 */
public final class VersionProvider implements CommandLine.IVersionProvider {
  @Override
  public String[] getVersion() {
    return new String[] {ValueHolder.VERSION};
  }

  private static final class ValueHolder {
    private static final String VERSION;

    static {
      String version;
      String commit;
      try (var stream =
          VersionProvider.class.getClassLoader().getResourceAsStream("version.properties")) {
        final var prop = new Properties();
        prop.load(stream);
        version = prop.getProperty("version", "0.0.0");
        commit = prop.getProperty("commit", "unknown commit");
      } catch (final IOException ex) {
        version = "-0.0.0";
        commit = "unknown commit";
      }
      VERSION = version + " (" + commit + ") https://www.ebsnet.de";
    }
  }
}
