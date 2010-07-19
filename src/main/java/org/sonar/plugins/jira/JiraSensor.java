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

package org.sonar.plugins.jira;

import com.atlassian.jira.rpc.soap.client.JiraSoapService;
import com.atlassian.jira.rpc.soap.client.RemoteFilter;
import com.atlassian.jira.rpc.soap.client.RemoteIssue;
import com.atlassian.jira.rpc.soap.client.RemotePriority;
import org.apache.commons.configuration.Configuration;
import org.apache.commons.lang.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.sonar.api.batch.Sensor;
import org.sonar.api.batch.SensorContext;
import org.sonar.api.measures.Measure;
import org.sonar.api.measures.PropertiesBuilder;
import org.sonar.api.resources.Project;
import org.sonar.api.utils.SonarException;
import org.sonar.plugins.jira.soap.JiraSoapSession;

import java.net.URL;
import java.rmi.RemoteException;
import java.util.HashMap;
import java.util.Map;

public class JiraSensor implements Sensor {
  private static final Logger LOG = LoggerFactory.getLogger(JiraSensor.class);

  private String serverUrl;
  private String username;
  private String password;
  private String filterName;

  public void analyse(Project project, SensorContext context) {
    initParams(project);
    if (!isMandatoryParametersNotEmpty()) {
      LOG.warn("The server url, the filter name, the username and the password must not be empty.");
      return;
    }
    try {
      JiraSoapSession session = new JiraSoapSession(new URL(serverUrl + "/rpc/soap/jirasoapservice-v2"));
      session.connect(username, password);

      JiraSoapService service = session.getJiraSoapService();
      String authToken = session.getAuthenticationToken();

      analyze(context, service, authToken);

      session.disconnect();
    } catch (Exception e) {
      LOG.error("Error accessing Jira web service, please verify the parameters", e);
    }
  }

  private void analyze(SensorContext context, JiraSoapService service, String authToken) throws RemoteException {
    // TODO use google-collections
    Map<String, String> priorities = new HashMap<String, String>();
    for (RemotePriority priority : service.getPriorities(authToken)) {
      priorities.put(priority.getId(), priority.getName());
    }

    RemoteFilter[] filters;
    try {
      filters = service.getFavouriteFilters(authToken);
    } catch (Exception e) {
      // for Jira prior to 3.13
      filters = service.getSavedFilters(authToken);
    }
    RemoteFilter filter = null;
    for (RemoteFilter f : filters) {
      if (filterName.equals(f.getName())) {
        filter = f;
      }
    }

    if (filter == null) {
      throw new SonarException("Unable to find filter '" + filterName + "' in JIRA");
    }

    RemoteIssue[] issues = service.getIssuesFromFilter(authToken, filter.getId());
    Map<String, Integer> issuesByPriority = new HashMap<String, Integer>();
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

    String url = serverUrl + "/secure/IssueNavigator.jspa?mode=hide&requestId=" + filter.getId();
    saveMeasures(context, url, total, distribution.buildData());
  }

  private void initParams(Project project) {
    Configuration configuration = project.getConfiguration();
    serverUrl = configuration.getString(JiraPlugin.SERVER_URL_PROPERTY);
    username = configuration.getString(JiraPlugin.USERNAME_PROPERTY);
    password = configuration.getString(JiraPlugin.PASSWORD_PROPERTY);
    filterName = configuration.getString(JiraPlugin.FILTER_PROPERTY);
  }

  private boolean isMandatoryParametersNotEmpty() {
    return StringUtils.isNotEmpty(serverUrl) &&
        StringUtils.isNotEmpty(filterName) &&
        StringUtils.isNotEmpty(username) &&
        StringUtils.isNotEmpty(password);
  }

  protected void saveMeasures(SensorContext context, String issueUrl, double totalPrioritiesCount, String priorityDistribution) {
    Measure issuesMeasure = new Measure(JiraMetrics.ISSUES, totalPrioritiesCount);
    issuesMeasure.setUrl(issueUrl);
    issuesMeasure.setData(priorityDistribution);
    context.saveMeasure(issuesMeasure);
  }

  public boolean shouldExecuteOnProject(Project project) {
    return project.isRoot();
  }

  @Override
  public String toString() {
    return getClass().getSimpleName();
  }
}
