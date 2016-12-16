package org.symphonyoss.integration.provisioning.properties;

/**
 * A class that holds a catalog of properties used for setting up the authentication of the
 * provisioning user.
 *
 * Created by rsanchez on 17/10/16.
 */
public final class AuthenticationProperties {

  /**
   * Provisioning user id. Used internally in the Provisioning App to identify the user that will
   * authenticate on the Authentication Proxy. This ID is not the actual backend user id, as the
   * backend user id is defined by the certificate given as input to the provisioning tool for the
   * "provisioning" user.
   */
  public static final String DEFAULT_USER_ID = "admin";

  /**
   * The key of the configuration property that contains the truststore file.
   */
  public static final String TRUST_STORE = "integration_bridge.truststore_file";

  /**
   * The key of the configuration property that contains the truststore type.
   */
  public static final String TRUST_STORE_TYPE = "integration_bridge.truststore_type";

  /**
   * The key of the configuration property that contains the Truststore password.
   */
  public static final String TRUST_STORE_PASSWORD = "truststorePassword";

  /**
   * The key of the configuration property that contains the Keystore file.
   */
  public static final String KEY_STORE = "admin_user.keystore_file";

  /**
   * The key of the configuration property that contains the Keystore password.
   */
  public static final String KEY_STORE_PASSWORD = "keystorePassword";

  /**
   * A private constructor to avoid class instantiation.
   */
  private AuthenticationProperties() {}

}
