package org.symphonyoss.integration.core;

import static javax.ws.rs.core.MediaType.TEXT_HTML_TYPE;
import static org.symphonyoss.integration.model.healthcheck.IntegrationFlags.ValueEnum.NOK;
import static org.symphonyoss.integration.model.healthcheck.IntegrationFlags.ValueEnum.OK;

import com.symphony.atlas.AtlasException;
import com.symphony.atlas.IAtlas;
import com.symphony.logging.ISymphonyLogger;
import com.symphony.logging.SymphonyLoggerFactory;

import com.google.common.cache.CacheBuilder;
import com.google.common.cache.CacheLoader;
import com.google.common.cache.LoadingCache;
import org.apache.commons.lang3.StringUtils;
import org.glassfish.jersey.client.ClientConfig;
import org.glassfish.jersey.client.ClientProperties;
import org.springframework.beans.factory.annotation.Autowired;
import org.symphonyoss.integration.IntegrationAtlas;
import org.symphonyoss.integration.IntegrationPropertiesReader;
import org.symphonyoss.integration.authentication.AuthenticationProxy;
import org.symphonyoss.integration.core.exception.CertificateNotFoundException;
import org.symphonyoss.integration.core.exception.LoadKeyStoreException;
import org.symphonyoss.integration.healthcheck.IntegrationHealthManager;
import org.symphonyoss.integration.model.Application;
import org.symphonyoss.integration.model.IntegrationBridge;
import org.symphonyoss.integration.model.IntegrationProperties;
import org.symphonyoss.integration.model.healthcheck.IntegrationFlags;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.security.GeneralSecurityException;
import java.security.KeyStore;
import java.util.concurrent.TimeUnit;

import javax.ws.rs.client.Client;
import javax.ws.rs.client.ClientBuilder;
import javax.ws.rs.client.WebTarget;
import javax.ws.rs.core.Response;

/**
 * Abstract integration class that contains the key services for all the integrations.
 * Separates methods, objects and logic that are common to any kind of integration on Integration Bridge so other
 * implementations don't need to "re-implement" their own.
 * Created by rsanchez on 21/11/16.
 */
public abstract class BaseIntegration {

  private static final ISymphonyLogger LOG = SymphonyLoggerFactory.getLogger(BaseIntegration.class);

  private static final String KEY_STORE_SUFFIX = ".keystore";
  private static final String KEY_STORE_PASSWORD_SUFFIX = ".keystore.password";
  private static final String KEY_STORE_TYPE_SUFFIX = ".keystore.type";

  private static final String DEFAULT_KEYSTORE_TYPE = "pkcs12";
  private static final String DEFAULT_KEYSTORE_TYPE_SUFFIX = ".p12";
  private static final String DEFAULT_KEYSTORE_PASSWORD = "changeit";
  private static final String CERTS_DIR = "certs";

  private static final String APPS_CONTEXT = "apps";

  private static final String APP_DEFAULT_PAGE = "controller.html";

  @Autowired
  protected IntegrationAtlas integrationAtlas;

  @Autowired
  protected AuthenticationProxy authenticationProxy;

  @Autowired
  private IntegrationPropertiesReader propertiesReader;

  /**
   * Status of the integration
   */
  protected IntegrationHealthManager healthManager = new IntegrationHealthManager();

  /**
   * HTTP client
   */
  private Client client;

  /**
   * Cache to 'configurator installed' flag.
   */
  private LoadingCache<String, IntegrationFlags.ValueEnum> configuratorFlagsCache;

  public BaseIntegration() {
    final ClientConfig configuration = new ClientConfig();
    configuration.property(ClientProperties.CONNECT_TIMEOUT, 1000);
    configuration.property(ClientProperties.READ_TIMEOUT, 1000);

    this.client = ClientBuilder.newBuilder().withConfig(configuration).build();

    this.configuratorFlagsCache = CacheBuilder.newBuilder().expireAfterWrite(60, TimeUnit.SECONDS)
        .build(new CacheLoader<String, IntegrationFlags.ValueEnum>() {
          @Override
          public IntegrationFlags.ValueEnum load(String key) throws Exception {
            return getConfiguratorInstalledFlag(key);
          }
        });
  }

  /**
   * Get application identifier based on integration username
   * @param integrationUser Integration username
   * @return Application identifier
   */
  protected String getApplicationId(String integrationUser) {
    Application application = propertiesReader.getProperties().getApplication(integrationUser);

    if ((application == null) || (StringUtils.isEmpty(application.getId()))) {
      return integrationUser;
    } else {
      return application.getId();
    }
  }

  /**
   * Read the key store and register the user with new SSL context.
   * @param integrationUser Integration username
   */
  protected void registerUser(String integrationUser) {
    IAtlas atlas = integrationAtlas.getAtlas();

    String locationProperty = integrationUser + KEY_STORE_SUFFIX;
    String passwordProperty = integrationUser + KEY_STORE_PASSWORD_SUFFIX;
    String typeProperty = integrationUser + KEY_STORE_TYPE_SUFFIX;

    String type =
        atlas.containsKey(typeProperty) ? atlas.get(typeProperty).trim() : DEFAULT_KEYSTORE_TYPE;

    String storeLocation;

    try {
      String certsDir = atlas.getConfigDir(CERTS_DIR).getAbsolutePath() + File.separator;
      String locationFile = atlas.containsKey(locationProperty) ? atlas.get(locationProperty).trim()
          : integrationUser + DEFAULT_KEYSTORE_TYPE_SUFFIX;
      storeLocation = certsDir + locationFile;
    } catch (AtlasException e) {
      throw new CertificateNotFoundException("Certificate folder not found at atlas home", e);
    }

    String password = atlas.containsKey(passwordProperty) ? atlas.get(passwordProperty).trim()
        : DEFAULT_KEYSTORE_PASSWORD;

    KeyStore keyStore;
    try {
      keyStore = loadKeyStore(storeLocation, password, type);
    } catch (GeneralSecurityException | IOException e) {
      throw new LoadKeyStoreException(
          String.format("Fail to load keystore file at %s", storeLocation), e);
    }

    healthManager.certificateInstalled(OK);
    authenticationProxy.registerUser(integrationUser, keyStore, password);
  }

  /**
   * Load the keystore file.
   * @param storeLocation Keystore path.
   * @param password Keystore password
   * @param type Keystore type
   * @return Keystore object
   */
  private KeyStore loadKeyStore(String storeLocation, String password, String type)
      throws GeneralSecurityException, IOException {
    final KeyStore ks = KeyStore.getInstance(type);
    ks.load(new FileInputStream(storeLocation), password.toCharArray());
    return ks;
  }

  /**
   * Retrieves the cached 'configurator installed' flag.
   * @param appType Application type
   */
  protected IntegrationFlags.ValueEnum getCachedConfiguratorInstalledFlag(String appType) {
    return configuratorFlagsCache.getUnchecked(appType);
  }

  /**
   * Retrieves the 'configurator installed' flag.
   * @param appType Application type
   */
  protected IntegrationFlags.ValueEnum getConfiguratorInstalledFlag(String appType) {
    IntegrationProperties properties = propertiesReader.getProperties();

    Application application = properties.getApplication(appType);
    IntegrationBridge bridge = properties.getIntegrationBridge();

    if ((application == null) || (bridge == null)) {
      return NOK;
    }

    return getConfiguratorInstalledFlag(bridge.getHost(), application.getContext());
  }

  /**
   * Retrieves the 'configurator installed' flag. This method sends a request to Configurator app if
   * the response is HTTP 200 then 'configurator installed' flag will be OK, otherwise it will be
   * NOK.
   * @param host Integration Bridge host
   * @param context Application context
   */
  private IntegrationFlags.ValueEnum getConfiguratorInstalledFlag(String host, String context) {
    if ((StringUtils.isEmpty(host)) || (StringUtils.isEmpty(context))) {
      return NOK;
    }

    try {
      String baseUrl = String.format("https://%s", host);

      WebTarget target =
          client.target(baseUrl).path(APPS_CONTEXT).path(context).path(APP_DEFAULT_PAGE);
      Response response = target.request().accept(TEXT_HTML_TYPE).get();

      if (Response.Status.OK.getStatusCode() == response.getStatus()) {
        return OK;
      } else {
        return NOK;
      }
    } catch (Exception e) {
      LOG.error("Fail to verify configurator status.", e);
      return NOK;
    }
  }

}
