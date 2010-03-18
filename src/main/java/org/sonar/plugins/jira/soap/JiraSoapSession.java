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
 * @author Evgeny Mandrikov
 */
public class JiraSoapSession {
  private static final Logger LOG = LoggerFactory.getLogger(JiraSoapSession.class);

  private JiraSoapServiceService jiraSoapServiceLocator;
  private JiraSoapService jiraSoapService;
  private String token;

  public JiraSoapSession(URL webServicePort) {
    jiraSoapServiceLocator = new JiraSoapServiceServiceLocator();
    try {
      if (webServicePort == null) {
        jiraSoapService = jiraSoapServiceLocator.getJirasoapserviceV2();
      } else {
        jiraSoapService = jiraSoapServiceLocator.getJirasoapserviceV2(webServicePort);
        LOG.debug("SOAP Session service endpoint at " + webServicePort.toExternalForm());
      }
    } catch (ServiceException e) {
      throw new RuntimeException("ServiceException during SOAPClient contruction", e);
    }
  }

  public void connect(String userName, String password) throws RemoteException {
    System.out.println("\tConnnecting via SOAP as : " + userName);
    token = getJiraSoapService().login(userName, password);
    System.out.println("\tConnected");
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
}
