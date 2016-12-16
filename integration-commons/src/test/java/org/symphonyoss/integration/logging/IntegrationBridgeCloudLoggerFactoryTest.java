package org.symphonyoss.integration.logging;


import static com.symphony.atlas.config.SymphonyAtlas.ACCOUNT;
import static com.symphony.atlas.config.SymphonyAtlas.SECRET;
import static com.symphony.atlas.config.SymphonyAtlas.SYMPHONY_URL;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;
import static org.mockito.Mockito.any;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.powermock.api.mockito.PowerMockito.mock;
import static org.powermock.api.mockito.PowerMockito.mockStatic;
import static org.powermock.api.mockito.PowerMockito.when;
import static org.powermock.api.mockito.PowerMockito.whenNew;

import com.symphony.security.clientsdk.client.Auth;
import com.symphony.security.clientsdk.client.AuthProvider;
import com.symphony.security.clientsdk.client.ClientIdentifierFilter;
import com.symphony.security.clientsdk.client.SymphonyClient;
import com.symphony.security.clientsdk.client.SymphonyClientConfig;
import com.symphony.security.clientsdk.client.impl.SymphonyClientFactory;

import org.apache.commons.lang3.StringUtils;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.powermock.core.classloader.annotations.PowerMockIgnore;
import org.powermock.core.classloader.annotations.PrepareForTest;
import org.powermock.modules.junit4.PowerMockRunner;

import java.util.Properties;
import java.util.concurrent.AbstractExecutorService;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.FutureTask;

/**
 * Unit test for {@link IntegrationBridgeCloudLoggerFactory}
 *
 * Created by cmarcondes on 12/7/16.
 */
@RunWith(PowerMockRunner.class)
@PrepareForTest(
    {IntegrationBridgeCloudLoggerFactory.class, SymphonyClientFactory.class, Executors.class})
@PowerMockIgnore({"javax.management.*"})
public class IntegrationBridgeCloudLoggerFactoryTest {

  private FutureTask future;

  /**
   * Mocks the constructor of {@link IBProperties}, the getClient method of {@link
   * SymphonyClientFactory}
   * and the method submit of {@link ExecutorService}
   * @throws Exception
   */
  @Before
  public void setup() throws Exception {
    mockIBProperties();
    mockClientFactory();
    future = mockThreadExecutor();
  }

  /**
   * Tests the method getSessionKey, simulating multi-threads. It's going to follow this steps:
   *
   * The test will call the method getSessionKey four times.
   * - First time: Sessionkey and AuthenticationFuture will be null, so it's going to start a new
   * thread.
   * - Second time: Sessionkey is null, the thread started above has not finished yet, so
   * AuthenticationFuture.isClosed returns false, and won't start a new thread.
   * - Third time: The thread created at first time finished, AuthenticationFuture.isClosed returns
   * true, but the Sessionkey still null, so will create a new thread.
   * - Fourth time: The thread created above fineshed and the Sessionkey was filled
   * @throws Exception
   */
  @Test
  public void test() throws Exception {
    AuthProvider authProviderMocked = mock(AuthProvider.class);
    Auth authMocked = mock(Auth.class);

    /*
     * Mocks the number of times that the getSession will return null, until returns a valid value.
     */
    when(authMocked.getSession())
        .thenReturn(null)
        .thenReturn(null)
        .thenReturn(null)
        .thenReturn("45123138714312");

    when(authProviderMocked.getSymphonyAuth()).thenReturn(authMocked);

    whenNew(AuthProvider.class).withAnyArguments().thenReturn(authProviderMocked);

    IntegrationBridgeCloudLoggerFactory instace = new IntegrationBridgeCloudLoggerFactory();

    assertTrue(StringUtils.isEmpty(instace.getSessionKey()));
    assertTrue(StringUtils.isEmpty(instace.getSessionKey()));
    assertTrue(StringUtils.isEmpty(instace.getSessionKey()));
    verify(future, times(2)).isDone();
    assertEquals("45123138714312", instace.getSessionKey());

  }

  private FutureTask mockThreadExecutor() {
    ExecutorService executorMocked = mock(AbstractExecutorService.class);

    FutureTask future = mock(FutureTask.class);

    //returns false for the first call and true the next.

    when(future.isDone()).thenReturn(false).thenReturn(true);

    when(executorMocked.submit(any(Runnable.class))).thenReturn(future);

    mockStatic(Executors.class);
    when(Executors.newSingleThreadExecutor()).thenReturn(executorMocked);
    return future;
  }

  private void mockClientFactory() {
    mockStatic(SymphonyClientFactory.class);
    when(SymphonyClientFactory.getClient(any(ClientIdentifierFilter.class), any
        (SymphonyClientConfig.class))).thenReturn(mock(SymphonyClient.class));
  }

  private void mockIBProperties() throws Exception {
    IBProperties ibProperties = mock(IBProperties.class);

    Properties properties = new Properties();
    properties.put(SECRET, "/uM9z6JeaGIA85JN9vtrPYVYzeyMArgxZNNGGkrXqCE=");
    properties.put(ACCOUNT, "cloudlogger");
    properties.put(SYMPHONY_URL, "https://nexus.symphony.com:443");

    when(ibProperties.getProperties()).thenReturn(properties);

    whenNew(IBProperties.class).withAnyArguments().thenReturn(ibProperties);
  }


}
