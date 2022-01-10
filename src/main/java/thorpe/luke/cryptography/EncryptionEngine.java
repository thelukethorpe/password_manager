package thorpe.luke.cryptography;

public interface EncryptionEngine {
  String encrypt(String plainText);

  String decrypt(String cipherText);
}
