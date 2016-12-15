package org.symphonyoss.integration.healthcheck.verifier;

import static javax.ws.rs.core.Response.Status.Family.REDIRECTION;
import static javax.ws.rs.core.Response.Status.Family.SUCCESSFUL;

import com.symphony.logging.ISymphonyLogger;
import com.symphony.logging.SymphonyLoggerFactory;

import org.apache.commons.lang3.StringUtils;
import org.glassfish.jersey.client.ClientProperties;
import org.springframework.beans.factory.annotation.Autowired;
import org.symphonyoss.integration.IntegrationPropertiesReader;
import org.symphonyoss.integration.authentication.AuthenticationProxy;
import org.symphonyoss.integration.authentication.exception.UnregisteredUserAuthException;
import org.symphonyoss.integration.model.Application;
import org.symphonyoss.integration.model.ApplicationState;

import javax.ws.rs.ProcessingException;
import javax.ws.rs.client.Client;
import javax.ws.rs.client.Invocation;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.Response.Status;

/**
 * Holds common methods to all integration bridge verifiers.
 *
 * Created by Milton Quilzini on 11/11/16.
 */
public abstract class AbstractConnectivityVerifier {

  private static final ISymphonyLogger LOG =
      SymphonyLoggerFactory.getLogger(AbstractConnectivityVerifier.class);


  /**
   * Possible values for the connectivity status.
   */
  public enum ConnectivityStatus {
    /**
     * Indicates a given component can be reached, i.e. connectivity is up.
     */
    UP,

    /**
     * Indicates a given component can not be reached, i.e. connectivity is down.
     */
    DOWN
  }


  private static final int CONNECT_TIMEOUT_MILLIS = 1000;

  private static final int READ_TIMEOUT_MILLIS = 5000;

  /**
   * Default http protocol to be used for connectivity check calls.
   */
  protected static final String DEFAULT_PROTOCOL = "https://";

  @Autowired
  protected IntegrationPropertiesReader propertiesReader;

  @Autowired
  private AuthenticationProxy authenticationProxy;

  /**
   * Determine the user to be used on connectivity checks, looking on the base YAML file for a
   * provisioned one.
   * @return the user name.
   */
  protected String availableIntegrationUser() {
    for (Application app : this.propertiesReader.getProperties().getApplications()) {
      if (app.getState().equals(ApplicationState.PROVISIONED)) {
        return app.getType();
      }
    }
    return StringUtils.EMPTY;
  }

  /**
   * Build the specific health check URL for the component which connectivity will be checked for.
   * @return the built service URL.
   */
  protected abstract String getHealthCheckUrl();

  /**
   * Hits the built URL to the corresponding service, checks its response
   * {@link Status.Family}, and returns the corresponding connectivity status.
   * @return Connectivity status: "UP" if the check is successful, "DOWN" otherwise.
   */
  public ConnectivityStatus currentConnectivityStatus() {
    try {
      Client client = authenticationProxy.httpClientForUser(availableIntegrationUser());

      Invocation.Builder invocationBuilder = client.target(getHealthCheckUrl())
          .property(ClientProperties.CONNECT_TIMEOUT, CONNECT_TIMEOUT_MILLIS)
          .property(ClientProperties.READ_TIMEOUT, READ_TIMEOUT_MILLIS)
          .request()
          .accept(MediaType.APPLICATION_JSON_TYPE);

      Response response = invocationBuilder.get();
      Status.Family statusFamily = response.getStatusInfo().getFamily();

      return statusFamily.equals(SUCCESSFUL) || statusFamily.equals(REDIRECTION) ?
          ConnectivityStatus.UP : ConnectivityStatus.DOWN;

    } catch (ProcessingException | UnregisteredUserAuthException e) {
      LOG.error("Trying to reach {} but getting exception: {}", getHealthCheckUrl(), e.getMessage
          (), e);
      return ConnectivityStatus.DOWN;
    }
  }
}
