package org.symphonyoss.integration.config;

import static javax.ws.rs.core.Response.Status.FORBIDDEN;
import static org.junit.Assert.assertEquals;
import static org.mockito.Matchers.any;
import static org.mockito.Matchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import com.symphony.api.pod.api.ConfigurationApi;
import com.symphony.api.pod.api.ConfigurationInstanceApi;
import com.symphony.api.pod.client.ApiException;
import com.symphony.api.pod.model.ConfigurationInstance;
import com.symphony.api.pod.model.ConfigurationInstanceSubmissionCreate;
import com.symphony.api.pod.model.ConfigurationInstanceSubmissionUpdate;
import com.symphony.api.pod.model.V1Configuration;
import com.symphony.api.pod.model.V1ConfigurationSubmissionCreate;
import com.symphony.atlas.IAtlas;

import com.fasterxml.jackson.core.JsonProcessingException;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;
import org.symphonyoss.integration.IntegrationAtlas;
import org.symphonyoss.integration.authentication.AuthenticationProxy;
import org.symphonyoss.integration.config.exception.ConfigurationNotFoundException;
import org.symphonyoss.integration.config.exception.ForbiddenUserException;
import org.symphonyoss.integration.config.exception.RemoteConfigurationException;

import javax.ws.rs.core.Response;

/**
 * Tests for {@link RemoteConfigurationService}
 *
 * Created by Milton Quilzini on 02/06/16.
 */
@RunWith(MockitoJUnitRunner.class)
public class RemoteConfigurationServiceTest {

  private static final String USER_ID = "userId";
  private static final String CONFIGURATION_ID = "configurationId";
  private static final String CONFIGURATION_TYPE = "type";
  private static final String NAME = "name";
  private static final String DESCRIPTION = "description";
  private static final String INSTANCE_ID = "id";
  private static final String CREATOR_ID = "CreatorId";
  private static final long CREATED_DATE = 123456L;
  private static final String URL = "Url";
  private static final String POD_URL = "pod.url";
  private static final String API_EXCEPTION_MESSAGE = "message";
  private static final int STATUS_CODE_BAD_REQUEST = Response.Status.BAD_REQUEST.getStatusCode();
  private static final String TOKEN = "token";

  @Mock
  private AuthenticationProxy authenticationProxy;

  @Mock
  private ConfigurationApi configurationApi;

  @Mock
  private ConfigurationInstanceApi configurationInstanceApi;

  @Mock
  private IntegrationAtlas integrationAtlas;

  @InjectMocks
  private ConfigurationService remoteConfigurationService = new RemoteConfigurationService();

  @Before
  public void setUp() throws Exception {
    when(authenticationProxy.getSessionToken(USER_ID)).thenReturn(TOKEN);
  }

  @Test
  public void testInit() throws Exception {
    IAtlas iAtlas = mock(IAtlas.class);
    when(iAtlas.get(POD_URL)).thenReturn(URL);

    when(integrationAtlas.getAtlas()).thenReturn(iAtlas);

    remoteConfigurationService.init();
  }

  @Test(expected = RemoteConfigurationException.class)
  public void testGetConfigurationByIdFailed() throws Exception {
    when(configurationApi.v1ConfigurationConfigurationIdGetGet(CONFIGURATION_ID, TOKEN)).thenThrow(
        ApiException.class);

    remoteConfigurationService.getConfigurationById(CONFIGURATION_ID, USER_ID);
  }

  @Test(expected = ForbiddenUserException.class)
  public void testGetConfigurationByIdForbidden() throws Exception {
    ApiException apiException = new ApiException(FORBIDDEN.getStatusCode(), "Forbidden user");
    when(configurationApi.v1ConfigurationConfigurationIdGetGet(CONFIGURATION_ID, TOKEN)).thenThrow(
        apiException);

    remoteConfigurationService.getConfigurationById(CONFIGURATION_ID, USER_ID);
  }

  @Test
  public void testGetConfigurationById() throws Exception {
    V1Configuration configuration = buildV1Configuration();

    when(configurationApi.v1ConfigurationConfigurationIdGetGet(CONFIGURATION_ID, TOKEN)).thenReturn(
        configuration);

    assertEquals(configuration,
        remoteConfigurationService.getConfigurationById(CONFIGURATION_ID, USER_ID));
  }

  @Test(expected = RemoteConfigurationException.class)
  public void testGetConfigurationByTypeFailed() throws Exception {
    when(configurationApi.v1ConfigurationTypeConfigurationTypeGetGet(CONFIGURATION_TYPE,
        TOKEN)).thenThrow(ApiException.class);

    remoteConfigurationService.getConfigurationByType(CONFIGURATION_TYPE, USER_ID);
  }

  @Test(expected = ConfigurationNotFoundException.class)
  public void testGetConfigurationByTypeNotFound() throws Exception {
    ApiException exception = new ApiException(STATUS_CODE_BAD_REQUEST, "Configuration not found");

    when(configurationApi.v1ConfigurationTypeConfigurationTypeGetGet(CONFIGURATION_TYPE,
        TOKEN)).thenThrow(exception);

    remoteConfigurationService.getConfigurationByType(CONFIGURATION_TYPE, USER_ID);
  }

  @Test
  public void testGetConfigurationByType() throws Exception {
    V1Configuration configuration = buildV1Configuration();

    when(configurationApi.v1ConfigurationTypeConfigurationTypeGetGet(CONFIGURATION_TYPE,
        TOKEN)).thenReturn(configuration);

    assertEquals(configuration,
        remoteConfigurationService.getConfigurationByType(CONFIGURATION_TYPE, USER_ID));
  }

  @Test(expected = RemoteConfigurationException.class)
  public void testSaveConfigurationCreateFailed() throws Exception {
    V1Configuration configuration = buildV1Configuration();

    when(configurationApi.v1ConfigurationConfigurationIdGetGet(CONFIGURATION_ID,
        TOKEN)).thenThrow(new ApiException(STATUS_CODE_BAD_REQUEST,
        API_EXCEPTION_MESSAGE));

    when(configurationApi.v1ConfigurationCreatePost(eq(TOKEN),
        any(V1ConfigurationSubmissionCreate.class))).thenThrow(ApiException.class);

    remoteConfigurationService.save(configuration, USER_ID);
  }

  @Test
  public void testSaveConfigurationCreate() throws Exception {
    V1Configuration configuration = buildV1Configuration();

    when(configurationApi.v1ConfigurationConfigurationIdGetGet(CONFIGURATION_ID,
        TOKEN)).thenThrow(new ApiException(STATUS_CODE_BAD_REQUEST,
        API_EXCEPTION_MESSAGE));

    when(configurationApi.v1ConfigurationCreatePost(eq(TOKEN),
        any(V1ConfigurationSubmissionCreate.class))).thenReturn(configuration);

    assertEquals(configuration, remoteConfigurationService.save(configuration, USER_ID));
  }

  @Test(expected = RemoteConfigurationException.class)
  public void testSaveConfigurationUpdateFailed() throws Exception {
    V1Configuration configuration = buildV1Configuration();
    // make the api update work
    when(configurationApi.v1ConfigurationConfigurationIdUpdatePut(eq(CONFIGURATION_ID), eq(TOKEN),
        any(V1ConfigurationSubmissionCreate.class))).thenThrow(ApiException.class);
    remoteConfigurationService.save(configuration, USER_ID);
  }

  @Test
  public void testSaveConfigurationUpdate() throws Exception {
    V1Configuration configuration = buildV1Configuration();
    // make the api update work
    when(configurationApi.v1ConfigurationConfigurationIdUpdatePut(eq(CONFIGURATION_ID), eq(TOKEN),
        any(V1ConfigurationSubmissionCreate.class))).thenReturn(configuration);
    assertEquals(configuration, remoteConfigurationService.save(configuration, USER_ID));
  }

  @Test(expected = RemoteConfigurationException.class)
  public void testGetInstanceByIdFailed() throws Exception {
    when(configurationInstanceApi.v1AdminConfigurationConfigurationIdInstanceInstanceIdGetGet(
        CONFIGURATION_ID, INSTANCE_ID, TOKEN)).thenThrow(ApiException.class);

    remoteConfigurationService.getInstanceById(CONFIGURATION_ID, INSTANCE_ID, USER_ID);
  }

  @Test
  public void testGetInstanceById() throws Exception {
    ConfigurationInstance expectedConfigurationInstance = buildConfigInstance();

    when(configurationInstanceApi.v1AdminConfigurationConfigurationIdInstanceInstanceIdGetGet(
        CONFIGURATION_ID, INSTANCE_ID, TOKEN)).thenReturn(expectedConfigurationInstance);

    ConfigurationInstance configurationInstance =
        remoteConfigurationService.getInstanceById(CONFIGURATION_ID, INSTANCE_ID, USER_ID);

    assertEquals(expectedConfigurationInstance, configurationInstance);
  }

  @Test(expected = RemoteConfigurationException.class)
  public void testSaveConfigurationInstanceCreateFailed() throws Exception {
    ConfigurationInstance instance = buildConfigInstance();

    when(configurationInstanceApi.v1AdminConfigurationConfigurationIdInstanceInstanceIdGetGet(
        CONFIGURATION_ID,
        INSTANCE_ID, TOKEN)).thenThrow(
        new ApiException(STATUS_CODE_BAD_REQUEST, API_EXCEPTION_MESSAGE));

    when(configurationInstanceApi.v1ConfigurationConfigurationIdInstanceCreatePost(
        eq(CONFIGURATION_ID), eq(TOKEN),
        any(ConfigurationInstanceSubmissionCreate.class))).thenThrow(ApiException.class);

    remoteConfigurationService.save(instance, USER_ID);
  }

  @Test
  public void testSaveConfigurationInstanceCreate() throws Exception {
    ConfigurationInstance instance = buildConfigInstance();

    when(configurationInstanceApi.v1AdminConfigurationConfigurationIdInstanceInstanceIdGetGet(
        CONFIGURATION_ID,
        INSTANCE_ID, TOKEN)).thenThrow(
        new ApiException(STATUS_CODE_BAD_REQUEST, API_EXCEPTION_MESSAGE));

    when(configurationInstanceApi.v1ConfigurationConfigurationIdInstanceCreatePost(
        eq(CONFIGURATION_ID), eq(TOKEN),
        any(ConfigurationInstanceSubmissionCreate.class))).thenReturn(instance);

    assertEquals(instance, remoteConfigurationService.save(instance, USER_ID));
  }

  @Test(expected = RemoteConfigurationException.class)
  public void testSaveConfigurationInstanceUpdateFailed() throws Exception {
    ConfigurationInstance instance = buildConfigInstance();

    when(configurationInstanceApi.v1AdminConfigurationConfigurationIdInstanceInstanceIdUpdatePut(
        eq(CONFIGURATION_ID), eq(INSTANCE_ID), eq(TOKEN),
        any(ConfigurationInstanceSubmissionUpdate.class))).thenThrow(ApiException.class);

    remoteConfigurationService.save(instance, USER_ID);
  }

  @Test
  public void testSaveConfigurationInstanceUpdate() throws Exception {
    ConfigurationInstance instance = buildConfigInstance();

    when(configurationInstanceApi.v1AdminConfigurationConfigurationIdInstanceInstanceIdUpdatePut(
        eq(CONFIGURATION_ID), eq(INSTANCE_ID), eq(TOKEN),
        any(ConfigurationInstanceSubmissionUpdate.class))).thenReturn(instance);

    assertEquals(instance, remoteConfigurationService.save(instance, USER_ID));
  }

  private V1Configuration buildV1Configuration() {
    V1Configuration configuration = new V1Configuration();
    configuration.setConfigurationId(CONFIGURATION_ID);
    configuration.setType(CONFIGURATION_TYPE);
    configuration.setName(NAME);
    configuration.setDescription(DESCRIPTION);
    configuration.setEnabled(true);
    configuration.setVisible(true);

    return configuration;
  }

  private ConfigurationInstance buildConfigInstance() throws JsonProcessingException {
    String optionalProperties =
        "{ \"lastPostedDate\": 1, \"owner\": \"owner\", \"streams\": [ \"stream1\", \"stream2\"] }";

    ConfigurationInstance configInstance = new ConfigurationInstance();
    configInstance.setInstanceId(INSTANCE_ID);
    configInstance.setConfigurationId(CONFIGURATION_ID);
    configInstance.setName(NAME);
    configInstance.setDescription(DESCRIPTION);
    configInstance.setCreatorId(CREATOR_ID);
    configInstance.setCreatedDate(CREATED_DATE);
    configInstance.setOptionalProperties(optionalProperties);

    return configInstance;
  }

}