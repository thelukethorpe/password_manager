package thorpe.luke.password;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.assertj.core.api.Fail.fail;

import java.io.File;
import java.io.IOException;
import java.time.LocalDateTime;
import java.util.Collections;
import java.util.HashMap;
import org.junit.Test;

public class PasswordLibraryTest {

  @Test
  public void testFileEncryptionIsPasswordSensitive() {
    File file;
    try {
      file = File.createTempFile("password_library", "testFileEncryptionIsPasswordSensitive");
    } catch (IOException e) {
      e.printStackTrace();
      fail(e.getMessage());
      return;
    }
    String password = "my_p4s$w0Rd";
    String salt = "my_$4Lt";
    PasswordLibrary passwordLibrary =
        PasswordLibrary.fromPassword(
                "My Password Library", "Stores my super secret passwords.", password, salt)
            .addEntry(
                new PasswordEntry(
                    "Bank Account",
                    "Where my money goes.",
                    "pls_dont_steal",
                    LocalDateTime.now(),
                    Collections.emptyMap()))
            .addEntry(
                new PasswordEntry(
                    "Gaming Account",
                    "Because I'm an epic gamer.",
                    "mr-beast29",
                    LocalDateTime.now(),
                    new HashMap<LocalDateTime, String>() {
                      {
                        put(LocalDateTime.now().minusDays(4), "mr-wolf74");
                      }
                    }));
    try {
      passwordLibrary.writeToEncryptedFile(file, salt);
    } catch (IOException e) {
      e.printStackTrace();
      fail(e.getMessage());
      return;
    }
    PasswordLibrary passwordLibraryFromDisk;
    try {
      passwordLibraryFromDisk = PasswordLibrary.readFromEncryptedFile(file, password, salt);
    } catch (IOException | PasswordMismatchException e) {
      e.printStackTrace();
      fail(e.getMessage());
      return;
    }
    assertThat(passwordLibraryFromDisk).isEqualTo(passwordLibrary);
    assertThatThrownBy(() -> PasswordLibrary.readFromEncryptedFile(file, "wrong_password", salt))
        .hasCauseInstanceOf(Exception.class);
    assertThatThrownBy(() -> PasswordLibrary.readFromEncryptedFile(file, password, "wrong_salt"))
        .hasCauseInstanceOf(Exception.class);
  }
}
