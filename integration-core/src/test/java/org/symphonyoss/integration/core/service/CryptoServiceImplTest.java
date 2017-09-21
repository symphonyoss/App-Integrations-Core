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
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

import org.junit.Ignore;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;
import org.symphonyoss.integration.exception.CryptoException;
import org.symphonyoss.integration.logging.LogMessageSource;

import javax.crypto.IllegalBlockSizeException;

/**
 * Class with unit tests for {@link CryptoServiceImpl}
 * Created by campidelli on 05-sep-17.
 */
@RunWith(MockitoJUnitRunner.class)
public class CryptoServiceImplTest {

  private static final String TEXT = "Lorem ipsum dolor sit amet...";
  private static final String KEY = "crypto@key123";

  @Mock
  private LogMessageSource logMessage;

  @InjectMocks
  private CryptoServiceImpl cryptoService;

  @Ignore
  public void testEncrypt() throws CryptoException {
    String encryptedText = cryptoService.encrypt(TEXT, KEY);
    assertNotEquals(TEXT, encryptedText);
  }

  @Ignore
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

  @Ignore
  public void testDecryptInvalidBlockSize() throws CryptoException {
    try {
      cryptoService.decrypt("M4D4iJoiXPyhaJhdRq04BqY4UDmpObk9M4D4iJoiXPyhaJhdRq04BqY4UDmpObk", KEY);
      fail("Should have thrown CryptoException.");
    } catch (CryptoException e) {
      assertTrue(e.getCause() instanceof IllegalBlockSizeException);
    }
  }

}