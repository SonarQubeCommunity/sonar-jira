package org.sonar.plugins.jira;

import java.util.Arrays;
import java.util.List;

import org.sonar.plugins.api.EditableProperties;
import org.sonar.plugins.api.EditableProperty;
import org.sonar.plugins.api.Extension;
import org.sonar.plugins.api.Plugin;

@EditableProperties({
	  @EditableProperty(key=JiraPlugin.JIRA_USER_AND_PASS, defaultValue = "",
	    name = "Jira server, user and pass settings", description = "Settings to define which user will be used to retreive " +
	    		"jira metrics for a given server I.E : jira.foo.com;foouser;testpass,jira.bar.com;baruser;pass")
})
public class JiraPlugin implements Plugin {
	
  public final static String JIRA_USER_AND_PASS = "sonar.jira.serveruserpass.keys";
  public final static String JIRA_COMPONENT_FILTER = "sonar.jira.component.filter";
 
	public String getDescription() {
		return "Jira plugin, collect metrics on the Jira server defined in the project pom";
	}
	
	public String getKey() {
    return "jira";
  }

  public String getName() {
    return "Jira";
  }

	public List<Class<? extends Extension>> getExtensions() {
		return Arrays.asList(JiraMetrics.class, JiraMavenCollector.class, JiraWidget.class);
	}

}
