package org.symphonyoss.integration.exception;

/**
 * Created by cmarcondes on 9/8/16.
 */
public class EntityXMLGeneratorException extends IntegrationException {

  public EntityXMLGeneratorException(Exception e) {
    super("Commons", e.getMessage(), e);
  }
}
