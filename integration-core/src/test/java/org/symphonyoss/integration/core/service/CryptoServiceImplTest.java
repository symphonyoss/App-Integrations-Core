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

import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;
import org.symphonyoss.integration.exception.CryptoRuntimeException;
import org.symphonyoss.integration.logging.LogMessageSource;

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

  @Test
  public void testEncrypt() {
    String encryptedText = cryptoService.encrypt(TEXT, KEY);
    assertNotEquals(TEXT, encryptedText);
  }

  @Test
  public void testDecrypt() {
    String encryptedText = cryptoService.encrypt(TEXT, KEY);
    String decryptedText = cryptoService.decrypt(encryptedText, KEY);
    assertEquals(TEXT, decryptedText);
  }

  @Test(expected = CryptoRuntimeException.class)
  public void testEncryptInvalidText() {
    cryptoService.encrypt(null, KEY);
  }

  @Test(expected = CryptoRuntimeException.class)
  public void testEncryptInvalidKey() {
    cryptoService.encrypt(TEXT, null);
  }

  @Test(expected = CryptoRuntimeException.class)
  public void testDecryptInvalidText() {
    cryptoService.decrypt(null, KEY);
  }

  @Test(expected = CryptoRuntimeException.class)
  public void testDecryptInvalidKey() {
    cryptoService.decrypt(TEXT, null);
  }
}