package org.symphonyoss.integration.web.model;

import static com.fasterxml.jackson.annotation.JsonInclude.Include.NON_NULL;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonUnwrapped;

/**
 * Represents an error response returned by Integration Bridge API
 *
 * Created by rsanchez on 27/07/17.
 */
@JsonInclude(NON_NULL)
public class ErrorResponse {

  private int status;

  private String message;

  private Object properties;

  public ErrorResponse() {}

  public ErrorResponse(int status, String message) {
    this.status = status;
    this.message = message;
  }

  public int getStatus() {
    return status;
  }

  public void setStatus(int status) {
    this.status = status;
  }

  public String getMessage() {
    return message;
  }

  public void setMessage(String message) {
    this.message = message;
  }

  @JsonUnwrapped
  public Object getProperties() {
    return properties;
  }

  public void setProperties(Object properties) {
    this.properties = properties;
  }

  @Override
  public boolean equals(Object o) {
    if (this == o) { return true; }
    if (o == null || getClass() != o.getClass()) { return false; }

    ErrorResponse that = (ErrorResponse) o;

    if (status != that.status) { return false; }
    if (message != null ? !message.equals(that.message) : that.message != null) { return false; }
    return properties != null ? properties.equals(that.properties) : that.properties == null;
  }

  @Override
  public int hashCode() {
    int result = status;
    result = 31 * result + (message != null ? message.hashCode() : 0);
    result = 31 * result + (properties != null ? properties.hashCode() : 0);
    return result;
  }
}
