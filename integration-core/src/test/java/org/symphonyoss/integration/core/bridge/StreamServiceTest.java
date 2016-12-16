package org.symphonyoss.integration.core.bridge;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;
import static org.mockito.Matchers.any;
import static org.mockito.Matchers.anyString;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import com.symphony.api.agent.api.MessagesApi;
import com.symphony.api.agent.client.ApiException;
import com.symphony.api.agent.model.V2Message;
import com.symphony.api.agent.model.V2MessageSubmission;
import com.symphony.api.pod.api.StreamsApi;
import com.symphony.api.pod.model.ConfigurationInstance;
import com.symphony.api.pod.model.Stream;
import com.symphony.api.pod.model.UserIdList;
import com.symphony.api.pod.model.V2RoomDetail;
import com.symphony.atlas.IAtlas;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;
import org.symphonyoss.integration.IntegrationAtlas;
import org.symphonyoss.integration.authentication.AuthenticationProxy;
import org.symphonyoss.integration.authentication.AuthenticationToken;
import org.symphonyoss.integration.config.WebHookConfigurationUtils.StreamType;

import java.util.List;

/**
 * Test class responsible to test the flows in the Stream Service.
 *
 * Created by rsanchez on 13/05/16.
 */
@RunWith(MockitoJUnitRunner.class)
public class StreamServiceTest {

  private static final String INTEGRATION_USER = "jirawebhook";

  private static final String STREAM = "stream1";

  private static final Long USER_ID = new Long(268745369);

  @Mock
  private AuthenticationProxy authenticationProxy;

  @Mock
  private MessagesApi messagesApi;

  @Mock
  private StreamsApi streamsApi;

  @Mock
  private IntegrationAtlas integrationAtlas;

  @InjectMocks
  private StreamServiceImpl streamService = new StreamServiceImpl();

  @Before
  public void setup() {
    IAtlas atlas = mock(IAtlas.class);
    when(integrationAtlas.getAtlas()).thenReturn(atlas);
  }

  @Test
  public void testGetStreamsEmpty() {
    ConfigurationInstance instance = mockInstance();
    instance.setOptionalProperties("");

    List<String> streams = streamService.getStreams(instance);
    assertNotNull(streams);
    assertTrue(streams.isEmpty());

    streams = streamService.getStreams("");
    assertNotNull(streams);
    assertTrue(streams.isEmpty());
  }

  @Test
  public void testGetStreams() {
    String optionalProperties = "{ \"streams\": [ \"stream1\", \"stream2\"] }";
    ConfigurationInstance instance = mockInstance();
    instance.setOptionalProperties(optionalProperties);

    List<String> streams = streamService.getStreams(instance);
    assertNotNull(streams);
    assertEquals(2, streams.size());
    assertEquals("stream1", streams.get(0));
    assertEquals("stream2", streams.get(1));

    streams = streamService.getStreams(optionalProperties);
    assertNotNull(streams);
    assertEquals(2, streams.size());
    assertEquals("stream1", streams.get(0));
    assertEquals("stream2", streams.get(1));
  }

  @Test
  public void testGetInvalidStreamType() {
    ConfigurationInstance instance = mockInstance();
    instance.setOptionalProperties("");

    StreamType streamType = streamService.getStreamType(instance);
    assertEquals(StreamType.NONE, streamType);

    instance.setOptionalProperties("{ \"streamType\": \"TEST\" }");
    streamType = streamService.getStreamType(instance);
    assertEquals(StreamType.NONE, streamType);
  }

  @Test
  public void testGetStreamType() {
    ConfigurationInstance instance = mockInstance();
    instance.setOptionalProperties("{ \"streamType\": \"IM\" }");

    StreamType streamType = streamService.getStreamType(instance);
    assertEquals(StreamType.IM, streamType);

    instance.setOptionalProperties("{ \"streamType\": \"CHATROOM\" }");
    streamType = streamService.getStreamType(instance);
    assertEquals(StreamType.CHATROOM, streamType);
  }

  private ConfigurationInstance mockInstance() {
    ConfigurationInstance instance = new ConfigurationInstance();
    instance.setConfigurationId("57756bca4b54433738037005");
    instance.setInstanceId("1234");

    return instance;
  }

  @Test(expected = ApiException.class)
  public void testPostMessageApiException() throws ApiException {
    when(authenticationProxy.isAuthenticated(INTEGRATION_USER)).thenReturn(true);
    when(authenticationProxy.getToken(INTEGRATION_USER)).thenReturn(
        AuthenticationToken.VOID_AUTH_TOKEN);
    when(messagesApi.v2StreamSidMessageCreatePost(anyString(), anyString(), anyString(),
        any(V2MessageSubmission.class))).thenThrow(ApiException.class);

    streamService.postMessage(INTEGRATION_USER, STREAM, new V2MessageSubmission());
  }

  @Test
  public void testPostMessageSuccessfully() throws ApiException {
    V2Message message = new V2Message();
    when(authenticationProxy.isAuthenticated(INTEGRATION_USER)).thenReturn(true);
    when(authenticationProxy.getToken(INTEGRATION_USER)).thenReturn(
        AuthenticationToken.VOID_AUTH_TOKEN);
    when(messagesApi.v2StreamSidMessageCreatePost(anyString(), anyString(), anyString(),
        any(V2MessageSubmission.class))).thenReturn(message);

    V2Message result =
        streamService.postMessage(INTEGRATION_USER, STREAM, new V2MessageSubmission());
    assertEquals(message, result);
  }

  @Test(expected = com.symphony.api.pod.client.ApiException.class)
  public void testGetRoomInfoApiException() throws com.symphony.api.pod.client.ApiException {
    when(authenticationProxy.isAuthenticated(INTEGRATION_USER)).thenReturn(true);
    when(authenticationProxy.getToken(INTEGRATION_USER)).thenReturn(
        AuthenticationToken.VOID_AUTH_TOKEN);
    when(streamsApi.v2RoomIdInfoGet(anyString(), anyString())).thenThrow(
        com.symphony.api.pod.client.ApiException.class);

    streamService.getRoomInfo(INTEGRATION_USER, STREAM);
  }

  @Test
  public void testGetRoomInfoSuccessfully() throws com.symphony.api.pod.client.ApiException {
    V2RoomDetail detail = new V2RoomDetail();
    when(authenticationProxy.isAuthenticated(INTEGRATION_USER)).thenReturn(true);
    when(authenticationProxy.getToken(INTEGRATION_USER)).thenReturn(
        AuthenticationToken.VOID_AUTH_TOKEN);
    when(streamsApi.v2RoomIdInfoGet(anyString(), anyString())).thenReturn(detail);

    V2RoomDetail result = streamService.getRoomInfo(INTEGRATION_USER, STREAM);
    assertEquals(detail, result);
  }

  @Test(expected = com.symphony.api.pod.client.ApiException.class)
  public void testCreateIMApiException() throws com.symphony.api.pod.client.ApiException {
    when(authenticationProxy.isAuthenticated(INTEGRATION_USER)).thenReturn(true);
    when(authenticationProxy.getToken(INTEGRATION_USER)).thenReturn(
        AuthenticationToken.VOID_AUTH_TOKEN);
    when(streamsApi.v1ImCreatePost(any(UserIdList.class), anyString())).thenThrow(
        com.symphony.api.pod.client.ApiException.class);

    streamService.createIM(INTEGRATION_USER, USER_ID);
  }

  @Test
  public void testCreateIMSuccessfully() throws com.symphony.api.pod.client.ApiException {
    Stream stream = new Stream();
    when(authenticationProxy.isAuthenticated(INTEGRATION_USER)).thenReturn(true);
    when(authenticationProxy.getToken(INTEGRATION_USER)).thenReturn(
        AuthenticationToken.VOID_AUTH_TOKEN);
    when(streamsApi.v1ImCreatePost(any(UserIdList.class), anyString())).thenReturn(stream);

    Stream result = streamService.createIM(INTEGRATION_USER, USER_ID);
    assertEquals(stream, result);
  }
}
