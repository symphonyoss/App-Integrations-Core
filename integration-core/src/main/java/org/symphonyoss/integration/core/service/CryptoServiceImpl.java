package org.symphonyoss.integration.core.service;

import com.symphony.security.exceptions.CiphertextTransportIsEmptyException;
import com.symphony.security.exceptions.CiphertextTransportVersionException;
import com.symphony.security.exceptions.InvalidDataException;
import com.symphony.security.exceptions.SymphonyEncryptionException;
import com.symphony.security.exceptions.SymphonyInputException;
import com.symphony.security.helper.ClientCryptoHandler;
import com.symphony.security.helper.IClientCryptoHandler;
import com.symphony.security.helper.KeyIdentifier;
import com.gs.ti.wpt.lc.security.cryptolib.PBKDF;

import org.apache.commons.codec.binary.Base64;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.symphonyoss.integration.exception.CryptoException;
import org.symphonyoss.integration.logging.LogMessageSource;
import org.symphonyoss.integration.service.CryptoService;

import java.io.UnsupportedEncodingException;
import java.nio.ByteBuffer;
import java.security.SecureRandom;

/**
 * Implementation of a text-based cryptography service. Basically it works as follows:
 *
 * 1. A String is passed to be used as a SecretKey to encrypt/decrypt another given String.
 * 2. The CryptoService derives from this key string.
 * 2.1. To derive, it follows these steps:
 * a. Generates a SecureRandom Salt;
 * b. Creates a password-based encryption spec using the PBKDF2_SHA256 algorithm,
 * the Key string as an array of bytes and the generated salt.
 * c. With the created spec, it generates an AES SecretKey, this will be the key used to
 * encrypt and decrypt the given plain text.
 * 3. We concatenate the generated salt array of bytes to the encrypted text array of bytes.
 * 4. The result array of bytes is returned as a Base64 string, when encrypting.
 * 5. When decrypting, we use the same flow, but we have to convert the Base64 string to an array
 * of bytes, strip off the salt and decrypt it using the AES derived key.
 *
 * Created by campidelli on 9/5/17.
 */
@Component
public class CryptoServiceImpl implements CryptoService {

  private static final int STREAM_ID_SIZE = 25;
  private static final int SALT_SIZE = 8;
  private static final int NUMBER_OF_ITERATIONS = 10000;
  private static final String CHARSET = "UTF-8";

  private static final String INVALID_PARAMETER = "core.crypto.invalid.parameter";
  private static final String INVALID_PARAMETER_SOLUTION = INVALID_PARAMETER + ".solution";
  private static final String INVALID_ENCRYPTED_TXT = "core.crypto.invalid.encrypted.text";
  private static final String INVALID_ENCRYPTED_TXT_SOLUTION = INVALID_ENCRYPTED_TXT + ".solution";
  private static final String UNSUPPORTED_ENCODING = "core.crypto.unsupported.encoding";
  private static final String UNSUPPORTED_ENCODING_SOLUTION = UNSUPPORTED_ENCODING + ".solution";
  private static final String GENERAL_CRYPTO_ERROR = "core.crypto.general.error";
  private static final String GENERAL_CRYPTO_ERROR_SOLUTION = GENERAL_CRYPTO_ERROR + ".solution";
  private static final String INVALID_INPUT_TXT = "core.crypto.invalid.input.text";
  private static final String INVALID_INPUT_TXT_SOLUTION = INVALID_INPUT_TXT + ".solution";
  private static final String INVALID_CIPHER_TXT = "core.crypto.invalid.ciphertext.transport";
  private static final String INVALID_CIPHER_TXT_SOLUTION = INVALID_CIPHER_TXT + ".solution";

  @Autowired
  private LogMessageSource logMessage;

  /**
   * @see CryptoService#encrypt(String, String)
   */
  @Override
  public String encrypt(String plainText, String key) throws CryptoException {
    checkParameters("plainText", plainText);
    checkParameters("key", key);

    // Generate a random salt
    SecureRandom random = new SecureRandom();
    byte saltBytes[] = new byte[SALT_SIZE];
    random.nextBytes(saltBytes);

    // Encrypt the plain text
    byte[] encryptedTextBytes = doCipher(key, plainText, saltBytes, true);

    // Prepend salt
    byte[] buffer = new byte[saltBytes.length + encryptedTextBytes.length];
    System.arraycopy(saltBytes, 0, buffer, 0, saltBytes.length);
    System.arraycopy(encryptedTextBytes, 0, buffer, saltBytes.length, encryptedTextBytes.length);
    return Base64.encodeBase64String(buffer);
  }

  /**
   * @see CryptoService#decrypt(String, String)
   */
  @Override
  public String decrypt(String encryptedText, String key) throws CryptoException {
    checkParameters("encryptedText", encryptedText);
    checkParameters("key", key);
    try {
      // Strip off the salt
      ByteBuffer buffer = ByteBuffer.wrap(Base64.decodeBase64(encryptedText));
      int minLength = SALT_SIZE + 1;
      if (buffer.capacity() < minLength) {
        throw new CryptoException(logMessage.getMessage(INVALID_ENCRYPTED_TXT, encryptedText),
            logMessage.getMessage(INVALID_ENCRYPTED_TXT_SOLUTION,
                String.valueOf(buffer.capacity()), String.valueOf(minLength)));
      }
      byte[] saltBytes = new byte[SALT_SIZE];
      buffer.get(saltBytes, 0, saltBytes.length);
      byte[] encryptedTextBytes = new byte[buffer.capacity() - saltBytes.length];
      buffer.get(encryptedTextBytes);
      encryptedText = Base64.encodeBase64String(encryptedTextBytes);

      // Decrypt the encrypted text
      byte[] decryptedTextBytes = doCipher(key, encryptedText, saltBytes, false);
      return new String(decryptedTextBytes, CHARSET);

    } catch (UnsupportedEncodingException e) {
      throw new CryptoException(logMessage.getMessage(UNSUPPORTED_ENCODING), e,
          logMessage.getMessage(UNSUPPORTED_ENCODING_SOLUTION, CHARSET));
    }
  }

  /**
   * Check if the given parameter is present and valid.
   * @param param Parameter name.
   * @param value Parameter value.
   * @throws CryptoException Thrown when the parameter is invalid or absent.
   */
  private void checkParameters(String param, String value) throws CryptoException {
    if (!StringUtils.isEmpty(param) && StringUtils.isEmpty(value)) {
      throw new CryptoException(logMessage.getMessage(INVALID_PARAMETER, param),
          logMessage.getMessage(INVALID_PARAMETER_SOLUTION, param));
    }
  }

  /**
   * This method was created to decrease the amount of duplicate code (exception
   * handling), since those exceptions are not throwable from the Unit Test and our minimum
   * coverage is 90% per project.
   *
   * It performs an encryption or decryption according to the passed MODE.
   *
   * @param key Key used to derive a PasswordBasedKey used during the cryptography process.
   * @param text Text to encrypted or decrypted.
   * @param salt Salt used to reinforce the security.
   * @param encrypt MODE, set to TRUE to encrypt or FALSE to decrypt.
   * @return Encrypted or decrypted text as an array of bytes.
   * @throws CryptoException In case of error during the cryptography process.
   */
  private byte[] doCipher(String key, String text, byte[] salt, boolean encrypt)
      throws CryptoException {
    try {
      if (encrypt) {
        // Derive a Password-based key from the key parameter + salt
        byte[] pbKey = PBKDF.PBKDF2_SHA256(key.getBytes(CHARSET), salt, NUMBER_OF_ITERATIONS);
        // Encrypt the plain text
        KeyIdentifier keyId = new KeyIdentifier(new byte[STREAM_ID_SIZE], 0L, 0L);
        IClientCryptoHandler clientCryptoHandler = new ClientCryptoHandler();
        return clientCryptoHandler.encryptMsg(pbKey, keyId, text.getBytes(CHARSET));
      }

      // Derive a Password-based key from the key parameter + salt
      byte[] pbKey = PBKDF.PBKDF2_SHA256(key.getBytes(CHARSET), salt, NUMBER_OF_ITERATIONS);
      // Decrypt the encrypted text
      IClientCryptoHandler clientCryptoHandler = new ClientCryptoHandler();
      byte[] encryptedTextBytes = Base64.decodeBase64(text);
      return clientCryptoHandler.decryptMsg(pbKey, encryptedTextBytes);

    } catch (SymphonyEncryptionException | InvalidDataException e) {
      throw new CryptoException(logMessage.getMessage(GENERAL_CRYPTO_ERROR), e,
          logMessage.getMessage(GENERAL_CRYPTO_ERROR_SOLUTION, e.getMessage()));
    } catch (SymphonyInputException e) {
      throw new CryptoException(logMessage.getMessage(INVALID_INPUT_TXT), e,
          logMessage.getMessage(INVALID_INPUT_TXT_SOLUTION, e.getMessage()));
    } catch (CiphertextTransportVersionException | CiphertextTransportIsEmptyException e) {
      throw new CryptoException(logMessage.getMessage(INVALID_CIPHER_TXT), e,
          logMessage.getMessage(INVALID_CIPHER_TXT_SOLUTION, e.getMessage()));
    } catch (UnsupportedEncodingException e) {
      throw new CryptoException(logMessage.getMessage(UNSUPPORTED_ENCODING), e,
          logMessage.getMessage(UNSUPPORTED_ENCODING_SOLUTION, CHARSET));
    }
  }
}
