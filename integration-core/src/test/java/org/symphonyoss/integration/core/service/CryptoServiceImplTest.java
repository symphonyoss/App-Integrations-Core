/**
 * Copyright 2016-2017 Symphony Integrations - Symphony LLC
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.symphonyoss.integration.core.service;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;
import static org.mockito.Matchers.any;
import static org.mockito.Matchers.anyString;
import static org.mockito.Mockito.doThrow;

import com.symphony.security.exceptions.CiphertextTransportVersionException;
import com.symphony.security.exceptions.SymphonyEncryptionException;
import com.symphony.security.exceptions.SymphonyInputException;
import com.symphony.security.helper.ClientCryptoHandler;
import com.symphony.security.helper.IClientCryptoHandler;
import com.symphony.security.helper.KeyIdentifier;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Spy;
import org.mockito.runners.MockitoJUnitRunner;
import org.symphonyoss.integration.exception.CryptoException;
import org.symphonyoss.integration.logging.LogMessageSource;

import java.io.UnsupportedEncodingException;

/**
 * Class with unit tests for {@link CryptoServiceImpl}
 * Created by campidelli on 05-sep-17.
 */
@RunWith(MockitoJUnitRunner.class)
public class CryptoServiceImplTest {

  private static final String TEXT = "Lorem ipsum dolor sit amet...";
  private static final String KEY = "crypto@key123";
  private static final String FAIL_MSG = "Should have thrown a CryptoException.";

  @Mock
  private LogMessageSource logMessage;

  @Spy
  private IClientCryptoHandler clientCryptoHandler = new ClientCryptoHandler();

  @InjectMocks
  private CryptoServiceImpl cryptoService;

  @Test
  public void testEncrypt() throws CryptoException {
    String encryptedText = cryptoService.encrypt(TEXT, KEY);
    assertNotEquals(TEXT, encryptedText);
  }

  @Test
  public void testDecrypt() throws CryptoException {
    String encryptedText = cryptoService.encrypt(TEXT, KEY);
    String decryptedText = cryptoService.decrypt(encryptedText, KEY);
    assertEquals(TEXT, decryptedText);
  }

  @Test(expected = CryptoException.class)
  public void testEncryptInvalidText() throws CryptoException {
    cryptoService.encrypt(null, KEY);
  }

  @Test(expected = CryptoException.class)
  public void testEncryptInvalidKey() throws CryptoException {
    cryptoService.encrypt(TEXT, null);
  }

  @Test(expected = CryptoException.class)
  public void testDecryptInvalidText() throws CryptoException {
    cryptoService.decrypt(null, KEY);
  }

  @Test(expected = CryptoException.class)
  public void testDecryptInvalidTextLength() throws CryptoException {
    cryptoService.decrypt("123", KEY);
  }

  @Test(expected = CryptoException.class)
  public void testDecryptInvalidKey() throws CryptoException {
    cryptoService.decrypt(TEXT, null);
  }

  @Test
  public void testEncryptSymphonyEncryptionException() {
    testEncryptCryptoLibExceptions(SymphonyEncryptionException.class);
  }

  @Test
  public void testEncryptSymphonyInputException() {
    testEncryptCryptoLibExceptions(SymphonyInputException.class);
  }

  @Test
  public void testEncryptCiphertextTransportVersionException() {
    testEncryptCryptoLibExceptions(CiphertextTransportVersionException.class);
  }

  @Test
  public void testEncryptUnsupportedEncodingException() {
    testEncryptCryptoLibExceptions(UnsupportedEncodingException.class);
  }

  private void testEncryptCryptoLibExceptions(Class exceptionClass) {
    try {
      doThrow(exceptionClass).when(clientCryptoHandler).encryptMsg(
          any(byte[].class), any(KeyIdentifier.class), any(byte[].class));
      cryptoService.encrypt(TEXT, KEY);
      fail(FAIL_MSG);
    } catch (Exception e) {
      assertTrue(e instanceof CryptoException);
      CryptoException ce = (CryptoException) e;
      assertNotNull(ce.getCause());
      assertEquals(ce.getCause().getClass(), exceptionClass);
    }
  }

  @Test
  public void testDecryptSymphonyEncryptionException() {
    testDecryptCryptoLibExceptions(SymphonyEncryptionException.class);
  }

  @Test
  public void testDecryptSymphonyInputException() {
    testDecryptCryptoLibExceptions(SymphonyInputException.class);
  }

  @Test
  public void testDecryptCiphertextTransportVersionException() {
    testDecryptCryptoLibExceptions(CiphertextTransportVersionException.class);
  }

  @Test
  public void testDecryptUnsupportedEncodingException() {
    testDecryptCryptoLibExceptions(UnsupportedEncodingException.class);
  }

  private void testDecryptCryptoLibExceptions(Class exceptionClass) {
    try {
      doThrow(exceptionClass).when(clientCryptoHandler).decryptMsg(any(byte[].class),
          any(byte[].class));
      cryptoService.decrypt(TEXT, KEY);
      fail(FAIL_MSG);
    } catch (Exception e) {
      assertTrue(e instanceof CryptoException);
      CryptoException ce = (CryptoException) e;
      assertNotNull(ce.getCause());
      assertEquals(ce.getCause().getClass(), exceptionClass);
    }
  }
}