package org.symphonyoss.integration.webhook.jira.parser;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.runners.MockitoJUnitRunner;
import org.symphonyoss.integration.webhook.exception.WebHookParseException;

import java.io.IOException;

/**
 * Unit tests for {@link CommentJiraParser}.
 *
 * Created by mquilzini on 18/05/16.
 */
@RunWith(MockitoJUnitRunner.class)
public class EmailAddressWithSpaceTest extends JiraParserTest {

  private static final String FILENAME =
      "jiraCallbackSampleEmailAddressWithSpace.json";

  @InjectMocks
  private CommentJiraParser commentJiraParser = new CommentJiraParser();

  private ObjectMapper mapper = new ObjectMapper();

  @Test
  public void testParseCommentAdded() throws WebHookParseException, IOException {
    ClassLoader classLoader = getClass().getClassLoader();
    JsonNode node = mapper.readTree(classLoader.getResourceAsStream(FILENAME));
    String expectedMessage = readFile("parser/commentJiraParser/commentAndEmailAddressWithSpace.xml");
    Assert.assertEquals(expectedMessage, this.commentJiraParser.parse(null, node));
  }

}
