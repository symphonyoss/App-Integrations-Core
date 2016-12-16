package org.symphonyoss.integration.webhook.salesforce;

import static java.util.Collections.EMPTY_MAP;
import static org.junit.Assert.assertEquals;
import static org.mockito.Matchers.any;
import static org.mockito.Mockito.when;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Spy;
import org.mockito.runners.MockitoJUnitRunner;
import org.springframework.test.util.ReflectionTestUtils;
import org.symphonyoss.integration.entity.Entity;
import org.symphonyoss.integration.entity.MessageMLParser;
import org.symphonyoss.integration.webhook.WebHookPayload;
import org.symphonyoss.integration.webhook.salesforce.parser.AccountStatusParser;
import org.symphonyoss.integration.webhook.salesforce.parser.SalesforceParser;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import javax.xml.bind.JAXBException;

/**
 * Created by rsanchez on 25/08/16.
 */
@RunWith(MockitoJUnitRunner.class)
public class SalesforceWebHookIntegrationTest extends BaseSalesforceTest{

  @Spy
  private List<SalesforceParser> salesforceParserBeans = new ArrayList<>();

  @Mock
  private AccountStatusParser accountStatusParser = new AccountStatusParser();

  @InjectMocks
  private SalesforceWebHookIntegration salesforceWebHookIntegration = new SalesforceWebHookIntegration();

  private MessageMLParser messageMLParser = new MessageMLParser();

  @Before
  public void setup() {
    when(accountStatusParser.getEvents()).thenReturn(Arrays.asList("com.symphony.integration.sfdc.event.accountStatus"));
    salesforceParserBeans.add(accountStatusParser);

    salesforceWebHookIntegration.init();

    this.messageMLParser.init();
    ReflectionTestUtils.setField(salesforceWebHookIntegration, "messageMLParser", messageMLParser);
  }

  @Test(expected = SalesforceParseException.class)
  public void testInvalidPayload(){
    WebHookPayload payload = new WebHookPayload(EMPTY_MAP, EMPTY_MAP, "invalid_payload");
    salesforceWebHookIntegration.parse(payload);
  }

  @Test
  public void testUnregistredParser() throws IOException{
    String xml = readFile("executiveReport.xml");
    WebHookPayload payload = new WebHookPayload(EMPTY_MAP, EMPTY_MAP, xml);
    String result = salesforceWebHookIntegration.parse(payload);
    Assert.assertEquals(xml, result);
  }

  @Test
  public void testRegistredParser() throws IOException, JAXBException {
    String xml = readFile("accountStatus.xml");
    WebHookPayload payload = new WebHookPayload(EMPTY_MAP, EMPTY_MAP, xml);

    String expected = readFile("parser/accountStatus_withMentionTags_expected.xml");
    when(accountStatusParser.parse(any(Entity.class))).thenReturn(expected);

    String result = salesforceWebHookIntegration.parse(payload);
    assertEquals("<messageML>" + expected + "</messageML>", result);
  }

}
