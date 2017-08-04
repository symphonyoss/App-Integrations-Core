package org.symphonyoss.integration.pod.api.model;

import org.symphonyoss.integration.authorization.UserAuthorizationData;

import java.util.ArrayList;

/**
 * Holds a list of {@link UserAuthorizationData} to simplify the deserialization of the JSON array
 * received from the Integration API.
 *
 * Created by rsanchez on 04/08/17.
 */
public class UserAuthorizationDataList extends ArrayList<UserAuthorizationData> {

}
