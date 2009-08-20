/*
 * Sonar, open source software quality management tool.
 * Copyright (C) 2009 SonarSource SA
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
package org.sonar.plugins.jira;

import org.apache.commons.lang.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.sonar.api.batch.Sensor;
import org.sonar.api.batch.SensorContext;
import org.sonar.api.measures.Measure;
import org.sonar.api.resources.Project;

public class JiraSensor implements Sensor {

  private static final Logger LOG = LoggerFactory.getLogger(JiraSensor.class);

  private String serverURL;
  private String projectKey;
  private String login;
  private String password;
  private String urlParams;

  public void analyse(Project project, SensorContext context) {
    initParams(project);
    if (isMandatoryParametersNotEmpty()) {
      try {
        JiraWebService jiraWebService = new JiraWebService(serverURL, projectKey, login, password, urlParams);
        jiraWebService.init();
        JiraPriorities jiraPriorities = new JiraPriorities(jiraWebService.getIssues());
        saveMeasures(context, jiraWebService, jiraPriorities);

      } catch (Exception e) {
        LOG.error("Error accessing Jira web service, please verify the parameters. Returned error is '{}'", e.getMessage());
      }
    } else {
      LOG.error("The server url, the project key, the login and the password must not be empty.");
    }
  }

  private void initParams(Project project){
    this.serverURL = (String) project.getProperty(JiraPlugin.SERVER_URL);
    this.projectKey = (String) project.getProperty(JiraPlugin.PROJECT_KEY);
    this.login = (String) project.getProperty(JiraPlugin.LOGIN);
    this.password = (String) project.getProperty(JiraPlugin.PASSWORD);
    String urlParams = (String) project.getProperty(JiraPlugin.URL_PARAMS);
    this.urlParams = urlParams != null ? urlParams : JiraPlugin.DEFAULT_URL_PARAMS;
  }

  private boolean isMandatoryParametersNotEmpty() {
    return StringUtils.isNotEmpty(serverURL) && StringUtils.isNotEmpty(projectKey) &&
      StringUtils.isNotEmpty(login) && StringUtils.isNotEmpty(password);
  }

  private void saveMeasures(SensorContext context, JiraWebService jiraWebService, JiraPriorities jiraPriorities) {
    Measure totalOpenIssuesMeasure = new Measure(JiraMetrics.OPEN_ISSUES, (double) jiraPriorities.getTotalSize());
    totalOpenIssuesMeasure.setUrl(jiraWebService.getWebUrl());
    context.saveMeasure(totalOpenIssuesMeasure);

    Measure blockerIssuesMeasure = new Measure(JiraMetrics.BLOCKER_OPEN_ISSUES, (double) jiraPriorities.getBlockerSize());
    blockerIssuesMeasure.setUrl(jiraWebService.getPriorityUrl(jiraPriorities.getBlockerIndex()));
    context.saveMeasure(blockerIssuesMeasure);

    Measure criticalIssuesMeasure = new Measure(JiraMetrics.CRITICAL_OPEN_ISSUES, (double) jiraPriorities.getCriticalSize());
    criticalIssuesMeasure.setUrl(jiraWebService.getPriorityUrl(jiraPriorities.getCriticalIndex()));
    context.saveMeasure(criticalIssuesMeasure);

    Measure majorIssuesMeasure = new Measure(JiraMetrics.MAJOR_OPEN_ISSUES, (double) jiraPriorities.getMajorSize());
    majorIssuesMeasure.setUrl(jiraWebService.getPriorityUrl(jiraPriorities.getMajorIndex()));
    context.saveMeasure(majorIssuesMeasure);

    Measure minorIssuesMeasure = new Measure(JiraMetrics.MINOR_OPEN_ISSUES, (double) jiraPriorities.getMinorSize());
    minorIssuesMeasure.setUrl(jiraWebService.getPriorityUrl(jiraPriorities.getMinorIndex()));
    context.saveMeasure(minorIssuesMeasure);

    Measure trivalIssuesMeasure = new Measure(JiraMetrics.TRIVIAL_OPEN_ISSUES, (double) jiraPriorities.getTrivialSize());
    trivalIssuesMeasure.setUrl(jiraWebService.getPriorityUrl(jiraPriorities.getTrivialIndex()));
    context.saveMeasure(trivalIssuesMeasure);
  }

  public boolean shouldExecuteOnProject(Project project) {
    return project.isRoot();
  }

}
