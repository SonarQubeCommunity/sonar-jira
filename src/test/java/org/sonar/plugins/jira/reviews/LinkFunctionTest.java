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

import static org.fest.assertions.Assertions.assertThat;
import static org.mockito.Matchers.anyString;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.rmi.RemoteException;

import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;
import org.sonar.api.config.Settings;
import org.sonar.api.issue.Issue;
import org.sonar.api.issue.action.Function;
import org.sonar.api.issue.internal.DefaultIssue;
import org.sonar.plugins.jira.JiraConstants;

import com.atlassian.jira.rest.client.api.domain.BasicIssue;

public class LinkFunctionTest {

  @Rule
  public ExpectedException thrown = ExpectedException.none();
  private LinkFunction function;
  private JiraIssueCreator jiraIssueCreator;
  private Issue sonarIssue;
  private Function.Context context;
  private BasicIssue remoteIssue;
  private Settings settings;

  @Before
  public void init() throws Exception {
    sonarIssue = new DefaultIssue().setKey("ABCD");
    settings = new Settings();

    context = mock(Function.Context.class);
    when(context.issue()).thenReturn(sonarIssue);
    when(context.projectSettings()).thenReturn(settings);

    jiraIssueCreator = mock(JiraIssueCreator.class);
    remoteIssue = new BasicIssue(null, "FOO-15", 1l);
    when(jiraIssueCreator.createIssue(sonarIssue, settings)).thenReturn(remoteIssue);

    function = new LinkFunction(jiraIssueCreator);
  }

  @Test
  public void should_execute() throws Exception {
    function.createJiraIssue(context);

    verify(jiraIssueCreator).createIssue(sonarIssue, settings);
    verify(context).addComment(anyString());
    verify(context).setAttribute(JiraConstants.SONAR_ISSUE_DATA_PROPERTY_KEY, "FOO-15");
  }

  @Test
  public void should_fail_execute_if_remote_problem() throws Exception {
    when(jiraIssueCreator.createIssue(sonarIssue, settings)).thenThrow(new RemoteException("Server Error"));

    thrown.expect(IllegalStateException.class);
    thrown.expectMessage("Impossible to create an issue on JIRA. A problem occured with the remote server: Server Error");

    function.createJiraIssue(context);
  }

  @Test
  public void test_create_comment() throws Exception {
    settings.setProperty(JiraConstants.SERVER_URL_PROPERTY, "http://my.jira.server");

    function.createComment(remoteIssue, context);

    verify(context).addComment("Issue linked to JIRA issue: http://my.jira.server/browse/FOO-15");
  }

  @Test
  public void test_generate_comment_text() throws Exception {
    settings.setProperty(JiraConstants.SERVER_URL_PROPERTY, "http://my.jira.server");

    String commentText = function.generateCommentText(remoteIssue, context);
    assertThat(commentText).isEqualTo("Issue linked to JIRA issue: http://my.jira.server/browse/FOO-15");
  }

  @Test
  public void should_check_settings() {
    settings.setProperty(JiraConstants.SERVER_URL_PROPERTY, "http://my.jira.server");
    settings.setProperty(JiraConstants.SOAP_BASE_URL_PROPERTY, "/rpc/soap/jirasoapservice-v2");
    settings.setProperty(JiraConstants.USERNAME_PROPERTY, "john");
    settings.setProperty(JiraConstants.PASSWORD_PROPERTY, "1234");
    settings.setProperty(JiraConstants.JIRA_PROJECT_KEY_PROPERTY, "SONAR");
    settings.setProperty(JiraConstants.JIRA_INFO_PRIORITY_ID, 5);
    settings.setProperty(JiraConstants.JIRA_MINOR_PRIORITY_ID, 4);
    settings.setProperty(JiraConstants.JIRA_MAJOR_PRIORITY_ID, 3);
    settings.setProperty(JiraConstants.JIRA_CRITICAL_PRIORITY_ID, 2);
    settings.setProperty(JiraConstants.JIRA_BLOCKER_PRIORITY_ID, 1);
    settings.setProperty(JiraConstants.JIRA_ISSUE_TYPE_ID, 3);
    settings.setProperty(JiraConstants.JIRA_ISSUE_COMPONENT_ID, 18);

    function.checkConditions(settings);
  }

  @Test
  public void should_fail_if_settings_is_empty() {
    try {
      function.checkConditions(settings);
    } catch (Exception e) {
      assertThat(e).isInstanceOf(IllegalStateException.class);
    }
  }

}
