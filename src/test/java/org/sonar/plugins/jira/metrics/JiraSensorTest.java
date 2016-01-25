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

package org.sonar.plugins.jira.metrics;

import com.atlassian.jira.rest.client.api.domain.Filter;
import com.atlassian.jira.rpc.soap.client.JiraSoapService;
import com.atlassian.jira.rpc.soap.client.RemoteFilter;
import com.atlassian.jira.rpc.soap.client.RemoteIssue;
import com.atlassian.jira.rpc.soap.client.RemotePriority;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;
import org.mockito.Matchers;
import org.sonar.api.batch.SensorContext;
import org.sonar.api.config.Settings;
import org.sonar.api.resources.Project;
import org.sonar.api.rules.RuleFinder;
import org.sonar.api.test.IsMeasure;
import org.sonar.plugins.jira.JiraConstants;
import org.sonar.plugins.jira.soap.JiraSoapServiceWrapper;
import org.sonar.plugins.jira.soap.JiraSoapSession;

import java.rmi.RemoteException;
import java.util.Map;

import static org.fest.assertions.Assertions.assertThat;
import static org.mockito.Matchers.argThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoMoreInteractions;
import static org.mockito.Mockito.when;

public class JiraSensorTest {

    @Rule
    public ExpectedException thrown = ExpectedException.none();

    private JiraSensor sensor;
    private Settings settings;

    @Before
    public void setUp() {
        settings = new Settings();
        settings.setProperty(JiraConstants.SERVER_URL_PROPERTY, "http://my.jira.server");
        settings.setProperty(JiraConstants.USERNAME_PROPERTY, "admin");
        settings.setProperty(JiraConstants.PASSWORD_PROPERTY, "adminPwd");
        settings.setProperty(JiraConstants.FILTER_PROPERTY, "myFilter");
        sensor = new JiraSensor(settings);
    }

    @Test
    public void testToString() throws Exception {
        assertThat(sensor.toString()).isEqualTo("JIRA issues sensor");
    }

    @Test
    public void testPresenceOfProperties() throws Exception {
        assertThat(sensor.missingMandatoryParameters()).isEqualTo(false);

        settings.removeProperty(JiraConstants.PASSWORD_PROPERTY);
        sensor = new JiraSensor(settings);
        assertThat(sensor.missingMandatoryParameters()).isEqualTo(true);

        settings.removeProperty(JiraConstants.USERNAME_PROPERTY);
        sensor = new JiraSensor(settings);
        assertThat(sensor.missingMandatoryParameters()).isEqualTo(true);

        settings.removeProperty(JiraConstants.FILTER_PROPERTY);
        sensor = new JiraSensor(settings);
        assertThat(sensor.missingMandatoryParameters()).isEqualTo(true);

        settings.removeProperty(JiraConstants.SERVER_URL_PROPERTY);
        sensor = new JiraSensor(settings);
        assertThat(sensor.missingMandatoryParameters()).isEqualTo(true);
    }

    @Test
    public void shouldExecuteOnRootProjectWithAllParams() throws Exception {
        Project project = mock(Project.class);
        when(project.isRoot()).thenReturn(true).thenReturn(false);

        assertThat(sensor.shouldExecuteOnProject(project)).isEqualTo(true);
    }

    @Test
    public void shouldNotExecuteOnNonRootProject() throws Exception {
        assertThat(sensor.shouldExecuteOnProject(mock(Project.class))).isEqualTo(false);
    }

    @Test
    public void shouldNotExecuteOnRootProjectifOneParamMissing() throws Exception {
        Project project = mock(Project.class);
        when(project.isRoot()).thenReturn(true).thenReturn(false);

        settings.removeProperty(JiraConstants.SERVER_URL_PROPERTY);
        sensor = new JiraSensor(settings);

        assertThat(sensor.shouldExecuteOnProject(project)).isEqualTo(false);
    }

    @Test
    public void testSaveMeasures() {
        SensorContext context = mock(SensorContext.class);
        String url = "http://localhost/jira";
        String priorityDistribution = "Critical=1";

        sensor.saveMeasures(context, url, 1, priorityDistribution);

        verify(context).saveMeasure(argThat(new IsMeasure(JiraMetrics.ISSUES, 1.0, priorityDistribution)));
        verifyNoMoreInteractions(context);
    }

    @Test
    public void shouldCollectPriorities() throws Exception {
        JiraSoapService jiraSoapService = mock(JiraSoapService.class);
        RemotePriority priority1 = new RemotePriority();
        priority1.setId("1");
        priority1.setName("Minor");
        when(jiraSoapService.getPriorities("token")).thenReturn(new RemotePriority[] { priority1 });

        JiraSoapSession soapSession = mock(JiraSoapSession.class);
        when(soapSession.getJiraSoapService()).thenReturn(jiraSoapService);

        JiraSoapServiceWrapper wrapper = new JiraSoapServiceWrapper(jiraSoapService, null, settings);
        when(soapSession.getJiraService(Matchers.<RuleFinder> any(), Matchers.<Settings> any())).thenReturn(wrapper);

        Map<Long, String> foundPriorities = sensor.collectPriorities(wrapper, "token");
        assertThat(foundPriorities.size()).isEqualTo(1);
        assertThat(foundPriorities.get(1l)).isEqualTo("Minor");
    }

    @Test
    public void shouldCollectIssuesByPriority() throws Exception {
        Filter filter = new Filter(null, 1l, null, null, null, null, null, null, false);
        JiraSoapService jiraSoapService = mock(JiraSoapService.class);

        JiraSoapSession soapSession = mock(JiraSoapSession.class);
        when(soapSession.getJiraSoapService()).thenReturn(jiraSoapService);

        JiraSoapServiceWrapper wrapper = new JiraSoapServiceWrapper(jiraSoapService, null, settings);
        when(soapSession.getJiraService(Matchers.<RuleFinder> any(), Matchers.<Settings> any())).thenReturn(wrapper);

        RemoteIssue issue1 = new RemoteIssue();
        issue1.setPriority("1");
        issue1.setId("1");
        RemoteIssue issue2 = new RemoteIssue();
        issue2.setPriority("1");
        issue2.setId("2");
        RemoteIssue issue3 = new RemoteIssue();
        issue3.setPriority("2");
        issue3.setId("3");
        when(jiraSoapService.getIssuesFromFilter("token", "1")).thenReturn(new RemoteIssue[] { issue1, issue2, issue3 });

        Map<Long, Integer> foundIssues = sensor.collectIssuesByPriority(wrapper, "token", filter);
        assertThat(foundIssues.size()).isEqualTo(2);
        assertThat(foundIssues.get(1l)).isEqualTo(2);
        assertThat(foundIssues.get(2l)).isEqualTo(1);
    }

    @Test
    public void shouldFindFilters() throws Exception {
        JiraSoapService jiraSoapService = mock(JiraSoapService.class);

        JiraSoapSession soapSession = mock(JiraSoapSession.class);
        when(soapSession.getJiraSoapService()).thenReturn(jiraSoapService);

        JiraSoapServiceWrapper wrapper = new JiraSoapServiceWrapper(jiraSoapService, null, settings);
        when(soapSession.getJiraService(Matchers.<RuleFinder> any(), Matchers.<Settings> any())).thenReturn(wrapper);

        RemoteFilter filter1 = new RemoteFilter();
        filter1.setName("fooFilter");
        filter1.setId("1");
        RemoteFilter myFilter = new RemoteFilter();
        myFilter.setName("myFilter");
        myFilter.setId("2");
        when(jiraSoapService.getFavouriteFilters("token")).thenReturn(new RemoteFilter[] { filter1, myFilter });

        Filter foundFilter = sensor.findJiraFilter(wrapper, "token");
        assertThat(foundFilter.getName()).isEqualTo(myFilter.getName());
    }

    @Test
    public void shouldFindFiltersWithPreviousJiraVersions() throws Exception {
        JiraSoapService jiraSoapService = mock(JiraSoapService.class);

        JiraSoapSession soapSession = mock(JiraSoapSession.class);
        when(soapSession.getJiraSoapService()).thenReturn(jiraSoapService);

        JiraSoapServiceWrapper wrapper = new JiraSoapServiceWrapper(jiraSoapService, null, settings);
        when(soapSession.getJiraService(Matchers.<RuleFinder> any(), Matchers.<Settings> any())).thenReturn(wrapper);

        RemoteFilter myFilter = new RemoteFilter();
        myFilter.setName("myFilter");
        myFilter.setId("1");
        when(jiraSoapService.getSavedFilters("token")).thenReturn(new RemoteFilter[] { myFilter });
        when(jiraSoapService.getFavouriteFilters("token")).thenThrow(RemoteException.class);

        Filter foundFilter = sensor.findJiraFilter(wrapper, "token");
        assertThat(foundFilter.getName()).isEqualTo(myFilter.getName());
    }

    @Test
    public void faillIfNoFilterFound() throws Exception {
        JiraSoapService jiraSoapService = mock(JiraSoapService.class);

        JiraSoapSession soapSession = mock(JiraSoapSession.class);
        when(soapSession.getJiraSoapService()).thenReturn(jiraSoapService);

        JiraSoapServiceWrapper wrapper = new JiraSoapServiceWrapper(jiraSoapService, null, settings);
        when(soapSession.getJiraService(Matchers.<RuleFinder> any(), Matchers.<Settings> any())).thenReturn(wrapper);

        when(jiraSoapService.getFavouriteFilters("token")).thenReturn(new RemoteFilter[0]);

        thrown.expect(IllegalStateException.class);
        thrown.expectMessage("Unable to find filter 'myFilter' in JIRA");

        sensor.findJiraFilter(wrapper, "token");
    }

}
