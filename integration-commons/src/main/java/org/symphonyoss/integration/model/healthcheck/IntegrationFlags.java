package org.symphonyoss.integration.model.healthcheck;

import com.fasterxml.jackson.annotation.JsonProperty;


/**
 * Part of an {@link IntegrationHealth}, it holds flags to indicate whether the specified integration is:
 * 1. Successfully provisioned.
 * 2. Its configurator is properly installed.
 * 3. Its certificate is properly installed.
 * 4. Its user is authenticated with Symphony.
 *
 * Created by Milton Quilzini on 01/12/16.
 */
public class IntegrationFlags {

  private ValueEnum parserInstalled;
  private ValueEnum configuratorInstalled;
  private ValueEnum certificateInstalled;
  private ValueEnum userAuthenticated;

  @JsonProperty("parser_installed")
  public ValueEnum getParserInstalled() {
    return parserInstalled;
  }

  public void setParserInstalled(ValueEnum parserInstalled) {
    this.parserInstalled = parserInstalled;
  }

  @JsonProperty("configurator_installed")
  public ValueEnum getConfiguratorInstalled() {
    return configuratorInstalled;
  }

  public void setConfiguratorInstalled(ValueEnum configuratorInstalled) {
    this.configuratorInstalled = configuratorInstalled;
  }

  @JsonProperty("certificate_installed")
  public ValueEnum getCertificateInstalled() {
    return certificateInstalled;
  }

  public void setCertificateInstalled(ValueEnum certificateInstalled) {
    this.certificateInstalled = certificateInstalled;
  }

  @JsonProperty("user_authenticated")
  public ValueEnum getUserAuthenticated() {
    return userAuthenticated;
  }

  public void setUserAuthenticated(ValueEnum userAuthenticated) {
    this.userAuthenticated = userAuthenticated;
  }

  public enum ValueEnum {
    OK,
    NOK;
  }
}
