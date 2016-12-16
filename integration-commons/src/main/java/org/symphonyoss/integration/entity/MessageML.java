package org.symphonyoss.integration.entity;

import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;

/**
 * A container for an <messageML> element to be inserted on an messageML document.
 * Created by cmarcondes on 11/2/16.
 */
@XmlRootElement
public class MessageML {

  @XmlElement(name = "entity")
  private Entity entity;

  public Entity getEntity() {
    return entity;
  }
}
