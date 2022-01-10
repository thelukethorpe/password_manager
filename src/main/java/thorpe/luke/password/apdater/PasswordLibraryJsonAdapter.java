package thorpe.luke.password.apdater;

import java.util.Collection;

public class PasswordLibraryJsonAdapter {
  private String name;
  private String description;
  private String passwordHash;
  private Collection<PasswordEntryJsonAdapter> entries;

  public PasswordLibraryJsonAdapter() {}

  public PasswordLibraryJsonAdapter(
      String name,
      String description,
      String passwordHash,
      Collection<PasswordEntryJsonAdapter> entries) {
    this.name = name;
    this.description = description;
    this.passwordHash = passwordHash;
    this.entries = entries;
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

  public String getPasswordHash() {
    return passwordHash;
  }

  public void setPasswordHash(String passwordHash) {
    this.passwordHash = passwordHash;
  }

  public Collection<PasswordEntryJsonAdapter> getEntries() {
    return entries;
  }

  public void setEntries(Collection<PasswordEntryJsonAdapter> entries) {
    this.entries = entries;
  }
}
