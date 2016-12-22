package org.symphonyoss.integration.core;

import static org.junit.Assert.assertEquals;
import static org.mockito.Matchers.anyString;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.mock;

import com.symphony.atlas.AtlasException;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;
import org.symphonyoss.integration.BaseIntegration;
import org.symphonyoss.integration.IntegrationAtlas;
import org.symphonyoss.integration.IntegrationPropertiesReader;
import org.symphonyoss.integration.authentication.AuthenticationProxy;
import org.symphonyoss.integration.exception.bootstrap.CertificateNotFoundException;
import org.symphonyoss.integration.exception.bootstrap.LoadKeyStoreException;
import org.symphonyoss.integration.model.Application;
import org.symphonyoss.integration.model.IntegrationBridge;
import org.symphonyoss.integration.model.IntegrationProperties;
import org.symphonyoss.integration.model.healthcheck.IntegrationFlags;
import org.symphonyoss.integration.model.healthcheck.IntegrationHealth;

import java.io.IOException;
import java.security.KeyStoreException;
import java.security.NoSuchAlgorithmException;
import java.security.cert.CertificateException;
import java.util.Collections;

import javax.ws.rs.client.Client;
import javax.ws.rs.client.Invocation;
import javax.ws.rs.client.WebTarget;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

/**
 * Test class to validate {@link BaseIntegration}
 * Created by rsanchez on 22/11/16.
 */
@RunWith(MockitoJUnitRunner.class)
public class BaseIntegrationTest extends CommonIntegrationTest {

  private static final String APP_ID = "jira";

  private static final String APP_TYPE = "jiraWebHookIntegration";

  private static final String MOCK_HOST = "test.symphony.com";

  private static final String MOCK_CONTEXT = "jira";

  @Mock
  private IntegrationAtlas integrationAtlas;

  @Mock
  private AuthenticationProxy authenticationProxy;

  @Mock
  private IntegrationPropertiesReader propertiesReader;

  @Mock
  private Client client;

  @InjectMocks
  private BaseIntegration integration = new NullIntegration(integrationAtlas, authenticationProxy);

  @Before
  public void init() {
    doReturn(atlas).when(integrationAtlas).getAtlas();
  }

  @Test
  public void testApplicationId() {
    IntegrationProperties properties = new IntegrationProperties();
    doReturn(properties).when(propertiesReader).getProperties();

    assertEquals(APP_TYPE, integration.getApplicationId(APP_TYPE));

    Application application = new Application();
    application.setType(APP_TYPE);
    properties.setApplications(Collections.singletonList(application));

    assertEquals(APP_TYPE, integration.getApplicationId(APP_TYPE));

    application.setId(APP_ID);
    assertEquals(APP_ID, integration.getApplicationId(APP_TYPE));
  }

  @Test(expected = CertificateNotFoundException.class)
  public void testRegisterUserCertNotFound() {
    integration.registerUser(APP_TYPE);
  }

  @Test(expected = LoadKeyStoreException.class)
  public void testRegisterUserLoadKeystoreException() throws AtlasException, IOException {
    mockCertDir();
    integration.registerUser(APP_TYPE);
  }

  @Test
  public void testRegisterUser()
      throws AtlasException, IOException, KeyStoreException, CertificateException,
      NoSuchAlgorithmException {
    mockCertDir();
    mockKeyStore();

    integration.registerUser(APP_TYPE);

    IntegrationHealth health = integration.getHealthManager().getHealth();
    assertEquals(IntegrationFlags.ValueEnum.OK, health.getFlags().getCertificateInstalled());
  }

  @Test
  public void testConfiguratorInstalledFlag() {
    IntegrationProperties properties = new IntegrationProperties();
    doReturn(properties).when(propertiesReader).getProperties();

    Application application = new Application();
    application.setType(APP_TYPE);
    properties.setApplications(Collections.singletonList(application));

    assertEquals(IntegrationFlags.ValueEnum.NOK,
        integration.getConfiguratorInstalledFlag(APP_TYPE));

    IntegrationBridge bridge = new IntegrationBridge();
    properties.setIntegrationBridge(bridge);

    assertEquals(IntegrationFlags.ValueEnum.NOK,
        integration.getConfiguratorInstalledFlag(APP_TYPE));

    bridge.setHost(MOCK_HOST);
    assertEquals(IntegrationFlags.ValueEnum.NOK,
        integration.getConfiguratorInstalledFlag(APP_TYPE));

    application.setContext(MOCK_CONTEXT);
    assertEquals(IntegrationFlags.ValueEnum.NOK,
        integration.getConfiguratorInstalledFlag(APP_TYPE));
  }

  @Test
  public void testConfiguratorInstalledFlagNOK() {
    testConfiguratorInstalledFlag();

    Invocation.Builder builder = mockRequest();

    doReturn(Response.status(Response.Status.NOT_FOUND).build()).when(builder).get();
    assertEquals(IntegrationFlags.ValueEnum.NOK,
        integration.getConfiguratorInstalledFlag(APP_TYPE));
  }

  @Test
  public void testConfiguratorInstalledFlagOK() {
    testConfiguratorInstalledFlag();

    Invocation.Builder builder = mockRequest();

    doReturn(Response.status(Response.Status.OK).build()).when(builder).get();
    assertEquals(IntegrationFlags.ValueEnum.OK,
        integration.getConfiguratorInstalledFlag(APP_TYPE));
  }

  private Invocation.Builder mockRequest() {
    Invocation.Builder builder = mock(Invocation.Builder.class);
    WebTarget target = mock(WebTarget.class);

    doReturn(target).when(client).target("https://" + MOCK_HOST);
    doReturn(target).when(target).path(anyString());
    doReturn(builder).when(target).request();
    doReturn(builder).when(builder).accept(MediaType.TEXT_HTML_TYPE);
    return builder;
  }

}
