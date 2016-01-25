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

import java.net.MalformedURLException;
import java.net.URL;
import java.rmi.RemoteException;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.sonar.api.Properties;
import org.sonar.api.Property;
import org.sonar.api.PropertyType;
import org.sonar.api.ServerExtension;
import org.sonar.api.config.Settings;
import org.sonar.api.issue.Issue;
import org.sonar.api.rules.RuleFinder;
import org.sonar.plugins.jira.JiraConstants;
import org.sonar.plugins.jira.JiraService;
import org.sonar.plugins.jira.JiraSession;
import org.sonar.plugins.jira.rest.JiraRestSession;
import org.sonar.plugins.jira.soap.JiraSoapSession;

import com.atlassian.jira.rest.client.api.domain.BasicIssue;

/**
 * Client class that is used for creating issues on a JIRA server
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
    key = JiraConstants.REST_BASE_URL_PROPERTY,
    defaultValue = JiraConstants.REST_BASE_URL_DEF_VALUE,
    name = "REST base URL",
    description = "Base URL for the REST API of the JIRA server",
    global = true,
    project = true
  ),
  @Property(
    key = JiraConstants.JIRA_PROJECT_KEY_PROPERTY,
    defaultValue = "",
    name = "JIRA project key",
    description = "Key of the JIRA project on which the JIRA issues should be created.",
    global = false,
    project = true
  ),
  @Property(
    key = JiraConstants.JIRA_INFO_PRIORITY_ID,
    defaultValue = "5",
    name = "JIRA priority id for INFO",
    description = "JIRA priority id used to create JIRA issues for SonarQube issues with severity INFO. Default is 5 (Trivial).",
    global = true,
    project = true,
    type = PropertyType.INTEGER
  ),
  @Property(
    key = JiraConstants.JIRA_MINOR_PRIORITY_ID,
    defaultValue = "4",
    name = "JIRA priority id for MINOR",
    description = "JIRA priority id used to create JIRA issues for SonarQube issues with severity MINOR. Default is 4 (Minor).",
    global = true,
    project = true,
    type = PropertyType.INTEGER
  ),
  @Property(
    key = JiraConstants.JIRA_MAJOR_PRIORITY_ID,
    defaultValue = "3",
    name = "JIRA priority id for MAJOR",
    description = "JIRA priority id used to create JIRA issues for SonarQube issues with severity MAJOR. Default is 3 (Major).",
    global = true,
    project = true,
    type = PropertyType.INTEGER
  ),
  @Property(
    key = JiraConstants.JIRA_CRITICAL_PRIORITY_ID,
    defaultValue = "2",
    name = "JIRA priority id for CRITICAL",
    description = "JIRA priority id used to create JIRA issues for SonarQube issues with severity CRITICAL. Default is 2 (Critical).",
    global = true,
    project = true,
    type = PropertyType.INTEGER
  ),
  @Property(
    key = JiraConstants.JIRA_BLOCKER_PRIORITY_ID,
    defaultValue = "1",
    name = "JIRA priority id for BLOCKER",
    description = "JIRA priority id used to create JIRA issues for SonarQube issues with severity BLOCKER. Default is 1 (Blocker).",
    global = true,
    project = true,
    type = PropertyType.INTEGER
  ),
  @Property(
    key = JiraConstants.JIRA_ISSUE_TYPE_ID,
    defaultValue = "3",
    name = "Id of JIRA issue type",
    description = "JIRA issue type id used to create JIRA issues for SonarQube issues. Default is 3 (= Task in a default JIRA installation).",
    global = true,
    project = true,
    type = PropertyType.INTEGER
  ),
  @Property(
    key = JiraConstants.JIRA_ISSUE_COMPONENT_ID,
    defaultValue = "",
    name = "Id of JIRA component",
    description = "JIRA component id used to create JIRA issues for SonarQube issues. By default no component is set.",
    global = false,
    project = true,
    type = PropertyType.INTEGER
  ),
  @Property(
    key = JiraConstants.JIRA_ISSUE_SONAR_LABEL,
    defaultValue = "SONAR",
    name = "Jira label",
    description = "JIRA label for all sonar issues.",
    global = true,
    project = true,
    type = PropertyType.STRING
  )
})
public class JiraIssueCreator implements ServerExtension {
	
  private static final Logger LOG = LoggerFactory.getLogger(JiraIssueCreator.class);
  private final RuleFinder ruleFinder;
  
  public JiraIssueCreator(RuleFinder ruleFinder) {
    this.ruleFinder = ruleFinder;
  }
  
  public BasicIssue createIssue(Issue sonarIssue, Settings settings) throws RemoteException {    
    try (JiraSession soapSession = createJiraSession(settings);) 
    {
    	return doCreateIssue(sonarIssue, soapSession, settings);
    }
  }
  
  protected JiraSession createJiraSession(Settings settings) {        
	Boolean useRest = settings.getBoolean(JiraConstants.JIRA_USE_REST_PROPERTY);
	  
	if(useRest)
    {
    	return createRestSession(settings);
    } else {
    	return createSoapSession(settings);
    }
  }
  
  private JiraSession createRestSession(Settings settings) {
	String jiraUrl = settings.getString(JiraConstants.SERVER_URL_PROPERTY);
	String baseUrl = settings.getString(JiraConstants.REST_BASE_URL_PROPERTY);
	
	String completeUrl = jiraUrl + baseUrl;
	try {
	  return new JiraRestSession(new URL(completeUrl));
  	} catch (MalformedURLException e) {
      LOG.error("The JIRA server URL is not a valid one: " + completeUrl, e);
      throw new IllegalStateException("The JIRA server URL is not a valid one: " + completeUrl, e);
    }
  }

  protected JiraSession createSoapSession(Settings settings) {
	String jiraUrl = settings.getString(JiraConstants.SERVER_URL_PROPERTY);
    String baseUrl = settings.getString(JiraConstants.SOAP_BASE_URL_PROPERTY);
    
    String completeUrl = jiraUrl + baseUrl;
	// get handle to the JIRA SOAP Service from a client point of view
    try {
      return new JiraSoapSession(new URL(completeUrl));      
    } catch (MalformedURLException e) {
      LOG.error("The JIRA server URL is not a valid one: " + completeUrl, e);
      throw new IllegalStateException("The JIRA server URL is not a valid one: " + completeUrl, e);
    }
  }
  
  protected BasicIssue doCreateIssue(Issue sonarIssue, JiraSession soapSession, Settings settings) 
  {
    // Connect to JIRA
    String userName = settings.getString(JiraConstants.USERNAME_PROPERTY);
    String password = settings.getString(JiraConstants.PASSWORD_PROPERTY);
    soapSession.connect(userName, password);
    // The JIRA SOAP Service and authentication token are used to make authentication calls
    JiraService jiraSoapService = soapSession.getJiraService(ruleFinder, settings);
    String authToken = soapSession.getAuthenticationToken();
    
    // And create the issue
    BasicIssue returnedIssue = jiraSoapService.createIssue(authToken, sonarIssue);
    String issueKey = returnedIssue.getKey();
    LOG.debug("Successfully created issue {}", issueKey);

    return returnedIssue;
  }
  
}
