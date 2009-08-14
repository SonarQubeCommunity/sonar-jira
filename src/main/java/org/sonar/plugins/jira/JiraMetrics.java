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

  public final static Metric ISSUES_PRIORITIES = new Metric("issues_priorities", "Issues priorities", "The distribution of issues by priority", Metric.ValueType.DISTRIB, Metric.DIRECTION_NONE, false, "Issues");
  public final static Metric ISSUES_COUNT = new Metric("issues_count", "Issues count", "The total number of issues", Metric.ValueType.INT, Metric.DIRECTION_NONE, false, "Issues");
    
  public List<Metric> getMetrics() {
    return Arrays.asList(ISSUES_PRIORITIES, ISSUES_COUNT);
  }

}
