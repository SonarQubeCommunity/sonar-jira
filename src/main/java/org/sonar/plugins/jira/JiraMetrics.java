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

import java.util.Arrays;
import java.util.List;

import org.sonar.commons.Metric;
import org.sonar.plugins.api.metrics.Metrics;

public class JiraMetrics implements Metrics {

  public final static Metric ISSUES_PRIORITIES = new Metric("issues_priorities", "Issues priorities", "The distribution of issues by priority", Metric.ValueType.DISTRIB, Metric.DIRECTION_NONE, false, "Issues", false);
  public final static Metric ISSUES_STATUS = new Metric("issues_status", "Issues status", "The distribution of issues by status", Metric.ValueType.DISTRIB, Metric.DIRECTION_NONE, false, "Issues", false);
  public final static Metric ISSUES_TYPE = new Metric("issues_type", "Issues type", "The distribution of issues by type", Metric.ValueType.DISTRIB, Metric.DIRECTION_NONE, false, "Issues", false);
  public final static Metric ISSUES_RESOLUTION = new Metric("issues_resolution", "Issues resolution", "The distribution of issues by resolution", Metric.ValueType.DISTRIB, Metric.DIRECTION_NONE, false, "Issues", false);
  public final static Metric ISSUES_VERSION = new Metric("issues_version", "Issues version", "The distribution of issues by version", Metric.ValueType.DISTRIB, Metric.DIRECTION_NONE, false, "Issues", false);
  
  public final static Metric ISSUES_COUNT = new Metric("issues_count", "Issues count", "The total number of issues", Metric.ValueType.INT, Metric.DIRECTION_NONE, false, "Issues", false);
  public final static Metric OPEN_BUGS_COUNT = new Metric("open_bugs_count", "Open bugs", "The total number of open bugs", Metric.ValueType.INT, Metric.DIRECTION_NONE, false, "Issues", false);
  
  
  public List<Metric> getMetrics() {
    return Arrays.asList(ISSUES_PRIORITIES, ISSUES_STATUS, ISSUES_TYPE, ISSUES_RESOLUTION, ISSUES_VERSION, ISSUES_COUNT, OPEN_BUGS_COUNT);
  }

}
