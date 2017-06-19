package org.symphonyoss.integration.provisioning;

import static junit.framework.TestCase.assertFalse;
import static org.mockito.Mockito.doReturn;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.springframework.test.context.junit4.SpringRunner;
import org.symphonyoss.integration.model.yaml.AdminUser;
import org.symphonyoss.integration.model.yaml.IntegrationBridge;
import org.symphonyoss.integration.model.yaml.IntegrationProperties;

/**
 * Unit test to validate {@link IntegrationProvisioningApp}
 * Created by crepache on 15/06/17.
 */
@RunWith(SpringRunner.class)
public class IntegrationProvisioningAppTest {

  @InjectMocks
  private IntegrationProvisioningApp app;

  @Mock
  IntegrationProperties properties;

  @Test
  public void testFailExecute() {
    doReturn(mockAdminUser()).when(properties).getAdminUser();
    doReturn(mockIntegrationBridge()).when(properties).getIntegrationBridge();
    assertFalse(app.execute());
  }

  private AdminUser mockAdminUser() {
    AdminUser adminUser = new AdminUser();
    adminUser.setKeystoreFile("keystoreFile");
    adminUser.setKeystorePassword("keystorePassword");
    return adminUser;
  }

  private IntegrationBridge mockIntegrationBridge() {
    IntegrationBridge ib = new IntegrationBridge();
    ib.setTruststoreFile("truststoreFile");
    ib.setTruststoreType("truststoreType");
    ib.setTruststorePassword("truststorePassword");
    return ib;
  }
}
