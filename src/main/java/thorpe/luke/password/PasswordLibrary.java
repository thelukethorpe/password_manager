package thorpe.luke.password;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.nio.file.Files;
import java.util.*;
import java.util.stream.Collectors;
import thorpe.luke.cryptography.*;
import thorpe.luke.password.apdater.PasswordLibraryJsonAdapter;
import thorpe.luke.util.JsonException;
import thorpe.luke.util.JsonUtils;

public class PasswordLibrary {
  public static final String FILE_SUFFIX = ".passlib";

  private final String name;
  private final String description;
  private final String passwordHash;
  private final Set<PasswordEntry> entries;

  private PasswordLibrary(
      String name, String description, String passwordHash, Collection<PasswordEntry> entries) {
    this.name = name;
    this.description = description;
    this.passwordHash = passwordHash;
    this.entries = Collections.unmodifiableSet(new HashSet<>(entries));
  }

  public static PasswordLibrary fromAdapter(PasswordLibraryJsonAdapter passwordLibraryJsonAdapter) {
    return new PasswordLibrary(
        passwordLibraryJsonAdapter.getName(),
        passwordLibraryJsonAdapter.getDescription(),
        passwordLibraryJsonAdapter.getPasswordHash(),
        passwordLibraryJsonAdapter
            .getEntries()
            .stream()
            .map(PasswordEntry::fromAdapter)
            .collect(Collectors.toList()));
  }

  public PasswordLibraryJsonAdapter toAdapter() {
    return new PasswordLibraryJsonAdapter(
        name,
        description,
        passwordHash,
        entries.stream().map(PasswordEntry::toAdapter).collect(Collectors.toList()));
  }

  private static String hash(String password, String salt) {
    CryptographicHashingEngine hashingEngine = SHA256CryptographicHashingEngine.fromSalt(salt);
    return hashingEngine.hash(password);
  }

  public static PasswordLibrary fromPassword(
      String name, String description, String password, String salt) {
    return PasswordLibrary.fromPassword(name, description, password, salt, Collections.emptyList());
  }

  public static PasswordLibrary fromPassword(
      String name,
      String description,
      String password,
      String salt,
      Collection<PasswordEntry> passwordEntries) {
    return new PasswordLibrary(name, description, hash(password, salt), passwordEntries);
  }

  public static PasswordLibrary readFromEncryptedFile(File file, String password, String salt)
      throws IOException, PasswordMismatchException {
    String cipherJson = new String(Files.readAllBytes(file.toPath()));
    String passwordHash = hash(password, salt);
    EncryptionEngine encryptionEngine = AES256EncryptionEngine.fromPassword(passwordHash, salt);
    PasswordLibrary passwordLibrary;
    try {
      String plainJson = encryptionEngine.decrypt(cipherJson);
      passwordLibrary =
          PasswordLibrary.fromAdapter(
              JsonUtils.fromJson(plainJson, PasswordLibraryJsonAdapter.class));
    } catch (JsonException e) {
      throw new IOException(e);
    } catch (KeyMismatchException e) {
      throw new PasswordMismatchException();
    }
    if (!passwordLibrary.getPasswordHash().equals(passwordHash)) {
      throw new PasswordMismatchException();
    }
    return passwordLibrary;
  }

  public void writeToEncryptedFile(File file, String salt) throws IOException {
    BufferedWriter fileWriter = new BufferedWriter(new FileWriter(file));
    String plainJson;
    try {
      plainJson = JsonUtils.toJson(this);
    } catch (JsonException e) {
      throw new IOException(e);
    }
    EncryptionEngine encryptionEngine = AES256EncryptionEngine.fromPassword(passwordHash, salt);
    String cipherJson = encryptionEngine.encrypt(plainJson);
    fileWriter.write(cipherJson);
    fileWriter.close();
  }

  public PasswordLibrary addEntry(PasswordEntry entry) {
    Collection<PasswordEntry> entries = new LinkedList<>(this.entries);
    entries.add(entry);
    return new PasswordLibrary(name, description, passwordHash, entries);
  }

  public String getName() {
    return name;
  }

  public String getDescription() {
    return description;
  }

  public String getPasswordHash() {
    return passwordHash;
  }

  public Set<PasswordEntry> getEntries() {
    return new HashSet<>(entries);
  }

  @Override
  public int hashCode() {
    return Objects.hash(name, description, passwordHash, entries);
  }

  @Override
  public boolean equals(Object object) {
    if (this == object) {
      return true;
    } else if (object instanceof PasswordLibrary) {
      PasswordLibrary that = (PasswordLibrary) object;
      return this.name.equals(that.name)
          && this.description.equals(that.description)
          && this.passwordHash.equals(that.passwordHash)
          && this.entries.equals(that.entries);
    }
    return false;
  }

  @Override
  public String toString() {
    return "Password Library called \"" + name + "\", described as \"" + description + "\".";
  }
}
