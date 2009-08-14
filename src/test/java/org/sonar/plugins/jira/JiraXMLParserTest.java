/*
 * Sonar, entreprise quality control tool.
 * Copyright (C) 2007-2008 Hortis-GRC SA
 * mailto:be_agile HAT hortis DOT ch
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
package org.sonar.plugins.jira;

import static org.hamcrest.Matchers.is;
import static org.junit.Assert.assertThat;
import org.junit.Test;

import java.io.InputStream;


public class JiraXMLParserTest {

  @Test
  public void souldGetNumberOfIssues() throws Exception {
    InputStream inputStream = getClass().getResourceAsStream("/org/sonar/plugins/jira/sampleJiraXML.xml");
    JiraXMLParser jiraXMLParser = new JiraXMLParser(inputStream);
    jiraXMLParser.parse();

    assertThat(jiraXMLParser.getNumberIssues(), is(8));
  }

  @Test
  public void shouldgetPriorities(){
    InputStream inputStream = getClass().getResourceAsStream("/org/sonar/plugins/jira/sampleJiraXML.xml");
    JiraXMLParser jiraXMLParser = new JiraXMLParser(inputStream);
    jiraXMLParser.parse();

    assertThat(jiraXMLParser.getPriorities().getCount("Blocker"), is(1));
    assertThat(jiraXMLParser.getPriorities().getCount("Critical"), is(1));
    assertThat(jiraXMLParser.getPriorities().getCount("Major"), is(4));
    assertThat(jiraXMLParser.getPriorities().getCount("Minor"), is(2));
    assertThat(jiraXMLParser.getPriorities().getCount("Trivial"), is(0));
  }
}
