package org.sonar.plugins.jira.rest;

import java.util.Collection;
import java.util.List;
import java.util.Map.Entry;
import java.util.concurrent.ExecutionException;

import org.codehaus.jettison.json.JSONException;
import org.codehaus.jettison.json.JSONObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.sonar.api.config.Settings;
import org.sonar.api.issue.Issue;
import org.sonar.api.rules.RuleFinder;
import org.sonar.api.rules.RulePriority;
import org.sonar.plugins.jira.BasicJiraService;

import com.atlassian.jira.rest.client.api.JiraRestClient;
import com.atlassian.jira.rest.client.api.RestClientException;
import com.atlassian.jira.rest.client.api.domain.BasicIssue;
import com.atlassian.jira.rest.client.api.domain.IssueFieldId;
import com.atlassian.jira.rest.client.api.domain.input.ComplexIssueInputFieldValue;
import com.atlassian.jira.rest.client.api.domain.input.IssueInput;
import com.atlassian.jira.rest.client.api.domain.input.IssueInputBuilder;
import com.atlassian.jira.rest.client.api.domain.util.ErrorCollection;
import com.atlassian.jira.rest.client.internal.json.gen.IssueInputJsonGenerator;
import com.atlassian.jira.rpc.soap.client.RemoteComponent;
import com.atlassian.util.concurrent.Promise;
import com.google.common.collect.Lists;

public class JiraRestServiceWrapper extends BasicJiraService {

	private static final Logger LOG = LoggerFactory.getLogger(JiraRestServiceWrapper.class);
	
	private JiraRestClient jiraRestClient;
	
	public JiraRestServiceWrapper(JiraRestClient jiraRestClient, RuleFinder ruleFinder, Settings settings) {
		super(settings, ruleFinder);
		this.jiraRestClient = jiraRestClient;
	}
	
	@Override
	public BasicIssue createIssue(String authToken, Issue sonarIssue) {
		return createIssue(jiraRestClient.getIssueClient().createIssue(convertIssue(sonarIssue)));
	}
	
	private IssueInput convertIssue(Issue sonarIssue) {
		IssueInputBuilder builder = new IssueInputBuilder(getProject(), Long.valueOf(getType()), generateIssueSummary(sonarIssue));
		builder.setPriorityId(Long.valueOf(sonarSeverityToJiraPriorityId(RulePriority.valueOf(sonarIssue.severity()))));
		builder.setDescription(generateIssueDescription(sonarIssue));		
		
		String componentId = getComponentId();
	    if(componentId != null)
	    {	    	
	    	RemoteComponent rc = new RemoteComponent();
	    	rc.setId(componentId);
	    	builder.setFieldValue(IssueFieldId.COMPONENTS_FIELD.id, Lists.newArrayList(ComplexIssueInputFieldValue.with("id", componentId)));
	    }
	    
	    List<String> labels = getLabels();
	    if(labels != null && !labels.isEmpty())
	    	builder.setFieldValue(IssueFieldId.LABELS_FIELD.id, labels);
	    IssueInput in = builder.build();
	    
	    
	    
	    if(LOG.isDebugEnabled())
	    	generateJson(in);
	    return in;
	}
	
	private void generateJson( IssueInput in)
	{
		 try {
			IssueInputJsonGenerator generator = new IssueInputJsonGenerator();
			JSONObject generate = generator.generate(in);
	     	LOG.debug(generate.toString());
		 } catch(JSONException e) {
			 e.printStackTrace();
		 }
	}
	
	private BasicIssue createIssue(Promise<BasicIssue> createIssue) {
		try {
			BasicIssue basicIssue = createIssue.claim();			
			return basicIssue;
		} catch (RestClientException e) {
			Collection<ErrorCollection> errorCollections = e.getErrorCollections();
			throw new IllegalStateException(collectJiraErrorMessages(errorCollections), e);
		}
		
	}
	
	private String collectJiraErrorMessages(Collection<ErrorCollection> errorCollections) {
		StringBuilder stringBuilder = new StringBuilder();
		for(ErrorCollection errorCollection : errorCollections)
		{
			for (String string : errorCollection.getErrorMessages()) {
				stringBuilder.append(string).append('\n');
			}
			for (Entry<String, String> entry : errorCollection.getErrors().entrySet()) {
				//project is required may be project key is invalid ;)
				stringBuilder.append(entry.getValue()).append('\n');
			}
		}
		return stringBuilder.toString();
	}
	
}
