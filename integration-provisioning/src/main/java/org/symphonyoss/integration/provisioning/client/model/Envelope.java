package org.symphonyoss.integration.provisioning.client.model;

/**
 * Wrap the data into an envelope.
 * Created by rsanchez on 07/03/17.
 */
public class Envelope<T> {

  private T data;

  public Envelope() {}

  public Envelope(T data) {
    this.data = data;
  }

  public void setData(T data) {
    this.data = data;
  }

  public T getData() {
    return this.data;
  }

  @Override
  public String toString() {
    return "RequestEnvelope{" +
        "data=" + data +
        '}';
  }

}
