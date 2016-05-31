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

import java.net.MalformedURLException;
import java.net.URL;
import java.rmi.RemoteException;
import java.util.List;
import java.util.Map;

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
import org.sonar.plugins.jira.JiraService;
import org.sonar.plugins.jira.JiraSession;
import org.sonar.plugins.jira.rest.JiraRestSession;
import org.sonar.plugins.jira.soap.JiraSoapSession;

import com.atlassian.jira.rest.client.api.domain.BasicIssue;
import com.atlassian.jira.rest.client.api.domain.BasicPriority;
import com.atlassian.jira.rest.client.api.domain.Filter;
import com.atlassian.jira.rest.client.api.domain.Issue;
import com.atlassian.jira.rpc.soap.client.JiraSoapService;
import com.atlassian.jira.rpc.soap.client.RemoteFilter;
import com.atlassian.jira.rpc.soap.client.RemoteIssue;
import com.atlassian.jira.rpc.soap.client.RemotePriority;
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

  private boolean isUseRestApi() {
      return settings.getBoolean(JiraConstants.JIRA_USE_REST_PROPERTY);  
  }
  
  public boolean shouldExecuteOnProject(Project project) {
    if (missingMandatoryParameters()) {
      LOG.info("JIRA issues sensor will not run as some parameters are missing.");
    }
    return project.isRoot() && !missingMandatoryParameters();
  }
  
  public void analyse(Project project, SensorContext context) {
    try (JiraSession session = getSession();) {
      
      session.connect(getUsername(), getPassword());
      
      JiraService service = session.getJiraService(null, settings);
      String authToken = session.getAuthenticationToken();
      
      runAnalysis(context, service, authToken);
    } catch (RemoteException e) {
      LOG.error("Error accessing Jira web service, please verify the parameters", e);
    } catch (MalformedURLException e) {
      LOG.error("The specified JIRA URL is not valid: " + getServerUrl(), e);
    }
  }

  private JiraSession getSession() throws MalformedURLException {
      if(isUseRestApi())
          return new JiraSoapSession(new URL(getServerUrl() + "/"));
      else          
		  return new JiraRestSession(new URL(getServerUrl() + "/rpc/soap/jirasoapservice-v2"));
}

protected void runAnalysis(SensorContext context, JiraService service, String authToken) throws RemoteException {
    Map<Long, String> priorities = collectPriorities(service, authToken);
    Filter filter = findJiraFilter(service, authToken);
    Map<Long, Integer> issuesByPriority = collectIssuesByPriority(service, authToken, filter);

    double total = 0;
    PropertiesBuilder<String, Integer> distribution = new PropertiesBuilder<String, Integer>();
    for (Map.Entry<Long, Integer> entry : issuesByPriority.entrySet()) {
      total += entry.getValue();
      distribution.add(priorities.get(entry.getKey()), entry.getValue());
    }

    String url = getServerUrl() + "/secure/IssueNavigator.jspa?mode=hide&requestId=" + filter.getId();
    saveMeasures(context, url, total, distribution.buildData());
  }

  protected Map<Long, String> collectPriorities(JiraService service, String authToken) throws RemoteException {
    Map<Long, String> priorities = Maps.newHashMap();
    for (BasicPriority priority : service.getPriorities(authToken)) {
      priorities.put(priority.getId(), priority.getName());
    }
    return priorities;
  }

  protected Map<Long, Integer> collectIssuesByPriority(JiraService service, String authToken, Filter filter) throws RemoteException {
    Map<Long, Integer> issuesByPriority = Maps.newHashMap();
    List<Issue> issues = service.getIssuesFromFilter(authToken, filter.getId().toString());
    for (Issue issue : issues) {
      Long priority = issue.getPriority().getId();
      if (!issuesByPriority.containsKey(priority)) {
        issuesByPriority.put(priority, 1);
      } else {
        issuesByPriority.put(priority, issuesByPriority.get(priority) + 1);
      }
    }
    return issuesByPriority;
  }

  protected Filter findJiraFilter(JiraService service, String authToken) throws RemoteException {
    Filter filter = null;
    List<Filter> filters;
    try {
      filters = service.getFavouriteFilters(authToken);
    } catch (Exception e) {
      // for Jira prior to 3.13
      filters = service.getSavedFilters(authToken);
    }
    for (Filter f : filters) {
      if (getFilterName().equals(f.getName())) {
        filter = f;
        continue;
      }
    }

    if (filter == null) {
      throw new IllegalStateException("Unable to find filter '" + getFilterName() + "' in JIRA");
    }
    return filter;
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
