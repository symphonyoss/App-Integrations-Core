package org.symphonyoss.integration.core;

import com.symphony.atlas.AbstractAtlas;
import com.symphony.atlas.AtlasException;

import java.io.File;

/**
 * Mock to represent a Atlas object.
 * Created by rsanchez on 22/11/16.
 */
public class MockAtlas extends AbstractAtlas {

  @Override
  public File getConfigDir(String name) throws AtlasException {
    throw new AtlasException("getConfigDir not supported in this implementation");
  }

  @Override
  public File getConfigFile(String name) throws AtlasException {
    throw new AtlasException("getConfigDir not supported in this implementation");
  }
}
