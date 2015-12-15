package org.sonar.plugins.jira;

import org.sonar.api.issue.Issue;

import com.atlassian.jira.rest.client.api.domain.BasicIssue;

public interface JiraService {

	BasicIssue createIssue(String authToken, Issue sonarIssue);

}
