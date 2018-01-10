package org.symphonyoss.integration.authentication.exception;

import org.symphonyoss.integration.authentication.api.enums.ServiceName;

/**
 * Created by hamitay on 10/01/18.
 */
public class MissingClientException extends RuntimeException {

  public MissingClientException(ServiceName serviceName) {
    super(String.format("Missing %s client configuration", serviceName.toString()));
  }
}
