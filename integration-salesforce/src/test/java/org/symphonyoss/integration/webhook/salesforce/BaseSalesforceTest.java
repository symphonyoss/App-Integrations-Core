package org.symphonyoss.integration.webhook.salesforce;

import org.apache.commons.io.FileUtils;
import org.symphonyoss.integration.entity.model.User;

import java.io.File;
import java.io.IOException;

/**
 * Created by cmarcondes on 11/3/16.
 */
public class BaseSalesforceTest {

  protected User createUser(String username, String emailAddress, String displayName, Long id) {
    User user = new User();
    user.setUserName(username);
    user.setEmailAddress(emailAddress);
    user.setDisplayName(displayName);
    user.setId(id);

    return user;
  }

  protected String readFile(String fileName) throws IOException {
    ClassLoader classLoader = getClass().getClassLoader();
    String expected =
        FileUtils.readFileToString(new File(classLoader.getResource(fileName).getPath()));
    return expected = expected.replaceAll("\n", "");
  }
}
