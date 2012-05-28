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

import com.atlassian.jira.rpc.soap.client.JiraSoapService;
import com.atlassian.jira.rpc.soap.client.RemoteAuthenticationException;
import com.atlassian.jira.rpc.soap.client.RemoteIssue;
import com.atlassian.jira.rpc.soap.client.RemotePermissionException;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;
import org.sonar.api.config.Settings;
import org.sonar.api.workflow.internal.DefaultReview;
import org.sonar.plugins.jira.JiraConstants;
import org.sonar.plugins.jira.soap.JiraSoapSession;

import java.rmi.RemoteException;

import static org.hamcrest.Matchers.is;
import static org.junit.Assert.assertThat;
import static org.mockito.Matchers.any;
import static org.mockito.Matchers.anyString;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

public class JiraIssueCreatorTest {

  @Rule
  public ExpectedException thrown = ExpectedException.none();
  private JiraIssueCreator jiraIssueCreator;
  private DefaultReview review;
  private Settings settings;

  @Before
  public void init() throws Exception {
    review = new DefaultReview();
    review.setReviewId(456L);
    review.setMessage("The Cyclomatic Complexity of this method is 14 which is greater than 10 authorized.");
    review.setSeverity("MINOR");
    review.setRuleName("Wrong identation");

    settings = new Settings();
    settings.appendProperty("sonar.core.serverBaseURL", "http://my.sonar.com");
    settings.appendProperty(JiraConstants.SERVER_URL_PROPERTY, "http://my.jira.com");
    settings.appendProperty(JiraConstants.SOAP_BASE_URL_PROPERTY, JiraConstants.SOAP_BASE_URL_DEF_VALUE);
    settings.appendProperty(JiraConstants.USERNAME_PROPERTY, "foo");
    settings.appendProperty(JiraConstants.PASSWORD_PROPERTY, "bar");
    settings.appendProperty(JiraConstants.JIRA_PROJECT_KEY_PROPERTY, "TEST");

    jiraIssueCreator = new JiraIssueCreator();
  }

  @Test
  public void shouldCreateSoapSession() throws Exception {
    JiraSoapSession soapSession = jiraIssueCreator.createSoapSession(settings);
    assertThat(soapSession.getWebServiceUrl().toString(), is("http://my.jira.com/rpc/soap/jirasoapservice-v2"));
  }

  @Test
  public void shouldFailToCreateSoapSessionWithIncorrectUrl() throws Exception {
    settings.removeProperty(JiraConstants.SERVER_URL_PROPERTY);
    settings.appendProperty(JiraConstants.SERVER_URL_PROPERTY, "my.server");

    thrown.expect(IllegalStateException.class);
    thrown.expectMessage("The JIRA server URL is not a valid one: my.server/rpc/soap/jirasoapservice-v2");

    jiraIssueCreator.createSoapSession(settings);
  }

  @Test
  public void shouldFailToCreateIssueIfCantConnect() throws Exception {
    // Given that
    JiraSoapSession soapSession = mock(JiraSoapSession.class);
    doThrow(RemoteException.class).when(soapSession).connect(anyString(), anyString());

    // Verify
    thrown.expect(IllegalStateException.class);
    thrown.expectMessage("Impossible to connect to the JIRA server");

    jiraIssueCreator.doCreateIssue(review, soapSession, settings, null);
  }

  @Test
  public void shouldFailToCreateIssueIfCantAuthenticate() throws Exception {
    // Given that
    JiraSoapService jiraSoapService = mock(JiraSoapService.class);
    doThrow(RemoteAuthenticationException.class).when(jiraSoapService).createIssue(anyString(), any(RemoteIssue.class));

    // Verify
    thrown.expect(IllegalStateException.class);
    thrown.expectMessage("Impossible to connect to the JIRA server (my.jira) because of invalid credentials for user foo");

    jiraIssueCreator.sendRequest(jiraSoapService, "", null, "my.jira", "foo");
  }

  @Test
  public void shouldFailToCreateIssueIfNotEnoughRights() throws Exception {
    // Given that
    JiraSoapService jiraSoapService = mock(JiraSoapService.class);
    doThrow(RemotePermissionException.class).when(jiraSoapService).createIssue(anyString(), any(RemoteIssue.class));

    // Verify
    thrown.expect(IllegalStateException.class);
    thrown.expectMessage("Impossible to create the issue on the JIRA server (my.jira) because user foo does not have enough rights.");

    jiraIssueCreator.sendRequest(jiraSoapService, "", null, "my.jira", "foo");
  }

  @Test
  public void shouldFailToCreateIssueIfRemoteError() throws Exception {
    // Given that
    JiraSoapService jiraSoapService = mock(JiraSoapService.class);
    doThrow(RemoteException.class).when(jiraSoapService).createIssue(anyString(), any(RemoteIssue.class));

    // Verify
    thrown.expect(IllegalStateException.class);
    thrown.expectMessage("Impossible to create the issue on the JIRA server (my.jira)");

    jiraIssueCreator.sendRequest(jiraSoapService, "", null, "my.jira", "foo");
  }

  @Test
  public void shouldCreateIssue() throws Exception {
    // Given that
    RemoteIssue issue = new RemoteIssue();
    JiraSoapService jiraSoapService = mock(JiraSoapService.class);
    when(jiraSoapService.createIssue(anyString(), any(RemoteIssue.class))).thenReturn(issue);

    JiraSoapSession soapSession = mock(JiraSoapSession.class);
    when(soapSession.getJiraSoapService()).thenReturn(jiraSoapService);

    // Verify
    RemoteIssue returnedIssue = jiraIssueCreator.doCreateIssue(review, soapSession, settings, null);

    verify(soapSession).connect("foo", "bar");
    verify(soapSession).getJiraSoapService();
    verify(soapSession).getAuthenticationToken();

    assertThat(returnedIssue, is(issue));
  }

  @Test
  public void shouldInitRemoteIssue() throws Exception {
    // Given that
    RemoteIssue issue = new RemoteIssue();
    issue.setProject("TEST");
    issue.setType("3");
    issue.setPriority("4");
    issue.setSummary("Sonar Review #456 - Wrong identation");
    issue.setDescription("Violation detail:\n{quote}\nThe Cyclomatic Complexity of this method is 14 which is greater than 10 authorized.\n" +
      "{quote}\n\nMessage from reviewer:\n{quote}\nHello world!\n{quote}\n\n\nCheck it on Sonar: http://my.sonar.com/project_reviews/view/456");

    // Verify
    RemoteIssue returnedIssue = jiraIssueCreator.initRemoteIssue(review, settings, "Hello world!");

    assertThat(returnedIssue, is(issue));
  }

  @Test
  public void shouldGiveDefaultPriority() throws Exception {
    assertThat(jiraIssueCreator.sonarSeverityToJiraPriority("UNKNOWN"), is("3"));
  }
}
