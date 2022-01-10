package thorpe.luke.password;

import java.time.LocalDateTime;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;
import thorpe.luke.password.apdater.PasswordEntryJsonAdapter;

public class PasswordEntry {
  private final String name;
  private final String description;
  private final String password;
  private final LocalDateTime dateTimeOfCreation;
  private final Map<LocalDateTime, String> oldPasswords;

  public PasswordEntry(
      String name,
      String description,
      String password,
      LocalDateTime dateTimeOfCreation,
      Map<LocalDateTime, String> oldPasswords) {
    this.name = name;
    this.description = description;
    this.password = password;
    this.dateTimeOfCreation = dateTimeOfCreation;
    this.oldPasswords = Collections.unmodifiableMap(oldPasswords);
  }

  public static PasswordEntry fromAdapter(PasswordEntryJsonAdapter passwordEntryJsonAdapter) {
    return new PasswordEntry(
        passwordEntryJsonAdapter.getName(),
        passwordEntryJsonAdapter.getDescription(),
        passwordEntryJsonAdapter.getPassword(),
        passwordEntryJsonAdapter.getDateTimeOfCreation(),
        passwordEntryJsonAdapter.getOldPasswords());
  }

  public PasswordEntryJsonAdapter toAdapter() {
    return new PasswordEntryJsonAdapter(
        name, description, password, dateTimeOfCreation, oldPasswords);
  }

  public String getName() {
    return name;
  }

  public String getDescription() {
    return description;
  }

  public String getPassword() {
    return password;
  }

  public LocalDateTime getDateTimeOfCreation() {
    return dateTimeOfCreation;
  }

  public Map<LocalDateTime, String> getOldPasswords() {
    return new HashMap<>(oldPasswords);
  }

  @Override
  public int hashCode() {
    return Objects.hash(name, description, password, oldPasswords);
  }

  @Override
  public boolean equals(Object object) {
    if (this == object) {
      return true;
    } else if (object instanceof PasswordEntry) {
      PasswordEntry that = (PasswordEntry) object;
      return this.name.equals(that.name)
          && this.description.equals(that.description)
          && this.password.equals(that.password)
          && this.oldPasswords.equals(that.oldPasswords);
    }
    return false;
  }

  @Override
  public String toString() {
    return "Password Entry called \"" + name + "\", described as \"" + description + "\".";
  }
}
