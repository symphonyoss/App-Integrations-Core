package org.symphonyoss.integration.webhook.github;

import org.symphonyoss.integration.json.JsonUtils;

import com.fasterxml.jackson.databind.JsonNode;
import org.apache.commons.io.FileUtils;
import org.junit.Ignore;

import java.io.File;
import java.io.IOException;
import java.util.Scanner;

/**
 * Created by cmarcondes on 9/15/16.
 */
@Ignore("not a test per se")
public class CommonGithubTest {

  protected ClassLoader classLoader = getClass().getClassLoader();

  protected String readFile(String file) throws IOException {
    return FileUtils.readFileToString(new File(classLoader.getResource(file).getPath()))
        .replaceAll("\n", "");
  }

  protected String getExpectedMessageML(String expectedMessageFileName) {
    Scanner scan = new Scanner(
        classLoader.getResourceAsStream(expectedMessageFileName));
    StringBuilder expectedMessage = new StringBuilder();
    try {
      while (scan.hasNextLine()) {
        expectedMessage.append(scan.nextLine().trim());
      }
    } finally {
      scan.close();
    }
    return expectedMessage.toString();
  }

  protected JsonNode getJsonFile(String jsonFileName) throws IOException {
    return JsonUtils.readTree(classLoader.getResourceAsStream(jsonFileName));
  }
}
