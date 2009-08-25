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
    key = JiraPlugin.SERVER_URL,
    defaultValue = "",
    name = "JIRA server url",
    description = "example : http://jira.codehaus.org",
    project = true,
    module = true,
    global = true
  ),
  @Property(
    key = JiraPlugin.PROJECT_KEY,
    defaultValue = "",
    name = "JIRA project key",
    project = true,
    module = true,
    global = false
  ),
  @Property(
    key = JiraPlugin.LOGIN,
    defaultValue = "",
    name = "JIRA login",
    project = true,
    module = true,
    global = true
  ),
  @Property(
    key = JiraPlugin.PASSWORD,
    defaultValue = "",
    name = "JIRA pasword",
    project = true,
    module = true,
    global = true
  ),
  @Property(
    key = JiraPlugin.URL_PARAMS,
    defaultValue = JiraPlugin.DEFAULT_URL_PARAMS,
    name = "JIRA param url",
    project = true,
    module = true,
    global = true
  )

})
public class JiraPlugin implements Plugin {

  public final static String SERVER_URL = "sonar.jira.url";
  public final static String PROJECT_KEY = "sonar.jira.key";
  public final static String LOGIN = "sonar.jira.login";
  public final static String PASSWORD = "sonar.jira.password";
  public final static String URL_PARAMS = "sonar.jira.url.param";

  public final static String DEFAULT_URL_PARAMS = "reset=true&status=1&status=3&status=4&sorter/field=issuekey&sorter/order=DESC&sorter/field=priority&sorter/order=DESC";

  public String getDescription() {
    return "JIRA plugin, collect metrics on the JIRA server defined in the project pom";
  }

  public String getKey() {
    return "jira";
  }

  public String getName() {
    return "JIRA";
  }

  public List<Class<? extends Extension>> getExtensions() {
    List<Class<? extends Extension>> list = new ArrayList<Class<? extends Extension>>();
    list.add(JiraMetrics.class);
    list.add(JiraSensor.class);
    list.add(JiraWidget.class);
    return list;
  }

}
