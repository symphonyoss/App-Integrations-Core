package org.symphonyoss.integration.authentication.properties;

import org.symphonyoss.integration.authentication.AuthenticationProxyImpl;

/**
 * Exception message keys used by the component {@link AuthenticationProxyImpl}
 * Created by crepache on 26/06/17.
 */
public class AuthenticationProxyProperties {

  public static final String UNREGISTERED_USER = "auth.user.unregistered";

  public static final String UNAUTHORIZED_USER = "auth.user.unauthorized";

  public static final String UNREGISTERED_SESSION_TOKEN = "auth.session.token.unregistered";

  public static final String UNAUTHORIZED_SESSION_TOKEN = "auth.session.token.unauthorized";

  public static final String FORBIDDEN_SESSION_TOKEN = "auth.session.token.forbidden";

  public static final String UNEXPECTED_SESSION_TOKEN = "auth.session.token.unexpected";

}
