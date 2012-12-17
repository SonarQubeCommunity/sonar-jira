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

import com.atlassian.jira.rpc.soap.client.JiraSoapService;
import com.atlassian.jira.rpc.soap.client.JiraSoapServiceService;
import com.atlassian.jira.rpc.soap.client.JiraSoapServiceServiceLocator;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.xml.rpc.ServiceException;

import java.net.URL;
import java.rmi.RemoteException;

/**
 * This represents a SOAP session with JIRA including that state of being logged in or not
 */
public class JiraSoapSession {
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

  public void connect(String userName, String password) throws RemoteException {
    LOG.debug("Connnecting via SOAP as : {}", userName);
    token = getJiraSoapService().login(userName, password);
    LOG.debug("Connected");
  }

  public void disconnect() throws RemoteException {
    getJiraSoapService().logout(getAuthenticationToken());
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
}
