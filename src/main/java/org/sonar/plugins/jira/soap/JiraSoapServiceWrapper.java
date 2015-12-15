package org.sonar.plugins.jira.soap;

import java.rmi.RemoteException;

import org.apache.commons.lang.StringUtils;
import org.sonar.api.config.Settings;
import org.sonar.api.issue.Issue;
import org.sonar.api.rules.RuleFinder;
import org.sonar.api.rules.RulePriority;
import org.sonar.plugins.jira.BasicJiraService;

import com.atlassian.jira.rest.client.api.domain.BasicIssue;
import com.atlassian.jira.rpc.soap.client.JiraSoapService;
import com.atlassian.jira.rpc.soap.client.RemoteAuthenticationException;
import com.atlassian.jira.rpc.soap.client.RemoteComponent;
import com.atlassian.jira.rpc.soap.client.RemoteIssue;
import com.atlassian.jira.rpc.soap.client.RemotePermissionException;
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
			throw new IllegalStateException("Impossible to connect to the JIRA server because of invalid credentials.",
					e);
		} catch (RemotePermissionException e) {
			throw new IllegalStateException(
					"Impossible to create the issue on the JIRA server because user does not have enough rights.", e);
		} catch (RemoteValidationException e) {
			// Unfortunately the detailed cause of the error is not in fault
			// details (ie stack) but only in fault string
			String message = StringUtils
					.removeStart(e.getFaultString(), "com.atlassian.jira.rpc.exception.RemoteValidationException:")
					.trim();
			throw new IllegalStateException("Impossible to create the issue on the JIRA server: " + message, e);
		} catch (RemoteException e) {
			throw new IllegalStateException("Impossible to create the issue on the JIRA server", e);
		}
	}
	
	private BasicIssue convertIssue(RemoteIssue createdIssue) {
		return new BasicIssue(null, createdIssue.getKey(), Long.valueOf(createdIssue.getId()));
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

}
