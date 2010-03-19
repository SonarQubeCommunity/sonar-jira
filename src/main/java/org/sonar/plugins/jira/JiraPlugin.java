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

import org.sonar.api.Extension;
import org.sonar.api.Plugin;
import org.sonar.api.Properties;
import org.sonar.api.Property;

import java.util.ArrayList;
import java.util.List;


@Properties({
    @Property(
        key = JiraPlugin.SERVER_URL_PROPERTY,
        defaultValue = "",
        name = "JIRA server url",
        description = "Example : http://jira.codehaus.org",
        project = true,
        module = true,
        global = true
    ),
    @Property(
        key = JiraPlugin.PROJECT_KEY_PROPERTY,
        defaultValue = "",
        name = "JIRA project key",
        project = true,
        module = true,
        global = false
    ),
    @Property(
        key = JiraPlugin.USERNAME_PROPERTY,
        defaultValue = "",
        name = "JIRA username",
        project = true,
        module = true,
        global = true
    ),
    @Property(
        key = JiraPlugin.PASSWORD_PROPERTY,
        defaultValue = "",
        name = "JIRA password",
        project = true,
        module = true,
        global = true
    ),
    @Property(
        key = JiraPlugin.URL_PARAMS_PROPERTY,
        defaultValue = JiraPlugin.URL_PARAMS_DEFAULT_VALUE,
        name = "JIRA param url",
        project = true,
        module = true,
        global = true
    )

})
public class JiraPlugin implements Plugin {
  public final static String SERVER_URL_PROPERTY = "sonar.jira.url";
  public final static String PROJECT_KEY_PROPERTY = "sonar.jira.key";
  public final static String USERNAME_PROPERTY = "sonar.jira.login.secured";
  public final static String PASSWORD_PROPERTY = "sonar.jira.password.secured";
  public final static String URL_PARAMS_PROPERTY = "sonar.jira.url.param";
  public final static String URL_PARAMS_DEFAULT_VALUE = "reset=true&status=1&status=3&status=4&sorter/field=issuekey&sorter/order=DESC&sorter/field=priority&sorter/order=DESC";

  public String getKey() {
    return "jira";
  }

  public String getName() {
    return "JIRA";
  }

  public String getDescription() {
    return "This plugin retrieves number of issues associated to a project from <a href='http://www.atlassian.com/software/jira/'>JIRA</a>.";
  }

  public List<Class<? extends Extension>> getExtensions() {
    List<Class<? extends Extension>> list = new ArrayList<Class<? extends Extension>>();
    list.add(JiraMetrics.class);
    list.add(JiraSensor.class);
    list.add(JiraWidget.class);
    return list;
  }

}
