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
import org.apache.commons.collections.bag.HashBag;
import org.apache.commons.lang.StringUtils;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;

public class JiraPriorities {

  private Bag prioritiesNameBag;

  public JiraPriorities(Collection priorities) {
    initPrioritiesBag(priorities);
  }

  private void initPrioritiesBag(Collection priorities) {
    prioritiesNameBag = new HashBag();
    for (Object priorityObj : priorities) {
      String currentPriority = (String) priorityObj;
      prioritiesNameBag.add(currentPriority);
    }
  }

  public int getTotalPrioritesCount() {
    return prioritiesNameBag.size();
  }

  public String getPriorityDistributionText() {
    String result = "";
    for (Object o : getOrderedPriorities()) {
      String priority = (String) o;
      int prioritySize = prioritiesNameBag.getCount(priority);
      result += priority + "=" + prioritySize + ",";
    }
    result = StringUtils.removeEnd(result, ",");
    return result;
  }

  private List<String> getOrderedPriorities() {
    List<String> sortedPriorities = new ArrayList<String>(prioritiesNameBag.uniqueSet());
    Collections.sort(sortedPriorities);
    return sortedPriorities;
  }

}
