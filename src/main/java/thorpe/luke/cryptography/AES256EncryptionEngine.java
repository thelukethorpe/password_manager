package thorpe.luke.cryptography;

import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.security.spec.InvalidKeySpecException;
import java.security.spec.KeySpec;
import java.util.Base64;
import javax.crypto.*;
import javax.crypto.spec.PBEKeySpec;
import javax.crypto.spec.SecretKeySpec;

public class AES256EncryptionEngine implements EncryptionEngine {
  private static final String ENCRYPTION_ALGORITHM = "AES/ECB/PKCS5Padding";
  private static final String KEY_GENERATION_ALGORITHM = "PBKDF2WithHmacSHA256";
  private static final String KEY_SPECIFICATION_ALGORITHM = "AES";

  private final SecretKey key;
  private final Cipher cipher;

  private AES256EncryptionEngine(SecretKey key)
      throws NoSuchPaddingException, NoSuchAlgorithmException {
    this.key = key;
    this.cipher = Cipher.getInstance(ENCRYPTION_ALGORITHM);
  }

  public static AES256EncryptionEngine fromPassword(String password, String salt) {
    SecretKeyFactory keyFactory;
    try {
      keyFactory = SecretKeyFactory.getInstance(KEY_GENERATION_ALGORITHM);
      KeySpec keySpec = new PBEKeySpec(password.toCharArray(), salt.getBytes(), 65536, 256);
      SecretKey key =
          new SecretKeySpec(
              keyFactory.generateSecret(keySpec).getEncoded(), KEY_SPECIFICATION_ALGORITHM);
      return new AES256EncryptionEngine(key);
    } catch (NoSuchAlgorithmException | InvalidKeySpecException | NoSuchPaddingException e) {
      throw new AES256EncryptionException(e);
    }
  }

  @Override
  public synchronized String encrypt(String plainText) {
    try {
      cipher.init(Cipher.ENCRYPT_MODE, key);
      byte[] cipherText = cipher.doFinal(plainText.getBytes());
      return Base64.getEncoder().encodeToString(cipherText);
    } catch (IllegalBlockSizeException | BadPaddingException | InvalidKeyException e) {
      throw new AES256EncryptionException(e);
    }
  }

  @Override
  public synchronized String decrypt(String cipherText) throws KeyMismatchException {
    try {
      cipher.init(Cipher.DECRYPT_MODE, key);
      byte[] plainText = cipher.doFinal(Base64.getDecoder().decode(cipherText));
      return new String(plainText);
    } catch (IllegalBlockSizeException | BadPaddingException | InvalidKeyException e) {
      throw new KeyMismatchException(e);
    }
  }
}
