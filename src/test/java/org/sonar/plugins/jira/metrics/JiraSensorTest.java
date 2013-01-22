/*
 * Sonar, open source software quality management tool.
 * Copyright (C) 2009 SonarSource
 * mailto:contact AT sonarsource DOT com
 *
 * Sonar is free software; you can redistribute it and/or
 * modify it under the terms of the GNU Lesser General Public
 * License as published by the Free Software Foundation; either
 * version 3 of the License, or (at your option) any later version.
 *
 * Sonar is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
 * Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public
 * License along with Sonar; if not, write to the Free Software
 * Foundation, Inc., 51 Franklin Street, Fifth Floor, Boston, MA  02
 */

package org.sonar.plugins.jira.metrics;

import static org.fest.assertions.Assertions.assertThat;
import static org.mockito.Matchers.argThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoMoreInteractions;
import static org.mockito.Mockito.when;

import java.util.Arrays;
import java.util.HashSet;
import java.util.Map;

import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;
import org.mockito.Mockito;
import org.sonar.api.batch.SensorContext;
import org.sonar.api.config.Settings;
import org.sonar.api.resources.Project;
import org.sonar.api.test.IsMeasure;
import org.sonar.plugins.jira.JiraConstants;

import com.atlassian.jira.rest.client.api.JiraRestClient;
import com.atlassian.jira.rest.client.api.MetadataRestClient;
import com.atlassian.jira.rest.client.api.SearchRestClient;
import com.atlassian.jira.rest.client.api.domain.BasicPriority;
import com.atlassian.jira.rest.client.api.domain.Filter;
import com.atlassian.jira.rest.client.api.domain.Issue;
import com.atlassian.jira.rest.client.api.domain.Priority;
import com.atlassian.jira.rest.client.api.domain.SearchResult;
import com.atlassian.util.concurrent.Promise;

public class JiraSensorTest {

	@Rule
	public ExpectedException thrown = ExpectedException.none();

	private JiraSensor sensor;
	private Settings settings;

	@Before
	public void setUp() {
		settings = new Settings();
		settings.setProperty(JiraConstants.SERVER_URL_PROPERTY, "http://my.jira.server");
		settings.setProperty(JiraConstants.USERNAME_PROPERTY, "admin");
		settings.setProperty(JiraConstants.PASSWORD_PROPERTY, "adminPwd");
		settings.setProperty(JiraConstants.FILTER_PROPERTY, "myFilter");
		sensor = new JiraSensor(settings);

	}

	@Test
	public void testToString() throws Exception {
		assertThat(sensor.toString()).isEqualTo("JIRA issues sensor");
	}

	@Test
	public void testPresenceOfProperties() throws Exception {
		assertThat(sensor.missingMandatoryParameters()).isEqualTo(false);

		settings.removeProperty(JiraConstants.PASSWORD_PROPERTY);
		sensor = new JiraSensor(settings);
		assertThat(sensor.missingMandatoryParameters()).isEqualTo(true);

		settings.removeProperty(JiraConstants.USERNAME_PROPERTY);
		sensor = new JiraSensor(settings);
		assertThat(sensor.missingMandatoryParameters()).isEqualTo(true);

		settings.removeProperty(JiraConstants.FILTER_PROPERTY);
		sensor = new JiraSensor(settings);
		assertThat(sensor.missingMandatoryParameters()).isEqualTo(true);

		settings.removeProperty(JiraConstants.SERVER_URL_PROPERTY);
		sensor = new JiraSensor(settings);
		assertThat(sensor.missingMandatoryParameters()).isEqualTo(true);
	}

	@Test
	public void shouldExecuteOnRootProjectWithAllParams() throws Exception {
		Project project = mock(Project.class);
		when(project.isRoot()).thenReturn(true).thenReturn(false);

		assertThat(sensor.shouldExecuteOnProject(project)).isEqualTo(true);
	}

	@Test
	public void shouldNotExecuteOnNonRootProject() throws Exception {
		assertThat(sensor.shouldExecuteOnProject(mock(Project.class))).isEqualTo(false);
	}

	@Test
	public void shouldNotExecuteOnRootProjectifOneParamMissing() throws Exception {
		Project project = mock(Project.class);
		when(project.isRoot()).thenReturn(true).thenReturn(false);

		settings.removeProperty(JiraConstants.SERVER_URL_PROPERTY);
		sensor = new JiraSensor(settings);

		assertThat(sensor.shouldExecuteOnProject(project)).isEqualTo(false);
	}

	@Test
	public void testSaveMeasures() {
		SensorContext context = mock(SensorContext.class);
		String url = "http://localhost/jira";
		String priorityDistribution = "Critical=1";

		sensor.saveMeasures(context, url, 1, priorityDistribution);

		verify(context).saveMeasure(argThat(new IsMeasure(JiraMetrics.ISSUES, 1.0, priorityDistribution)));
		verifyNoMoreInteractions(context);
	}

	@Test
	public void shouldCollectPriorities() throws Exception {

		Priority priority1 = new Priority(null, Long.valueOf("1"), "Minor", null, null, null);

		JiraRestClient jiraRestClient = mock(JiraRestClient.class);
		MetadataRestClient metadataRestClient = mock(MetadataRestClient.class);
		when(jiraRestClient.getMetadataClient()).thenReturn(metadataRestClient);

		Promise<Iterable<Priority>> promise = mock(Promise.class); 
		when(metadataRestClient.getPriorities()).thenReturn(promise);
		when(promise.get()).thenReturn(new HashSet<Priority>(Arrays.asList(priority1)));

		Map<Long, String> foundPriorities = sensor.collectPriorities(jiraRestClient);
		assertThat(foundPriorities.size()).isEqualTo(1);
		assertThat(foundPriorities.get(1L)).isEqualTo("Minor");
	}

	@Test
	public void shouldCollectIssuesByPriority() throws Exception {

		JiraRestClient jiraRestClient = mock(JiraRestClient.class);
		SearchRestClient searchRestClient = mock(SearchRestClient.class);
		when(jiraRestClient.getSearchClient()).thenReturn(searchRestClient);

		Filter filter = createFilter(Long.valueOf(1), null);
		Issue issue1 = createIssue(1L, "minor");
		Issue issue2 = createIssue(1L, "critical");
		Issue issue3 = createIssue(1L, "critical");
		Iterable<Issue> issues = Arrays.asList(issue1, issue2, issue3);
		
		SearchResult searchResult = new SearchResult(0, 10, 3, issues);
		Promise<SearchResult> promise = mock(Promise.class); 
		when(promise.claim()).thenReturn(searchResult);
		
		when(searchRestClient.searchJql(Mockito.anyString())).thenReturn(promise);
		
		Map<String, Integer> foundIssues = sensor.collectIssuesByPriority(jiraRestClient, filter);
		assertThat(foundIssues.size()).isEqualTo(2);
		assertThat(foundIssues.get("critical")).isEqualTo(2);
		assertThat(foundIssues.get("minor")).isEqualTo(1);
	}

	private Issue createIssue(Long id, String priority) {
		BasicPriority prio = new BasicPriority(null, Long.valueOf(1), priority);
		Issue issue = new Issue(null, null, null, id, null, null, null, null, prio, null, 
				null, null, null, null, null, null, null, null, 
				null, null, null, null, null, null, null, null, 
				null, null, null, null, null);
		
		return issue;
	}

	@Test
	public void shouldFindFilters() throws Exception {
		JiraRestClient jiraRestClient = mock(JiraRestClient.class);
		SearchRestClient searchRestClient = mock(SearchRestClient.class);
		when(jiraRestClient.getSearchClient()).thenReturn(searchRestClient);

		Filter filter1 = createFilter(null, "fooFilter");
		Filter myFilter = createFilter(null, "myFilter");
		
		Iterable<Filter> filters = Arrays.asList(filter1, myFilter);
		Promise<Iterable<Filter>> promise = mock(Promise.class); 
		when(promise.claim()).thenReturn(filters);
		
		when(searchRestClient.getFavouriteFilters()).thenReturn(promise);

		Filter foundFilter = sensor.findJiraFilter(jiraRestClient);
		assertThat(foundFilter).isEqualTo(myFilter);
	}
	
	private Filter createFilter(Long id, String name) {
		Filter filter = new Filter(null, id, name, null, null, null, null, null, false);
		return filter;
	}

	@Test
	public void faillIfNoFilterFound() throws Exception {
		JiraRestClient jiraRestClient = mock(JiraRestClient.class);
		SearchRestClient searchRestClient = mock(SearchRestClient.class);
		when(jiraRestClient.getSearchClient()).thenReturn(searchRestClient);
		
		Iterable<Filter> filters = Arrays.asList();
		Promise<Iterable<Filter>> promise = mock(Promise.class); 
		when(promise.claim()).thenReturn(filters);
		
		when(searchRestClient.getFavouriteFilters()).thenReturn(promise);

		thrown.expect(IllegalStateException.class);
		thrown.expectMessage("Unable to find filter 'myFilter' in JIRA");

		sensor.findJiraFilter(jiraRestClient);
	}

}
