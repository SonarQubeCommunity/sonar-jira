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
import static org.mockito.Matchers.any;
import static org.mockito.Matchers.anyString;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;
import org.mockito.Matchers;
import org.sonar.api.CoreProperties;
import org.sonar.api.config.PropertyDefinitions;
import org.sonar.api.config.Settings;
import org.sonar.api.issue.Issue;
import org.sonar.api.issue.internal.DefaultIssue;
import org.sonar.api.rule.RuleKey;
import org.sonar.api.rules.RuleFinder;
import org.sonar.plugins.jira.JiraConstants;
import org.sonar.plugins.jira.JiraPlugin;
import org.sonar.plugins.jira.JiraSession;
import org.sonar.plugins.jira.soap.JiraSoapServiceWrapper;
import org.sonar.plugins.jira.soap.JiraSoapSession;

import com.atlassian.jira.rest.client.api.domain.BasicIssue;
import com.atlassian.jira.rpc.soap.client.JiraSoapService;
import com.atlassian.jira.rpc.soap.client.RemoteIssue;

public class JiraIssueCreatorTest {

  @Rule
  public ExpectedException thrown = ExpectedException.none();
  private JiraIssueCreator jiraIssueCreator;
  private Issue sonarIssue;
  private Settings settings;
  private RuleFinder ruleFinder;
  
  @Before
  public void init() throws Exception {
    sonarIssue = new DefaultIssue()
      .setKey("ABCD")
      .setMessage("The Cyclomatic Complexity of this method is 14 which is greater than 10 authorized.")
      .setSeverity("MINOR")
      .setRuleKey(RuleKey.of("squid", "CycleBetweenPackages"));

    ruleFinder = mock(RuleFinder.class);
    when(ruleFinder.findByKey(RuleKey.of("squid", "CycleBetweenPackages"))).thenReturn(org.sonar.api.rules.Rule.create().setName("Avoid cycle between java packages"));

    settings = new Settings(new PropertyDefinitions(JiraIssueCreator.class, JiraPlugin.class));
    settings.setProperty(CoreProperties.SERVER_BASE_URL, "http://my.sonar.com");
    settings.setProperty(JiraConstants.SERVER_URL_PROPERTY, "http://my.jira.com");
    settings.setProperty(JiraConstants.USERNAME_PROPERTY, "foo");
    settings.setProperty(JiraConstants.PASSWORD_PROPERTY, "bar");
    settings.setProperty(JiraConstants.JIRA_PROJECT_KEY_PROPERTY, "TEST");

    jiraIssueCreator = new JiraIssueCreator(ruleFinder);
  }

  @Test
  public void shouldCreateSoapSession() throws Exception {
    JiraSession soapSession = jiraIssueCreator.createSoapSession(settings);
    assertThat(soapSession.getWebServiceUrl().toString()).isEqualTo("http://my.jira.com/rpc/soap/jirasoapservice-v2");
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
  public void shouldCreateIssue() throws Exception {
    // Given that
    RemoteIssue issue = new RemoteIssue();
    issue.setId("1");
    JiraSoapService jiraSoapService = mock(JiraSoapService.class);
    when(jiraSoapService.createIssue(anyString(), any(RemoteIssue.class))).thenReturn(issue);

    JiraSoapSession soapSession = mock(JiraSoapSession.class);
    when(soapSession.getJiraSoapService()).thenReturn(jiraSoapService);
    
    JiraSoapServiceWrapper wrapper = new JiraSoapServiceWrapper(jiraSoapService, ruleFinder, settings);    
    when(soapSession.getJiraService(Matchers.<RuleFinder>any(), Matchers.<Settings>any())).thenReturn(wrapper);
    
    // Verify
    BasicIssue returnedIssue = jiraIssueCreator.doCreateIssue(sonarIssue, soapSession, settings);
    
    verify(soapSession).connect("foo", "bar");
    verify(soapSession).getJiraService(Matchers.<RuleFinder>any(), Matchers.<Settings>any());
    verify(soapSession).getAuthenticationToken();

    assertThat(String.valueOf(returnedIssue.getId())).isEqualTo(issue.getId());
    assertThat(returnedIssue.getKey()).isEqualTo(issue.getKey());
  }

}
