package org.symphonyoss.integration.entity;

import static org.junit.Assert.assertEquals;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.runners.MockitoJUnitRunner;
import org.symphonyoss.integration.exception.EntityXMLGeneratorException;

import javax.xml.bind.JAXBException;

;

/**
 * Unit tests for {@link MessageMLParserTest}
 *
 * Created by cmarcondes on 11/2/16.
 */
@RunWith(MockitoJUnitRunner.class)
public class MessageMLParserTest {

  private static final String INVALID_MESSAGEML = "<messageML><mention email=\"rsanchez@symphony"
      + ".com\"/> created SAM-25 (<a href=\"https://whiteam1.atlassian.net/browse/SAM-25\"/>) "
      + "(<b>Highest Story in Sample 1 with labels &quot;production&quot;)<br>Description: Issue "
      + "Test<br>Assignee: <mention email=\"rsanchez@symphony.com\"/></messageML>";
  private static final String VALID_MESSAGEML = "<messageML><mention email=\"rsanchez@symphony"
      + ".com\"/> created SAM-25 (<a href=\"https://whiteam1.atlassian.net/browse/SAM-25\"/>) "
      + "(<b>Highest</b> Story in Sample 1 with labels &quot;production&quot;)<br/>Description: "
      + "Issue "
      + "Test<br/>Assignee: <mention email=\"rsanchez@symphony.com\"/></messageML>";

  private MessageMLParser parser = new MessageMLParser();

  @Before
  public void setup() {
    this.parser.init();
  }

  @Test
  public void testParse() throws JAXBException, EntityXMLGeneratorException {
    String xml = "<messageML>"
        + "<entity type=\"com.symphony.integration.sfdc.event.opportunityNotification\" "
        + "version=\"1.0\">"
        + "<presentationML>teste1</presentationML>"
        + "<attribute name=\"username\" type=\"org.symphonyoss.string\" value=\"test\"/>"
        + "</entity>"
        + "</messageML>";
    MessageML messageML = parser.parse(xml);
    String result = EntityBuilder.forEntity(messageML.getEntity()).generateXML();

    Assert.assertEquals(xml, "<messageML>" + result + "</messageML>");
  }

  @Test
  public void testPresentationMLWithMarkups() throws JAXBException, EntityXMLGeneratorException {
    String xml = "<messageML>"
        + "<entity type=\"com.symphony.integration.sfdc.event.opportunityNotification\" "
        + "version=\"1.0\">"
        + "<presentationML>teste1<br/>teste2<br/><p>teste3</p></presentationML>"
        + "<attribute name=\"username\" type=\"org.symphonyoss.string\" value=\"test\"/>"
        + "</entity>"
        + "</messageML>";

    MessageML messageML = parser.parse(xml);
    String result = EntityBuilder.forEntity(messageML.getEntity()).generateXML();

    Assert.assertEquals(xml, "<messageML>" + result + "</messageML>");
  }

  @Test
  public void testParseNestedEntity() throws JAXBException, EntityXMLGeneratorException {
    String xml = "<messageML>"
        + "<entity type=\"com.symphony.integration.sfdc.event.opportunityNotification\" "
        + "version=\"1.0\">"
        + "<presentationML>teste1</presentationML>"
        + "<attribute name=\"username\" type=\"org.symphonyoss.string\" value=\"test\"/>"
        + "<entity type=\"com.symphony.integration.sfdc.account\" version=\"1.0\">"
        + "<attribute name=\"name\" type=\"org.symphonyoss.string\" value=\"Wells Fargo\"/>"
        + "<entity name=\"totalAmount\" type=\"com.symphony.integration.sfdc.amount\" "
        + "version=\"1.0\">"
        + "<attribute name=\"currency\" type=\"org.symphonyoss.fin.ccy\" value=\"USD\"/>"
        + "</entity>"
        + "</entity>"
        + "</entity>"
        + "</messageML>";
    MessageML messageML = parser.parse(xml);
    String result = EntityBuilder.forEntity(messageML.getEntity()).generateXML();

    Assert.assertEquals(xml, "<messageML>" + result + "</messageML>");
  }

  @Test(expected = MessageMLParseException.class)
  public void testNullBody() {
    parser.validate(null);
  }

  @Test(expected = MessageMLParseException.class)
  public void testInvalidBody() {
    parser.validate(INVALID_MESSAGEML);
  }

  @Test
  public void testValidBody() {
    String result = parser.validate(VALID_MESSAGEML);
    assertEquals(VALID_MESSAGEML, result);
  }

}
