package org.symphonyoss.integration.healthcheck;

import static org.symphonyoss.integration.model.healthcheck.IntegrationFlags.ValueEnum.NOK;
import static org.symphonyoss.integration.model.healthcheck.IntegrationFlags.ValueEnum.OK;

import org.symphonyoss.integration.IntegrationStatus;
import org.symphonyoss.integration.model.healthcheck.IntegrationFlags;
import org.symphonyoss.integration.model.healthcheck.IntegrationHealth;

import java.text.SimpleDateFormat;
import java.util.Arrays;
import java.util.Date;
import java.util.List;

/**
 * Maintains the state of an individual Integration's health ({@link IntegrationHealth}), such as Jira, Github, etc.
 * Usually part of the basic attributes owned by any Integration on Integration Bridge.
 * Created by rsanchez on 03/08/16.
 */
public class IntegrationHealthManager {

  private static final String SUCCESS = "Success";

  private static final String TIMESTAMP_FORMAT = "yyyy-MM-dd'T'HH:mm:ss'Z'Z";

  private IntegrationHealth health = new IntegrationHealth();

  private IntegrationFlags flags = new IntegrationFlags();

  public IntegrationHealthManager() {
    this.flags.setParserInstalled(OK);
    this.flags.setConfiguratorInstalled(NOK);
    this.flags.setCertificateInstalled(NOK);
    this.flags.setUserAuthenticated(NOK);

    this.health.setStatus(IntegrationStatus.INACTIVE.name());
    this.health.setFlags(flags);
  }

  public void setName(String name) {
    this.health.setName(name);
  }

  public IntegrationHealth getHealth() {
    return health;
  }

  public void success() {
    List<String> expectedStatus = Arrays.asList(IntegrationStatus.INACTIVE.name(),
        IntegrationStatus.RETRYING_BOOTSTRAP.name());

    if (expectedStatus.contains(health.getStatus())) {
      this.health.setStatus(IntegrationStatus.ACTIVE.name());
      this.health.setMessage(SUCCESS);
    }
  }

  public void retry(String message) {
    List<String> expectedStatus = Arrays.asList(IntegrationStatus.INACTIVE.name(),
        IntegrationStatus.RETRYING_BOOTSTRAP.name());

    if (expectedStatus.contains(health.getStatus())) {
      this.health.setStatus(IntegrationStatus.RETRYING_BOOTSTRAP.name());
      this.health.setMessage(message);
    }
  }

  public void failBootstrap(String message) {
    if (IntegrationStatus.INACTIVE.name().equals(health.getStatus())) {
      this.health.setStatus(IntegrationStatus.FAILED_BOOTSTRAP.name());
      this.health.setMessage(message);
    }
  }

  public void updateLatestPostTimestamp(Long timestamp) {
    SimpleDateFormat formatter = new SimpleDateFormat(TIMESTAMP_FORMAT);
    Date date = new Date(timestamp);
    this.health.setLatestPostTimestamp(formatter.format(date));
  }

  public void parserInstalled(IntegrationFlags.ValueEnum value) {
    this.flags.setParserInstalled(value);
  }

  public void configuratorInstalled(IntegrationFlags.ValueEnum value) {
    this.flags.setConfiguratorInstalled(value);
  }

  public void certificateInstalled(IntegrationFlags.ValueEnum value) {
    this.flags.setCertificateInstalled(value);
  }

  public void userAuthenticated(IntegrationFlags.ValueEnum value) {
    this.flags.setUserAuthenticated(value);
  }

}
