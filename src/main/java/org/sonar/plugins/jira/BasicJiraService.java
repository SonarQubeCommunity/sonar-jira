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

import java.util.ArrayList;
import java.util.List;

import org.sonar.api.CoreProperties;
import org.sonar.api.config.Settings;
import org.sonar.api.issue.Issue;
import org.sonar.api.rules.Rule;
import org.sonar.api.rules.RuleFinder;
import org.sonar.api.rules.RulePriority;
import org.sonar.api.utils.SonarException;

public abstract class BasicJiraService implements JiraService {

	private final Settings settings;
	private final RuleFinder ruleFinder;

	private static final String QUOTE = "\n{quote}\n";
	
	public BasicJiraService(Settings sonarSettings, RuleFinder ruleFinder) {
		super();
		this.settings = sonarSettings;
		this.ruleFinder = ruleFinder;
	}
	
	protected String getType() {
		return settings.getString(JiraConstants.JIRA_ISSUE_TYPE_ID);
	}
	
	protected String getProject() {
		return settings.getString(JiraConstants.JIRA_PROJECT_KEY_PROPERTY);
	}
	
	protected String sonarSeverityToJiraPriorityId(RulePriority reviewSeverity) {
		    final String priorityId;
		    switch (reviewSeverity) {
		      case INFO:
		        priorityId = settings.getString(JiraConstants.JIRA_INFO_PRIORITY_ID);
		        break;
		      case MINOR:
		        priorityId = settings.getString(JiraConstants.JIRA_MINOR_PRIORITY_ID);
		        break;
		      case MAJOR:
		        priorityId = settings.getString(JiraConstants.JIRA_MAJOR_PRIORITY_ID);
		        break;
		      case CRITICAL:
		        priorityId = settings.getString(JiraConstants.JIRA_CRITICAL_PRIORITY_ID);
		        break;
		      case BLOCKER:
		        priorityId = settings.getString(JiraConstants.JIRA_BLOCKER_PRIORITY_ID);
		        break;
		      default:
		        throw new SonarException("Unable to convert review severity to JIRA priority: " + reviewSeverity);
		    }
		    return priorityId;
	}
	
	protected String generateIssueSummary(Issue sonarIssue) {
		  Rule rule = ruleFinder.findByKey(sonarIssue.ruleKey());

		  StringBuilder summary = new StringBuilder("SonarQube Issue #");
		  summary.append(sonarIssue.key());
		  if (rule != null && rule.getName() != null) {
		      summary.append(" - ");
		      summary.append(rule.getName().toString());
		    }
		    return summary.toString();
	}
	
    protected String generateIssueDescription(Issue sonarIssue) {
		    StringBuilder description = new StringBuilder("Issue detail:");
		    description.append(QUOTE);
		    description.append(sonarIssue.message());
		    description.append(QUOTE);
		    if(sonarIssue.componentKey() != null)
		    	description.append("\ncomponent "+sonarIssue.componentKey());
			if(sonarIssue.line() != null && sonarIssue.line() > 0)
				description.append(", line "+sonarIssue.line());		
		    description.append("\n\nCheck it on SonarQube: ");
		    description.append(settings.getString(CoreProperties.SERVER_BASE_URL));
		    description.append("/issues/show/");
		    description.append(sonarIssue.key());
		    return description.toString();
	}
	
    protected String getComponentId()
    {
    	if (settings.hasKey(JiraConstants.JIRA_ISSUE_COMPONENT_ID)) {
  	      	return settings.getString(JiraConstants.JIRA_ISSUE_COMPONENT_ID);  	      
    	} else {
    		return null;
    	}
    }
    
    protected List<String> getLabels() {
		List<String> labels = new ArrayList<>();
		String sonarLabel = settings.getString(JiraConstants.JIRA_ISSUE_SONAR_LABEL);
		if(sonarLabel != null)
			labels.add(sonarLabel);
		return labels;
	}
    
}
