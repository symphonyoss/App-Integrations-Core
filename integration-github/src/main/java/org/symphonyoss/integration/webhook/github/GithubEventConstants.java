package org.symphonyoss.integration.webhook.github;

/**
 * Groups all handled events from GitHub.
 *
 * Created by Milton Quilzini on 08/09/16.
 */
public final class GithubEventConstants {

  /** Prevents initialization. */
  private GithubEventConstants() {
  }

  public static final String GITHUB_HEADER_EVENT_NAME = "x-github-event";

  public static final String GITHUB_EVENT_PUSH = "push";

  public static final String GITHUB_EVENT_PULL_REQUEST = "pull_request";

  public static final String GITHUB_EVENT_PULL_REQUEST_REVIEW_COMMENT = "pull_request_review_comment";

  public static final String GITHUB_EVENT_DEPLOYMENT = "deployment";

  public static final String GITHUB_EVENT_DEPLOYMENT_STATUS = "deployment_status";

  public static final String GITHUB_EVENT_COMMIT_COMMENT = "commit_comment";

  public static final String GITHUB_EVENT_ISSUE_COMMENT = "issue_comment";

  public static final String GITHUB_EVENT_PUBLIC = "public";

  public static final String GITHUB_EVENT_RELEASE = "release";

  public static final String GITHUB_EVENT_STATUS = "status";

  public static final String CREATE = "create";

  public static final String EDITED = "edited";

  public static final String DELETED = "deleted";
}
