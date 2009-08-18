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

import org.apache.commons.collections.Bag;
import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.collections.Predicate;
import org.apache.commons.collections.bag.HashBag;
import org.codehaus.stax2.XMLInputFactory2;
import org.codehaus.stax2.XMLStreamReader2;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.xml.stream.XMLStreamConstants;
import javax.xml.stream.XMLStreamException;
import java.io.InputStream;
import java.util.Arrays;

public class JiraXMLParser {

  private static final Logger LOG = LoggerFactory.getLogger(JiraXMLParser.class);

  private InputStream xml;
  private int issuesCount;
  private Bag prioritiesCount;

  private static String[] prioritiesName = {"Blocker", "Critical", "Major", "Minor", "Trivial"};

  public JiraXMLParser(InputStream xml) {
    issuesCount = 0;
    initPriorities();
    this.xml = xml;
  }

  private void initPriorities() {
    prioritiesCount = new HashBag();
    for (String priority : prioritiesName) {
      prioritiesCount.add(priority, 0);
    }
  }

  public void parse() {
    try {
      collectData();
    } catch (XMLStreamException e) {
      throw new JiraException("Can't parse jira xml data");
    }
  }

  public void collectData() throws XMLStreamException {
    XMLInputFactory2 xmlFactory = (XMLInputFactory2) XMLInputFactory2.newInstance();
    XMLStreamReader2 reader = (XMLStreamReader2) xmlFactory.createXMLStreamReader(xml);
    while (reader.next() != XMLStreamConstants.END_DOCUMENT) {
      if (reader.isStartElement()) {
        String elementName = reader.getLocalName();
        if (elementName.equals("item")) {
          issuesCount++;
        } else if (elementName.equals("priority")){
          reader.next();
          String priority = reader.getText();
          addPriority(priority);
        }
      }
    }
  }

  private void addPriority(final String priority) {
    Object result = CollectionUtils.find(Arrays.asList(prioritiesName), new Predicate(){
      public boolean evaluate(Object o) {
        String currentPriority = (String) o;
        return currentPriority.equals(priority);
      }
    });
    if( result != null){
      prioritiesCount.add(priority);
    }
    
  }

  public int getNumberIssues() {
    return issuesCount;
  }

  public Bag getPriorities() {
    return prioritiesCount;
  }

  public String[] getPrioritiesName(){
    return prioritiesName;
  }


}
