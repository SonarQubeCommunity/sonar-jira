package org.sonar.plugins.jira;

import org.apache.maven.model.IssueManagement;
import org.apache.maven.project.MavenProject;
import org.junit.Before;
import org.junit.Test;
import static org.junit.Assert.*;

import org.sonar.plugins.api.maven.ProjectContext;
import org.sonar.plugins.api.maven.model.MavenPom;

import static org.mockito.Mockito.*;
//import static org.mockito.Matchers.*;

public class JiraMavenCollectorTest {

  private MavenPom pom;
  private JiraMavenCollector collector;
  private MavenProject project;
  private IssueManagement issues;
  
  
  @Before
  public void setup() {
    collector = new JiraMavenCollector();
    pom = mock(MavenPom.class);
    project = mock(MavenProject.class);
    issues = mock(IssueManagement.class);
    when(pom.getMavenProject()).thenReturn(project);
    when(pom.isRoot()).thenReturn(true);
  }
  
  @Test
  public void testCall() {
    when(project.getIssueManagement()).thenReturn(issues);
    when(project.getVersion()).thenReturn("1.9-SNAPSHOT");
    when(issues.getUrl()).thenReturn("http://jira.codehaus.org/browse/SONAR");

    //when(pom.getProperty(JiraPlugin.JIRA_USER_AND_PASS)).thenReturn("jira.codehaus.org;test;test");
    //ProjectContext ctx = mock(ProjectContext.class);
    //collector.collect(pom, ctx);
  }
  
  @Test
  public void testShouldCollectOnWithNoIssueManagement() {
    when(project.getIssueManagement()).thenReturn(null);
    assertFalse(collector.shouldCollectOn(pom));
  }
  
  @Test
  public void testShouldCollectOnWithUnsupportedIssueManagement() {
    when(project.getIssueManagement()).thenReturn(issues);
    when(issues.getSystem()).thenReturn("unknownsystem");
    assertFalse(collector.shouldCollectOn(pom));
  }
  
  @Test
  public void testShouldCollectOnWithSupportedIssueManagement() {
    when(project.getIssueManagement()).thenReturn(issues);
    when(issues.getSystem()).thenReturn("jIrA");
    when(issues.getUrl()).thenReturn("http://jira.foo.org/browse/TEST_PROJECT");
    assertTrue(collector.shouldCollectOn(pom));
  }
  
  @Test
  public void testShouldCollectOnWithSupportedIssueManagementAndNoRootPom() {
    when(project.getIssueManagement()).thenReturn(issues);
    when(issues.getSystem()).thenReturn("jIrA");
    when(issues.getUrl()).thenReturn("http://jira.foo.org/browse/TEST_PROJECT");
    when(pom.isRoot()).thenReturn(false);
    assertFalse(collector.shouldCollectOn(pom));
  }
  
  @Test
  public void testShouldCollectOnWithBadIssueManagementUrl() {
    when(project.getIssueManagement()).thenReturn(issues);
    when(issues.getSystem()).thenReturn("jIrA");
    when(issues.getUrl()).thenReturn("http://jira.foo.org/no-browse/TEST_PROJECT");
    assertFalse(collector.shouldCollectOn(pom));
    
    when(issues.getUrl()).thenReturn("http://jira.foo.org/browse/");
    assertFalse(collector.shouldCollectOn(pom));
    when(issues.getUrl()).thenReturn("http://jira.foo.org/browse");
    assertFalse(collector.shouldCollectOn(pom));
  }
  
}
