/*
 * Sonar JIRA Plugin
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

import com.atlassian.jira.rpc.soap.client.RemoteIssue;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;
import org.sonar.api.config.Settings;
import org.sonar.api.issue.Issue;
import org.sonar.api.issue.action.Function;
import org.sonar.api.issue.internal.DefaultIssue;
import org.sonar.plugins.jira.JiraConstants;

import java.rmi.RemoteException;
import java.util.HashMap;

import static org.fest.assertions.Assertions.assertThat;
import static org.mockito.Matchers.anyString;
import static org.mockito.Mockito.*;

public class LinkFunctionTest {

  @Rule
  public ExpectedException thrown = ExpectedException.none();
  private LinkFunction function;
  private JiraIssueCreator jiraIssueCreator;
  private Issue sonarIssue;
  private Function.Context context;
  private RemoteIssue remoteIssue;
  private Settings settings;

  @Before
  public void init() throws Exception {
    sonarIssue = new DefaultIssue().setKey("ABCD");
    settings = new Settings();

    context = mock(Function.Context.class);
    when(context.issue()).thenReturn(sonarIssue);
    when(context.projectSettings()).thenReturn(settings);

    jiraIssueCreator = mock(JiraIssueCreator.class);
    remoteIssue = new RemoteIssue();
    remoteIssue.setKey("FOO-15");
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
    settings.appendProperty(JiraConstants.SERVER_URL_PROPERTY, "http://my.jira.server");

    function.createComment(remoteIssue, context);

    verify(context).addComment("Issue linked to JIRA issue: http://my.jira.server/browse/FOO-15");
  }

  @Test
  public void test_generate_comment_text() throws Exception {
    settings.appendProperty(JiraConstants.SERVER_URL_PROPERTY, "http://my.jira.server");

    String commentText = function.generateCommentText(remoteIssue, context);
    assertThat(commentText).isEqualTo("Issue linked to JIRA issue: http://my.jira.server/browse/FOO-15");
  }

}
