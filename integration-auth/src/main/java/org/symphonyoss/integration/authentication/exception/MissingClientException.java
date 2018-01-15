package org.symphonyoss.integration.authentication.exception;

import org.symphonyoss.integration.authentication.api.enums.ServiceName;
import org.symphonyoss.integration.exception.IntegrationRuntimeException;

/**
 * Created by hamitay on 10/01/18.
 */
public class MissingClientException extends IntegrationRuntimeException {

  public MissingClientException(String component, ServiceName serviceName) {
    super(component, String.format("Missing %s client configuration", serviceName.toString()));
  }
}
