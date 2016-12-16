package org.symphonyoss.integration.core;

import static org.mockito.Mockito.doReturn;

import com.symphony.atlas.AtlasException;
import com.symphony.atlas.IAtlas;

import org.mockito.Spy;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.security.KeyStore;
import java.security.KeyStoreException;
import java.security.NoSuchAlgorithmException;
import java.security.cert.CertificateException;

/**
 * Holds mocks for other unit tests on this package.
 * Created by rsanchez on 22/11/16.
 */
public class CommonIntegrationTest {

  private static final String APP_TYPE = "jiraWebHookIntegration";

  private static final String DEFAULT_KEYSTORE_TYPE = "pkcs12";

  private static final String DEFAULT_KEYSTORE_PASSWORD = "changeit";

  private static final String DEFAULT_KEYSTORE_TYPE_SUFFIX = ".p12";

  private static final String CERTS_DIR = "certs";

  @Spy
  protected IAtlas atlas = new MockAtlas();

  protected void mockCertDir() throws IOException, AtlasException {
    Path tempDirectory = Files.createTempDirectory(CERTS_DIR);
    doReturn(tempDirectory.toFile()).when(atlas).getConfigDir(CERTS_DIR);
  }

  protected void mockKeyStore()
      throws KeyStoreException, IOException, NoSuchAlgorithmException, CertificateException,
      AtlasException {
    KeyStore ks = KeyStore.getInstance(DEFAULT_KEYSTORE_TYPE);

    char[] password = DEFAULT_KEYSTORE_PASSWORD.toCharArray();
    ks.load(null, password);

    // Store away the keystore.
    String filename = APP_TYPE + DEFAULT_KEYSTORE_TYPE_SUFFIX;
    String certsDir = atlas.getConfigDir(CERTS_DIR).getAbsolutePath() + File.separator;
    String storeLocation = certsDir + filename;

    try (FileOutputStream fos = new FileOutputStream(storeLocation)) {
      ks.store(fos, password);
    }
  }
}
