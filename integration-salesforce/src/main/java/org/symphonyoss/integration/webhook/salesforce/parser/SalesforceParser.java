package org.symphonyoss.integration.webhook.salesforce.parser;

import org.symphonyoss.integration.entity.Entity;
import org.symphonyoss.integration.webhook.salesforce.SalesforceParseException;

import java.util.List;

/**
 * Interface that defines methods to validate Salesforce messages
 * Created by cmarcondes on 11/2/16.
 */
public interface SalesforceParser {

  String parse(Entity entity) throws SalesforceParseException;

  void setSalesforceUser(String user);

  List<String> getEvents();
}
