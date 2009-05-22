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
