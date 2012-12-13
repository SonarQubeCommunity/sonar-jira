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
import com.atlassian.jira.rpc.soap.client.RemoteCustomFieldValue;
import com.atlassian.jira.rpc.soap.client.RemoteIssue;
import com.atlassian.jira.rpc.soap.client.RemotePermissionException;
import com.atlassian.jira.rpc.soap.client.RemoteValidationException;
import org.apache.commons.lang.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.sonar.api.CoreProperties;
import org.sonar.api.Properties;
import org.sonar.api.Property;
import org.sonar.api.PropertyField;
import org.sonar.api.PropertyType;
import org.sonar.api.ServerExtension;
import org.sonar.api.config.Settings;
import org.sonar.api.rules.RulePriority;
import org.sonar.api.utils.SonarException;
import org.sonar.api.workflow.Review;
import org.sonar.plugins.jira.JiraConstants;
import org.sonar.plugins.jira.soap.JiraSoapSession;

import java.net.MalformedURLException;
import java.net.URL;
import java.rmi.RemoteException;
import java.util.LinkedList;
import java.util.List;

/**
 * SOAP client class that is used for creating issues on a JIRA server
 */
@Properties({
  @Property(
    key = JiraConstants.SOAP_BASE_URL_PROPERTY,
    defaultValue = JiraConstants.SOAP_BASE_URL_DEF_VALUE,
    name = "SOAP base URL",
    description = "Base URL for the SOAP API of the JIRA server",
    global = true,
    project = true
  ),
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
    description = "JIRA priority id used to create issues for Sonar violation with severity INFO. Default is 5 (Trivial).",
    global = true,
    project = true,
    type = PropertyType.INTEGER
  ),
  @Property(
    key = JiraConstants.JIRA_MINOR_PRIORITY_ID,
    defaultValue = "4",
    name = "JIRA priority id for MINOR",
    description = "JIRA priority id used to create issues for Sonar violation with severity MINOR. Default is 4 (Minor).",
    global = true,
    project = true,
    type = PropertyType.INTEGER
  ),
  @Property(
    key = JiraConstants.JIRA_MAJOR_PRIORITY_ID,
    defaultValue = "3",
    name = "JIRA priority id for MAJOR",
    description = "JIRA priority id used to create issues for Sonar violation with severity MAJOR. Default is 3 (Major).",
    global = true,
    project = true,
    type = PropertyType.INTEGER
  ),
  @Property(
    key = JiraConstants.JIRA_CRITICAL_PRIORITY_ID,
    defaultValue = "2",
    name = "JIRA priority id for CRITICAL",
    description = "JIRA priority id used to create issues for Sonar violation with severity CRITICAL. Default is 2 (Critical).",
    global = true,
    project = true,
    type = PropertyType.INTEGER
  ),
  @Property(
    key = JiraConstants.JIRA_BLOCKER_PRIORITY_ID,
    defaultValue = "1",
    name = "JIRA priority id for BLOCKER",
    description = "JIRA priority id used to create issues for Sonar violation with severity BLOCKER. Default is 1 (Blocker).",
    global = true,
    project = true,
    type = PropertyType.INTEGER
  ),
  @Property(
    key = JiraConstants.JIRA_CUSTOM_FIELDS_KEY,
    name = "Custom JIRA fields to be set on created issues",
    description = "Custom JIRA fields to be set on newly created issues",
    project = true,
    global = true,
    fields = {
      @PropertyField(
        key = JiraConstants.JIRA_CUSTOM_FIELD_ID_KEY,
        name = "Custom field id",
        description = "Internal JIRA ID of the custom field.",
        type = PropertyType.STRING,
        indicativeSize = 20),
      @PropertyField(
        key = JiraConstants.JIRA_CUSTOM_FIELD_VALUES_KEY,
        name = "Values (comma separated)",
        description = "Value (or comma-separated values) to set in this custom field",
        type = PropertyType.STRING,
        indicativeSize = 200)}),
})
public class JiraIssueCreator implements ServerExtension {

  private static final String QUOTE = "\n{quote}\n";
  private static final Logger LOG = LoggerFactory.getLogger(JiraIssueCreator.class);
  private static final String TASK_ISSUE_TYPE = "3";

  public JiraIssueCreator() {
  }

  @SuppressWarnings("rawtypes")
  public RemoteIssue createIssue(Review review, Settings settings, String commentText) throws RemoteException {
    JiraSoapSession soapSession = createSoapSession(settings);

    return doCreateIssue(review, soapSession, settings, commentText);
  }

  protected JiraSoapSession createSoapSession(Settings settings) {
    String jiraUrl = settings.getString(JiraConstants.SERVER_URL_PROPERTY);
    String baseUrl = settings.getString(JiraConstants.SOAP_BASE_URL_PROPERTY);
    String completeUrl = jiraUrl + baseUrl;

    // get handle to the JIRA SOAP Service from a client point of view
    JiraSoapSession soapSession = null;
    try {
      soapSession = new JiraSoapSession(new URL(completeUrl));
    } catch (MalformedURLException e) {
      LOG.error("The JIRA server URL is not a valid one: " + completeUrl, e);
      throw new IllegalStateException("The JIRA server URL is not a valid one: " + completeUrl, e);
    }
    return soapSession;
  }

  protected RemoteIssue doCreateIssue(Review review, JiraSoapSession soapSession, Settings settings, String commentText) {
    // Connect to JIRA
    String jiraUrl = settings.getString(JiraConstants.SERVER_URL_PROPERTY);
    String userName = settings.getString(JiraConstants.USERNAME_PROPERTY);
    String password = settings.getString(JiraConstants.PASSWORD_PROPERTY);
    try {
      soapSession.connect(userName, password);
    } catch (RemoteException e) {
      throw new IllegalStateException("Impossible to connect to the JIRA server (" + jiraUrl + ").", e);
    }

    // The JIRA SOAP Service and authentication token are used to make authentication calls
    JiraSoapService jiraSoapService = soapSession.getJiraSoapService();
    String authToken = soapSession.getAuthenticationToken();

    // And create the issue
    RemoteIssue issue = initRemoteIssue(review, settings, commentText);
    RemoteIssue returnedIssue = sendRequest(jiraSoapService, authToken, issue, jiraUrl, userName);

    String issueKey = returnedIssue.getKey();
    LOG.debug("Successfully created issue {}", issueKey);

    return returnedIssue;
  }

  protected RemoteIssue sendRequest(JiraSoapService jiraSoapService, String authToken, RemoteIssue issue, String jiraUrl, String userName) {
    try {
      return jiraSoapService.createIssue(authToken, issue);
    } catch (RemoteAuthenticationException e) {
      throw new IllegalStateException("Impossible to connect to the JIRA server (" + jiraUrl + ") because of invalid credentials for user " + userName, e);
    } catch (RemotePermissionException e) {
      throw new IllegalStateException("Impossible to create the issue on the JIRA server (" + jiraUrl + ") because user " + userName + " does not have enough rights.", e);
    } catch (RemoteValidationException e) {
      // Unfortunately the detailed cause of the error is not in fault details (ie stack) but only in fault string
      String message = StringUtils.removeStart(e.getFaultString(), "com.atlassian.jira.rpc.exception.RemoteValidationException:").trim();
      throw new IllegalStateException("Impossible to create the issue on the JIRA server (" + jiraUrl + "): " + message, e);
    } catch (RemoteException e) {
      throw new IllegalStateException("Impossible to create the issue on the JIRA server (" + jiraUrl + ")", e);
    }
  }

  protected RemoteIssue initRemoteIssue(Review review, Settings settings, String commentText) {
    RemoteIssue issue = new RemoteIssue();
    issue.setProject(settings.getString(JiraConstants.JIRA_PROJECT_KEY_PROPERTY));
    issue.setType(TASK_ISSUE_TYPE);
    issue.setPriority(sonarSeverityToJiraPriorityId(RulePriority.valueOfString(review.getSeverity()), settings));
    issue.setSummary(generateIssueSummary(review));
    issue.setDescription(generateIssueDescription(review, settings, commentText));
    // Custom fields
    List<RemoteCustomFieldValue> customFields = new LinkedList<RemoteCustomFieldValue>();
    String patternConf = StringUtils.defaultIfBlank(settings.getString(JiraConstants.JIRA_CUSTOM_FIELDS_KEY), "");
    for (String id : StringUtils.split(patternConf, ',')) {
      String propPrefix = JiraConstants.JIRA_CUSTOM_FIELDS_KEY + "." + id + ".";
      String customFieldId = settings.getString(propPrefix + JiraConstants.JIRA_CUSTOM_FIELD_ID_KEY);
      String valuesStr = settings.getString(propPrefix + JiraConstants.JIRA_CUSTOM_FIELD_VALUES_KEY);
      if (StringUtils.isNotBlank(valuesStr)) {
        String[] values = valuesStr.split(",");
        RemoteCustomFieldValue rcfv = new RemoteCustomFieldValue();
        rcfv.setCustomfieldId(customFieldId);
        rcfv.setValues(values);
        customFields.add(rcfv);
      }
    }
    if (customFields.size() > 0) {
      issue.setCustomFieldValues(customFields.toArray(new RemoteCustomFieldValue[customFields.size()]));
    }
    return issue;
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
    switch (reviewSeverity) {
      case INFO:
        return settings.getString(JiraConstants.JIRA_INFO_PRIORITY_ID);
      case MINOR:
        return settings.getString(JiraConstants.JIRA_MINOR_PRIORITY_ID);
      case MAJOR:
        return settings.getString(JiraConstants.JIRA_MAJOR_PRIORITY_ID);
      case CRITICAL:
        return settings.getString(JiraConstants.JIRA_CRITICAL_PRIORITY_ID);
      case BLOCKER:
        return settings.getString(JiraConstants.JIRA_BLOCKER_PRIORITY_ID);
      default:
        throw new SonarException("Unable to convert review severity to JIRA priority: " + reviewSeverity);
    }
  }

}
