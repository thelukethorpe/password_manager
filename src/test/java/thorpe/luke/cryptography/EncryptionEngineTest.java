package thorpe.luke.cryptography;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.Collection;
import java.util.function.BiConsumer;
import java.util.stream.Stream;
import org.junit.Test;

public abstract class EncryptionEngineTest {

  protected abstract Collection<EncryptionEngine> getSomeEncryptionEngines();

  protected void forManyPlainTexts(BiConsumer<EncryptionEngine, String> test) {
    Stream.of(
            "hello there!",
            "GENERAL KENOBI",
            "Hey guys, Mr MLG_n0sc0per_420_xXx here",
            "this is a very loooooooooooooooooong string",
            "")
        .forEach(
            plainText ->
                getSomeEncryptionEngines()
                    .forEach(encryptionEngine -> test.accept(encryptionEngine, plainText)));
  }

  @Test
  public void testDecryptedCipherTextIsEqualToPlainText() {
    forManyPlainTexts(
        ((encryptionEngine, plainText) -> {
          String cipherText = encryptionEngine.encrypt(plainText);
          String decryptedCipherText = encryptionEngine.decrypt(cipherText);
          assertThat(decryptedCipherText).isEqualTo(plainText);
        }));
  }
}
