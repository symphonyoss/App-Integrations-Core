package org.symphonyoss.integration.provisioning.client.model;

import org.symphonyoss.integration.provisioning.client.AppRepositoryClient;

/**
 * Wrap the data into an envelope.
 *
 * This class is used to the {@link AppRepositoryClient} because all requests performed to the
 * appstore requires the body payload to be wrapped into a 'data' object.
 *
 * Here's an example:
 *
 * <code>
 *   {
 *    "data": {
 *      "uiLoadType": "sandbox",
 *      "type": "sandbox",
 *      "version": "1.0.0",
 *      "domain": ".symphony.com",
 *      "id": "58c14beae4b0cb96fc983819",
 *      "enabled": false,
 *      "publisher": "Custom",
 *      "appGroupId": "794b0084b1ee4c7592ee660d9dab1d0e",
 *      "symphonyManaged": false,
 *      "publishedDate": 1489062890596,
 *      "assets": {
 *        "loadUrl": "https://test.symphony.com/custom"
 *      },
 *      "name": "New custom app"
 *    }
 *   }
 * </code>
 *
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
