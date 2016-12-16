package org.symphonyoss.integration.entity;

import static org.symphonyoss.integration.entity.model.EntityConstants.MENTION_TYPE;
import static org.symphonyoss.integration.entity.model.EntityConstants.NAME_ENTITY_FIELD;
import static org.symphonyoss.integration.entity.model.EntityConstants.USER_ID;

import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.runners.MockitoJUnitRunner;
import org.symphonyoss.integration.exception.EntityXMLGeneratorException;

import java.net.URI;
import java.net.URISyntaxException;

import javax.xml.bind.JAXBException;

/**
 * Created by cmarcondes on 8/24/16.
 */
@RunWith(MockitoJUnitRunner.class)
public class EntityBuilderTest {

  @Test
  public void testGenerateEntityWithAttributesWithDifferentTypes()
      throws EntityXMLGeneratorException, URISyntaxException {
    String result = EntityBuilder.forNestedEntity("integration", "event")
        .attribute("string", "xxx")
        .attribute("link", new URI("http://symphony.com"))
        .attribute("int", 1234)
        .dateAttribute("date", "2016-09-08T09:00:00-03:00").generateXML();

    final String expected =
        "<entity type=\"com.symphony.integration.integration.event\" version=\"1.0\">"
            + "<attribute name=\"string\" type=\"org.symphonyoss.string\" value=\"xxx\"/>"
            + "<attribute name=\"link\" type=\"com.symphony.uri\" value=\"http://symphony.com\"/>"
            + "<attribute name=\"int\" type=\"org.symphony.oss.number.int\" value=\"1234\"/>"
            +
            "<attribute name=\"date\" type=\"org.symphonyoss.time.rfc3339\" value=\"2016-09-08T09:00:00-03:00\"/>"
            + "</entity>";
    Assert.assertEquals(expected, result);
  }

  @Test
  public void testGenerateEntityWithNestedEntity()
      throws EntityXMLGeneratorException, URISyntaxException {
    Entity user = EntityBuilder.forNestedEntity("newIntegration", "user")
        .attribute("name", "Caue").build();

    Entity event = EntityBuilder.forNestedEntity("newIntegration", "myEvent")
        .attribute("key", "key-1")
        .nestedEntity(user).build();

    String result = EntityBuilder.forIntegrationEvent("newIntegration", "issue")
        .nestedEntity(event).generateXML();

    final String expected =
        "<entity type=\"com.symphony.integration.newIntegration.event.issue\" version=\"1.0\">"
            + "<entity type=\"com.symphony.integration.newIntegration.myEvent\" version=\"1.0\">"
            + "<attribute name=\"key\" type=\"org.symphonyoss.string\" value=\"key-1\"/>"
            + "<entity type=\"com.symphony.integration.newIntegration.user\" version=\"1.0\">"
            + "<attribute name=\"name\" type=\"org.symphonyoss.string\" value=\"Caue\"/>"
            + "</entity>"
            + "</entity>"
            + "</entity>";
    Assert.assertEquals(expected, result);
  }

  @Test
  public void testCreateEntityWithEmptyParameters()
      throws EntityXMLGeneratorException, URISyntaxException {
    try {
      String xml = EntityBuilder.forIntegrationEvent(null, null).generateXML();
      Assert.assertFalse("Should have returned an error", true);
    } catch (RuntimeException e) {
      Assert.assertTrue(true);
    }

    try {
      String xml = EntityBuilder.forIntegrationEvent("", "").generateXML();
      Assert.assertFalse("Should have returned an error", true);
    } catch (RuntimeException e) {
      Assert.assertTrue(true);
    }

    try {
      String xml = EntityBuilder.forNestedEntity(null, null).generateXML();
      Assert.assertFalse("Should have returned an error", true);
    } catch (RuntimeException e) {
      Assert.assertTrue(true);
    }

    try {
      String xml = EntityBuilder.forNestedEntity("", "").generateXML();
      Assert.assertFalse("Should have returned an error", true);
    } catch (RuntimeException e) {
      Assert.assertTrue(true);
    }
  }

  @Test
  public void testGenerateEntityWithPresentationML()
      throws EntityXMLGeneratorException, URISyntaxException {
    Entity user = EntityBuilder.forNestedEntity("newIntegration", "user")
        .attribute("name", "Cau<e").build();

    Entity event = EntityBuilder.forNestedEntity("newIntegration", "myEvent")
        .attribute("key", "key-1")
        .nestedEntity(user).build();

    String result = EntityBuilder.forIntegrationEvent("newIntegration", "issue")
        .nestedEntity(event)
        .presentationML("<a href=\"teste\"/>").generateXML();

    final String expected =
        "<entity type=\"com.symphony.integration.newIntegration.event.issue\" version=\"1.0\">"
            + "<presentationML><a href=\"teste\"/></presentationML>"
            + "<entity type=\"com.symphony.integration.newIntegration.myEvent\" version=\"1.0\">"
            + "<attribute name=\"key\" type=\"org.symphonyoss.string\" value=\"key-1\"/>"
            + "<entity type=\"com.symphony.integration.newIntegration.user\" version=\"1.0\">"
            +
            "<attribute name=\"name\" type=\"org.symphonyoss.string\" value=\"Cau&amp;lt;e\"/></entity></entity></entity>";
    Assert.assertEquals(expected, result);
  }

  @Test
  public void testGenerateEntityWithBreakLineAndLinks()
      throws EntityXMLGeneratorException, URISyntaxException {
    Entity user = EntityBuilder.forNestedEntity("newIntegration", "user")
        .attribute("name", "Cau<e").build();

    Entity event = EntityBuilder.forNestedEntity("newIntegration", "myEvent")
        .attribute("Description", "My <b>Description</b> I want a break line here \n <br/> testing")
        .attribute("myLink",
            "My webpage is https://www.symphony.com <a href=\"https://www.symphony.com\"/> try it")
        .nestedEntity(user).build();

    String result = EntityBuilder.forIntegrationEvent("newIntegration", "issue")
        .nestedEntity(event)
        .presentationML("<a href=\"teste\"/>").generateXML();

    final String expected =
        "<entity type=\"com.symphony.integration.newIntegration.event.issue\" version=\"1.0\">"
            + "<presentationML><a href=\"teste\"/></presentationML>"
            + "<entity type=\"com.symphony.integration.newIntegration.myEvent\" version=\"1.0\">"
            + "<attribute name=\"Description\" type=\"org.symphonyoss.string\" value=\"My &amp;"
            + "lt;b&amp;gt;Description&amp;lt;/b&amp;gt; I want a break line here &lt;br/&gt; "
            + "&amp;lt;br/&amp;gt; testing\"/>"
            + "<attribute name=\"myLink\" type=\"org.symphonyoss.string\" value=\"My webpage is "
            + "&lt;a href=&quot;https://www.symphony.com&quot;&gt;https://www.symphony.com&lt;"
            + "/a&gt; &amp;lt;a href=&amp;quot;&lt;a href=&quot;https://www.symphony.com&quot;"
            + "&gt;https://www.symphony.com&lt;/a&gt;&amp;quot;/&amp;gt; try it\"/>"
            + "<entity type=\"com.symphony.integration.newIntegration.user\" version=\"1.0\">"
            +
            "<attribute name=\"name\" type=\"org.symphonyoss.string\" value=\"Cau&amp;lt;e\"/></entity></entity></entity>";
    Assert.assertEquals(expected, result);
  }

  @Test
  public void testCreateBuilderWithAnEntity() throws JAXBException, EntityXMLGeneratorException {
    String xml = "<entity type=\"com.symphony.integration.sfdc.event.opportunityNotification\" "
        + "version=\"1.0\">"
        + "<presentationML>test"
        + "</presentationML>"
        + "<attribute name=\"username\" type=\"org.symphonyoss.string\" value=\"test\"/>"
        + "</entity>";
    MessageML messageML = MessageMLParser.parse("<messageML>" + xml + "</messageML>");
    EntityBuilder builder = EntityBuilder.forEntity(messageML.getEntity());

    Assert.assertEquals(xml, builder.generateXML());
  }

  @Test
  public void testCreateNestedEntityWithSpecificType() throws EntityXMLGeneratorException {
    EntityBuilder builder = EntityBuilder.forNestedEntity("com.symphony.myEntity");
    String result = builder.generateXML();
    Assert.assertEquals("<entity type=\"com.symphony.myEntity\" version=\"1.0\"/>", result);
  }

  @Test(expected = RuntimeException.class)
  public void testCreateNestedEntityWithoutSpecificType() throws EntityXMLGeneratorException {
    EntityBuilder builder = EntityBuilder.forNestedEntity(null);
    String result = builder.generateXML();
  }

  @Test(expected = RuntimeException.class)
  public void testCreateNestedEntityWithSpecificTypeEmpty() throws EntityXMLGeneratorException {
    EntityBuilder builder = EntityBuilder.forNestedEntity("");
    String result = builder.generateXML();
  }

  @Test
  public void testCreateMentionEntity() throws EntityXMLGeneratorException {
    EntityBuilder builder = EntityBuilder.forNestedEntity(MENTION_TYPE);
    builder.attribute(USER_ID, 123l).attributeIfNotEmpty(NAME_ENTITY_FIELD, "Caue Marcondes");
    String result = builder.generateXML();

    String expected = "<entity type=\"com.symphony.mention\" version=\"1.0\">"
        + "<attribute name=\"id\" type=\"org.symphony.oss.number.long\" value=\"123\"/>"
        + "<attribute name=\"name\" type=\"org.symphonyoss.string\" value=\"Caue Marcondes\"/>"
        + "</entity>";

    Assert.assertEquals(expected, result);
  }

}