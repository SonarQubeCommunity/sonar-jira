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

package org.sonar.plugins.jira.rest;

import java.io.IOException;
import java.net.URISyntaxException;
import java.net.URL;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.sonar.api.config.Settings;
import org.sonar.api.rules.RuleFinder;
import org.sonar.plugins.jira.JiraService;
import org.sonar.plugins.jira.JiraSession;

import com.atlassian.jira.rest.client.api.JiraRestClient;
import com.atlassian.jira.rest.client.api.JiraRestClientFactory;
import com.atlassian.jira.rest.client.internal.async.AsynchronousJiraRestClientFactory;

/**
 * This represents a REST session with JIRA
 */
public class JiraRestSession implements JiraSession {
	
	private static final Logger LOG = LoggerFactory.getLogger(JiraRestSession.class);

	private JiraRestClientFactory jcf;
	private JiraRestClient jiraRestClient;
	private URL webServiceUrl;
	
	public JiraRestSession(URL url) {
		this.webServiceUrl = url;
		jcf = new AsynchronousJiraRestClientFactory();
	}

	public void connect(String userName, String password) {
		LOG.debug("Connnecting via SOAP as : {}", userName);
		try {
			jiraRestClient = jcf.createWithBasicHttpAuthentication(webServiceUrl.toURI(), userName, password);
			LOG.debug("SOAP Session service endpoint at " + webServiceUrl.toString());
		} catch (URISyntaxException e) {
			throw new IllegalStateException("ServiceException during JiraSoapService contruction", e);
		}
		LOG.debug("Connected");
	}
	
	@Override
	public void close() {
		try {
			this.jiraRestClient.close();
		} catch (IOException e) {
			throw new IllegalStateException("ServiceException during JiraSoapService destruction", e);
		}
	}
	
	public JiraRestClient getJiraRestClient() {
		return jiraRestClient;
	}

	public URL getWebServiceUrl() {
		return webServiceUrl;
	}

	@Override
	public JiraService getJiraService(RuleFinder ruleFinder, Settings settings) {
		return new JiraRestServiceWrapper(this.getJiraRestClient(), ruleFinder, settings);
	}

	@Override
	public String getAuthenticationToken() {
		return null;
	}

}
