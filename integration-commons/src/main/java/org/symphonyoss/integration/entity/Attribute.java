package org.symphonyoss.integration.entity;

import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlRootElement;

/**
 * A container for an <attribute> element to be inserted on an entityML document created with
 * @see EntityBuilder.
 *
 * Created by cmarcondes on 8/23/16.
 */
@XmlRootElement
public class Attribute {
  // The value for the attribute's "name" XML attribute
  @XmlAttribute
  private String name;

  // The value for the attribute's "type" XML attribute
  @XmlAttribute
  private String type;

  // The value for the attribute's "value" XML attribute
  @XmlAttribute
  private String value;

  /**
   * A default constructor required for JAXB serialization.
   */
  public Attribute() {
  }

  /**
   * Instantiates an attribute with given name, type and value. Attributes can be serialized into
   * an XML element with @see {@link EntityBuilder}. For instance, the following call:
   *
   * new Attribute("author", "com.symphonyss.string", "John Smith")
   *
   * Results in the following XML element when serialized.
   *
   * <attribute name="author" type="com.symphonyss.string" value="John Smith"/>
   * @param name The value for name attribute.
   * @param type The value for type attribute.
   * @param value The value for value attribute.
   */
  public Attribute(String name, String type, String value) {
    this.name = name;
    this.type = type;
    this.value = value;
  }

  public String getName() {
    return name;
  }

  public String getValue() {
    return value;
  }
}
