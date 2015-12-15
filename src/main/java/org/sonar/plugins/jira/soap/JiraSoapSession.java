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

import java.net.URL;
import java.rmi.RemoteException;

import javax.xml.rpc.ServiceException;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.sonar.api.config.Settings;
import org.sonar.api.rules.RuleFinder;
import org.sonar.plugins.jira.JiraService;
import org.sonar.plugins.jira.JiraSession;

import com.atlassian.jira.rpc.soap.client.JiraSoapService;
import com.atlassian.jira.rpc.soap.client.JiraSoapServiceService;
import com.atlassian.jira.rpc.soap.client.JiraSoapServiceServiceLocator;

/**
 * This represents a SOAP session with JIRA including that state of being logged in or not
 */
public class JiraSoapSession implements JiraSession {
	
  private static final Logger LOG = LoggerFactory.getLogger(JiraSoapSession.class);

  private JiraSoapServiceService jiraSoapServiceLocator;
  private JiraSoapService jiraSoapService;
  private String token;
  private URL webServiceUrl;
  
  public JiraSoapSession(URL url) {
    this.webServiceUrl = url;
    jiraSoapServiceLocator = new JiraSoapServiceServiceLocator();
    try {
      jiraSoapService = jiraSoapServiceLocator.getJirasoapserviceV2(url);
      LOG.debug("SOAP Session service endpoint at " + url.toExternalForm());
    } catch (ServiceException e) {
      throw new IllegalStateException("ServiceException during JiraSoapService contruction", e);
    }
  }
  
  protected JiraSoapSession()
  {	 
  }

  public void connect(String userName, String password) {
    LOG.debug("Connnecting via SOAP as : {}", userName);
    try {
    	token = getJiraSoapService().login(userName, password);
  	} catch (RemoteException e) {
      throw new IllegalStateException("Impossible to connect to the JIRA server (" + webServiceUrl + ").", e);
    }
    LOG.debug("Connected");
  }
  
  @Override
  public void close() {
	  try {
		getJiraSoapService().logout(getAuthenticationToken());
	} catch (RemoteException e) {
		throw new IllegalStateException(e);
	}
  }

  public String getAuthenticationToken() {
    return token;
  }

  public JiraSoapService getJiraSoapService() {
    return jiraSoapService;
  }

  public JiraSoapServiceService getJiraSoapServiceLocator() {
    return jiraSoapServiceLocator;
  }

  public URL getWebServiceUrl() {
    return webServiceUrl;
  }

  @Override
  public JiraService getJiraService(RuleFinder ruleFinder, Settings settings) {
	 return new JiraSoapServiceWrapper(this.getJiraSoapService() ,ruleFinder, settings);
  }
  
}
