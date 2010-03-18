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

import com.atlassian.jira.rpc.soap.client.JiraSoapService;
import com.atlassian.jira.rpc.soap.client.RemoteFilter;
import com.atlassian.jira.rpc.soap.client.RemoteIssue;
import com.atlassian.jira.rpc.soap.client.RemotePriority;
import org.apache.commons.lang.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.sonar.api.batch.Sensor;
import org.sonar.api.batch.SensorContext;
import org.sonar.api.measures.Measure;
import org.sonar.api.measures.PropertiesBuilder;
import org.sonar.api.resources.Project;
import org.sonar.plugins.jira.soap.JiraSoapSession;

import java.net.URL;
import java.util.HashMap;
import java.util.Map;

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
        JiraSoapSession session = new JiraSoapSession(new URL(serverURL + "/rpc/soap/jirasoapservice-v2"));
        session.connect(login, password);

        JiraSoapService service = session.getJiraSoapService();
        String authToken = session.getAuthenticationToken();

        Map<String, String> priorities = new HashMap<String, String>();
        for (RemotePriority priority : service.getPriorities(authToken)) {
          priorities.put(priority.getId(), priority.getName());
        }

        Map<String, Integer> issuesByPriority = new HashMap<String, Integer>();
        RemoteIssue[] issues = getIssuesForFilter(session, urlParams); // TODO replace urlParams by filterName
        for (RemoteIssue issue : issues) {
          String priority = issue.getPriority();
          if (!issuesByPriority.containsKey(priority)) {
            issuesByPriority.put(priority, 1);
          } else {
            issuesByPriority.put(priority, issuesByPriority.get(priority) + 1);
          }
        }

        double total = 0;
        PropertiesBuilder<String, Integer> distribution = new PropertiesBuilder<String, Integer>();
        for (Map.Entry<String, Integer> entry : issuesByPriority.entrySet()) {
          total += entry.getValue();
          distribution.add(priorities.get(entry.getKey()), entry.getValue());
        }
        saveMeasures(context, serverURL, total, distribution.buildData());
      } catch (Exception e) {
        LOG.error("Error accessing Jira web service, please verify the parameters. Returned error is '{}'", e.getMessage());
      }
    } else {
      LOG.error("The server url, the project key, the login and the password must not be empty.");
    }
  }

  private void initParams(Project project) {
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

  protected void saveMeasures(SensorContext context, String issueUrl, double totalPrioritiesCount, String priorityDistribution) {
    Measure issuesMeasure = new Measure(JiraMetrics.ISSUES, totalPrioritiesCount);
    issuesMeasure.setData(priorityDistribution);
    context.saveMeasure(issuesMeasure);

    Measure issuesUrlMeasure = new Measure(JiraMetrics.ISSUES_URL, issueUrl);
    context.saveMeasure(issuesUrlMeasure);
  }

  public boolean shouldExecuteOnProject(Project project) {
    return project.isRoot();
  }

  public static RemoteIssue[] getIssuesForFilter(JiraSoapSession session, String filterName) throws Exception {
    JiraSoapService service = session.getJiraSoapService();
    String authToken = session.getAuthenticationToken();

    RemoteFilter[] filters = service.getFavouriteFilters(session.getAuthenticationToken());
    for (RemoteFilter filter : filters) {
      if (filterName.equals(filter.getName())) {
        return service.getIssuesFromFilter(authToken, filter.getId());
      }
    }
    return null;
  }
}
