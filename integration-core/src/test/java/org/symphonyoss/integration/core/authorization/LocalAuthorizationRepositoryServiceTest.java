package org.symphonyoss.integration.core.authorization;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.mockito.InjectMocks;
import org.symphonyoss.integration.authorization.AuthorizationException;
import org.symphonyoss.integration.authorization.UserAuthorizationData;

import java.util.HashMap;
import java.util.List;

/**
 * Unit tests for {@link LocalAuthorizationRepositoryService}
 * Created by hamitay on 8/18/17.
 */
public class LocalAuthorizationRepositoryServiceTest {

  private static final String INTEGRATION_USER = "integrationUser";

  private static final String CONFIGURATION_ID = "configurationId";

  private static final String INTEGRATION_URL = "integrationURL";

  private static final Long USER_ID = new Long(123456);

  private HashMap<String, String> data1 = new HashMap<>();

  private HashMap<String, String> data2 = new HashMap<>();

  private UserAuthorizationData authorizationData1;

  private UserAuthorizationData authorizationData2;

  @InjectMocks
  private LocalAuthorizationRepositoryService service;
  
  @Before
  public void init() {
    service = new LocalAuthorizationRepositoryService();
    data1.put("data1Key","data1Value");
    data2.put("data1Key","data2Value");
    authorizationData1 = new UserAuthorizationData(INTEGRATION_URL, USER_ID, data1);
    authorizationData2 = new UserAuthorizationData(INTEGRATION_URL, USER_ID, data2);

  }

  @Test
  public void testSaveAndFind() throws AuthorizationException {
    service.save(INTEGRATION_USER, CONFIGURATION_ID, authorizationData1);
    UserAuthorizationData result =
        service.find(INTEGRATION_USER, CONFIGURATION_ID, INTEGRATION_URL, USER_ID);

    Assert.assertEquals(authorizationData1, result);
  }

  //TO DO

  @Test
  public void testSaveAndSearch() throws AuthorizationException {
    service.save(INTEGRATION_USER, CONFIGURATION_ID, authorizationData1);
    service.save(INTEGRATION_USER, CONFIGURATION_ID, authorizationData2);

    HashMap<String, String> filter = new HashMap<>();
    filter.put(INTEGRATION_URL, CONFIGURATION_ID);
    List<UserAuthorizationData> result = service.search(INTEGRATION_USER, CONFIGURATION_ID, filter);
    int a = 0;

  }

}
