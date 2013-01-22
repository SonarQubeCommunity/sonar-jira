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

import java.net.SocketException;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.Arrays;
import java.util.Map;
import java.util.concurrent.ExecutionException;

import org.apache.commons.lang.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.sonar.api.CoreProperties;
import org.sonar.api.Properties;
import org.sonar.api.Property;
import org.sonar.api.PropertyType;
import org.sonar.api.ServerExtension;
import org.sonar.api.config.Settings;
import org.sonar.api.rules.RulePriority;
import org.sonar.api.utils.SonarException;
import org.sonar.api.workflow.Review;
import org.sonar.plugins.jira.JiraConstants;

import com.atlassian.jira.rest.client.api.JiraRestClient;
import com.atlassian.jira.rest.client.api.RestClientException;
import com.atlassian.jira.rest.client.api.domain.BasicIssue;
import com.atlassian.jira.rest.client.api.domain.Issue;
import com.atlassian.jira.rest.client.api.domain.input.IssueInput;
import com.atlassian.jira.rest.client.api.domain.input.IssueInputBuilder;
import com.atlassian.jira.rest.client.internal.async.AsynchronousJiraRestClientFactory;
import com.atlassian.util.concurrent.Promise;

/**
 * SOAP client class that is used for creating issues on a JIRA server
 */
@Properties({
  @Property(
    key = JiraConstants.JIRA_PROJECT_KEY_PROPERTY,
    defaultValue = "",
    name = "JIRA project key",
    description = "Key of the JIRA project on which the issues should be created.",
    global = false,
    project = true
  ),
  @Property(
    key = JiraConstants.JIRA_INFO_PRIORITY_ID,
    defaultValue = "5",
    name = "JIRA priority id for INFO",
    description = "JIRA priority id used to create issues for Sonar violations with severity INFO. Default is 5 (Trivial).",
    global = true,
    project = true,
    type = PropertyType.INTEGER
  ),
  @Property(
    key = JiraConstants.JIRA_MINOR_PRIORITY_ID,
    defaultValue = "4",
    name = "JIRA priority id for MINOR",
    description = "JIRA priority id used to create issues for Sonar violations with severity MINOR. Default is 4 (Minor).",
    global = true,
    project = true,
    type = PropertyType.INTEGER
  ),
  @Property(
    key = JiraConstants.JIRA_MAJOR_PRIORITY_ID,
    defaultValue = "3",
    name = "JIRA priority id for MAJOR",
    description = "JIRA priority id used to create issues for Sonar violations with severity MAJOR. Default is 3 (Major).",
    global = true,
    project = true,
    type = PropertyType.INTEGER
  ),
  @Property(
    key = JiraConstants.JIRA_CRITICAL_PRIORITY_ID,
    defaultValue = "2",
    name = "JIRA priority id for CRITICAL",
    description = "JIRA priority id used to create issues for Sonar violations with severity CRITICAL. Default is 2 (Critical).",
    global = true,
    project = true,
    type = PropertyType.INTEGER
  ),
  @Property(
    key = JiraConstants.JIRA_BLOCKER_PRIORITY_ID,
    defaultValue = "1",
    name = "JIRA priority id for BLOCKER",
    description = "JIRA priority id used to create issues for Sonar violations with severity BLOCKER. Default is 1 (Blocker).",
    global = true,
    project = true,
    type = PropertyType.INTEGER
  ),
  @Property(
    key = JiraConstants.JIRA_ISSUE_TYPE_ID,
    defaultValue = "3",
    name = "Id of JIRA issue type",
    description = "JIRA issue type id used to create issues for Sonar violations. Default is 3 (= Task in a default JIRA installation).",
    global = true,
    project = true,
    type = PropertyType.INTEGER
  ),
  @Property(
    key = JiraConstants.JIRA_ISSUE_COMPONENT_ID,
    defaultValue = JiraConstants.JIRA_ISSUE_COMPONENT_ID_BLANK,
    name = "Name of JIRA component",
    description = "JIRA component name used to create issues for Sonar violations. By default no component is set.",
    global = false,
    project = true,
    type = PropertyType.STRING
  )
})
public class JiraIssueCreator implements ServerExtension {

	private static final String QUOTE = "\n{quote}\n";
	private static final Logger LOG = LoggerFactory.getLogger(JiraIssueCreator.class);

	public JiraIssueCreator() {
	}

	public Issue createIssue(Review review, Settings settings, Map<String, String> params) /*throws ExecutionException*/ {
		LOG.info("Create new issue in JIRA, review: '{}', settings.properties: '{}'", review, settings.getProperties());
		
		JiraRestClient jrc = createRestClient(settings);

		try {
			return doCreateIssue(review, jrc, settings, params);
		}
		finally {
			LOG.info("Remote call finished.");
		}
	}

	protected JiraRestClient createRestClient(Settings settings) {
		String jiraUrl = settings.getString(JiraConstants.SERVER_URL_PROPERTY);
		String userName = settings.getString(JiraConstants.USERNAME_PROPERTY);
		String password = settings.getString(JiraConstants.PASSWORD_PROPERTY);

		LOG.info("Use JIRA server url: {}, userName: {}", jiraUrl, userName);
		LOG.debug("Use this password: {}", password);

		// get handle to the JIRA Rest Service from a client point of view
		JiraRestClient jrc = null;
		try {
			AsynchronousJiraRestClientFactory f = new AsynchronousJiraRestClientFactory();
			jrc = f.createWithBasicHttpAuthentication(new URI(jiraUrl), userName, password);
		} 
		catch (URISyntaxException e) {
			LOG.error("The JIRA server URL is not a valid one: " + jiraUrl, e);
//			throw new ExecutionException("The JIRA server URL is not a valid one: " + jiraUrl, e);
			throw new IllegalStateException("The JIRA server URL is not a valid one: " + jiraUrl, e);
		}
		return jrc;
	}

	protected Issue doCreateIssue(Review review, JiraRestClient jrc, Settings settings, Map<String, String> params) {
		// Connect to JIRA
		String jiraUrl = settings.getString(JiraConstants.SERVER_URL_PROPERTY);
		String user = settings.getString(JiraConstants.USERNAME_PROPERTY);

		// And create the issue
		IssueInput issue = initRemoteIssue(review, settings, params);
		LOG.info("Prepared issue to create: {}", issue);
		Issue returnedIssue = null;
		try {
			Promise<BasicIssue> promisedReturnedIssue = jrc.getIssueClient().createIssue(issue);
			//get the newly created issue
			returnedIssue = jrc.getIssueClient().getIssue(promisedReturnedIssue.get().getKey()).get();
			LOG.info("Returned issue #: {}", returnedIssue.getKey());
		} 
		catch (RestClientException e) {
			LOG.warn("The reason was: {}", e.getMessage());
			throw new IllegalStateException("Impossible to create the issue on the JIRA server (" + jiraUrl + "). Check the RestClientException cause.", e);
		}
		catch (InterruptedException e) {
			throw new IllegalStateException("Impossible to create the issue on the JIRA server (" + jiraUrl + ")", e);
		} 
		catch (ExecutionException e) {
			LOG.warn("Create issue failed.");
			if (e.getCause() != null) {
				if (e.getCause() instanceof RestClientException) {
					RestClientException ex = (RestClientException) e.getCause();
					//				LOG.warn("The reason was: {}", ex.getMessage());
					if (ex != null && ex.getStatusCode().isPresent()) {
						switch (ex.getStatusCode().get()) {
						case 401:	// ACCESS DENIED
							throw new IllegalStateException("Impossible to connect to the JIRA server (" + jiraUrl + ") because of invalid credentials for user " + user, ex);
						default:
							break;
						}
					}
					throw new IllegalStateException("Impossible to create the issue on the JIRA server (" + jiraUrl + "). Check the RestClientException cause.", ex);
				}
				else if (e.getCause() instanceof SocketException) {
					throw new IllegalStateException("Impossible to connect to the JIRA server (" + jiraUrl + ")", e.getCause());
				}
			}
			throw new IllegalStateException("Impossible to create the issue on the JIRA server (" + jiraUrl + ")", e);
		}
		catch (Exception e) {
			throw new IllegalStateException("Impossible to create the issue on the JIRA server (" + jiraUrl + ")", e);
		}
		return returnedIssue;
	}


	protected IssueInput initRemoteIssue(Review review, Settings settings, Map<String, String> params) {
		
		IssueInputBuilder issueBuilder = new IssueInputBuilder(settings.getString(JiraConstants.JIRA_PROJECT_KEY_PROPERTY), 
				Long.valueOf(settings.getString(JiraConstants.JIRA_ISSUE_TYPE_ID)));

		String commentText = params.get("text");
		issueBuilder.setDescription(generateIssueDescription(review, settings, commentText));
		issueBuilder.setSummary(generateIssueSummary(review));
		issueBuilder.setPriorityId(Long.valueOf( sonarSeverityToJiraPriorityId(RulePriority.valueOfString(review.getSeverity()), settings)));
		
		String componentId = settings.getString(JiraConstants.JIRA_ISSUE_COMPONENT_ID);
		if (!JiraConstants.JIRA_ISSUE_COMPONENT_ID_BLANK.equals(componentId)) {
			LOG.info("Set component Id: {}", componentId);
			issueBuilder.setComponentsNames(Arrays.asList(componentId));
		}
		if ( params.containsKey(JiraConstants.JIRA_ISSUE_REPORTER_PROPERTY)) {
			LOG.debug("Set the reporter.");
			issueBuilder.setReporterName(params.get(JiraConstants.JIRA_ISSUE_REPORTER_PROPERTY));
		}
		
		if ( params.containsKey(JiraConstants.JIRA_ISSUE_ASSIGNEE_PROPERTY)) {
			LOG.debug("Set the assignee.");
			issueBuilder.setAssigneeName(params.get(JiraConstants.JIRA_ISSUE_ASSIGNEE_PROPERTY));
		}
		

		IssueInput issueInput = issueBuilder.build();
		LOG.info("Prepared issueInput: {}", issueInput);
		return issueInput;
	}

	protected String generateIssueSummary(Review review) {
		StringBuilder summary = new StringBuilder("Sonar Review #");
		summary.append(review.getReviewId());
		summary.append(" - ");
		summary.append(review.getRuleName());
		return summary.toString();
	}

	protected String generateIssueDescription(Review review, Settings settings, String commentText) {
		StringBuilder description = new StringBuilder("Violation detail:");
		description.append(QUOTE);
		description.append(review.getMessage());
		description.append(QUOTE);
		if (StringUtils.isNotBlank(commentText)) {
			description.append("\nMessage from reviewer:");
			description.append(QUOTE);
			description.append(commentText);
			description.append(QUOTE);
		}
		description.append("\n\nCheck it on Sonar: ");
		description.append(settings.getString(CoreProperties.SERVER_BASE_URL));
		description.append("/project_reviews/view/");
		description.append(review.getReviewId());
		return description.toString();
	}

	protected String sonarSeverityToJiraPriorityId(RulePriority reviewSeverity, Settings settings) {
		final String priorityId;
		switch (reviewSeverity) {
		case INFO:
			priorityId = settings.getString(JiraConstants.JIRA_INFO_PRIORITY_ID);
			break;
		case MINOR:
			priorityId = settings.getString(JiraConstants.JIRA_MINOR_PRIORITY_ID);
			break;
		case MAJOR:
			priorityId = settings.getString(JiraConstants.JIRA_MAJOR_PRIORITY_ID);
			break;
		case CRITICAL:
			priorityId = settings.getString(JiraConstants.JIRA_CRITICAL_PRIORITY_ID);
			break;
		case BLOCKER:
			priorityId = settings.getString(JiraConstants.JIRA_BLOCKER_PRIORITY_ID);
			break;
		default:
			throw new SonarException("Unable to convert review severity to JIRA priority: " + reviewSeverity);
		}
		return priorityId;
	}

}
