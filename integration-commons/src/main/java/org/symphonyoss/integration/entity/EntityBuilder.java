package org.symphonyoss.integration.entity;

import static org.apache.commons.lang3.StringUtils.isAnyBlank;
import static org.apache.commons.lang3.StringUtils.isBlank;
import static org.apache.commons.lang3.StringUtils.isEmpty;
import static org.symphonyoss.integration.parser.ParserUtils.MESSAGEML_LINEBREAK;

import org.apache.commons.lang3.StringEscapeUtils;
import org.symphonyoss.integration.exception.EntityXMLGeneratorException;
import org.symphonyoss.integration.parser.ParserUtils;
import org.symphonyoss.integration.parser.SafeString;
import org.symphonyoss.integration.parser.SafeStringUtils;

import java.io.StringWriter;
import java.net.URI;

import javax.xml.bind.JAXBContext;
import javax.xml.bind.Marshaller;

/**
 * A builder for "entity" XML elements to be used within an entityML document.
 *
 * This sample call:
 * <pre>
 *  {@code
 *    Entity user = EntityBuilder.forNestedEntity("integration", "event")
 *      .attribute("username"     , "xxx")
 *      .attribute("emailAddress" , "xxx@mycompany.com")
 *      .attribute("displayName"  , "Xxxx Xxxx").build();
 *
 *    String xml = EntityBuilder.forIntegrationEvent("integration", "issue")
 *      .nestedEntity(user).generateXML();
 * }
 * Returns a XML document as:
 * <pre>
 *  {@code
 *    <entity type="com.symphony.integration.integration.event.issue" version="1.0">
 *      <entity type="com.symphony.integration.integration.event" version="1.0">
 *        <attribute name="username" type="org.symphonyoss.string" value="xxx"/>
 *        <attribute name="emailAddress" type="org.symphonyoss.string" value="xxx@mycompany.com"/>
 *        <attribute name="displayName" type="org.symphonyoss.string" value="Xxxx Xxxx"/>
 *      </entity>
 *    </entity>
 * }
 *
 * Created by cmarcondes on 9/8/16.
 */
public class EntityBuilder {

  /**
   * Symphony type to be used when creating a new integration entity
   */
  private final static String TYPE_INTEGRATION_EVENT = "com.symphony.integration.%s.event.%s";
  /**
   * Symphony type to be used when creating a new nested entity for events
   */
  private final static String TYPE_INTEGRATION_ENTITY = "com.symphony.integration.%s.%s";

  private Entity entity;

  /**
   * Constructor that will create a new entity for the type
   * @param type type of this entity.
   */
  private EntityBuilder(String type) {
    this.entity = new Entity(type);
  }

  /**
   * Constructor that will create a new entity for the name and type passed.
   * @param name name of this entity.
   * @param type type of this entity.
   */
  private EntityBuilder(String name, String type) {
    this.entity = new Entity(name, type);
  }

  private EntityBuilder(Entity entity) {
    this.entity = entity;
  }

  /**
   * Creates a new EntityBuilder for an integration event
   * @param integration the name of the integration that you are building
   * @param event - event type that you are handling
   * @return EntityBuilder
   */
  public static EntityBuilder forIntegrationEvent(String integration, String event) {
    if (isEmpty(integration) || isEmpty(event)) {
      throw new RuntimeException(
          "To create an integration entity you have to inform a valid integration and event");
    }
    return new EntityBuilder(String.format(TYPE_INTEGRATION_EVENT, integration, event));
  }

  /**
   * Creates a new EntityBuilder for a nested entity
   * @param integration -  the name of the integration that you are building
   * @param type - entity type that you are handling
   * @return EntityBuilder
   */
  public static EntityBuilder forNestedEntity(String integration, String type) {
    if (isEmpty(integration) || isEmpty(type)) {
      throw new RuntimeException(
          "To create a nested entity you have to inform a valid integration and type");
    }
    return new EntityBuilder(String.format(TYPE_INTEGRATION_ENTITY, integration, type));
  }

  /**
   * Creates a new EntityBuilder for a nested entity
   * @param type Entity type that's going to be created
   * @return EntityBuilder
   */
  public static EntityBuilder forNestedEntity(String type) {
    if (isEmpty(type)) {
      throw new RuntimeException(
          "To create a nested entity you have to inform a valid type");
    }
    return new EntityBuilder(type);
  }

  /**
   * Creates a new EntityBuilder for a nested entity
   * @param entity Entity to create the builder
   * @return EntityBuilder
   */
  public static EntityBuilder forEntity(Entity entity) {
    if (entity == null) {
      throw new RuntimeException("The Entity Object informed is null.");
    }

    return new EntityBuilder(entity);
  }

  /**
   * Creates a new EntityBuilder for a nested entity
   * @param integration -  the name of the integration that you are building
   * @param type - entity type that you are handling
   * @param type - entity name that you are handling
   * @return EntityBuilder
   */
  public static EntityBuilder forNestedEntity(String integration, String name, String type) {
    if (isEmpty(integration) || isEmpty(type)) {
      throw new RuntimeException(
          "To create a nested entity you have to inform a valid integration and type");
    }
    return new EntityBuilder(name, String.format(TYPE_INTEGRATION_ENTITY, integration, type));
  }

  /**
   * Creates and Adds an Attribute for String type
   * @param name - tag name of your attribute
   * @param value - tag value of your attribute
   * @return <attribute name="name" type="org.symphonyoss.string" value="value"/>
   */
  public EntityBuilder attribute(String name, String value) {
    String formatted = StringEscapeUtils.escapeXml10(value).replaceAll("\n", MESSAGEML_LINEBREAK);
    formatted = ParserUtils.markupLinks(formatted);
    Attribute attr = new Attribute(name, "org.symphonyoss.string", formatted);
    this.entity.addAttribute(attr);
    return this;
  }

  /**
   * Creates and Adds an Attribute for String type
   * @param name - tag name of your attribute
   * @param value - tag value of your attribute
   * @return <attribute name="name" type="org.symphonyoss.string" value="value"/>
   */
  public EntityBuilder attribute(String name, SafeString value) {
    String formatted = ParserUtils.markupLinks(value.toString());
    Attribute attr = new Attribute(name, "org.symphonyoss.string", formatted);
    this.entity.addAttribute(attr);
    return this;
  }

  /**
   * Creates and Adds an Attribute for String type, if the provided string is not empty
   * @param name - tag name of your attribute
   * @param value - tag value of your attribute
   * @return <attribute name="name" type="org.symphonyoss.string" value="value"/>
   */
  public EntityBuilder attributeIfNotEmpty(String name, String value) {
    if (!isAnyBlank(name, value)) {
      attribute(name, value);
    }
    return this;
  }

  /**
   * Creates and Adds an Attribute for String type, if the provided string is not empty
   * @param name - tag name of your attribute
   * @param value - tag value of your attribute
   * @return <attribute name="name" type="org.symphonyoss.string" value="value"/>
   */
  public EntityBuilder attributeIfNotEmpty(String name, SafeString value) {
    if (!isBlank(name) && !SafeStringUtils.isBlank(value)) {
      attribute(name, value);
    }
    return this;
  }

  /**
   * Creates and Adds an Attribute for Integer type
   * @param name - tag name of your attribute
   * @param value - tag value of your attribute
   * @return <attribute name="name" type="org.symphony.oss.number.int" value="value"/>
   */
  public EntityBuilder attribute(String name, Integer value) {
    Attribute attr = new Attribute(name, "org.symphony.oss.number.int", value.toString());
    this.entity.addAttribute(attr);
    return this;
  }

  /**
   * Creates and Adds an Attribute for Long type
   * @param name - tag name of your attribute
   * @param value - tag value of your attribute
   * @return <attribute name="name" type="org.symphony.oss.number.long" value="value"/>
   */
  public EntityBuilder attribute(String name, Long value) {
    Attribute attr = new Attribute(name, "org.symphony.oss.number.long", value.toString());
    this.entity.addAttribute(attr);
    return this;
  }

  /**
   * Creates and Adds an Attribute for URI type
   * @param name - tag name of your attribute
   * @param value - tag value of your attribute
   * @return
   * <attribute name="name" type="com.symphony.uri" value="https://mycompany.atlassian.net/"/>
   */
  public EntityBuilder attribute(String name, URI value) {
    Attribute attr = new Attribute(name, "com.symphony.uri", value.toString());
    this.entity.addAttribute(attr);
    return this;
  }

  /**
   * Creates and Adds an Attribute for URI type, if the provided URI is not null
   * @param name - tag name of your attribute
   * @param value - tag value of your attribute
   * @return
   * <attribute name="name" type="com.symphony.uri" value="https://mycompany.atlassian.net/"/>
   */
  public EntityBuilder attributeIfNotNull(String name, URI value) {
    if (name != null && value != null) {
      Attribute attr = new Attribute(name, "com.symphony.uri", value.toString());
      this.entity.addAttribute(attr);
    }
    return this;
  }

  public EntityBuilder presentationML(String presentationML) {
    presentationML = presentationML.replaceAll("\n", MESSAGEML_LINEBREAK);
    this.entity.setPresentationML(presentationML);
    return this;
  }

  public EntityBuilder presentationML(SafeString safePresentationML) {
    this.entity.setPresentationML(SafeStringUtils.stringValueOf(safePresentationML));
    return this;
  }

  /**
   * Creates and Adds an Attribute for Date type. It will use the rfc3339 format.
   * @param name - tag name of your attribute
   * @param date - tag value of your attribute
   * @return
   * <attribute name="name" type="org.symphonyoss.time.rfc3339" value="2015-05-05T23:40:27Z"/>
   */
  public EntityBuilder dateAttribute(String name, String date) {
    Attribute attr = new Attribute(name, "org.symphonyoss.time.rfc3339", date);
    this.entity.addAttribute(attr);
    return this;
  }

  /**
   * Creates and Adds an Attribute for Date type. It will use the rfc3339 format.
   * This method will only add this attribute if the passed date and name are not blank (non null
   * and non empty)
   * @param name - tag name of your attribute
   * @param date - tag value of your attribute
   * @return
   * <attribute name="name" type="org.symphonyoss.time.rfc3339" value="2015-05-05T23:40:27Z"/>
   */
  public EntityBuilder dateAttributeIfNotBlank(String name, String date) {
    if (!isAnyBlank(name, date)) {
      Attribute attr = new Attribute(name, "org.symphonyoss.time.rfc3339", date);
      this.entity.addAttribute(attr);
    }
    return this;
  }

  /**
   * Adds a new nested entity to other.
   * @param entity
   * @return EntityBuider
   */
  public EntityBuilder nestedEntity(Entity entity) {
    this.entity.addEntity(entity);
    return this;
  }

  /**
   * Adds a new nested entity to other if the entity passed is not null.
   * @param entity
   * @return EntityBuider
   */
  public EntityBuilder nestedEntityIfNotNull(Entity entity) {
    if (entity != null) {
      this.entity.addEntity(entity);
    }
    return this;
  }

  /**
   * @return The current Entity object
   */
  public Entity build() {
    return this.entity;
  }

  /**
   * Marshals an entity object to XML.
   * @return Marshaled entity.
   * <pre>
   *  {@code
   *  <entity type="com.symphony.integration.integration.event.issue" version="1.0">
   *    <entity type="com.symphony.integration.integration.event" version="1.0">
   *      <attribute name="username" type="org.symphonyoss.string" value="xxx"/>
   *      <attribute name="emailAddress" type="org.symphonyoss.string" value="xxx@mycompany.com"/>
   *      <attribute name="displayName" type="org.symphonyoss.string" value="Xxxx Xxxx"/>
   *    </entity>
   *   </entity>}
   * </pre>
   * @throws EntityXMLGeneratorException
   */
  public String generateXML() throws EntityXMLGeneratorException {
    try {
      JAXBContext context = JAXBContext.newInstance(Entity.class);
      Marshaller m = context.createMarshaller();
      m.setProperty(Marshaller.JAXB_FRAGMENT, true);
      StringWriter sw = new StringWriter();
      m.setProperty(Marshaller.JAXB_ENCODING, "utf-8");
      m.marshal(this.entity, sw);

      return ParserUtils.unescapePresentationML(sw.toString());
    } catch (Exception e) {
      throw new EntityXMLGeneratorException(e);
    }
  }

}
