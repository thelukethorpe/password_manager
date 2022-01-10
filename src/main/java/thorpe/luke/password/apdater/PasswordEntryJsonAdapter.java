package thorpe.luke.password.apdater;

import java.time.LocalDateTime;
import java.util.Map;

public class PasswordEntryJsonAdapter {
  private String name;
  private String description;
  private String password;
  private LocalDateTime dateTimeOfCreation;
  private Map<LocalDateTime, String> oldPasswords;

  public PasswordEntryJsonAdapter() {}

  public PasswordEntryJsonAdapter(
      String name,
      String description,
      String password,
      LocalDateTime dateTimeOfCreation,
      Map<LocalDateTime, String> oldPasswords) {
    this.name = name;
    this.description = description;
    this.password = password;
    this.dateTimeOfCreation = dateTimeOfCreation;
    this.oldPasswords = oldPasswords;
  }

  public String getName() {
    return name;
  }

  public void setName(String name) {
    this.name = name;
  }

  public String getDescription() {
    return description;
  }

  public void setDescription(String description) {
    this.description = description;
  }

  public String getPassword() {
    return password;
  }

  public void setPassword(String password) {
    this.password = password;
  }

  public LocalDateTime getDateTimeOfCreation() {
    return dateTimeOfCreation;
  }

  public void setDateTimeOfCreation(LocalDateTime dateTimeOfCreation) {
    this.dateTimeOfCreation = dateTimeOfCreation;
  }

  public Map<LocalDateTime, String> getOldPasswords() {
    return oldPasswords;
  }

  public void setOldPasswords(Map<LocalDateTime, String> oldPasswords) {
    this.oldPasswords = oldPasswords;
  }
}
