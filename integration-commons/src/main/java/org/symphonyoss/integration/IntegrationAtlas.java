package org.symphonyoss.integration;

import com.symphony.atlas.AtlasException;
import com.symphony.atlas.IAtlas;
import com.symphony.atlas.PropertyFileAtlas;
import com.symphony.atlas.config.SymphonyAtlas;
import com.symphony.logging.ISymphonyLogger;
import com.symphony.logging.SymphonyLoggerFactory;

import org.springframework.context.annotation.Lazy;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;

import javax.annotation.PostConstruct;

/**
 * Holds a common instance for {@link IAtlas} to use on the bootstrapping process.
 *
 * Created by Milton Quilzini on 03/06/16.
 */
@Component
@Lazy
public class IntegrationAtlas {

  private static final ISymphonyLogger LOGGER =
      SymphonyLoggerFactory.getLogger(IntegrationAtlas.class);

  private IAtlas atlas;

  @PostConstruct
  public void init() {
    try {
      this.atlas = new PropertyFileAtlas(SymphonyAtlas.SYMPHONY_APP,
          SymphonyAtlas.INTEGRATION_PROPERTIES);
      LOGGER.info("Integration Bridge configured from Atlas.");
    } catch (AtlasException e) {
      throw new IntegrationAtlasException("Fail to initialize ATLAS", e);
    }
  }

  public IAtlas getAtlas() {
    return atlas;
  }

  public String getRequiredUrl(String key) {
    String url = atlas.get(key);
    if (StringUtils.isEmpty(url)) {
      throw new IntegrationAtlasException("No URL configured for " + key);
    }
    return url;
  }
}
