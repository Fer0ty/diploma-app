package diploma.ecommerce.backend.shopbase.service;

public interface CryptoService {
    String encrypt(String plainText);
    String decrypt(String encryptedText);
}