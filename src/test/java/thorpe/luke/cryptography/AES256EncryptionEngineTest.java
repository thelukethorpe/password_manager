package thorpe.luke.cryptography;

import java.util.Arrays;
import java.util.Collection;

public class AES256EncryptionEngineTest extends EncryptionEngineTest {

  @Override
  protected Collection<EncryptionEngine> getSomeEncryptionEngines() {
    return Arrays.asList(
        AES256EncryptionEngine.fromPassword("password", "salt"),
        AES256EncryptionEngine.fromPassword("myP4s$w0rD", "mY$4Lt"),
        AES256EncryptionEngine.fromPassword("my_P4s$w0rD!", "mY_$4Lt!"),
        AES256EncryptionEngine.fromPassword(
            "loooooooooooooooooong_P4s$w0rD", "loooooooooooooooooong_$4Lt"),
        AES256EncryptionEngine.fromPassword(
            "veryyyyyyyyyyyyyyyy_loooooooooooooooooooooooooooooooooooong_P4s$w0rD",
            "veryyyyyyyyyyyyyyyy_loooooooooooooooooooooooooooooooooooong_$4Lt"));
  }
}
