package org.symphonyoss.integration.provisioning.model;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by rsanchez on 18/10/16.
 */
@Configuration
@ConfigurationProperties()
public class ApplicationList {

  private final List<Application> applications = new ArrayList<>();

  public List<Application> getApplications() {
    return applications;
  }
}
