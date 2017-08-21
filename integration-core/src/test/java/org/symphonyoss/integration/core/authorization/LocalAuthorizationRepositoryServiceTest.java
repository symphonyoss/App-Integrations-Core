package org.symphonyoss.integration.core.authorization;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;

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

  private static final String INTEGRATION_URL1 = "https://test1.symphony.com";

  private static final String INTEGRATION_URL2 = "https://test2.symphony.com";

  private static final Long USER_ID = new Long(123456);

  private static final String MOCK_ACCESS_TOKEN = "as4e435tdfst4302ds8dfs9883249328dsf9";

  private static final String MOCK_VERIFIER = "a32er9";

  private MockOAuth1Data data = new MockOAuth1Data(MOCK_ACCESS_TOKEN, MOCK_VERIFIER);

  private UserAuthorizationData authorizationData;

  @InjectMocks
  private LocalAuthorizationRepositoryService service;
  
  @Before
  public void init() {
    this.service = new LocalAuthorizationRepositoryService();

    authorizationData = new UserAuthorizationData(INTEGRATION_URL1, USER_ID, data);
  }

  @Test
  public void testSaveAndFind() throws AuthorizationException {
    UserAuthorizationData result =
        service.find(INTEGRATION_USER, CONFIGURATION_ID, INTEGRATION_URL1, USER_ID);

    assertNull(result);

    service.save(INTEGRATION_USER, CONFIGURATION_ID, authorizationData);

    result = service.find(INTEGRATION_USER, CONFIGURATION_ID, INTEGRATION_URL1, USER_ID);

    assertEquals(authorizationData, result);

    result = service.find(INTEGRATION_USER, CONFIGURATION_ID, INTEGRATION_URL2, USER_ID);

    assertNull(result);
  }

  @Test
  public void testSaveAndSearch() throws AuthorizationException {
    service.save(INTEGRATION_USER, CONFIGURATION_ID, authorizationData);

    HashMap<String, String> filter = new HashMap<>();
    filter.put("url", INTEGRATION_URL1);

    List<UserAuthorizationData> result = service.search(INTEGRATION_USER, CONFIGURATION_ID, filter);

    assertTrue(result.isEmpty());

    filter.clear();
    filter.put("accessToken", MOCK_ACCESS_TOKEN);

    result = service.search(INTEGRATION_USER, CONFIGURATION_ID, filter);

    assertEquals(1, result.size());
    assertEquals(authorizationData, result.get(0));

    filter.put("verifier", MOCK_VERIFIER);

    result = service.search(INTEGRATION_USER, CONFIGURATION_ID, filter);

    assertEquals(1, result.size());
    assertEquals(authorizationData, result.get(0));

    filter.put("verifier", "test");

    result = service.search(INTEGRATION_USER, CONFIGURATION_ID, filter);

    assertTrue(result.isEmpty());
  }

}
