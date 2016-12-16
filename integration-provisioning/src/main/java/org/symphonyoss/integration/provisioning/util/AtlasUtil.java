package org.symphonyoss.integration.provisioning.util;

import com.symphony.atlas.AtlasException;
import com.symphony.atlas.IAtlas;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.symphonyoss.integration.IntegrationAtlas;

import java.io.File;

import javax.annotation.PostConstruct;

/**
 * Created by rsanchez on 25/10/16.
 */
@Component
public class AtlasUtil {

  private static final String CERTS_DIR = "certs";

  private static final String KEYS_DIR = "keys";

  @Autowired
  private IntegrationAtlas integrationAtlas;

  private IAtlas atlas;

  @PostConstruct
  public void init() {
    this.atlas = integrationAtlas.getAtlas();
  }

  public String getCertsDirectory() throws AtlasException {
    return atlas.getConfigDir(CERTS_DIR).getAbsolutePath() + File.separator;
  }

  public String getCertKeysDirectory() throws AtlasException {
    return getCertsDirectory() + KEYS_DIR + File.separator;
  }

  public String getPropertiesFile(String file) throws AtlasException {
    return atlas.getConfigFile(file).getAbsolutePath();
  }
}
