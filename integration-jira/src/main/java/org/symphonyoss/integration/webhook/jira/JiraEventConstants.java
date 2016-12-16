package org.symphonyoss.integration.webhook.jira;

/**
 * All mapped JIRA events.
 *
 * Created by mquilzini on 16/05/16.
 */
public class JiraEventConstants {

  public static final String WEBHOOK_EVENT = "webhookEvent";
  public static final String ISSUE_EVENT_TYPE_NAME = "issue_event_type_name";

  public static final String USER_KEY_PARAMETER = "user_key";

  public final static String JIRA_ISSUE_CREATED = "jira:issue_created";
  public final static String JIRA_ISSUE_DELETED = "jira:issue_deleted";
  public final static String JIRA_ISSUE_UPDATED = "jira:issue_updated";

  public final static String JIRA_WORKLOG_UPDATED = "jira:worklog_updated";

  public final static String WORKLOG_CREATED = "worklog_created";
  public final static String WORKLOG_UPDATED = "worklog_updated";
  public final static String WORKLOG_DELETED = "worklog_deleted";

  public final static String JIRA_ISSUE_COMMENTED = "issue_commented";
  public final static String JIRA_ISSUE_COMMENT_EDITED = "issue_comment_edited";
  public final static String JIRA_ISSUE_COMMENT_DELETED = "issue_comment_deleted";

  public final static String JIRA_COMMENT_ADDED = "comment_created";
  public final static String JIRA_COMMENT_UPDATED = "comment_updated";
  public final static String JIRA_COMMENT_DELETED = "comment_deleted";

  public final static String JIRA_SPRINT_STARTED = "sprint_started";
  public final static String JIRA_SPRINT_CREATED = "sprint_created";
  public final static String JIRA_SPRINT_CLOSED = "sprint_closed";
  public final static String JIRA_SPRINT_UPDATED = "sprint_updated";
  public final static String JIRA_SPRINT_DELETED = "sprint_deleted";

  public final static String JIRA_VERSION_CREATED = "jira:version_created";
  public final static String JIRA_VERSION_UPDATED = "jira:version_updated";
  public final static String JIRA_VERSION_MOVED = "jira:version_moved";
  public final static String JIRA_VERSION_DELETED = "jira:version_deleted";
  public final static String JIRA_VERSION_RELEASED = "jira:version_released";
  public final static String JIRA_VERSION_UNRELEASED = "jira:version_unreleased";

  public final static String PROJECT_CREATED = "project_created";
  public final static String PROJECT_UPDATED = "project_updated";
  public final static String PROJECT_DELETED = "project_deleted";

  public final static String BOARD_CREATED = "board_created";
  public final static String BOARD_UPDATED = "board_updated";
  public final static String BOARD_DELETED = "board_deleted";
  public final static String BOARD_CONFIGURATION_CHANGED = "board_configuration_changed";

  public final static String USER_CREATED = "user_created";
  public final static String USER_UPDATED = "user_updated";
  public final static String USER_DELETED = "user_deleted";

  public final static String OPTION_VOTING_CHANGED = "option_voting_changed";
  public final static String OPTION_WATCHING_CHANGED = "option_watching_changed";
  public final static String OPTION_ISSUELINKS_CHANGED = "option_issuelinks_changed";
  public final static String OPTION_TIMETRACKING_CHANGED = "option_timetracking_changed";
  public final static String OPTION_UNASSIGNED_ISSUES_CHANGED = "option_unassigned_issues_changed";

}
