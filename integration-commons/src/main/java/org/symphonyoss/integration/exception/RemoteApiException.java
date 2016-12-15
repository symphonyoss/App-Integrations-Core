package org.symphonyoss.integration.exception;

/**
 * Created by rsanchez on 09/08/16.
 */
public class RemoteApiException extends IntegrationException {

  private int code;

  public RemoteApiException(int code, Exception e) {
    super("Commons", e.getMessage(), e);
    this.code = code;
  }

  public int getCode() {
    return code;
  }

}
