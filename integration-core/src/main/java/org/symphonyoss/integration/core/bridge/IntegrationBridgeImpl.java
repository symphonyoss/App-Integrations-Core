package org.symphonyoss.integration.core.bridge;

import com.symphony.api.agent.client.ApiException;
import com.symphony.api.agent.model.V2Message;
import com.symphony.api.agent.model.V2MessageList;
import com.symphony.api.agent.model.V2MessageSubmission;
import com.symphony.api.pod.model.ConfigurationInstance;
import com.symphony.logging.ISymphonyLogger;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.symphonyoss.integration.Integration;
import org.symphonyoss.integration.authentication.AuthenticationProxy;
import org.symphonyoss.integration.exception.authentication.ConnectivityException;
import org.symphonyoss.integration.core.bootstrap.IntegrationBootstrapContext;
import org.symphonyoss.integration.exception.RemoteApiException;
import org.symphonyoss.integration.logging.IntegrationBridgeCloudLoggerFactory;
import org.symphonyoss.integration.service.IntegrationBridge;
import org.symphonyoss.integration.service.StreamService;

import java.util.List;

import javax.ws.rs.ProcessingException;

/**
 * See @{@link IntegrationBridge} for further details.
 */
@Component
public class IntegrationBridgeImpl implements IntegrationBridge {

  private static final ISymphonyLogger LOGGER =
      IntegrationBridgeCloudLoggerFactory.getLogger(IntegrationBridgeImpl.class);

  @Autowired
  private IntegrationBootstrapContext bootstrap;

  @Autowired
  private StreamService streamService;

  @Autowired
  private IntegrationBridgeExceptionHandler exceptionHandler;

  @Override
  public V2MessageList sendMessage(ConfigurationInstance instance, String integrationUser,
      String message) {
    V2MessageList result = new V2MessageList();
    List<String> streams = streamService.getStreams(instance);

    if (streams.isEmpty()) {
      LOGGER.info("No streams configured to instance {}", instance.getInstanceId());
      return result;
    }

    return sendMessage(instance, integrationUser, streams, message);
  }

  @Override
  public V2MessageList sendMessage(ConfigurationInstance instance, String integrationUser,
      List<String> streams, String message) {
    V2MessageList result = new V2MessageList();

    for (String stream : streams) {
      try {
        V2Message messageResponse = postMessageWithRetry(integrationUser, stream, message);
        result.add(messageResponse);
      } catch (RemoteApiException e) {
        exceptionHandler.handleRemoteApiException(e, instance, integrationUser, message, stream);
      } catch (ConnectivityException e) {
        throw e;
      } catch (ProcessingException e) {
        throw e;
      } catch (Exception e) {
        exceptionHandler.handleUnexpectedException(e);
      }
    }

    return result;
  }

  /**
   * Sends a message to a specific stream using {@link AuthenticationProxy}.
   * @param integrationUser the user of integration
   * @param stream the stream identifier.
   * @param message the actual message. It's expected to be already on proper format.
   * @return
   * @throws ApiException
   */
  private V2Message postMessageWithRetry(String integrationUser, String stream, String message)
      throws RemoteApiException {
    try {
      return postMessageToStream(integrationUser, stream, message);
    } catch (ApiException e) {
      LOGGER.error("Fail to post message", e);
      throw new RemoteApiException(e.getCode(), e);
    }
  }

  private V2Message postMessageToStream(String integrationUser, String stream, String message)
      throws ApiException {

    V2MessageSubmission messageSubmission = buildMessage(message);
    V2Message messageResponse =
        streamService.postMessage(integrationUser, stream, messageSubmission);
    LOGGER.info("Message posted to stream {} ", stream);

    return messageResponse;
  }


  /**
   * Build the Message Object
   * @param message Message text
   * @return
   */
  private V2MessageSubmission buildMessage(String message) {
    V2MessageSubmission messageSubmission = new V2MessageSubmission();
    messageSubmission.setFormat(V2MessageSubmission.FormatEnum.MESSAGEML);
    messageSubmission.setMessage(message);
    return messageSubmission;
  }

  @Override
  public Integration getIntegrationById(String integrationId) {
    return this.bootstrap.getIntegrationById(integrationId);
  }

  @Override
  public void removeIntegration(String integrationId) {
    this.bootstrap.removeIntegration(integrationId);
  }

}
