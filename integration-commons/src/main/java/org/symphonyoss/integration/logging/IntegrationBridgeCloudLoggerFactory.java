package org.symphonyoss.integration.logging;

import static com.symphony.atlas.config.SymphonyAtlas.ACCOUNT;
import static com.symphony.atlas.config.SymphonyAtlas.SECRET;
import static com.symphony.atlas.config.SymphonyAtlas.SYMPHONY_URL;

import com.symphony.config.ConfigurationException;
import com.symphony.logging.ISymphonyCloudIdentifierProvider;
import com.symphony.logging.ISymphonyLogger;
import com.symphony.logging.ISymphonyOnPremKeyProvider;
import com.symphony.logging.SymphonyLoggerFactory;
import com.symphony.security.cache.InMemoryPersister;
import com.symphony.security.clientsdk.client.AuthProvider;
import com.symphony.security.clientsdk.client.ClientIdentifierFilter;
import com.symphony.security.clientsdk.client.SymphonyClient;
import com.symphony.security.clientsdk.client.SymphonyClientConfig;
import com.symphony.security.clientsdk.client.impl.SymphonyClientFactory;
import com.symphony.security.exceptions.SymphonyEncryptionException;
import com.symphony.security.exceptions.SymphonyInputException;
import com.symphony.security.exceptions.SymphonyNativeException;

import com.google.common.annotations.VisibleForTesting;

import java.io.IOException;
import java.util.Properties;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;

/**
 * Factory responsible to create the cloud logger.
 *
 * This instance will post ERROR and FATAL levels to Symphony's cloud.
 *
 * Created by cmarcondes on 11/17/16.
 */
public class IntegrationBridgeCloudLoggerFactory implements ISymphonyOnPremKeyProvider,
    ISymphonyCloudIdentifierProvider {

  private static final ISymphonyLogger LOCAL_LOGGER =
      SymphonyLoggerFactory.getLogger(IntegrationBridgeCloudLoggerFactory.class);

  private static final String IB_LOGGER_NAME = "IntegrationBridgeLog";
  private static final String CONFIG_ENABLE_REMOTE = "enableRemote";
  private static final String CONFIG_LOG_LEVEL = "cloudLoggerLevel";
  private static final String CONFIG_HARVESTER_URL = "cloudLogHarvesterURL";

  private static final int READ_TIME_OUT = 5000;
  private static final int CONNECTION_TIME_OUT = 1000;

  private static IntegrationBridgeCloudLoggerFactory instance =
      new IntegrationBridgeCloudLoggerFactory();

  private AuthProvider authProvider;
  private SymphonyClient symphonyClient;
  private boolean initialized;
  private ExecutorService executorForAuthentication = Executors.newSingleThreadExecutor();
  private Future<?> authenticationFuture;

  /**
   * Initiates all the variable needed to be used by the Cloud logger.
   * Reads properties from atlas file.
   * Creates a Symphony Client.
   * Creates an AuthProvider to authenticate with the username and password got from atlas file.
   */
  @VisibleForTesting
  IntegrationBridgeCloudLoggerFactory() {
    try {
      Properties properties = getProperties();

      prepareAuthentication(properties);

      setSystemProperties(properties);

      initialized = true;

    } catch (Exception e) {
      String msg = "Faileld to initialize factory for integration bridge's cloud logger";
      LOCAL_LOGGER.error(msg, new CloudLoggerException(msg, e));
    }
  }

  /**
   * Returns an instance of the Symphony's cloud logger
   * @param clazz
   * @return Logger instance
   */
  public static ISymphonyLogger getLogger(Class<?> clazz) {
    if (instance.initialized) {
      return SymphonyLoggerFactory.getCloudLogger(clazz, instance, instance).useRemoteLog();
    }

    LOCAL_LOGGER.warn(
        "Factory for Integration Bridge's cloud logger is not initialized. Default local logger"
            + " will be used.");
    return SymphonyLoggerFactory.getLogger(clazz);
  }

  private void setSystemProperties(Properties properties) {
    if (System.getProperty(CONFIG_ENABLE_REMOTE) == null) {
      System.setProperty(CONFIG_ENABLE_REMOTE, "true");
    }
    if (System.getProperty(CONFIG_LOG_LEVEL) == null) {
      System.setProperty(CONFIG_LOG_LEVEL, "DEBUG");
    }
    if (System.getProperty(CONFIG_HARVESTER_URL) == null) {
      System.setProperty(CONFIG_HARVESTER_URL,
          properties.get(SYMPHONY_URL).toString());
    }
  }

  private void prepareAuthentication(Properties properties)
      throws SymphonyInputException, SymphonyNativeException, IOException,
      SymphonyEncryptionException {

    initializeSymphonyClient(properties);

    initializeAuthProvider(properties);
  }

  private void initializeAuthProvider(Properties properties)
      throws SymphonyNativeException, SymphonyInputException, SymphonyEncryptionException {

    authProvider = new AuthProvider(new InMemoryPersister());
    authProvider.setAuthKeyManager(false);
    authProvider.setSymphonyChallengePath(AuthProvider.STD_SYMPHONY_KEY_CHALLENGE);
    authProvider.setSymphonyResponsePath(AuthProvider.STD_SYMPHONY_KEY_RESPONSE);

    authProvider.generateAuth(symphonyClient);
    authProvider.getSymphonyAuth().setAccountName(properties.getProperty(ACCOUNT));
    authProvider.getSymphonyAuth().storeSecretKey((String) properties.get(SECRET));
  }

  private void initializeSymphonyClient(Properties properties) throws IOException {
    SymphonyClientConfig symphonyClientConfig = new SymphonyClientConfig();
    symphonyClientConfig.setAcountName((String) properties.get(ACCOUNT));
    symphonyClientConfig.setSymphonyUrl((String) properties.get(SYMPHONY_URL));
    symphonyClientConfig.setLoginUrl(properties.get(SYMPHONY_URL) + "/login");
    symphonyClientConfig.setReadTimeout(READ_TIME_OUT);
    symphonyClientConfig.setConnectTimeout(CONNECTION_TIME_OUT);

    symphonyClient = SymphonyClientFactory.getClient(
        new ClientIdentifierFilter("1.0.0", "IntegrationBridgeLogger"), symphonyClientConfig);
  }

  private Properties getProperties() throws ConfigurationException {
    IBProperties prop = new IBProperties();
    return prop.getProperties();
  }

  /**
   * Returns the session key to log into the cloud.
   * It's going to verify if the session was created, if not will try to authenticate.
   * @return Session token
   */
  @Override
  public String getSessionKey() {
    String sReturn = null;
    try {
      sReturn = authProvider.getSymphonyAuth().getSession();

      if (sReturn == null) {
        //the following is for lazy authentication in a separate thread while we are waiting for
        // the pod to come up.
        //Until we can return a valid session key value, the cloud logger will keep log messages
        //in an internal queue. As soon as authentication is complete and session key can be
        //returned, the messages will be pushed to the cloud.

        //This method may be called by multiple threads. Don't start authentication multiple times -
        //only if we haven't run this or if it has already finished then fire a new one
        synchronized (IntegrationBridgeCloudLoggerFactory.class) {
          if ((authenticationFuture == null) ||
              ((authenticationFuture != null) && (authenticationFuture.isDone()))) {

            authenticationFuture = executorForAuthentication.submit(new Runnable() {
              @Override
              public void run() {
                try {
                  //wait a minute before trying - otherwise authentication code
                  //logs too many exceptions
                  Thread.sleep(60000);
                  reAuth();
                } catch (Exception e) {
                  //there will be multiple exceptions during pod start up. They do not indicate real
                  //problems - simply not all components are up and running yet. Hence the log level
                  //"info" and only exception message, not the full stacktrace
                }
              }
            });
          }
        }
      }
    } catch (Exception e) {
      sReturn = "";
    }
    //As of the time of this change: onpremhttpsclient catches only IOException when calling this
    //method and does not check for NULLs. Therefore, replace null with empty string.
    if (sReturn == null) {
      sReturn = "";
    }

    return sReturn;
  }

  @Override
  public String getSessionName() {
    return authProvider.getSymphonyAuth().getSessionName();
  }

  /**
   * Tries to authenticate to the cloud
   */
  @Override
  public void reAuth() {
    authProvider.getSymphonyAuth().authIfNeeded(symphonyClient);
  }

  @Override
  public String getIdentifier() {
    return IB_LOGGER_NAME;
  }
}
