/*
 * JIRA Plugin for SonarQube
 * Copyright (C) 2009 SonarSource
 * dev@sonar.codehaus.org
 *
 * This program is free software; you can redistribute it and/or
 * modify it under the terms of the GNU Lesser General Public
 * License as published by the Free Software Foundation; either
 * version 3 of the License, or (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
 * Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public
 * License along with this program; if not, write to the Free Software
 * Foundation, Inc., 51 Franklin Street, Fifth Floor, Boston, MA  02
 */
package org.sonar.plugins.jira.reviews;

import java.rmi.RemoteException;

import org.sonar.api.ServerExtension;
import org.sonar.api.config.Settings;
import org.sonar.api.issue.action.Function;
import org.sonar.plugins.jira.JiraConstants;

import com.atlassian.jira.rest.client.api.domain.BasicIssue;
import com.google.common.annotations.VisibleForTesting;

public class LinkFunction implements Function, ServerExtension {

  private final JiraIssueCreator jiraIssueCreator;

  public LinkFunction(JiraIssueCreator jiraIssueCreator) {
    this.jiraIssueCreator = jiraIssueCreator;
  }

  public void execute(Context context) {
    checkConditions(context.projectSettings());
    createJiraIssue(context);
  }

  protected void createJiraIssue(Context context) {
	BasicIssue issue;
    try {
      issue = jiraIssueCreator.createIssue(context.issue(), context.projectSettings());
    } catch (RemoteException e) {
      throw new IllegalStateException("Impossible to create an issue on JIRA. A problem occured with the remote server: " + e.getMessage(), e);
    }

    createComment(issue, context);
    // and add the property
    context.setAttribute(JiraConstants.SONAR_ISSUE_DATA_PROPERTY_KEY, issue.getKey());
  }

  @VisibleForTesting
  void checkConditions(Settings settings) {
    checkProperty(JiraConstants.SERVER_URL_PROPERTY, settings);
    checkProperty(JiraConstants.SOAP_BASE_URL_PROPERTY, settings);
    checkProperty(JiraConstants.USERNAME_PROPERTY, settings);
    checkProperty(JiraConstants.PASSWORD_PROPERTY, settings);
    checkProperty(JiraConstants.JIRA_PROJECT_KEY_PROPERTY, settings);
    checkProperty(JiraConstants.JIRA_INFO_PRIORITY_ID, settings);
    checkProperty(JiraConstants.JIRA_MINOR_PRIORITY_ID, settings);
    checkProperty(JiraConstants.JIRA_MAJOR_PRIORITY_ID, settings);
    checkProperty(JiraConstants.JIRA_CRITICAL_PRIORITY_ID, settings);
    checkProperty(JiraConstants.JIRA_BLOCKER_PRIORITY_ID, settings);
    checkProperty(JiraConstants.JIRA_ISSUE_TYPE_ID, settings);
    checkProperty(JiraConstants.JIRA_ISSUE_COMPONENT_ID, settings);
  }

  private void checkProperty(String property, Settings settings) {
    if (!settings.hasKey(property) && !settings.hasDefaultValue(property)) {
      throw new IllegalStateException("The JIRA property \"" + property + "\" must be defined before you can use the \"Link to Jira\" button");
    }
  }

  protected void createComment(BasicIssue issue, Context context) {
    context.addComment(generateCommentText(issue, context));
  }
  
  protected String generateCommentText(BasicIssue issue, Context context) {
    StringBuilder message = new StringBuilder();
    message.append("Issue linked to JIRA issue: ");
    message.append(context.projectSettings().getString(JiraConstants.SERVER_URL_PROPERTY));
    message.append("/browse/");
    message.append(issue.getKey());
    return message.toString();
  }

}
