package thorpe.luke.password.ui.model;

import java.util.Objects;

public class Options {

  public enum Field {
    DATA_PATH,
    SALT;
  }

  private final String dataPath;
  private final String salt;

  public Options(String dataPath, String salt) {
    this.dataPath = dataPath;
    this.salt = salt;
  }

  public String getDataPath() {
    return dataPath;
  }

  public String getSalt() {
    return salt;
  }

  public static OptionsBuilder where() {
    return new OptionsBuilder();
  }

  public static class OptionsBuilder {

    private String dataPath;
    private String salt;

    public OptionsBuilder dataPathIsSetTo(String dataPath) {
      this.dataPath = dataPath;
      return this;
    }

    public OptionsBuilder saltIsSetTo(String salt) {
      this.salt = salt;
      return this;
    }

    public Options build() {
      return new Options(dataPath, salt);
    }
  }

  @Override
  public int hashCode() {
    return Objects.hash(dataPath, salt);
  }

  @Override
  public boolean equals(Object object) {
    if (this == object) {
      return true;
    } else if (object instanceof Options) {
      Options that = (Options) object;
      return this.dataPath.equals(that.dataPath) && this.salt.equals(that.salt);
    }
    return false;
  }
}
