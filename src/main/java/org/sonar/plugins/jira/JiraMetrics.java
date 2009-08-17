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

import org.sonar.api.measures.Metrics;
import org.sonar.api.measures.Metric;

import java.util.Arrays;
import java.util.List;


public class JiraMetrics implements Metrics {

  public final static Metric OPEN_ISSUES = new Metric("open_issues", "Open issues", "The total number of issues", Metric.ValueType.INT, Metric.DIRECTION_NONE, false, "Issues");
  public final static Metric BLOCKER_OPEN_ISSUES = new Metric("blocker_open_issues", "Blocker issues", "The number of blocker issues", Metric.ValueType.INT, Metric.DIRECTION_NONE, false, "Issues");
  public final static Metric CRITICAL_OPEN_ISSUES = new Metric("critical_open_issues", "Critical issues", "The number of critical issues", Metric.ValueType.INT, Metric.DIRECTION_NONE, false, "Issues");
  public final static Metric MAJOR_OPEN_ISSUES = new Metric("major_open_issues", "Major issues", "The number of major issues", Metric.ValueType.INT, Metric.DIRECTION_NONE, false, "Issues");
  public final static Metric MINOR_OPEN_ISSUES = new Metric("minor_open_issues", "Minor issues", "The number of minor issues", Metric.ValueType.INT, Metric.DIRECTION_NONE, false, "Issues");
  public final static Metric TRIVIAL_OPEN_ISSUES = new Metric("trivial_open_issues", "Trivial issues", "The number of trivial issues", Metric.ValueType.INT, Metric.DIRECTION_NONE, false, "Issues");

  public List<Metric> getMetrics() {
    return Arrays.asList(OPEN_ISSUES, BLOCKER_OPEN_ISSUES, CRITICAL_OPEN_ISSUES, MAJOR_OPEN_ISSUES, MINOR_OPEN_ISSUES, TRIVIAL_OPEN_ISSUES);
  }

}
