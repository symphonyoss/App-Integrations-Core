package org.symphonyoss.integration.webhook.salesforce.parser;

import static org.symphonyoss.integration.webhook.salesforce.SalesforceConstants.OPPORTUNITY;
import static org.symphonyoss.integration.webhook.salesforce.SalesforceConstants.OWNER;

import org.springframework.stereotype.Component;
import org.symphonyoss.integration.entity.Entity;
import org.symphonyoss.integration.entity.EntityBuilder;
import org.symphonyoss.integration.exception.EntityXMLGeneratorException;
import org.symphonyoss.integration.webhook.salesforce.SalesforceParseException;

import java.util.Arrays;
import java.util.List;

/**
 * Class responsable to handle the Opportunity Notification event of Salesforce
 *
 * Created by cmarcondes on 11/2/16.
 */
@Component
public class OpportunityNotificationParser extends BaseSalesforceParser {

  @Override
  public List<String> getEvents() {
    return Arrays.asList("com.symphony.integration.sfdc.event.opportunityNotification");
  }

  @Override
  public String parse(Entity entity) throws SalesforceParseException {
    createMentionTagFor(entity.getEntityByType(OPPORTUNITY), OWNER);

    try {
      return EntityBuilder.forEntity(entity).generateXML();
    } catch (EntityXMLGeneratorException e) {
      throw new SalesforceParseException("Something went wrong while building the message for Salesforce Opportunity Notification event.", e);
    }
  }
}
