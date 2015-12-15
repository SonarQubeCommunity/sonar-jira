package org.sonar.plugins.jira;

import static org.fest.assertions.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import org.junit.Before;
import org.junit.Test;
import org.sonar.api.CoreProperties;
import org.sonar.api.config.PropertyDefinitions;
import org.sonar.api.config.Settings;
import org.sonar.api.rule.RuleKey;
import org.sonar.api.rules.RuleFinder;
import org.sonar.api.rules.RulePriority;
import org.sonar.plugins.jira.reviews.JiraIssueCreator;
import org.sonar.plugins.jira.soap.JiraSoapServiceWrapper;

import com.atlassian.jira.rpc.soap.client.JiraSoapService;

public class BasicJiraServiceTest {

	private Settings settings;
	private RuleFinder ruleFinder;

	@Before
	public void init() throws Exception {
		settings = new Settings(new PropertyDefinitions(JiraIssueCreator.class, JiraPlugin.class));
		settings.setProperty(CoreProperties.SERVER_BASE_URL, "http://my.sonar.com");
		settings.setProperty(JiraConstants.SERVER_URL_PROPERTY, "http://my.jira.com");
		settings.setProperty(JiraConstants.USERNAME_PROPERTY, "foo");
		settings.setProperty(JiraConstants.PASSWORD_PROPERTY, "bar");
		settings.setProperty(JiraConstants.JIRA_PROJECT_KEY_PROPERTY, "TEST");

		ruleFinder = mock(RuleFinder.class);
		when(ruleFinder.findByKey(RuleKey.of("squid", "CycleBetweenPackages")))
				.thenReturn(org.sonar.api.rules.Rule.create().setName("Avoid cycle between java packages"));
	}

	@Test
	public void shouldGiveDefaultPriority() throws Exception {
		JiraSoapService jiraSoapService = mock(JiraSoapService.class);
		BasicJiraService wrapper = new JiraSoapServiceWrapper(jiraSoapService, ruleFinder, settings);

		assertThat(wrapper.sonarSeverityToJiraPriorityId(RulePriority.BLOCKER)).isEqualTo("1");
		assertThat(wrapper.sonarSeverityToJiraPriorityId(RulePriority.CRITICAL)).isEqualTo("2");
		assertThat(wrapper.sonarSeverityToJiraPriorityId(RulePriority.MAJOR)).isEqualTo("3");
		assertThat(wrapper.sonarSeverityToJiraPriorityId(RulePriority.MINOR)).isEqualTo("4");
		assertThat(wrapper.sonarSeverityToJiraPriorityId(RulePriority.INFO)).isEqualTo("5");
	}

}
