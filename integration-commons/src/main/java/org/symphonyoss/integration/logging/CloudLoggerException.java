package org.symphonyoss.integration.logging;

import org.symphonyoss.integration.exception.IntegrationRuntimeException;

import java.util.Arrays;

/**
 * RuntimeException created when trying to connect to the Symphony cloud log.
 *
 * Created by cmarcondes on 11/25/16.
 */
public class CloudLoggerException extends IntegrationRuntimeException {

  public static final String COMPONENT = "IB Cloud Logger";

  private static final String INTRODUTION = "In order to fix this problem, make sure the proper configurations are in place.";

  private static final String TOMCAT_CONFIGS = "The Integration Bridge Tomcat instance must be configured with the following "
          + "variable:\n"
          + "'-Djava.library.path': The path to the cryptography libraries. It must be set to "
          + "\"<INTEGRATION_BRIDGE_HOME>/webapps/integration/WEB-INF/lib/cryptolibs/\".\n"
          + "Check if this property is set at your tomcat. Go to "
          + "\"<INTEGRATION_BRIDGE_HOME>/bin/\" and open the Integration Bridge file, e.g. 'vim "
          + "startup.sh'. Search for 'JAVA_OPTS' variable and check if the property exist.\n"
          + "If the variable is not configured, add it to the JAVA_OPTS as indicated above.\n"
          + "If the variable is configured, make sure the path is correct, the file system "
          + "really contains files on that path and Tomcat can access such files (check the group"
          + " and owner for the files).";

  private static final String ATLAS_CONFIGS = "The Integration Bridge Data must be configured with the following variables:\n"
      + "- 'account': User name provided used to connect into the Symphony Cloud Logger.\n"
      + "- 'secret': Account password used to connect into the Symphony Cloud Logger.\n"
      + "Check if those files are configured at the file located at:\n"
      + "<INTEGRATION_BRIDGE_DATA_HOME>/symphony/atlas/symphony/env/<NAME>/integration.properties"
      + "If those variables are not configured, you should get those information with Symphony.\n"
      + "If those variables are configured, make sure the values were set correctly.";


  /**
   * Constructs a new CloudLoggerException with the specified detail message,
   * a list of possible solutions and the root cause of the problem.
   * @param message The message why the exceptions happened.
   * @param cause The root cause of the error.
   */
  public CloudLoggerException(String message, Throwable cause) {
    super(COMPONENT, message, Arrays.asList(INTRODUTION, TOMCAT_CONFIGS, ATLAS_CONFIGS), cause);
  }
}
