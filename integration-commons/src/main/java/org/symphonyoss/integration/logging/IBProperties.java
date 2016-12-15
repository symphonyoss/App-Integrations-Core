package org.symphonyoss.integration.logging;

import static com.symphony.atlas.config.SymphonyAtlas.ACCOUNT;
import static com.symphony.atlas.config.SymphonyAtlas.SECRET;
import static com.symphony.atlas.config.SymphonyAtlas.SYMPHONY_URL;

import com.symphony.atlas.IAtlas;
import com.symphony.config.ConfigurationException;

import org.symphonyoss.integration.IntegrationAtlas;

import java.util.Properties;

/**
 * Class responsible to get all properties to be able to connect in Symphony cloud
 *
 * Created by cmarcondes on 11/17/16.
 */
public class IBProperties {

  private IntegrationAtlas integrationAtlas;

  public IBProperties() {
    integrationAtlas = new IntegrationAtlas();
    integrationAtlas.init();
  }

  /**
   * Gets the properties from atlas file
   * @return Properties to connect into the cloud
   * @throws ConfigurationException - if couldn't get the properties
   */
  public Properties getProperties() {
    Properties properties = new Properties();

    IAtlas atlas = integrationAtlas.getAtlas();
    properties.put(SECRET, atlas.get(SECRET));
    properties.put(ACCOUNT, atlas.get(ACCOUNT));
    properties.put(SYMPHONY_URL, atlas.get(SYMPHONY_URL));

    return properties;
  }
}
