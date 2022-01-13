package thorpe.luke.password.ui.service;

import java.io.*;
import java.util.Properties;
import thorpe.luke.password.ui.model.Options;

public class OptionsService {
  private static final String DEFAULT_DATA_PATH = "./.data/";
  private static final String DEFAULT_SALT = "s0me_$4Lt";

  private static final Options DEFAULT_OPTIONS =
      Options.where().dataPathIsSetTo(DEFAULT_DATA_PATH).saltIsSetTo(DEFAULT_SALT).build();

  private final String optionsPath;

  public OptionsService(String configPath) {
    this.optionsPath = configPath + "/options.properties";
  }

  public Options loadOptionsFromDisk() throws IOException {
    InputStream inputStream = new FileInputStream(optionsPath);
    Properties properties = new Properties();
    properties.load(inputStream);

    String dataPath = parseDataPathFrom(properties);
    String salt = parseSaltFrom(properties);

    return Options.where().dataPathIsSetTo(dataPath).saltIsSetTo(salt).build();
  }

  private String parseDataPathFrom(Properties properties) {
    return properties.getProperty(Options.Field.DATA_PATH.name(), DEFAULT_OPTIONS.getDataPath());
  }

  private String parseSaltFrom(Properties properties) {
    return properties.getProperty(Options.Field.SALT.name(), DEFAULT_OPTIONS.getSalt());
  }

  public Options loadDefaultOptions() {
    return DEFAULT_OPTIONS;
  }

  public void writeOptionsFileToDisk(Options options) throws IOException {
    File file = new File(optionsPath);
    if (!file.exists() && !(file.getParentFile().mkdirs() && file.createNewFile())) {
      throw new IOException("Failed to create new file at " + file.getPath());
    }
    OutputStream outputStream = new FileOutputStream(file);
    Properties properties = new Properties();

    properties.setProperty(Options.Field.DATA_PATH.name(), options.getDataPath());

    properties.store(outputStream, "Password Manager options file.");
  }

  public Options writeNewOptionsFileToDisk() throws IOException {
    Options defaultOptions = loadDefaultOptions();
    writeOptionsFileToDisk(defaultOptions);
    return defaultOptions;
  }
}
