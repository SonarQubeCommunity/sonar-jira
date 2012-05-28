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
import org.sonar.api.workflow.Comment;
import org.sonar.api.workflow.MutableReview;
import org.sonar.api.workflow.Review;
import org.sonar.api.workflow.WorkflowContext;
import org.sonar.plugins.jira.JiraConstants;

import java.rmi.RemoteException;
import java.util.HashMap;

import static org.hamcrest.Matchers.is;
import static org.junit.Assert.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

public class LinkFunctionTest {

  @Rule
  public ExpectedException thrown = ExpectedException.none();

  private LinkFunction action;
  private JiraIssueCreator jiraIssueCreator;
  private MutableReview mutableReview;
  private Comment comment;
  private Review review;
  private WorkflowContext workflowContext;
  private RemoteIssue remoteIssue;
  private Settings settings;

  @Before
  public void init() throws Exception {
    mutableReview = mock(MutableReview.class);
    comment = mock(Comment.class);
    when(mutableReview.createComment()).thenReturn(comment);
    review = mock(Review.class);
    workflowContext = mock(WorkflowContext.class);
    settings = new Settings();
    when(workflowContext.getProjectSettings()).thenReturn(settings);

    jiraIssueCreator = mock(JiraIssueCreator.class);
    remoteIssue = new RemoteIssue();
    remoteIssue.setKey("FOO-15");
    when(jiraIssueCreator.createIssue(review, settings, null)).thenReturn(remoteIssue);

    action = new LinkFunction(jiraIssueCreator);
  }

  @Test
  public void shouldExecute() throws Exception {
    action.doExecute(mutableReview, review, workflowContext, new HashMap<String, String>());

    verify(jiraIssueCreator).createIssue(review, settings, null);
    verify(mutableReview).createComment();
    verify(mutableReview).setProperty(JiraConstants.REVIEW_DATA_PROPERTY_KEY, "FOO-15");
  }

  @Test
  public void shouldFailExecuteIfRemoteProblem() throws Exception {
    when(jiraIssueCreator.createIssue(review, settings, null)).thenThrow(new RemoteException("Server Error"));

    thrown.expect(IllegalStateException.class);
    thrown.expectMessage("Impossible to create an issue on JIRA. A problem occured with the remote server: Server Error");

    action.doExecute(mutableReview, review, workflowContext, new HashMap<String, String>());
  }

  @Test
  public void testCreateComment() throws Exception {
    when(workflowContext.getUserId()).thenReturn(45L);
    settings.appendProperty(JiraConstants.SERVER_URL_PROPERTY, "http://my.jira.server");

    action.createComment(remoteIssue, mutableReview, workflowContext, new HashMap<String, String>());

    verify(comment).setUserId(45L);
    verify(comment).setMarkdownText("Review linked to JIRA issue: http://my.jira.server/browse/FOO-15");
  }

  @Test
  public void testGenerateCommentText() throws Exception {
    HashMap<String, String> params = new HashMap<String, String>();
    params.put("text", "Hello world");
    settings.appendProperty(JiraConstants.SERVER_URL_PROPERTY, "http://my.jira.server");

    String commentText = action.generateCommentText(remoteIssue, workflowContext, params);
    assertThat(commentText, is("Hello world\n\nReview linked to JIRA issue: http://my.jira.server/browse/FOO-15"));
  }

}
