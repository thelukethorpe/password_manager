package thorpe.luke.cryptography;

import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.Base64;

public class SHA256CryptographicHashingEngine implements CryptographicHashingEngine {
  private static final String HASHING_ALGORITHM = "SHA-256";

  private final MessageDigest messageDigest;
  private final String salt;

  private SHA256CryptographicHashingEngine(String salt) throws NoSuchAlgorithmException {
    this.messageDigest = MessageDigest.getInstance(HASHING_ALGORITHM);
    this.salt = salt;
  }

  public static SHA256CryptographicHashingEngine fromSalt(String salt) {
    try {
      return new SHA256CryptographicHashingEngine(salt);
    } catch (NoSuchAlgorithmException e) {
      throw new SHA256CryptographicHashing(e);
    }
  }

  @Override
  public String hash(String text) {
    byte[] hash = messageDigest.digest((text + salt).getBytes());
    return Base64.getEncoder().encodeToString(hash);
  }
}
