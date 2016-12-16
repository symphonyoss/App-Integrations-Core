package org.symphonyoss.integration.webhook.salesforce.parser;

import static org.symphonyoss.integration.webhook.salesforce.SalesforceConstants.ACCOUNT;
import static org.symphonyoss.integration.webhook.salesforce.SalesforceConstants.ACTIVITIES;
import static org.symphonyoss.integration.webhook.salesforce.SalesforceConstants.ASSIGNEE;
import static org.symphonyoss.integration.webhook.salesforce.SalesforceConstants.OPPORTUNITIES;
import static org.symphonyoss.integration.webhook.salesforce.SalesforceConstants.OWNER;

import org.springframework.stereotype.Component;
import org.symphonyoss.integration.entity.Entity;
import org.symphonyoss.integration.entity.EntityBuilder;
import org.symphonyoss.integration.exception.EntityXMLGeneratorException;
import org.symphonyoss.integration.webhook.salesforce.SalesforceParseException;

import java.util.Arrays;
import java.util.List;

/**
 * Class responsible to handle the Account Status event of Salesforce
 *
 * Created by cmarcondes on 11/3/16.
 */
@Component
public class AccountStatusParser extends BaseSalesforceParser {

  @Override
  public String parse(Entity entity) throws SalesforceParseException {

    createMentionTagFor(entity.getEntityByType(ACCOUNT), OWNER);
    createListOfMentionsFor(entity, OPPORTUNITIES, OWNER);
    createListOfMentionsFor(entity, ACTIVITIES, ASSIGNEE);

    try {
      return EntityBuilder.forEntity(entity).generateXML();
    } catch (EntityXMLGeneratorException e) {
      throw new SalesforceParseException("Something went wrong while building the message for Salesforce Account Status event.", e);
    }
  }

  @Override
  public List<String> getEvents() {
    return Arrays.asList("com.symphony.integration.sfdc.event.accountStatus");
  }
}
