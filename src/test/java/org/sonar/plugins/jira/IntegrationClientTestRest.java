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
