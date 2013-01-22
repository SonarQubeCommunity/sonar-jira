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

import static org.fest.assertions.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.net.ConnectException;
import java.util.Arrays;
import java.util.Map;
import java.util.concurrent.ExecutionException;

import org.codehaus.jettison.json.JSONException;
import org.codehaus.jettison.json.JSONObject;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;
import org.mockito.Mockito;
import org.sonar.api.CoreProperties;
import org.sonar.api.config.PropertyDefinitions;
import org.sonar.api.config.Settings;
import org.sonar.api.rules.RulePriority;
import org.sonar.api.workflow.internal.DefaultReview;
import org.sonar.plugins.jira.JiraConstants;
import org.sonar.plugins.jira.JiraPlugin;
import org.sonar.plugins.jira.util.ResourceUtil;

import com.atlassian.jira.rest.client.api.IssueRestClient;
import com.atlassian.jira.rest.client.api.JiraRestClient;
import com.atlassian.jira.rest.client.api.RestClientException;
import com.atlassian.jira.rest.client.api.domain.BasicIssue;
import com.atlassian.jira.rest.client.api.domain.Issue;
import com.atlassian.jira.rest.client.api.domain.input.IssueInput;
import com.atlassian.jira.rest.client.api.domain.input.IssueInputBuilder;
import com.atlassian.jira.rest.client.internal.json.IssueJsonParser;
import com.atlassian.util.concurrent.Promise;
import com.google.common.base.Optional;
import com.google.common.collect.Maps;

public class JiraIssueCreatorTest {

	private static final String DESCRIPTION = "Violation detail:\n{quote}\nThe Cyclomatic Complexity of this method is 14 which is greater than 10 authorized.\n" +
			"{quote}\n\nMessage from reviewer:\n{quote}\nHello world!\n{quote}\n\n\nCheck it on Sonar: http://my.sonar.com/project_reviews/view/456";
	private static final String SUMMARY = "Sonar Review #456 - Wrong identation";

	
	@Rule
	public ExpectedException thrown = ExpectedException.none();
	private JiraIssueCreator jiraIssueCreator;
	private DefaultReview review;
	private Settings settings;
	private Map<String, String> params;

	@Before
	public void init() throws Exception {
		review = new DefaultReview();
		review.setReviewId(456L);
		review.setMessage("The Cyclomatic Complexity of this method is 14 which is greater than 10 authorized.");
		review.setSeverity("MINOR");
		review.setRuleName("Wrong identation");

		settings = new Settings(new PropertyDefinitions(JiraIssueCreator.class, JiraPlugin.class));
		settings.setProperty(CoreProperties.SERVER_BASE_URL, "http://my.sonar.com");
		settings.setProperty(JiraConstants.SERVER_URL_PROPERTY, "http://my.jira.com");
		settings.setProperty(JiraConstants.USERNAME_PROPERTY, "foo");
		settings.setProperty(JiraConstants.PASSWORD_PROPERTY, "bar");
		settings.setProperty(JiraConstants.JIRA_PROJECT_KEY_PROPERTY, "TEST");
		settings.setProperty(JiraConstants.JIRA_ISSUE_TYPE_ID, "3");

		jiraIssueCreator = new JiraIssueCreator();

		params = Maps.newHashMap();
		params.put("text", "Hello world!");

	}
	  
	@Test
	public void shouldFailToCreateSoapSessionWithIncorrectUrl() throws Exception {
		settings.removeProperty(JiraConstants.SERVER_URL_PROPERTY);
		settings.appendProperty(JiraConstants.SERVER_URL_PROPERTY, "httpw:/\\/my..server");

		thrown.expect(IllegalStateException.class);
		thrown.expectMessage("The JIRA server URL is not a valid one: httpw:/\\/my..server");

		jiraIssueCreator.createRestClient(settings);
	}

	@Test
	public void shouldFailToCreateIssueIfCantConnect() throws Exception {
		// Given that
		settings.setProperty(JiraConstants.SERVER_URL_PROPERTY, "my.jira");

		JiraRestClient jiraRestClient = mock(JiraRestClient.class);
		IssueRestClient issueRestClient = mock(IssueRestClient.class);
		when(jiraRestClient.getIssueClient()).thenReturn(issueRestClient);

		ConnectException ce = mock(ConnectException.class);
		when(ce.getMessage()).thenReturn(null);
		
		ExecutionException ex = new ExecutionException(ce);
		
		Promise<BasicIssue> promise = mock(Promise.class); 

		when(issueRestClient.createIssue(Mockito.any(IssueInput.class))).thenReturn(promise);
		
		// the promised return value get() operation causes the ExecutionException
		Mockito.doThrow(ex).when(promise).get();
		
		// Verify
		thrown.expect(IllegalStateException.class);
		thrown.expectMessage("Impossible to connect to the JIRA server (my.jira)");

		jiraIssueCreator.doCreateIssue(review, jiraRestClient, settings, params);
	}

	@Test
	public void shouldFailToCreateIssueIfCantAuthenticate() throws Exception {
		// Given that
		settings.setProperty(JiraConstants.SERVER_URL_PROPERTY, "my.jira");

		JiraRestClient jiraRestClient = mock(JiraRestClient.class);
		IssueRestClient issueRestClient = mock(IssueRestClient.class);
		when(jiraRestClient.getIssueClient()).thenReturn(issueRestClient);

		RestClientException rce = mock(RestClientException.class);
		when(rce.getMessage()).thenReturn(null);
		Optional<Integer> isPresent = Optional.of(Integer.valueOf(401));
		when(rce.getStatusCode()).thenReturn(isPresent);
		
		ExecutionException ex = new ExecutionException(rce);
		
		Promise<BasicIssue> promise = mock(Promise.class); 

		when(issueRestClient.createIssue(Mockito.any(IssueInput.class))).thenReturn(promise);
		
		// the promised return value get() operation causes the ExecutionException
		Mockito.doThrow(ex).when(promise).get();

		// Verify
		thrown.expect(IllegalStateException.class);
		thrown.expectMessage("Impossible to connect to the JIRA server (my.jira) because of invalid credentials for user foo");

		jiraIssueCreator.doCreateIssue(review, jiraRestClient, settings, params);
	}

	@Test
	public void shouldFailToCreateIssueIfNotEnoughRights() throws Exception {
		// Given that
		settings.setProperty(JiraConstants.SERVER_URL_PROPERTY, "my.jira");

		JiraRestClient jiraRestClient = mock(JiraRestClient.class);
		IssueRestClient issueRestClient = mock(IssueRestClient.class);
		when(jiraRestClient.getIssueClient()).thenReturn(issueRestClient);

		RestClientException rce = mock(RestClientException.class);
		when(rce.getMessage()).thenReturn(null);
		Optional<Integer> isPresent = Optional.absent();
		when(rce.getStatusCode()).thenReturn(isPresent);
		
		ExecutionException ex = new ExecutionException(rce);
		
		Promise<BasicIssue> promise = mock(Promise.class); 

		when(issueRestClient.createIssue(Mockito.any(IssueInput.class))).thenReturn(promise);
		
		// the promised return value get() operation causes the ExecutionException
		Mockito.doThrow(ex).when(promise).get();
		
		// Verify
		thrown.expect(IllegalStateException.class);
//		thrown.expectMessage("Impossible to create the issue on the JIRA server (my.jira) because user foo does not have enough rights.");
		thrown.expectMessage("Impossible to create the issue on the JIRA server (my.jira). Check the RestClientException cause.");

		jiraIssueCreator.doCreateIssue(review, jiraRestClient, settings, params);
	}

	@Test
	public void shouldFailToCreateIssueIfRemoteError() throws Exception {
		// Given that
		settings.setProperty(JiraConstants.SERVER_URL_PROPERTY, "my.jira");
		
		JiraRestClient jiraRestClient = mock(JiraRestClient.class);
		IssueRestClient issueRestClient = mock(IssueRestClient.class);
		when(jiraRestClient.getIssueClient()).thenReturn(issueRestClient);

		Mockito.doThrow(RestClientException.class).when(issueRestClient).createIssue(Mockito.any(IssueInput.class));
		
		// Verify
		thrown.expect(IllegalStateException.class);
		thrown.expectMessage("Impossible to create the issue on the JIRA server (my.jira)");

		jiraIssueCreator.doCreateIssue(review, jiraRestClient, settings, params);
	}

	@Test
	public void shouldCreateIssue() throws Exception {
		// Given that
		JiraRestClient jiraRestClient = mock(JiraRestClient.class);
		IssueRestClient issueRestClient = mock(IssueRestClient.class);
		when(jiraRestClient.getIssueClient()).thenReturn(issueRestClient);
		
		final Issue expectedIssue = parseIssue("/json/issue/issue-valid.json");
		Promise<BasicIssue> promise = mock(Promise.class); 
		when(promise.get()).thenReturn(expectedIssue);
		when(issueRestClient.createIssue(Mockito.any(IssueInput.class))).thenReturn(promise);
		Promise<Issue> promise2 = mock(Promise.class); 
		when(promise2.get()).thenReturn(expectedIssue);
		when(issueRestClient.getIssue(Mockito.anyString())).thenReturn(promise2);

		// Verify
		Issue returnedIssue = jiraIssueCreator.doCreateIssue(review, jiraRestClient, settings, params);

		verify(jiraRestClient, times(2)).getIssueClient();
		verify(issueRestClient).createIssue(Mockito.any(IssueInput.class));
		verify(issueRestClient).getIssue(Mockito.anyString());

		assertThat(returnedIssue).isEqualTo(expectedIssue);
	}

	@Test
	public void shouldInitRemoteIssue() throws Exception {
		// Given that
		IssueInput expectedIssue = prepareIssueInput("TEST", "3", "4", DESCRIPTION, SUMMARY, null);

		// Verify
		IssueInput returnedIssue = jiraIssueCreator.initRemoteIssue(review, settings, params);

		assertThat(String.valueOf(returnedIssue)).isEqualTo(String.valueOf(expectedIssue));
	}

	@Test
	public void shouldInitRemoteIssueWithTaskType() throws Exception {
		// Given that
		settings.setProperty(JiraConstants.JIRA_ISSUE_TYPE_ID, "4");

		String projectKey = "TEST"; 
		String issueTypeId = "4";
		String priorityId = "4";
		IssueInput expectedIssue = prepareIssueInput(projectKey, issueTypeId, priorityId, DESCRIPTION, SUMMARY, null);

		// Verify
		IssueInput returnedIssue = jiraIssueCreator.initRemoteIssue(review, settings, params);

		assertThat(String.valueOf(returnedIssue)).isEqualTo(String.valueOf(expectedIssue));
	}

	@Test
	public void shouldInitRemoteIssueWithComponent() throws Exception {
		// Given that
		settings.setProperty(JiraConstants.JIRA_ISSUE_COMPONENT_ID, "123");

		String projectKey = "TEST"; 
		String issueTypeId = "3";
		String priorityId = "4";
		String componentId = "123";
		IssueInput expectedIssue = prepareIssueInput(projectKey, issueTypeId, priorityId, DESCRIPTION, SUMMARY, componentId);

		// Verify
		IssueInput returnedIssue = jiraIssueCreator.initRemoteIssue(review, settings, params);

		assertThat(String.valueOf(returnedIssue)).isEqualTo(String.valueOf(expectedIssue));
	}

	@Test
	public void shouldGiveDefaultPriority() throws Exception {
		assertThat(jiraIssueCreator.sonarSeverityToJiraPriorityId(RulePriority.BLOCKER, settings)).isEqualTo("1");
		assertThat(jiraIssueCreator.sonarSeverityToJiraPriorityId(RulePriority.CRITICAL, settings)).isEqualTo("2");
		assertThat(jiraIssueCreator.sonarSeverityToJiraPriorityId(RulePriority.MAJOR, settings)).isEqualTo("3");
		assertThat(jiraIssueCreator.sonarSeverityToJiraPriorityId(RulePriority.MINOR, settings)).isEqualTo("4");
		assertThat(jiraIssueCreator.sonarSeverityToJiraPriorityId(RulePriority.INFO, settings)).isEqualTo("5");
	}

	private IssueInput prepareIssueInput(String projectKey, String issueTypeId, String priorityId, String description, 
			String summary, String componentId) {

		IssueInputBuilder issueBuilder = new IssueInputBuilder(projectKey, Long.valueOf(issueTypeId));

		issueBuilder.setDescription(description);
		issueBuilder.setSummary(summary);
		issueBuilder.setPriorityId(Long.valueOf(priorityId));
		if (componentId != null) {
			issueBuilder.setComponentsNames(Arrays.asList(componentId));
		}

		return issueBuilder.build();
	}

	  private Issue parseIssue(final String resourcePath) throws JSONException {
		  final JSONObject issueJson = ResourceUtil.getJsonObjectFromResource(resourcePath);
		  final IssueJsonParser parser = new IssueJsonParser();
		  return parser.parse(issueJson);
	  }
}
