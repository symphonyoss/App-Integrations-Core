package org.symphonyoss.integration.webhook.universal;

import static java.util.Collections.EMPTY_MAP;
import static org.junit.Assert.assertEquals;

import org.symphonyoss.integration.entity.MessageMLParseException;
import org.symphonyoss.integration.entity.MessageMLParser;
import org.symphonyoss.integration.webhook.WebHookPayload;
import org.symphonyoss.integration.webhook.exception.WebHookParseException;

import org.junit.Before;
import org.junit.Test;
import org.springframework.test.util.ReflectionTestUtils;

import java.util.HashMap;
import java.util.Map;

/**
 * Created by rsanchez on 25/08/16.
 */
public class UniversalWebHookIntegrationTest {

  private static final String INVALID_MESSAGEML = "<messageML><mention email=\"rsanchez@symphony"
      + ".com\"/> created SAM-25 (<a href=\"https://whiteam1.atlassian.net/browse/SAM-25\"/>) "
      + "(<b>Highest Story in Sample 1 with labels &quot;production&quot;)<br>Description: Issue "
      + "Test<br>Assignee: <mention email=\"rsanchez@symphony.com\"/></messageML>";

  private static final String VALID_MESSAGEML = "<messageML><mention email=\"rsanchez@symphony"
      + ".com\"/> created SAM-25 (<a href=\"https://whiteam1.atlassian.net/browse/SAM-25\"/>) "
      + "(<b>Highest</b> Story in Sample 1 with labels &quot;production&quot;)<br/>Description: Issue "
      + "Test<br/>Assignee: <mention email=\"rsanchez@symphony.com\"/></messageML>";

  private UniversalWebHookIntegration universalWebHookIntegration = new UniversalWebHookIntegration();

  private MessageMLParser parser = new MessageMLParser();

  @Before
  public void setup() {
    this.parser.init();
    ReflectionTestUtils.setField(universalWebHookIntegration, "parser", parser);
  }

  @Test(expected = MessageMLParseException.class)
  public void testInvalidBody() throws WebHookParseException {
    WebHookPayload payload = new WebHookPayload(EMPTY_MAP, EMPTY_MAP, INVALID_MESSAGEML);
    universalWebHookIntegration.parse(payload);
  }

  @Test(expected = MessageMLParseException.class)
  public void testEmptyForm() throws WebHookParseException {
    WebHookPayload payload = new WebHookPayload(EMPTY_MAP, EMPTY_MAP, null);
    universalWebHookIntegration.parse(payload);
  }

  @Test(expected = MessageMLParseException.class)
  public void testInvalidForm() throws WebHookParseException {
    Map<String, String> parameters = new HashMap<>();
    parameters.put(UniversalWebHookIntegration.PAYLOAD, INVALID_MESSAGEML);

    WebHookPayload payload = new WebHookPayload(parameters, EMPTY_MAP, null);
    universalWebHookIntegration.parse(payload);
  }

  @Test
  public void testValidBody() throws WebHookParseException {
    WebHookPayload payload = new WebHookPayload(EMPTY_MAP, EMPTY_MAP, VALID_MESSAGEML);
    String result = universalWebHookIntegration.parse(payload);
    assertEquals(VALID_MESSAGEML, result);
  }

  @Test
  public void testValidForm() throws WebHookParseException {
    Map<String, String> parameters = new HashMap<>();
    parameters.put(UniversalWebHookIntegration.PAYLOAD, VALID_MESSAGEML);

    WebHookPayload payload = new WebHookPayload(parameters, EMPTY_MAP, null);
    String result = universalWebHookIntegration.parse(payload);
    assertEquals(VALID_MESSAGEML, result);
  }
}
