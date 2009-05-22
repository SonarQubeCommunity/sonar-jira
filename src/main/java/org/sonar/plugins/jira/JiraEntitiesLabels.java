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
