package org.sonar.plugins.jira;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.io.FileInputStream;
import java.util.HashMap;
import java.util.Map;
import java.util.Properties;

import org.sonar.api.config.PropertyDefinitions;
import org.sonar.api.config.Settings;
import org.sonar.api.issue.internal.DefaultIssue;
import org.sonar.api.rule.RuleKey;
import org.sonar.api.rules.RuleFinder;
import org.sonar.plugins.jira.rest.JiraRestServiceWrapper;
import org.sonar.plugins.jira.reviews.JiraIssueCreator;

public class IntegrationClientTestRest {
	
	public static void main(String[] args) throws Exception {
		setLoggingLevel(ch.qos.logback.classic.Level.DEBUG);
		
		RuleFinder ruleFinder = mock(RuleFinder.class);
	    when(ruleFinder.findByKey(RuleKey.of("squid", "CycleBetweenPackages"))).thenReturn(org.sonar.api.rules.Rule.create().setName("Avoid cycle between java packages"));
		JiraIssueCreator jiraIssueCreator = new JiraIssueCreator(ruleFinder);
		
		Properties properties = new Properties();
		properties.load(new FileInputStream("sonar.test.properties"));
		
		Map<String, String> map = new HashMap<String, String>();
		for (final String name: properties.stringPropertyNames())
		    map.put(name, properties.getProperty(name));
		
		Settings settings = new Settings(new PropertyDefinitions(JiraIssueCreator.class, JiraPlugin.class));
		settings.setProperties(map);
		
	    DefaultIssue sonarIssue = new DefaultIssue()
	    	      .setKey("ABCD")
	    	      .setMessage("The Cyclomatic Complexity of this method is 14 which is greater than 10 authorized.")
	    	      .setSeverity("MINOR")
	    	      .setRuleKey(RuleKey.of("squid", "CycleBetweenPackages"));
	    
		jiraIssueCreator.createIssue(sonarIssue, settings);
	}
	
	public static void setLoggingLevel(ch.qos.logback.classic.Level level) {
	    ch.qos.logback.classic.Logger root = (ch.qos.logback.classic.Logger) org.slf4j.LoggerFactory.getLogger(JiraRestServiceWrapper.class);
	    root.setLevel(level);
	}
	
}
