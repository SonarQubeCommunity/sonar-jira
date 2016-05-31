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

package org.sonar.plugins.jira.soap;

import java.rmi.RemoteException;
import java.util.ArrayList;
import java.util.List;

import org.apache.commons.lang.StringUtils;
import org.sonar.api.config.Settings;
import org.sonar.api.issue.Issue;
import org.sonar.api.rules.RuleFinder;
import org.sonar.api.rules.RulePriority;
import org.sonar.plugins.jira.BasicJiraService;

import com.atlassian.jira.rest.client.api.domain.BasicIssue;
import com.atlassian.jira.rest.client.api.domain.BasicPriority;
import com.atlassian.jira.rest.client.api.domain.Filter;
import com.atlassian.jira.rest.client.api.domain.IssueType;
import com.atlassian.jira.rpc.soap.client.JiraSoapService;
import com.atlassian.jira.rpc.soap.client.RemoteAuthenticationException;
import com.atlassian.jira.rpc.soap.client.RemoteComponent;
import com.atlassian.jira.rpc.soap.client.RemoteFilter;
import com.atlassian.jira.rpc.soap.client.RemoteIssue;
import com.atlassian.jira.rpc.soap.client.RemotePermissionException;
import com.atlassian.jira.rpc.soap.client.RemotePriority;
import com.atlassian.jira.rpc.soap.client.RemoteValidationException;

public class JiraSoapServiceWrapper extends BasicJiraService {

    private JiraSoapService jiraSoapService;

    public JiraSoapServiceWrapper(JiraSoapService jiraSoapService, RuleFinder ruleFinder, Settings settings) {
        super(settings, ruleFinder);
        this.jiraSoapService = jiraSoapService;
    }

    @Override
    public BasicIssue createIssue(String authToken, Issue sonarIssue) {
        try {
            return convertIssue(jiraSoapService.createIssue(authToken, convertIssue(sonarIssue)));
        } catch (RemoteAuthenticationException e) {
            throw new IllegalStateException("Impossible to connect to the JIRA server because of invalid credentials.", e);
        } catch (RemotePermissionException e) {
            throw new IllegalStateException("Impossible to create the issue on the JIRA server because user does not have enough rights.", e);
        } catch (RemoteValidationException e) {
            // Unfortunately the detailed cause of the error is not in fault
            // details (ie stack) but only in fault string
            String message = StringUtils.removeStart(e.getFaultString(), "com.atlassian.jira.rpc.exception.RemoteValidationException:").trim();
            throw new IllegalStateException("Impossible to create the issue on the JIRA server: " + message, e);
        } catch (RemoteException e) {
            throw new IllegalStateException("Impossible to create the issue on the JIRA server", e);
        }
    }

    private BasicIssue convertIssue(RemoteIssue createdIssue) {
        return new BasicIssue(null, createdIssue.getKey(), Long.valueOf(createdIssue.getId()));
    }

    private com.atlassian.jira.rest.client.api.domain.Issue convertFullIssue(RemoteIssue issue) {
        Long typeId = issue.getType() != null ? Long.valueOf(issue.getType()) : null;
        Long priorityId = issue.getPriority() != null ? Long.valueOf(issue.getPriority()) : null;
        BasicPriority basicPriority = new BasicPriority(null, priorityId, null);
        IssueType issueType = new IssueType(null, typeId, null, false, null, null); 
        return new com.atlassian.jira.rest.client.api.domain.Issue(issue.getSummary(), null, issue.getKey(), Long.valueOf(issue.getId()), 
                null, issueType, null, 
                issue.getDescription(), basicPriority, null, null, null, null, null, null,
                null, null, null, null, null, null, null,
                null, null, null, null, null, null, null, null, null, null);
    }

    RemoteIssue convertIssue(Issue sonarIssue) {
        RemoteIssue issue = new RemoteIssue();
        issue.setProject(getProject());
        issue.setType(getType());
        issue.setPriority(sonarSeverityToJiraPriorityId(RulePriority.valueOf(sonarIssue.severity())));
        issue.setSummary(generateIssueSummary(sonarIssue));
        issue.setDescription(generateIssueDescription(sonarIssue));

        String component = getComponentId();
        if (component != null) {
            RemoteComponent rc = new RemoteComponent();
            rc.setId(component);
            issue.setComponents(new RemoteComponent[] { rc });
        }

        return issue;
    }

    @Override
    public List<BasicPriority> getPriorities(String authToken) {
        try {
            RemotePriority[] priorities = jiraSoapService.getPriorities(authToken);
            List<BasicPriority> returnValue = new ArrayList<>();
            for (RemotePriority remotePriority : priorities) {
                returnValue.add(convertPriorty(remotePriority));
            }
            return returnValue;
        } catch (RemoteAuthenticationException e) {
            throw new IllegalStateException("Impossible to connect to the JIRA server because of invalid credentials.", e);
        } catch (RemotePermissionException e) {
            throw new IllegalStateException("Impossible to create the issue on the JIRA server because user does not have enough rights.", e);
        } catch (RemoteValidationException e) {
            // Unfortunately the detailed cause of the error is not in fault
            // details (ie stack) but only in fault string
            String message = StringUtils.removeStart(e.getFaultString(), "com.atlassian.jira.rpc.exception.RemoteValidationException:").trim();
            throw new IllegalStateException("Impossible to create the issue on the JIRA server: " + message, e);
        } catch (RemoteException e) {
            throw new IllegalStateException("Impossible to create the issue on the JIRA server", e);
        }
    }

    private BasicPriority convertPriorty(RemotePriority remotePriority) {
        return new BasicPriority(null, Long.valueOf(remotePriority.getId()), remotePriority.getName());
    }

    @Override
    public List<Filter> getFavouriteFilters(String authToken) {
        try {
            RemoteFilter[] favouriteFilters = jiraSoapService.getFavouriteFilters(authToken);
            List<Filter> returnValue = new ArrayList<>();
            for (RemoteFilter remoteFilter : favouriteFilters) {
                returnValue.add(convertFilter(remoteFilter, true));
            }
            return returnValue;
        } catch (RemoteAuthenticationException e) {
            throw new IllegalStateException("Impossible to connect to the JIRA server because of invalid credentials.", e);
        } catch (RemotePermissionException e) {
            throw new IllegalStateException("Impossible to create the issue on the JIRA server because user does not have enough rights.", e);
        } catch (RemoteValidationException e) {
            // Unfortunately the detailed cause of the error is not in fault
            // details (ie stack) but only in fault string
            String message = StringUtils.removeStart(e.getFaultString(), "com.atlassian.jira.rpc.exception.RemoteValidationException:").trim();
            throw new IllegalStateException("Impossible to create the issue on the JIRA server: " + message, e);
        } catch (RemoteException e) {
            throw new IllegalStateException("Impossible to create the issue on the JIRA server", e);
        }
    }

    private Filter convertFilter(RemoteFilter remoteFilter, boolean fav) {
        return new Filter(null, Long.valueOf(remoteFilter.getId()), remoteFilter.getName(), remoteFilter.getDescription(), null, null, null, null, fav);
    }

    @Override
    public List<Filter> getSavedFilters(String authToken) {
        try {
            RemoteFilter[] favouriteFilters = jiraSoapService.getSavedFilters(authToken);
            List<Filter> returnValue = new ArrayList<>();
            for (RemoteFilter remoteFilter : favouriteFilters) {
                returnValue.add(convertFilter(remoteFilter, false));
            }
            return returnValue;
        } catch (RemoteAuthenticationException e) {
            throw new IllegalStateException("Impossible to connect to the JIRA server because of invalid credentials.", e);
        } catch (RemotePermissionException e) {
            throw new IllegalStateException("Impossible to create the issue on the JIRA server because user does not have enough rights.", e);
        } catch (RemoteValidationException e) {
            // Unfortunately the detailed cause of the error is not in fault
            // details (ie stack) but only in fault string
            String message = StringUtils.removeStart(e.getFaultString(), "com.atlassian.jira.rpc.exception.RemoteValidationException:").trim();
            throw new IllegalStateException("Impossible to create the issue on the JIRA server: " + message, e);
        } catch (RemoteException e) {
            throw new IllegalStateException("Impossible to create the issue on the JIRA server", e);
        }
    }

    @Override
    public List<com.atlassian.jira.rest.client.api.domain.Issue> getIssuesFromFilter(String authToken, String id) {
        try {
            RemoteIssue[] issuesFromFilter = jiraSoapService.getIssuesFromFilter(authToken, id);
            List<com.atlassian.jira.rest.client.api.domain.Issue> returnValue = new ArrayList<>();
            for (RemoteIssue remoteIssue : issuesFromFilter) {
                returnValue.add(convertFullIssue(remoteIssue));
            }
            return returnValue;
        } catch (RemoteAuthenticationException e) {
            throw new IllegalStateException("Impossible to connect to the JIRA server because of invalid credentials.", e);
        } catch (RemotePermissionException e) {
            throw new IllegalStateException("Impossible to create the issue on the JIRA server because user does not have enough rights.", e);
        } catch (RemoteValidationException e) {
            // Unfortunately the detailed cause of the error is not in fault
            // details (ie stack) but only in fault string
            String message = StringUtils.removeStart(e.getFaultString(), "com.atlassian.jira.rpc.exception.RemoteValidationException:").trim();
            throw new IllegalStateException("Impossible to create the issue on the JIRA server: " + message, e);
        } catch (RemoteException e) {
            throw new IllegalStateException("Impossible to create the issue on the JIRA server", e);
        }
    }

}
