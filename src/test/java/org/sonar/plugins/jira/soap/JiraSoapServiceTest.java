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

import static org.fest.assertions.Assertions.assertThat;
import static org.mockito.Matchers.any;
import static org.mockito.Matchers.anyString;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.spy;
import static org.mockito.Mockito.when;

import java.rmi.RemoteException;

import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;
import org.sonar.api.CoreProperties;
import org.sonar.api.config.PropertyDefinitions;
import org.sonar.api.config.Settings;
import org.sonar.api.issue.Issue;
import org.sonar.api.issue.internal.DefaultIssue;
import org.sonar.api.rule.RuleKey;
import org.sonar.api.rules.RuleFinder;
import org.sonar.plugins.jira.JiraConstants;
import org.sonar.plugins.jira.JiraPlugin;
import org.sonar.plugins.jira.reviews.JiraIssueCreator;

import com.atlassian.jira.rpc.soap.client.JiraSoapService;
import com.atlassian.jira.rpc.soap.client.RemoteAuthenticationException;
import com.atlassian.jira.rpc.soap.client.RemoteComponent;
import com.atlassian.jira.rpc.soap.client.RemoteIssue;
import com.atlassian.jira.rpc.soap.client.RemotePermissionException;

public class JiraSoapServiceTest {

	@Rule
	public ExpectedException thrown = ExpectedException.none();

	private Issue sonarIssue;
	private Settings settings;
	private RuleFinder ruleFinder;

	@Before
	public void init() throws Exception {
		sonarIssue = new DefaultIssue().setKey("ABCD")
				.setMessage("The Cyclomatic Complexity of this method is 14 which is greater than 10 authorized.")
				.setSeverity("MINOR").setRuleKey(RuleKey.of("squid", "CycleBetweenPackages"));

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
	public void shouldFailToCreateIssueIfCantAuthenticate() throws Exception {
		// Given that
		JiraSoapService jiraSoapService = mock(JiraSoapService.class);
		doThrow(RemoteAuthenticationException.class).when(jiraSoapService).createIssue(anyString(),
				any(RemoteIssue.class));

		JiraSoapServiceWrapper soapService = new JiraSoapServiceWrapper(jiraSoapService, ruleFinder, settings);

		// Verify
		thrown.expect(IllegalStateException.class);
		thrown.expectMessage("Impossible to connect to the JIRA server because of invalid credentials.");
		soapService.createIssue("XXX", sonarIssue);
	}

	@Test
	public void shouldFailToCreateIssueIfNotEnoughRights() throws Exception {
		// Given that
		JiraSoapService jiraSoapService = mock(JiraSoapService.class);
		doThrow(RemotePermissionException.class).when(jiraSoapService).createIssue(anyString(), any(RemoteIssue.class));

		JiraSoapServiceWrapper soapService = new JiraSoapServiceWrapper(jiraSoapService, ruleFinder, settings);

		// Verify
		thrown.expect(IllegalStateException.class);
		thrown.expectMessage(
				"Impossible to create the issue on the JIRA server because user does not have enough rights.");
		soapService.createIssue("XXX", sonarIssue);
	}

	@Test
	public void shouldFailToCreateIssueIfRemoteError() throws Exception {
		// Given that
		JiraSoapService jiraSoapService = mock(JiraSoapService.class);
		doThrow(RemoteException.class).when(jiraSoapService).createIssue(anyString(), any(RemoteIssue.class));

		JiraSoapServiceWrapper soapService = new JiraSoapServiceWrapper(jiraSoapService, ruleFinder, settings);

		// Verify
		thrown.expect(IllegalStateException.class);
		thrown.expectMessage("Impossible to create the issue on the JIRA server");
		soapService.createIssue("XXX", sonarIssue);
	}

	@Test
	public void shouldInitRemoteIssue() throws Exception {
		// Given that
		RemoteIssue expectedIssue = new RemoteIssue();
		expectedIssue.setProject("TEST");
		expectedIssue.setType("3");
		expectedIssue.setPriority("4");
		expectedIssue.setSummary("SonarQube Issue #ABCD - Avoid cycle between java packages");
		expectedIssue.setDescription(
				"Issue detail:\n{quote}\nThe Cyclomatic Complexity of this method is 14 which is greater than 10 authorized.\n"
						+ "{quote}\n\n\nCheck it on SonarQube: http://my.sonar.com/issues/show/ABCD");

		JiraSoapService jiraSoapService = mock(JiraSoapService.class);
		JiraSoapServiceWrapper wrapper = new JiraSoapServiceWrapper(jiraSoapService, ruleFinder, settings);

		// Verify
		RemoteIssue returnedIssue = wrapper.convertIssue(sonarIssue);

		assertThat(returnedIssue.getSummary()).isEqualTo(expectedIssue.getSummary());
		assertThat(returnedIssue.getDescription()).isEqualTo(expectedIssue.getDescription());
		assertThat(returnedIssue).isEqualTo(expectedIssue);
	}

	@Test
	public void shouldInitRemoteIssueWithExtendedDescription() throws Exception {
		// Given that
		RemoteIssue expectedIssue = new RemoteIssue();
		expectedIssue.setProject("TEST");
		expectedIssue.setType("3");
		expectedIssue.setPriority("4");
		expectedIssue.setSummary("SonarQube Issue #ABCD - Avoid cycle between java packages");
		expectedIssue.setDescription(
				"Issue detail:\n{quote}\nThe Cyclomatic Complexity of this method is 14 which is greater than 10 authorized.\n"
						+ "{quote}\n\n" + "component main:project:org.sonar.plugins.jira.JiraPlugin.java, line 64"
						+ "\n\nCheck it on SonarQube: http://my.sonar.com/issues/show/ABCD");

		Issue filledIssue = new DefaultIssue().setKey("ABCD")
				.setMessage("The Cyclomatic Complexity of this method is 14 which is greater than 10 authorized.")
				.setSeverity("MINOR").setRuleKey(RuleKey.of("squid", "CycleBetweenPackages"))
				.setComponentKey("main:project:org.sonar.plugins.jira.JiraPlugin.java").setLine(64);

		JiraSoapService jiraSoapService = mock(JiraSoapService.class);
		JiraSoapServiceWrapper wrapper = new JiraSoapServiceWrapper(jiraSoapService, ruleFinder, settings);
		// Verify
		RemoteIssue returnedIssue = wrapper.convertIssue(filledIssue);

		assertThat(returnedIssue.getSummary()).isEqualTo(expectedIssue.getSummary());
		assertThat(returnedIssue.getDescription()).isEqualTo(expectedIssue.getDescription());
		assertThat(returnedIssue).isEqualTo(expectedIssue);
	}
	
	  @Test
	  public void shouldInitRemoteIssueWithTaskType() throws Exception {
	    // Given that
	    settings.setProperty(JiraConstants.JIRA_ISSUE_TYPE_ID, "4");
	    RemoteIssue expectedIssue = new RemoteIssue();
	    expectedIssue.setProject("TEST");
	    expectedIssue.setType("4");
	    expectedIssue.setPriority("4");
	    expectedIssue.setSummary("SonarQube Issue #ABCD - Avoid cycle between java packages");
	    expectedIssue.setDescription("Issue detail:\n{quote}\nThe Cyclomatic Complexity of this method is 14 which is greater than 10 authorized.\n" +
	      "{quote}\n\n\nCheck it on SonarQube: http://my.sonar.com/issues/show/ABCD");

	    JiraSoapService jiraSoapService = mock(JiraSoapService.class);
		JiraSoapServiceWrapper wrapper = new JiraSoapServiceWrapper(jiraSoapService, ruleFinder, settings);
	    // Verify
	    RemoteIssue returnedIssue = wrapper.convertIssue(sonarIssue);

	    assertThat(returnedIssue.getSummary()).isEqualTo(expectedIssue.getSummary());
	    assertThat(returnedIssue.getDescription()).isEqualTo(expectedIssue.getDescription());
	    assertThat(returnedIssue).isEqualTo(expectedIssue);
	  }

	  @Test
	  public void shouldInitRemoteIssueWithComponent() throws Exception {
	    // Given that
	    settings.setProperty(JiraConstants.JIRA_ISSUE_COMPONENT_ID, "123");
	    RemoteIssue expectedIssue = new RemoteIssue();
	    expectedIssue.setProject("TEST");
	    expectedIssue.setType("3");
	    expectedIssue.setPriority("4");
	    expectedIssue.setSummary("SonarQube Issue #ABCD - Avoid cycle between java packages");
	    expectedIssue.setDescription("Issue detail:\n{quote}\nThe Cyclomatic Complexity of this method is 14 which is greater than 10 authorized.\n" +
	      "{quote}\n\n\nCheck it on SonarQube: http://my.sonar.com/issues/show/ABCD");
	    expectedIssue.setComponents(new RemoteComponent[] {new RemoteComponent("123", null)});

	    JiraSoapService jiraSoapService = mock(JiraSoapService.class);
		JiraSoapServiceWrapper wrapper = new JiraSoapServiceWrapper(jiraSoapService, ruleFinder, settings);
	    // Verify
	    RemoteIssue returnedIssue = wrapper.convertIssue(sonarIssue);

	    assertThat(returnedIssue).isEqualTo(expectedIssue);
	  }

	  @Test
	  public void shouldInitRemoteIssueWithoutName() throws Exception {
	    // Given that
	    when(ruleFinder.findByKey(RuleKey.of("squid", "CycleBetweenPackages"))).thenReturn(org.sonar.api.rules.Rule.create().setName(null));

	    RemoteIssue expectedIssue = new RemoteIssue();
	    expectedIssue.setProject("TEST");
	    expectedIssue.setType("3");
	    expectedIssue.setPriority("4");
	    expectedIssue.setSummary("SonarQube Issue #ABCD");
	    expectedIssue.setDescription("Issue detail:\n{quote}\nThe Cyclomatic Complexity of this method is 14 which is greater than 10 authorized.\n" +
	      "{quote}\n\n\nCheck it on SonarQube: http://my.sonar.com/issues/show/ABCD");

	    JiraSoapService jiraSoapService = mock(JiraSoapService.class);
		JiraSoapServiceWrapper wrapper = new JiraSoapServiceWrapper(jiraSoapService, ruleFinder, settings);
	    // Verify
	    RemoteIssue returnedIssue = wrapper.convertIssue(sonarIssue);

	    assertThat(returnedIssue.getSummary()).isEqualTo(expectedIssue.getSummary());
	    assertThat(returnedIssue.getDescription()).isEqualTo(expectedIssue.getDescription());
	    assertThat(returnedIssue).isEqualTo(expectedIssue);
	  }

	  @Test
	  public void shouldFailToCreateIssueIfCantConnect() throws Exception {
		// Given that
		JiraSoapService soapService = mock(JiraSoapService.class);
		doThrow(RemoteException.class).when(soapService).login(anyString(), anyString());
	    
		JiraSoapSession soapSession = spy(new JiraSoapSession());
		doReturn(soapService).when(soapSession).getJiraSoapService();
		
	    // Verify
	    thrown.expect(IllegalStateException.class);
	    thrown.expectMessage("Impossible to connect to the JIRA server");

	    soapSession.connect("aa", "vv");
	  }
	  
}
