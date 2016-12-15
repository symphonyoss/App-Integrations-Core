package org.symphonyoss.integration.authentication;

import static com.symphony.logging.DistributedTracingUtils.TRACE_ID;
import static com.symphony.logging.DistributedTracingUtils.TRACE_ID_SIZE;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotEquals;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.fail;
import static org.mockito.Matchers.any;
import static org.mockito.Matchers.anyString;
import static org.mockito.Matchers.eq;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;

import com.symphony.api.pod.client.ApiException;
import com.symphony.api.pod.client.Pair;
import com.symphony.api.pod.client.TypeRef;
import com.symphony.logging.DistributedTracingUtils;

import org.apache.commons.lang3.RandomStringUtils;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.runners.MockitoJUnitRunner;
import org.slf4j.MDC;
import org.symphonyoss.integration.IntegrationAtlasException;
import org.symphonyoss.integration.authentication.exception.PodConnectivityException;
import org.symphonyoss.integration.authentication.exception.PodUrlNotFoundException;
import org.symphonyoss.integration.exception.RemoteApiException;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by ecarrenho on 8/26/16.
 */
@RunWith(MockitoJUnitRunner.class)
public class PodApiClientDecoratorTest extends ApiClientDecoratorTest {

  protected TypeRef<String> returnType;
  protected List<Pair> queryParams = new ArrayList<Pair>();

  @InjectMocks protected PodApiClientDecorator apiClientDecorator = new PodApiClientDecorator();

  @Before
  public void setup() {
    initialSetup();

    returnType = new TypeRef<String>() {};

    apiClientDecorator.init();
    MDC.clear();
  }

  @Test
  public void testBasicInvocation() throws ApiException {
    String respBody = apiClientDecorator.invokeAPI(PATH, "GET", queryParams, body, headerParams, formParams,
        ACCEPT, CONTENT_TYPE, authNames, returnType);

    assertEquals(respBody, RESPONSE_BODY);
    assertNull(headerParams.get(TRACE_ID));
  }

  @Test
  public void testBasicInvocationWithMDCSetup() throws ApiException {
    DistributedTracingUtils.setMDC();

    String respBody = apiClientDecorator.invokeAPI(PATH, "GET", queryParams, body, headerParams, formParams,
        ACCEPT, CONTENT_TYPE, authNames, returnType);

    assertEquals(respBody, RESPONSE_BODY);
    assertEquals(MDC.get(TRACE_ID), headerParams.get(TRACE_ID));
  }

  @Test
  public void testBasicInvocationWithTraceAlreadyInHeader() throws ApiException {
    // emulates what WebHookTracingFilter does.
    String randHeaderTraceId = RandomStringUtils.randomAlphanumeric(TRACE_ID_SIZE);
    headerParams.put(TRACE_ID, randHeaderTraceId);
    DistributedTracingUtils.setMDC(randHeaderTraceId);

    String respBody =
        apiClientDecorator.invokeAPI(PATH, "GET", queryParams, body, headerParams, formParams,
            ACCEPT, CONTENT_TYPE, authNames, returnType);

    assertEquals(respBody, RESPONSE_BODY);
    // must be overwritten by what is currently on MDC.
    assertNotEquals(randHeaderTraceId, headerParams.get(TRACE_ID));
    // current MDC is a composition of the original header trace ID and a random generated number.
    assertEquals(MDC.get(TRACE_ID), headerParams.get(TRACE_ID));
  }

  @Test
  public void testSuccessfulReAuthorization() throws ApiException, RemoteApiException {

    successfulReAuthSetup();

    String respBody =
        apiClientDecorator.invokeAPI(PATH, "GET", queryParams, body, headerParams, formParams,
            ACCEPT, CONTENT_TYPE, authNames, returnType);

    verify(authenticationProxy, times(1))
        .reAuthSessionOrThrow(eq(SESSION_TOKEN), eq(UNAUTHORIZED), any(ApiException.class));
    assertEquals(respBody, RESPONSE_BODY);
    assertEquals(headerParams.get("sessionToken"), SESSION_TOKEN2);
  }

  @Test
  public void testFailedReAuthorization() throws RemoteApiException {

    failedReAuthSetup();

    doThrow(new RemoteApiException(UNAUTHORIZED, new ApiException()))
        .when(authenticationProxy)
        .reAuthSessionOrThrow(eq(SESSION_TOKEN), eq(UNAUTHORIZED), any(ApiException.class));

    try {
      apiClientDecorator.invokeAPI(PATH, "GET", queryParams, body, headerParams, formParams,
          ACCEPT, CONTENT_TYPE, authNames, returnType);
      fail();
    } catch (ApiException e) {
      verify(authenticationProxy, times(1))
          .reAuthSessionOrThrow(eq(SESSION_TOKEN), eq(UNAUTHORIZED), any(ApiException.class));
    }

  }

  @Test(expected = PodUrlNotFoundException.class)
  public void testPodUrlNotFoundException() throws RemoteApiException {

    doThrow(new IntegrationAtlasException("No URL configured for"))
        .when(integrationAtlas).getRequiredUrl(anyString());

    apiClientDecorator.init();

  }

  @Test
  public void testFailedReAuthorizationDueServerError() throws ApiException, RemoteApiException {

    failedReAuthDueServerErrorSetup();

    doThrow(new RemoteApiException(INTERNAL_SERVER_ERROR, new ApiException()))
        .when(authenticationProxy)
        .reAuthSessionOrThrow(eq(SESSION_TOKEN), eq(INTERNAL_SERVER_ERROR),
            any(ApiException.class));

    try {
      apiClientDecorator.invokeAPI(PATH, "GET", queryParams, body, headerParams, formParams,
          ACCEPT, CONTENT_TYPE, authNames, returnType);
      fail();
    } catch (ApiException e) {
      verify(authenticationProxy, times(1))
          .reAuthSessionOrThrow(eq(SESSION_TOKEN), eq(INTERNAL_SERVER_ERROR),
              any(ApiException.class));
    }
  }

  @Test(expected = PodConnectivityException.class)
  public void testFailedDueForbidden() throws ApiException {
    failedTimeout();
    apiClientDecorator.invokeAPI(PATH, "GET", queryParams, body, headerParams, formParams,
        ACCEPT, CONTENT_TYPE, authNames, returnType);
  }
}
