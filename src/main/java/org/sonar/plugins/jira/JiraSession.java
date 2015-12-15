package org.sonar.plugins.jira;

import java.net.URL;

import org.sonar.api.config.Settings;
import org.sonar.api.rules.RuleFinder;

public interface JiraSession extends AutoCloseable
{

	public void connect(String userName, String password);
	public JiraService getJiraService(RuleFinder ruleFinder, Settings settings);
	public String getAuthenticationToken();
	public URL getWebServiceUrl();
	public void close();
	
}
