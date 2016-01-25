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

import com.google.common.collect.ImmutableList;
import org.sonar.api.Properties;
import org.sonar.api.Property;
import org.sonar.api.SonarPlugin;
import org.sonar.plugins.jira.metrics.JiraMetrics;
import org.sonar.plugins.jira.metrics.JiraSensor;
import org.sonar.plugins.jira.metrics.JiraWidget;
import org.sonar.plugins.jira.reviews.JiraActionDefinition;
import org.sonar.plugins.jira.reviews.JiraIssueCreator;
import org.sonar.plugins.jira.reviews.LinkFunction;

import java.util.List;

@Properties({
  @Property(
    key = JiraConstants.SERVER_URL_PROPERTY,
    name = "Server URL",
    description = "Example : http://jira.codehaus.org",
    global = true,
    project = true,
    module = false
  ),
  @Property(
    key = JiraConstants.USERNAME_PROPERTY,
    defaultValue = "",
    name = "Username",
    global = true,
    project = true,
    module = false
  ),
  @Property(
    key = JiraConstants.PASSWORD_PROPERTY,
    name = "Password",
    global = true,
    project = true,
    module = false
  ),
  @Property(
    key = JiraConstants.JIRA_USE_REST_PROPERTY,
    defaultValue = "false",
    name = "Use rest api",
    description = "Use rest api instead of soap",
    global = true,
    project = false
  )
})
public final class JiraPlugin extends SonarPlugin {

  public List getExtensions() {
    return ImmutableList.of(
      // metrics part
      JiraMetrics.class, JiraSensor.class, JiraWidget.class,

      // issues part
      JiraIssueCreator.class, LinkFunction.class, JiraActionDefinition.class
    );
  }
}
