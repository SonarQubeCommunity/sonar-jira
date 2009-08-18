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

  public void analyse(Project project, SensorContext context) {

    String serverURL = (String) project.getProperty(JiraPlugin.SERVER_URL);
    String projectKey = (String) project.getProperty(JiraPlugin.PROJECT_KEY);
    String login = (String) project.getProperty(JiraPlugin.LOGIN);
    String password = (String) project.getProperty(JiraPlugin.PASSWORD);
    String urlParams = (String) project.getProperty(JiraPlugin.URL_PARAMS);

    if (StringUtils.isNotEmpty(serverURL)) {
      try {
        JiraIssuesReader jiraIssuesReader = new JiraIssuesReader(serverURL, projectKey, login, password, urlParams);
        JiraPriorities jiraPriorities = new JiraPriorities(jiraIssuesReader.getIssues());

        Measure measure = new Measure(JiraMetrics.OPEN_ISSUES, (double)jiraPriorities.getTotalSize());
        // FIXME add filter url to the url column of the measure, and add a link on the number of isssues to it in the widget
        context.saveMeasure(measure);

      } catch (Exception e) {
        throw new JiraException("Can't read jira rss", e);
      }


    } else {
      LOG.warn("No filter recorder!");
    }

  }

  public boolean shouldExecuteOnProject(Project project) {
    return project.isRoot();
  }

}
