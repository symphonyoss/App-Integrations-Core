package org.symphonyoss.integration.webhook;

import static java.util.Collections.EMPTY_MAP;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;
import static org.mockito.Matchers.any;
import static org.mockito.Matchers.anyString;
import static org.mockito.Matchers.eq;
import static org.mockito.Mockito.doAnswer;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;
import static org.symphonyoss.integration.config.WebHookConfigurationUtils.LAST_POSTED_DATE;

import com.symphony.api.agent.model.V2Message;
import com.symphony.api.agent.model.V2MessageList;
import com.symphony.api.pod.client.ApiException;
import com.symphony.api.pod.model.ConfigurationInstance;
import com.symphony.api.pod.model.V1Configuration;
import com.symphony.atlas.AtlasException;
import com.symphony.atlas.IAtlas;

import com.google.common.cache.LoadingCache;
import org.junit.Before;
import org.junit.Test;
import org.junit.rules.TemporaryFolder;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Spy;
import org.mockito.invocation.InvocationOnMock;
import org.mockito.runners.MockitoJUnitRunner;
import org.mockito.stubbing.Answer;
import org.symphonyoss.integration.IntegrationAtlas;
import org.symphonyoss.integration.IntegrationPropertiesReader;
import org.symphonyoss.integration.IntegrationStatus;
import org.symphonyoss.integration.authentication.AuthenticationProxy;
import org.symphonyoss.integration.config.ConfigurationService;
import org.symphonyoss.integration.config.WebHookConfigurationUtils;
import org.symphonyoss.integration.config.exception.ForbiddenUserException;
import org.symphonyoss.integration.core.bridge.IntegrationBridge;
import org.symphonyoss.integration.core.bridge.StreamService;
import org.symphonyoss.integration.core.bridge.StreamServiceImpl;
import org.symphonyoss.integration.core.exception.CertificateNotFoundException;
import org.symphonyoss.integration.core.exception.LoadKeyStoreException;
import org.symphonyoss.integration.core.exception.UnexpectedBootstrapException;
import org.symphonyoss.integration.core.service.UserService;
import org.symphonyoss.integration.entity.model.User;
import org.symphonyoss.integration.model.AllowedOrigin;
import org.symphonyoss.integration.model.Application;
import org.symphonyoss.integration.model.IntegrationProperties;
import org.symphonyoss.integration.model.healthcheck.IntegrationFlags;
import org.symphonyoss.integration.model.healthcheck.IntegrationHealth;
import org.symphonyoss.integration.webhook.exception.InvalidStreamTypeException;
import org.symphonyoss.integration.webhook.exception.StreamTypeNotFoundException;
import org.symphonyoss.integration.webhook.exception.WebHookDisabledException;
import org.symphonyoss.integration.webhook.exception.WebHookParseException;
import org.symphonyoss.integration.webhook.exception.WebHookUnavailableException;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.net.ConnectException;
import java.security.KeyStore;
import java.security.KeyStoreException;
import java.security.NoSuchAlgorithmException;
import java.security.cert.CertificateException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Locale;
import java.util.Set;
import java.util.TimeZone;
import java.util.concurrent.ScheduledExecutorService;

import javax.ws.rs.ProcessingException;

/**
 * Test class responsible to test the flows in the {@link WebHookIntegration}.
 * Created by rsanchez on 06/05/16.
 */
@RunWith(MockitoJUnitRunner.class)
public class WebHookIntegrationTest {

  private static final String CONFIGURATION_ID = "57bf581ae4b079de6a1cbbf9";

  private static final String INTEGRATION_USER = "jiraWebHookIntegration";

  private static final String CERTS_DIR = "certs";

  private static final String DEFAULT_KEYSTORE_TYPE = "pkcs12";

  private static final String DEFAULT_KEYSTORE_PASSWORD = "changeit";

  private static final String DEFAULT_KEYSTORE_TYPE_SUFFIX = ".p12";

  @Mock
  private IntegrationBridge service;

  @Spy
  private StreamService streamService = new StreamServiceImpl();

  @Mock
  private ConfigurationService configService;

  @Mock
  private AuthenticationProxy authenticationProxy;

  @Mock
  private WebHookExceptionHandler exceptionHandler;

  @Mock
  private IntegrationAtlas integrationAtlas;

  @Mock
  private UserService userService;

  @Mock
  private IntegrationPropertiesReader propertiesReader;

  @Mock
  private ScheduledExecutorService scheduler;

  @Mock
  private LoadingCache<String, IntegrationFlags.ValueEnum> configuratorFlagsCache;

  @InjectMocks
  private WebHookIntegration mockWHI = new MockWebHookIntegration();

  private IAtlas atlas;

  @Before
  public void setup() {
    TimeZone.setDefault(TimeZone.getTimeZone("UTC"));
    Locale.setDefault(Locale.US);

    when(authenticationProxy.isAuthenticated(anyString())).thenReturn(true);

    V1Configuration configuration = new V1Configuration();
    configuration.setConfigurationId(CONFIGURATION_ID);
    configuration.setName("JIRA");
    configuration.setType(INTEGRATION_USER);
    configuration.setEnabled(true);

    mockWHI.onConfigChange(configuration);

    IntegrationProperties integrationProperties = new IntegrationProperties();
    doReturn(integrationProperties).when(propertiesReader).getProperties();

    this.atlas = mock(IAtlas.class);
    doReturn(atlas).when(integrationAtlas).getAtlas();
  }

  @Test
  public void testOnCreateRuntimeException() {
    try {
      mockWHI.onConfigChange(null);
      mockWHI.onCreate(INTEGRATION_USER);
    } catch (UnexpectedBootstrapException e) {
      assertEquals(IntegrationStatus.FAILED_BOOTSTRAP.name(),
          mockWHI.getHealthStatus().getStatus());
    }
  }

  @Test
  public void testOnCreateCertNotFoundException() throws AtlasException {
    try {
      doThrow(AtlasException.class).when(atlas).getConfigDir(CERTS_DIR);

      mockWHI.onConfigChange(null);
      mockWHI.onCreate(INTEGRATION_USER);
    } catch (CertificateNotFoundException e) {
      IntegrationHealth health  = mockWHI.getHealthStatus();
      assertEquals(IntegrationStatus.FAILED_BOOTSTRAP.name(), health.getStatus());
      assertEquals(IntegrationFlags.ValueEnum.NOK, health.getFlags().getCertificateInstalled());
    }
  }

  @Test
  public void testOnCreateLoadKeystoreException() throws AtlasException, IOException {
    try {
      mockCertDir();

      mockWHI.onConfigChange(null);
      mockWHI.onCreate(INTEGRATION_USER);
    } catch (LoadKeyStoreException e) {
      IntegrationHealth health  = mockWHI.getHealthStatus();
      assertEquals(IntegrationStatus.FAILED_BOOTSTRAP.name(), health.getStatus());
      assertEquals(IntegrationFlags.ValueEnum.NOK, health.getFlags().getCertificateInstalled());
    }
  }

  @Test
  public void testOnCreateCertificateInstalled()
      throws KeyStoreException, CertificateException, NoSuchAlgorithmException, IOException,
      AtlasException {
    mockCertDir();
    mockKeystore();

    mockWHI.onCreate(INTEGRATION_USER);

    IntegrationHealth health  = mockWHI.getHealthStatus();
    assertEquals(IntegrationStatus.ACTIVE.name(), health.getStatus());
    assertEquals(IntegrationFlags.ValueEnum.OK, health.getFlags().getCertificateInstalled());
  }

  private void mockCertDir() throws IOException, AtlasException {
    File tmpDir = new File(System.getProperty("java.io.tmpdir"));
    TemporaryFolder folder = new TemporaryFolder(tmpDir);
    folder.create();

    doReturn(folder.getRoot()).when(atlas).getConfigDir(CERTS_DIR);
  }

  private void mockKeystore()
      throws KeyStoreException, IOException, NoSuchAlgorithmException, CertificateException,
      AtlasException {
    KeyStore ks = KeyStore.getInstance(DEFAULT_KEYSTORE_TYPE);

    char[] password = DEFAULT_KEYSTORE_PASSWORD.toCharArray();
    ks.load(null, password);

    // Store away the keystore.
    String filename = INTEGRATION_USER + DEFAULT_KEYSTORE_TYPE_SUFFIX;
    String certsDir = atlas.getConfigDir(CERTS_DIR).getAbsolutePath() + File.separator;
    String storeLocation = certsDir + filename;

    try (FileOutputStream fos = new FileOutputStream(storeLocation)) {
      ks.store(fos, password);
    }
  }

  @Test
  public void testHandleWithUpdateTimestamp()
      throws WebHookParseException, IOException {
    doReturn(mockWHI.getConfig()).when(configService)
        .getConfigurationById(CONFIGURATION_ID, INTEGRATION_USER);

    V2MessageList response = new V2MessageList();
    V2Message message1 = new V2Message();
    V2Message message2 = new V2Message();

    Long timestamp1 = 1476109880000L;
    Long timestamp2 = timestamp1 + 1000;

    message1.setTimestamp(timestamp1.toString());
    message2.setTimestamp(timestamp2.toString());

    response.add(message1);
    response.add(message2);

    when(service.sendMessage(any(ConfigurationInstance.class), anyString(), any(List.class),
        anyString())).thenReturn(response);

    Long optionalPropertiesTimestamp = timestamp1 - 1000;
    String optionalProperties = "{ \"lastPostedDate\": " + optionalPropertiesTimestamp
        + ", \"owner\": \"owner\", \"streams\": [ \"stream1\", \"stream2\"] }";

    ConfigurationInstance instance = new ConfigurationInstance();
    instance.setConfigurationId("jirawebhook");
    instance.setInstanceId("1234");
    instance.setOptionalProperties(optionalProperties);

    doReturn(instance).when(configService).getInstanceById(anyString(), anyString(), anyString());

    mockWHI.handle(instance.getInstanceId(), INTEGRATION_USER,
        new WebHookPayload(EMPTY_MAP, EMPTY_MAP, "{ \"webhookEvent\": \"mock\" }"));

    Long lastPostedDate = WebHookConfigurationUtils.fromJsonString(instance.getOptionalProperties())
        .path(LAST_POSTED_DATE).asLong();

    assertEquals(timestamp2, lastPostedDate);

    mockWHI.onConfigChange(null);

    IntegrationHealth integrationHealth = mockWHI.getHealthStatus();
    assertEquals("2016-10-10T14:31:21Z+0000", integrationHealth.getLatestPostTimestamp());
  }

  @Test
  public void testHandleFailedSend() throws WebHookParseException, IOException {
    doReturn(mockWHI.getConfig()).when(configService)
        .getConfigurationById(CONFIGURATION_ID, INTEGRATION_USER);

    doReturn(new V2MessageList()).when(service)
        .sendMessage(any(ConfigurationInstance.class), anyString(), any(List.class), anyString());

    Long optionalPropertiesTimestamp = System.currentTimeMillis();
    String optionalProperties = "{ \"lastPostedDate\": " + optionalPropertiesTimestamp
        + ", \"owner\": \"owner\", \"streams\": [ \"stream1\", \"stream2\"] }";

    ConfigurationInstance instance = new ConfigurationInstance();
    instance.setConfigurationId("jirawebhook");
    instance.setInstanceId("1234");
    instance.setOptionalProperties(optionalProperties);

    doReturn(instance).when(configService).getInstanceById(anyString(), anyString(), anyString());

    mockWHI.handle(instance.getInstanceId(), INTEGRATION_USER,
        new WebHookPayload(EMPTY_MAP, EMPTY_MAP, "{ \"webhookEvent\": \"mock\" }"));

    Long lastPostedDate = WebHookConfigurationUtils.fromJsonString(instance.getOptionalProperties())
        .path(LAST_POSTED_DATE).asLong();

    // no update to lastPostedDate
    assertEquals(optionalPropertiesTimestamp, lastPostedDate);

    mockWHI.onConfigChange(null);

    IntegrationHealth integrationHealth = mockWHI.getHealthStatus();
    assertNull(integrationHealth.getLatestPostTimestamp());
  }

  @Test
  public void testHandleSocketException() throws WebHookParseException, IOException {
    ProcessingException exception = new ProcessingException(new ConnectException());
    doThrow(exception).when(service)
        .sendMessage(any(ConfigurationInstance.class), anyString(), any(List.class), anyString());

    Long optionalPropertiesTimestamp = System.currentTimeMillis();
    String optionalProperties = "{ \"lastPostedDate\": " + optionalPropertiesTimestamp
        + ", \"owner\": \"owner\", \"streams\": [ \"stream1\", \"stream2\"] }";

    ConfigurationInstance instance = new ConfigurationInstance();
    instance.setConfigurationId("jirawebhook");
    instance.setInstanceId("1234");
    instance.setOptionalProperties(optionalProperties);

    V1Configuration configuration = mock(V1Configuration.class);
    doReturn(true).when(configuration).getEnabled();

    doReturn(instance).when(configService).getInstanceById(anyString(), anyString(), anyString());
    doReturn(configuration).when(configService).getConfigurationById(anyString(), anyString());

    mockWHI.handle("1234", INTEGRATION_USER,
        new WebHookPayload(EMPTY_MAP, EMPTY_MAP, "{ \"webhookEvent\": \"mock\" }"));

    Long lastPostedDate = WebHookConfigurationUtils.fromJsonString(instance.getOptionalProperties())
        .path(LAST_POSTED_DATE).asLong();

    // no update to lastPostedDate
    assertEquals(optionalPropertiesTimestamp, lastPostedDate);

    mockWHI.onConfigChange(null);

    IntegrationHealth integrationHealth = mockWHI.getHealthStatus();
    assertNull(integrationHealth.getLatestPostTimestamp());
  }

  @Test(expected = StreamTypeNotFoundException.class)
  public void testWelcomeInvalidPayload() throws IOException {
    SendMessageAnswer answer = new SendMessageAnswer();
    doAnswer(answer).when(service).sendMessage(any(ConfigurationInstance.class), anyString(),
        any(List.class), anyString());

    mockWHI.welcome(new ConfigurationInstance(), INTEGRATION_USER, "");
  }

  @Test(expected = StreamTypeNotFoundException.class)
  public void testWelcomeEmptyPayload() throws IOException {
    SendMessageAnswer answer = new SendMessageAnswer();
    doAnswer(answer).when(service).sendMessage(any(ConfigurationInstance.class), anyString(),
        any(List.class), anyString());

    Long optionalPropertiesTimestamp = System.currentTimeMillis();
    String optionalProperties = "{ \"lastPostedDate\": " + optionalPropertiesTimestamp
        + ", \"owner\": \"owner\", \"streams\": [ \"stream1\", \"stream2\"] }";

    ConfigurationInstance instance = new ConfigurationInstance();
    instance.setConfigurationId("jirawebhook");
    instance.setInstanceId("1234");
    instance.setOptionalProperties(optionalProperties);

    mockWHI.welcome(instance, INTEGRATION_USER, "{}");
  }

  @Test(expected = StreamTypeNotFoundException.class)
  public void testWelcomeInvalidStreams() throws IOException {
    SendMessageAnswer answer = new SendMessageAnswer();
    doAnswer(answer).when(service).sendMessage(any(ConfigurationInstance.class), anyString(),
        any(List.class), anyString());

    Long optionalPropertiesTimestamp = System.currentTimeMillis();
    String optionalProperties = "{ \"lastPostedDate\": " + optionalPropertiesTimestamp
        + ", \"owner\": \"owner\", \"streams\": [ \"stream1\", \"stream2\"] }";

    ConfigurationInstance instance = new ConfigurationInstance();
    instance.setConfigurationId("jirawebhook");
    instance.setInstanceId("1234");
    instance.setOptionalProperties(optionalProperties);

    mockWHI.welcome(instance, INTEGRATION_USER, "{ \"streams\": [ \"stream3\", \"stream4\"] }");
  }

  @Test(expected = InvalidStreamTypeException.class)
  public void testWelcomeEmptyStreamType() throws IOException {
    SendMessageAnswer answer = new SendMessageAnswer();
    doAnswer(answer).when(service).sendMessage(any(ConfigurationInstance.class), anyString(),
        any(List.class), anyString());

    Long optionalPropertiesTimestamp = System.currentTimeMillis();
    String optionalProperties = "{ \"lastPostedDate\": " + optionalPropertiesTimestamp
        + ", \"owner\": \"owner\", \"streams\": [ \"stream1\", \"stream2\"] }";

    ConfigurationInstance instance = new ConfigurationInstance();
    instance.setConfigurationId("jirawebhook");
    instance.setInstanceId("1234");
    instance.setOptionalProperties(optionalProperties);

    mockWHI.welcome(instance, INTEGRATION_USER, "{ \"streams\": [ \"stream1\" ] }");
  }

  @Test
  public void testWelcomeIMStreamType() throws IOException {
    SendMessageAnswer answer = new SendMessageAnswer();
    doAnswer(answer).when(service).sendMessage(any(ConfigurationInstance.class), anyString(),
        any(List.class), anyString());

    Long optionalPropertiesTimestamp = System.currentTimeMillis();
    String optionalProperties = "{ \"lastPostedDate\": " + optionalPropertiesTimestamp
        + ", \"owner\": \"owner\", \"streams\": [ \"stream1\", \"stream2\"], \"streamType\": "
        + "\"IM\" }";

    ConfigurationInstance instance = new ConfigurationInstance();
    instance.setConfigurationId("jirawebhook");
    instance.setInstanceId("1234");
    instance.setOptionalProperties(optionalProperties);

    mockWHI.welcome(instance, INTEGRATION_USER, "{ \"streams\": [ \"stream1\", \"stream3\" ] }");

    assertEquals(
        "<messageML>Hi there. This is the JIRA application. I'll let you know of any new events "
            + "sent from the JIRA integration you configured.</messageML>",
        answer.getMessage());
    assertEquals(1, answer.getCount());
  }

  @Test
  public void testWelcomeChatroomStreamType() throws IOException, ApiException {
    User user = new User();
    user.setDisplayName("Test user");

    when(authenticationProxy.getSessionToken(anyString())).thenReturn("");
    when(userService.getUserByUserId(anyString(), eq(7890L))).thenReturn(user);

    SendMessageAnswer answer = new SendMessageAnswer();
    doAnswer(answer).when(service).sendMessage(any(ConfigurationInstance.class), anyString(),
        any(List.class), anyString());

    Long optionalPropertiesTimestamp = System.currentTimeMillis();
    String optionalProperties = "{ \"lastPostedDate\": " + optionalPropertiesTimestamp
        + ", \"owner\": \"owner\", \"streams\": [ \"stream1\", \"stream2\"], \"streamType\": "
        + "\"CHATROOM\" }";

    ConfigurationInstance instance = new ConfigurationInstance();
    instance.setConfigurationId("jirawebhook");
    instance.setInstanceId("1234");
    instance.setCreatorId("7890");
    instance.setOptionalProperties(optionalProperties);

    mockWHI.welcome(instance, INTEGRATION_USER, "{ \"streams\": [ \"stream1\", \"stream3\", "
        + "\"stream2\" ] }");

    assertEquals(
        "<messageML>Hi there. This is the JIRA application. I'll let you know of any new events "
            + "sent from the JIRA integration configured by Test user.</messageML>",
        answer.getMessage());
    assertEquals(2, answer.getCount());
  }

  @Test
  public void testWelcomeChatroomWithoutUserStreamType() throws IOException {
    SendMessageAnswer answer = new SendMessageAnswer();
    doAnswer(answer).when(service).sendMessage(any(ConfigurationInstance.class), anyString(),
        any(List.class), anyString());

    Long optionalPropertiesTimestamp = System.currentTimeMillis();
    String optionalProperties = "{ \"lastPostedDate\": " + optionalPropertiesTimestamp
        + ", \"owner\": \"owner\", \"streams\": [ \"stream1\", \"stream2\"], \"streamType\": "
        + "\"CHATROOM\" }";

    ConfigurationInstance instance = new ConfigurationInstance();
    instance.setConfigurationId("jirawebhook");
    instance.setInstanceId("1234");
    instance.setOptionalProperties(optionalProperties);

    mockWHI.welcome(instance, INTEGRATION_USER, "{ \"streams\": [ \"stream1\", \"stream2\" ] }");

    assertEquals(
        "<messageML>Hi there. This is the JIRA application. I'll let you know of any new events "
            + "sent from the JIRA integration configured by UNKNOWN.</messageML>",
        answer.getMessage());
    assertEquals(2, answer.getCount());
  }

  @Test(expected = WebHookDisabledException.class)
  public void testUnavailable() {
    V1Configuration config = mockWHI.getConfig();
    config.setEnabled(false);

    doReturn(mockWHI.getConfig()).when(configService)
        .getConfigurationById(CONFIGURATION_ID, INTEGRATION_USER);
    doReturn(IntegrationFlags.ValueEnum.NOK).when(configuratorFlagsCache).getUnchecked
        (INTEGRATION_USER);

    mockWHI.isAvailable();
  }

  @Test(expected = WebHookUnavailableException.class)
  public void testUnavailableForbidden() {
    doThrow(ForbiddenUserException.class).when(configService)
        .getConfigurationById(CONFIGURATION_ID, INTEGRATION_USER);
    doReturn(IntegrationFlags.ValueEnum.NOK).when(configuratorFlagsCache).getUnchecked
        (INTEGRATION_USER);

    mockWHI.isAvailable();
  }

  @Test
  public void testAvailable() {
    doReturn(mockWHI.getConfig()).when(configService)
        .getConfigurationById(CONFIGURATION_ID, INTEGRATION_USER);

    assertTrue(mockWHI.isAvailable());
  }

  @Test
  public void testWhiteListNullConfig() {
    mockWHI.onConfigChange(null);

    Set<String> integrationWhiteList = mockWHI.getIntegrationWhiteList();
    assertNotNull(integrationWhiteList);
    assertTrue(integrationWhiteList.isEmpty());
  }

  @Test
  public void testWhiteListEmptyList() {
    doReturn(new IntegrationProperties()).when(propertiesReader).getProperties();

    Set<String> integrationWhiteList = mockWHI.getIntegrationWhiteList();
    assertNotNull(integrationWhiteList);
    assertTrue(integrationWhiteList.isEmpty());
  }

  @Test
  public void testWhiteList() {
    List<AllowedOrigin> originList = new ArrayList<>();

    AllowedOrigin origin1 = new AllowedOrigin();
    origin1.setHost("squid-104-1.sc1.uc-inf.net");
    origin1.setAddress("165.254.226.119");

    AllowedOrigin origin2 = new AllowedOrigin();
    origin2.setAddress("107.23.104.115");

    originList.add(origin1);
    originList.add(origin2);

    Application application = new Application();
    application.setAllowedOrigins(originList);
    application.setType(INTEGRATION_USER);

    IntegrationProperties properties = new IntegrationProperties();
    properties.setApplications(Collections.singletonList(application));

    doReturn(properties).when(propertiesReader).getProperties();

    Set<String> integrationWhiteList = mockWHI.getIntegrationWhiteList();
    assertNotNull(integrationWhiteList);
    assertEquals(3, integrationWhiteList.size());

    assertTrue(integrationWhiteList.contains("squid-104-1.sc1.uc-inf.net"));
    assertTrue(integrationWhiteList.contains("165.254.226.119"));
    assertTrue(integrationWhiteList.contains("107.23.104.115"));
  }

  public static final class SendMessageAnswer implements Answer<V2MessageList> {

    private String message;

    private int count;

    @Override
    public V2MessageList answer(InvocationOnMock invocationOnMock) throws Throwable {
      List<String> streams = (List<String>) invocationOnMock.getArguments()[2];
      this.message = (String) invocationOnMock.getArguments()[3];
      this.count = streams.size();
      return new V2MessageList();
    }

    public String getMessage() {
      return message;
    }

    public int getCount() {
      return count;
    }

  }

}
