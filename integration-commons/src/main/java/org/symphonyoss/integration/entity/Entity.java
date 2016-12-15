package org.symphonyoss.integration.entity;

import org.apache.commons.lang3.StringUtils;

import java.util.ArrayList;
import java.util.List;

import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;

/**
 * A container for an <entity> element to be inserted on an entityML document created with
 * {@link EntityBuilder}.
 *
 * Created by cmarcondes on 8/23/16.
 */
@XmlRootElement
public class Entity {

  //Tag PresentationML that will contains the presentation formatted
  @XmlElement(name = "presentationML")
  private String presentationML;

  // The nested <attribute> elements that will be serialized with this Entity.
  @XmlElement(name = "attribute")
  private List<Attribute> attributes;

  // The nested Entities that will be serialized with this Entity.
  @XmlElement(name = "entity")
  private List<Entity> entities;

  // The name for this entity.
  @XmlAttribute
  private String name;

  // The prefix for the type attribute that will be serialized by this entity.
  @XmlAttribute
  private String type;

  // The version attribute that will be serialized by this entity.
  @XmlAttribute
  private String version = "1.0";

  /**
   * A default constructor required for JAXB serialization.
   */
  public Entity() {
  }

  /**
   * Constructor to creates an instance of Entity object
   * @param type - Type of entity to be generated
   */
  protected Entity(String type) {
    this.type = type;
    this.attributes = new ArrayList<Attribute>();
    this.entities = new ArrayList<Entity>();
  }

  /**
   * Constructor to creates an instance of Entity object
   * @param type - Type of entity to be generated
   * @param name - Name of entity to be generated
   */
  protected Entity(String name, String type) {
    this(type);
    this.name = name;
  }

  /**
   * Adds an Attribute to this entity.
   * @param attribute The attribute to be added.
   */
  protected void addAttribute(Attribute attribute) {
    this.attributes.add(attribute);
  }

  /**
   * Adds a nested entity to this entity.
   * @param entity The entity to be added.
   */
  protected void addEntity(Entity entity) {
    if (entities == null) {
      entities = new ArrayList<Entity>();
    }
    entities.add(entity);
  }

  protected void setPresentationML(String presentationML) {
    this.presentationML = presentationML;
  }

  public String getType() {
    return type;
  }

  public List<Attribute> getAttributes() {
    return attributes;
  }

  public List<Entity> getEntities() {
    return entities;
  }

  public String getName() {
    return name;
  }

  /**
   * Returns first Entity that matches the given type.
   * @param type value to find an specific entity; this is a substring,
   * so any type that contains this substring will be returned
   * @return Entity or null
   */
  public Entity getEntityByType(String type) {
    if (StringUtils.isEmpty(type) || this.getEntities() == null) {
      return null;
    }

    for (Entity entity : this.getEntities()) {
      if (entity.getType().contains(type)) {
        return entity;
      }
    }

    return null;
  }

  /**
   * Returns first Entity that matched with the name.
   * @param name value to find an specific entity
   * @return Entity or null
   */
  public Entity getEntityByName(String name) {
    if (StringUtils.isEmpty(name) || this.getEntities() == null) {
      return null;
    }

    for (Entity entity : this.getEntities()) {
      if (name.equals(entity.getName())) {
        return entity;
      }
    }

    return null;
  }

  /**
   * Returns the value of an attribute found by name
   * @param name Name of the attribute that you want to find
   * @return String with the value or null
   */
  public String getAttributeValue(String name) {
    if (StringUtils.isEmpty(name) || this.getAttributes() == null) {
      return null;
    }

    for (Attribute attr : this.getAttributes()) {
      if (name.equals(attr.getName())) {
        return attr.getValue();
      }
    }

    return null;
  }

  public String getPresentationML() {
    return presentationML;
  }

}
