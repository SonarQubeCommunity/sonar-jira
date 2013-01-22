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

import java.net.URI;
import java.net.URISyntaxException;
import java.util.Map;
import java.util.concurrent.ExecutionException;

import org.apache.commons.lang.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.sonar.api.Properties;
import org.sonar.api.Property;
import org.sonar.api.batch.Sensor;
import org.sonar.api.batch.SensorContext;
import org.sonar.api.config.Settings;
import org.sonar.api.measures.Measure;
import org.sonar.api.measures.PropertiesBuilder;
import org.sonar.api.resources.Project;
import org.sonar.plugins.jira.JiraConstants;

import com.atlassian.jira.rest.client.api.JiraRestClient;
import com.atlassian.jira.rest.client.api.domain.Filter;
import com.atlassian.jira.rest.client.api.domain.Issue;
import com.atlassian.jira.rest.client.api.domain.Priority;
import com.atlassian.jira.rest.client.api.domain.SearchResult;
import com.atlassian.jira.rest.client.internal.async.AsynchronousJiraRestClientFactory;
import com.atlassian.util.concurrent.Promise;
import com.google.common.collect.Maps;

@Properties({
	@Property(
			key = JiraConstants.FILTER_PROPERTY,
			defaultValue = "",
			name = "Filter name",
			description = "Case sensitive, example : SONAR-current-iteration",
			global = false,
			project = true,
			module = true
			)
})
public class JiraSensor implements Sensor {
	private static final Logger LOG = LoggerFactory.getLogger(JiraSensor.class);

	private final Settings settings;

	public JiraSensor(Settings settings) {
		this.settings = settings;
	}

	private String getServerUrl() {
		return settings.getString(JiraConstants.SERVER_URL_PROPERTY);
	}

	private String getUsername() {
		return settings.getString(JiraConstants.USERNAME_PROPERTY);
	}

	private String getPassword() {
		return settings.getString(JiraConstants.PASSWORD_PROPERTY);
	}

	private String getFilterName() {
		return settings.getString(JiraConstants.FILTER_PROPERTY);
	}

	public boolean shouldExecuteOnProject(Project project) {
		if (missingMandatoryParameters()) {
			LOG.info("JIRA issues sensor will not run as some parameters are missing.");
		}
		return project.isRoot() && !missingMandatoryParameters();
	}

	public void analyse(Project project, SensorContext context) {

		LOG.info("Analyse JIRA for project: {}", project);

		try {
			JiraRestClient jrc = createRestClient(settings);

			runAnalysis(context, jrc);
		} 
		catch (IllegalStateException e) {
			LOG.error("Error accessing Jira web service, please verify the parameters", e);
		} catch (InterruptedException e) {
			LOG.error("Running analysis failed.", e);
		} catch (ExecutionException e) {
			LOG.error("Running analysis failed.", e);
		} 
	}

	protected JiraRestClient createRestClient(Settings settings) {
		String jiraUrl = settings.getString(JiraConstants.SERVER_URL_PROPERTY);
		String userName = settings.getString(JiraConstants.USERNAME_PROPERTY);
		String password = settings.getString(JiraConstants.PASSWORD_PROPERTY);

		LOG.info("Use JIRA server url: {}, userName: {}", jiraUrl, userName);
		LOG.debug("Use thispassword: {}", password);

		// get handle to the JIRA Rest Service from a client point of view
		JiraRestClient jc = null;
		try {
			AsynchronousJiraRestClientFactory f = new AsynchronousJiraRestClientFactory();
			jc = f.createWithBasicHttpAuthentication(new URI(jiraUrl), userName, password);
		} 
		catch (URISyntaxException e) {
			LOG.error("The JIRA server URL is not a valid one: " + jiraUrl, e);
			throw new IllegalStateException("The JIRA server URL is not a valid one: " + jiraUrl, e);
		}
		return jc;
	}

	protected void runAnalysis(SensorContext context, JiraRestClient jrc) throws InterruptedException, ExecutionException {
		Filter filter = findJiraFilter(jrc);
		Map<String, Integer> issuesByPriority = collectIssuesByPriority(jrc, filter);

		double total = 0;
		PropertiesBuilder<String, Integer> distribution = new PropertiesBuilder<String, Integer>();
		for (Map.Entry<String, Integer> entry : issuesByPriority.entrySet()) {
			total += entry.getValue();
			distribution.add(entry.getKey(), entry.getValue());
		}

		String url = getServerUrl() + "/secure/IssueNavigator.jspa?mode=hide&requestId=" + filter.getId();
		saveMeasures(context, url, total, distribution.buildData());
	}


	protected Map<Long, String> collectPriorities(JiraRestClient jrc) throws InterruptedException, ExecutionException {
		Map<Long, String> priorities = Maps.newHashMap();

		for (Priority priority : jrc.getMetadataClient().getPriorities().get()) {
			priorities.put(priority.getId(), priority.getName());
		}
		return priorities;
	}

	protected Filter findJiraFilter(JiraRestClient jrc) {
		Filter filter = null;
		Promise<Iterable<Filter>> promisedFilters = null;

		try {
			promisedFilters = jrc.getSearchClient().getFavouriteFilters();

			for (Filter f : promisedFilters.claim()) {
				LOG.debug("Process current filter: {}", f.getName());
				if (getFilterName().equals(f.getName())) {
					filter = f;
					LOG.debug("Found filter to use: {}", filter.getName());
					continue;
				}
			}
		} 
		catch (Exception e) {
			LOG.warn("Get favourite filters failed.", e);
		}

		if (filter == null) {
			LOG.warn("No filter found with name: {}", getFilterName());
			throw new IllegalStateException("Unable to find filter '" + getFilterName() + "' in JIRA");
		}
		return filter;
	}

	protected Map<String, Integer> collectIssuesByPriority(JiraRestClient jrc, Filter filter) {
		LOG.info("Get the issue from filter: {}", filter);
		Map<String, Integer> issuesByPriority = Maps.newHashMap();
		Promise<SearchResult> searchResult = jrc.getSearchClient().searchJql(filter.getJql());


		for (Issue issue : searchResult.claim().getIssues()) {
			String priority = issue.getPriority().getName();
			if (!issuesByPriority.containsKey(priority)) {
				issuesByPriority.put(priority, 1);
			} else {
				issuesByPriority.put(priority, issuesByPriority.get(priority) + 1);
			}
		}
		return issuesByPriority;
	}

	protected boolean missingMandatoryParameters() {
		return StringUtils.isEmpty(getServerUrl()) ||
				StringUtils.isEmpty(getFilterName()) ||
				StringUtils.isEmpty(getUsername()) ||
				StringUtils.isEmpty(getPassword());
	}

	protected void saveMeasures(SensorContext context, String issueUrl, double totalPrioritiesCount, String priorityDistribution) {
		Measure issuesMeasure = new Measure(JiraMetrics.ISSUES, totalPrioritiesCount);
		issuesMeasure.setUrl(issueUrl);
		issuesMeasure.setData(priorityDistribution);
		context.saveMeasure(issuesMeasure);
	}

	@Override
	public String toString() {
		return "JIRA issues sensor";
	}
}
