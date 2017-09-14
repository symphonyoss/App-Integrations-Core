package org.symphonyoss.integration.core.service;

import org.apache.commons.codec.binary.Base64;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.symphonyoss.integration.exception.CryptoException;
import org.symphonyoss.integration.logging.LogMessageSource;
import org.symphonyoss.integration.service.CryptoService;

import java.io.UnsupportedEncodingException;
import java.nio.ByteBuffer;
import java.security.AlgorithmParameters;
import java.security.InvalidAlgorithmParameterException;
import java.security.InvalidKeyException;
import java.security.Key;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.security.spec.AlgorithmParameterSpec;
import java.security.spec.InvalidKeySpecException;
import java.security.spec.InvalidParameterSpecException;

import javax.crypto.BadPaddingException;
import javax.crypto.Cipher;
import javax.crypto.IllegalBlockSizeException;
import javax.crypto.NoSuchPaddingException;
import javax.crypto.SecretKey;
import javax.crypto.SecretKeyFactory;
import javax.crypto.spec.IvParameterSpec;
import javax.crypto.spec.PBEKeySpec;
import javax.crypto.spec.SecretKeySpec;

/**
 * Implementation of a text-based cryptography service. Basically it works as follows:
 *
 * 1. A String is passed to be used as a SecretKey to encrypt/decrypt another given String.
 * 2. The CryptoService derives from this key string.
 *    2.1. To derive, it follows these steps:
 *         a. Generates a SecureRandom Salt with 20 bytes;
 *         b. Creates a password-based encryption spec using the PBKDF2WithHmacSHA1 algorithm,
 *            the Key string as an array of bytes and the generated salt.
 *         c. With the created spec, it generates an AES SecretKey, this will be the key used to
 *            encrypt and decrypt the given plain text.
 * 3. We concatenate the generated salt array of bytes to the encrypted text array of bytes.
 * 4. The result array of bytes is returned as a Base64 string, when encrypting.
 * 5. When decrypting, we use the same flow, but we have to convert the Base64 string to an array
 *    of bytes, strip off the salt and decrypt it using the AES derived key.
 *
 * Created by campidelli on 9/5/17.
 */
@Component
public class CryptoServiceImpl implements CryptoService {

  private static final int ITERATIONS = 65536;
  private static final int KEY_SIZE = 256;
  private static final int SALT_SIZE = 20;
  private static final String KEY_ALGORITHM = "PBKDF2WithHmacSHA1";
  private static final String SPEC_ALGORITHM = "AES";
  private static final String MODE = "CBC";
  private static final String PADDING = "PKCS5Padding";
  private static final String TRANSFORMATION = SPEC_ALGORITHM + "/" + MODE + "/" + PADDING;
  private static final String CHARSET = "UTF-8";

  private static final String INVALID_PARAMETER = "core.crypto.invalid.parameter";
  private static final String INVALID_PARAMETER_SOLUTION = INVALID_PARAMETER + ".solution";
  private static final String NO_SUCH_ALGORITHM = "core.crypto.no.such.algorithm";
  private static final String NO_SUCH_ALGORITHM_SOLUTION = NO_SUCH_ALGORITHM + ".solution";
  private static final String INVALID_KEY_SPEC = "core.crypto.invalid.key.spec";
  private static final String INVALID_KEY_SPEC_SOLUTION = INVALID_KEY_SPEC + ".solution";
  private static final String NO_SUCH_PADDING = "core.crypto.no.such.padding";
  private static final String NO_SUCH_PADDING_SOLUTION = NO_SUCH_PADDING + ".solution";
  private static final String INVALID_KEY = "core.crypto.invalid.key";
  private static final String INVALID_KEY_SOLUTION = INVALID_KEY + ".solution";
  private static final String INVALID_PARAM_SPEC = "core.crypto.invalid.param.spec";
  private static final String INVALID_PARAM_SPEC_SOLUTION = INVALID_PARAM_SPEC + ".solution";
  private static final String ILLEGAL_BLOCK_SIZE = "core.crypto.illegal.block.size";
  private static final String ILLEGAL_BLOCK_SIZE_SOLUTION = ILLEGAL_BLOCK_SIZE + ".solution";
  private static final String BAD_PADDING = "core.crypto.bad.padding";
  private static final String BAD_PADDING_SOLUTION = BAD_PADDING + ".solution";
  private static final String UNSUPPORTED_ENCODING = "core.crypto.unsupported.encoding";
  private static final String UNSUPPORTED_ENCODING_SOLUTION = UNSUPPORTED_ENCODING + ".solution";

  @Autowired
  private LogMessageSource logMessage;

  /**
   * @see CryptoService#encrypt(String, String)
   */
  @Override
  public String encrypt(String plainText, String key) throws CryptoException {
    checkParameters("plainText", plainText);
    checkParameters("key", key);
    try {
      byte[] saltBytes = generateSalt();
      SecretKeySpec secret = deriveKey(key, saltBytes);

      Cipher cipher = getCipher();
      initCipher(cipher, Cipher.ENCRYPT_MODE, secret, null);
      AlgorithmParameters params = cipher.getParameters();
      byte[] ivBytes = params.getParameterSpec(IvParameterSpec.class).getIV();
      byte[] encryptedTextBytes = doCipher(cipher, plainText.getBytes(CHARSET));

      // Prepend Salt and VI
      byte[] buffer = new byte[saltBytes.length + ivBytes.length + encryptedTextBytes.length];
      System.arraycopy(saltBytes, 0, buffer, 0, saltBytes.length);
      System.arraycopy(ivBytes, 0, buffer, saltBytes.length, ivBytes.length);
      System.arraycopy(encryptedTextBytes, 0, buffer, saltBytes.length + ivBytes.length,
          encryptedTextBytes.length);

      return Base64.encodeBase64String(buffer);
    } catch (InvalidParameterSpecException e) {
      throw new CryptoException(logMessage.getMessage(INVALID_PARAM_SPEC), e,
          logMessage.getMessage(INVALID_PARAM_SPEC_SOLUTION));
    } catch (UnsupportedEncodingException e) {
      throw new CryptoException(logMessage.getMessage(UNSUPPORTED_ENCODING), e,
          logMessage.getMessage(UNSUPPORTED_ENCODING_SOLUTION, CHARSET));
    }
  }

  /**
   * @see CryptoService#decrypt(String, String)
   */
  @Override
  public String decrypt(String encryptedText, String key) throws CryptoException {
    checkParameters("encryptedText", encryptedText);
    checkParameters("key", key);
    Cipher cipher = getCipher();
    // Strip off the Salt and IV
    ByteBuffer buffer = ByteBuffer.wrap(Base64.decodeBase64(encryptedText));
    byte[] saltBytes = new byte[SALT_SIZE];
    buffer.get(saltBytes, 0, saltBytes.length);
    byte[] ivBytes = new byte[cipher.getBlockSize()];
    buffer.get(ivBytes, 0, ivBytes.length);
    byte[] encryptedTextBytes = new byte[buffer.capacity() - saltBytes.length - ivBytes.length];
    buffer.get(encryptedTextBytes);

    SecretKey secret = deriveKey(key, saltBytes);
    initCipher(cipher, Cipher.DECRYPT_MODE, secret, new IvParameterSpec(ivBytes));
    byte[] decryptedTextBytes = doCipher(cipher, encryptedTextBytes);
    return new String(decryptedTextBytes);
  }

  private void checkParameters(String param, String value) throws CryptoException {
    if (!StringUtils.isEmpty(param) && StringUtils.isEmpty(value)) {
      throw new CryptoException(logMessage.getMessage(INVALID_PARAMETER, param),
          logMessage.getMessage(INVALID_PARAMETER_SOLUTION, param));
    }
  }

  private byte[] generateSalt() {
    SecureRandom random = new SecureRandom();
    byte bytes[] = new byte[SALT_SIZE];
    random.nextBytes(bytes);
    return bytes;
  }

  private SecretKeySpec deriveKey(String key, byte[] salt) throws CryptoException {
    try {
      char[] keyBytes = key.toCharArray();
      SecretKeyFactory factory = SecretKeyFactory.getInstance(KEY_ALGORITHM);
      PBEKeySpec spec = new PBEKeySpec(keyBytes, salt, ITERATIONS, KEY_SIZE);
      SecretKey secretKey = factory.generateSecret(spec);
      return new SecretKeySpec(secretKey.getEncoded(), SPEC_ALGORITHM);
    } catch (NoSuchAlgorithmException e) {
      throw new CryptoException(logMessage.getMessage(NO_SUCH_ALGORITHM), e,
          logMessage.getMessage(NO_SUCH_ALGORITHM_SOLUTION, KEY_ALGORITHM));
    } catch (InvalidKeySpecException e) {
      throw new CryptoException(logMessage.getMessage(INVALID_KEY_SPEC), e,
          logMessage.getMessage(INVALID_KEY_SPEC_SOLUTION, "PBEKeySpec"));
    }
  }

  // The following 3 methods were created to decrease the amount of duplicate code (exception
  // handling), since those exceptions are not throwable from the Unit Test and our minimum
  // coverage is 90% per project.
  private Cipher getCipher() throws CryptoException {
    try {
      return Cipher.getInstance(TRANSFORMATION);
    } catch (NoSuchAlgorithmException e) {
      throw new CryptoException(logMessage.getMessage(NO_SUCH_ALGORITHM), e,
          logMessage.getMessage(NO_SUCH_ALGORITHM_SOLUTION, SPEC_ALGORITHM));
    } catch (NoSuchPaddingException e) {
      throw new CryptoException(logMessage.getMessage(NO_SUCH_PADDING), e,
          logMessage.getMessage(NO_SUCH_PADDING_SOLUTION, PADDING));
    }
  }

  private void initCipher(Cipher cipher, int mode, Key key, AlgorithmParameterSpec spec)
      throws CryptoException {
    try {
      if (spec == null) {
        cipher.init(mode, key);
      } else {
        cipher.init(mode, key, spec);
      }
    } catch (InvalidKeyException | InvalidAlgorithmParameterException e) {
      throw new CryptoException(logMessage.getMessage(INVALID_KEY), e,
          logMessage.getMessage(INVALID_KEY_SOLUTION));
    }
  }

  private byte[] doCipher(Cipher cipher, byte[] text) throws CryptoException {
    int blockSize = cipher.getBlockSize();
    try {
      return cipher.doFinal(text);
    } catch (IllegalBlockSizeException e) {
      throw new CryptoException(logMessage.getMessage(ILLEGAL_BLOCK_SIZE), e,
          logMessage.getMessage(ILLEGAL_BLOCK_SIZE_SOLUTION, String.valueOf(blockSize)));
    } catch (BadPaddingException e) {
      throw new CryptoException(logMessage.getMessage(BAD_PADDING), e,
          logMessage.getMessage(BAD_PADDING_SOLUTION, TRANSFORMATION));
    }
  }
}
