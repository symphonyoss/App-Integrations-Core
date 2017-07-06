package org.symphonyoss.integration.web.properties;

import org.symphonyoss.integration.web.filter.WebHookOriginCheckFilter;

/** Exception message keys used by the component {@link WebHookOriginCheckFilter}
 * Created by  on 07/05/17.
 */
public class WebHookOriginCheckFilterProperties {

  public static final String CANNOT_FIND_HOST_FOR_IP = "integration.web.cannot.find.host";

  public static final String CANNOT_FIND_HOST_FOR_IP_SOLUTION = CANNOT_FIND_HOST_FOR_IP+ ".solution";

  public static final String WEBHOOK_REQUEST_BLOCKED = "integration.web.request.blocked";

  public static final String WEBHOOK_REQUEST_BLOCKED_SOLUTION = WEBHOOK_REQUEST_BLOCKED + ".solution";

  public static final String CANNOT_RETRIEVE_WHITELIST = "integration.web.cannot.retrieve.whitelist";

  public static final String CANNOT_RETRIEVE_WHITELIST_SOLUTION = CANNOT_RETRIEVE_WHITELIST + ".solution";
}
