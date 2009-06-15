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

import org.apache.axis.utils.StringUtils;
import org.sonar.plugins.jira.client.AbstractRemoteConstant;
import org.sonar.plugins.jira.client.RemoteIssueType;
import org.sonar.plugins.jira.client.RemotePriority;
import org.sonar.plugins.jira.client.RemoteResolution;
import org.sonar.plugins.jira.client.RemoteStatus;

public class JiraEntitiesLabels {
  
  private RemoteIssueType[] issueTypes;
  private RemoteStatus[] statusType;
  private RemoteResolution[] resolutionsType;
  private RemotePriority[] priorityTypes;
  
  public JiraEntitiesLabels(RemoteIssueType[] issueTypes, RemoteStatus[] statusType, RemoteResolution[] resolutionsType, RemotePriority[] priorityTypes) {
    super();
    this.issueTypes = issueTypes;
    this.statusType = statusType;
    this.resolutionsType = resolutionsType;
    this.priorityTypes = priorityTypes;
  }
  
  public String getTypeLabel(String key) {
    return getLabel(key, issueTypes);
  }
  
  public String getStatusLabel(String key) {
    return getLabel(key, statusType);
  }
  
  public String getResolutionLabel(String key) {
    return getLabel(key, resolutionsType);
  }
  
  public String getPriorityLabel(String key) {
    return getLabel(key, priorityTypes);
  }
  
  private String getLabel(String key, AbstractRemoteConstant[] constants) {
    if (StringUtils.isEmpty(key)) return "Undefined";
    for (AbstractRemoteConstant abstractRemoteConstant : constants) {
      if( abstractRemoteConstant.getId().equals(key)) return abstractRemoteConstant.getName();
    }
    throw new RuntimeException("Unable to find remote constant " + key);
  }
 
}
