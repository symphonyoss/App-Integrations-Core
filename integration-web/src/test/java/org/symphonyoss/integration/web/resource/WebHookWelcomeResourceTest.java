package org.symphonyoss.integration.web.resource;

import static junit.framework.TestCase.assertEquals;
import static org.mockito.Mockito.doThrow;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.runners.MockitoJUnitRunner;
import org.springframework.http.ResponseEntity;
import org.symphonyoss.integration.IntegrationStatus;
import org.symphonyoss.integration.config.exception.InstanceNotFoundException;

/**
 * Unit tests for {@link WebHookWelcomeResource}.
 *
 * Created by rsanchez on 19/10/16.
 */
@RunWith(MockitoJUnitRunner.class)
public class WebHookWelcomeResourceTest extends WebHookResourceTest {

  @InjectMocks
  private WebHookWelcomeResource resource = new WebHookWelcomeResource();

  @Before
  public void setup() {
    mockHealthCheckManager();
  }

  /**
   * Test welcome message with valid optional properties
   * @throws Exception
   */
  @Test
  public void testWelcomeSuccessfully() throws Exception {
    mockConfiguration(true);
    mockStatus(IntegrationStatus.ACTIVE);
    mockRequest();

    String body =
        "{ \"streams\": [ \"4A9FN4KrD-x1PQvuB2F3PH___qkZRU5idA\", "
            + "\"jZwwUGRiUyzdpA9fsBgCs3___qkjVetQdA\" ]}";
    assertEquals(ResponseEntity.ok().body(""),
        resource.handleWelcomeRequest(TEST_HASH, CONFIGURATION_ID, TEST_USER, body));
  }

  /**
   * Tests if handle welcome request is returning the proper error and message when there is no Instance
   * for the hash it's handling.
   */
  @Test(expected = InstanceNotFoundException.class)
  public void testHandleRequestInstanceNotFoundException() throws Exception {
    mockConfiguration(true);
    mockStatus(IntegrationStatus.ACTIVE);
    mockRequest();

    doThrow(InstanceNotFoundException.class).when(configurationService).getInstanceById(CONFIGURATION_ID, TEST_HASH, TEST_USER);

    resource.handleWelcomeRequest(TEST_HASH, CONFIGURATION_ID, TEST_USER, null);
  }

}
