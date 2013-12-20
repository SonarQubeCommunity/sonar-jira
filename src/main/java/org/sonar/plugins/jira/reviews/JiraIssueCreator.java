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
import com.atlassian.jira.rpc.soap.client.RemoteComponent;
import com.atlassian.jira.rpc.soap.client.RemoteIssue;
import com.atlassian.jira.rpc.soap.client.RemotePermissionException;
import com.atlassian.jira.rpc.soap.client.RemoteValidationException;
import org.apache.commons.lang.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.sonar.api.CoreProperties;
import org.sonar.api.Properties;
import org.sonar.api.Property;
import org.sonar.api.PropertyType;
import org.sonar.api.ServerExtension;
import org.sonar.api.config.Settings;
import org.sonar.api.issue.Issue;
import org.sonar.api.rules.Rule;
import org.sonar.api.rules.RuleFinder;
import org.sonar.api.rules.RulePriority;
import org.sonar.api.utils.SonarException;
import org.sonar.plugins.jira.JiraConstants;
import org.sonar.plugins.jira.soap.JiraSoapSession;

import java.net.MalformedURLException;
import java.net.URL;
import java.rmi.RemoteException;

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
    defaultValue = "",
    name = "Id of JIRA component",
    description = "JIRA component id used to create issues for Sonar violations. By default no component is set.",
    global = false,
    project = true,
    type = PropertyType.INTEGER
  )
})
public class JiraIssueCreator implements ServerExtension {

  private static final String QUOTE = "\n{quote}\n";
  private static final Logger LOG = LoggerFactory.getLogger(JiraIssueCreator.class);
  private final RuleFinder ruleFinder;

  public JiraIssueCreator(RuleFinder ruleFinder) {
    this.ruleFinder = ruleFinder;
  }

  public RemoteIssue createIssue(Issue sonarIssue, Settings settings) throws RemoteException {
    JiraSoapSession soapSession = createSoapSession(settings);

    return doCreateIssue(sonarIssue, soapSession, settings);
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

  protected RemoteIssue doCreateIssue(Issue sonarIssue, JiraSoapSession soapSession, Settings settings) {
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
    RemoteIssue issue = initRemoteIssue(sonarIssue, settings);
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

  protected RemoteIssue initRemoteIssue(Issue sonarIssue, Settings settings) {
    RemoteIssue issue = new RemoteIssue();
    issue.setProject(settings.getString(JiraConstants.JIRA_PROJECT_KEY_PROPERTY));
    issue.setType(settings.getString(JiraConstants.JIRA_ISSUE_TYPE_ID));
    issue.setPriority(sonarSeverityToJiraPriorityId(RulePriority.valueOf(sonarIssue.severity()), settings));
    issue.setSummary(generateIssueSummary(sonarIssue));
    issue.setDescription(generateIssueDescription(sonarIssue, settings));
    if (settings.hasKey(JiraConstants.JIRA_ISSUE_COMPONENT_ID)) {
      String componentId = settings.getString(JiraConstants.JIRA_ISSUE_COMPONENT_ID);
      RemoteComponent rc = new RemoteComponent();
      rc.setId(componentId);
      issue.setComponents(new RemoteComponent[] {rc});
    }
    return issue;
  }

  protected String generateIssueSummary(Issue sonarIssue) {
    Rule rule = ruleFinder.findByKey(sonarIssue.ruleKey());

    StringBuilder summary = new StringBuilder("Sonar Issue #");
    summary.append(sonarIssue.key());
    if (rule != null && rule.getName() != null) {
      summary.append(" - ");
      summary.append(rule.getName().toString());
    }
    return summary.toString();
  }

  protected String generateIssueDescription(Issue sonarIssue, Settings settings) {
    StringBuilder description = new StringBuilder("Issue detail:");
    description.append(QUOTE);
    description.append(sonarIssue.message());
    description.append(QUOTE);
    description.append("\n\nCheck it on Sonar: ");
    description.append(settings.getString(CoreProperties.SERVER_BASE_URL));
    description.append("/issue/show/");
    description.append(sonarIssue.key());
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
