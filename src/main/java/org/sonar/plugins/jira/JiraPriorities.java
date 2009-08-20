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

import java.util.Collection;

public class JiraPriorities {

  private Bag bag;

  public JiraPriorities(Collection priorities) {
    initPrioritiesBag(priorities);
  }

  private void initPrioritiesBag(Collection priorities){
    bag = new HashBag();
    for (Object priorityObj : priorities) {
      String currentPriority = (String) priorityObj;
      Priority priority = getPriority(currentPriority);
      bag.add(priority);
    }
  }

  private Priority getPriority(String textPriority){
    return Priority.valueOf(StringUtils.uncapitalize(textPriority));
  }

  public int getTotalSize(){
    return bag.size();
  }

  public int getBlockerSize() {
    return bag.getCount(Priority.blocker);
  }

  public int getBlockerIndex(){
    return Priority.blocker.getIndex();
  }

  public int getCriticalSize() {
    return bag.getCount(Priority.critical);
  }

  public int getCriticalIndex(){
    return Priority.critical.getIndex();
  }

  public int getMajorSize() {
    return bag.getCount(Priority.major);
  }

  public int getMajorIndex(){
    return Priority.major.getIndex();
  }

  public int getMinorSize() {
    return bag.getCount(Priority.minor);
  }

  public int getMinorIndex(){
    return Priority.minor.getIndex();
  }

  public int getTrivialSize() {
    return bag.getCount(Priority.trivial);
  }

  public int getTrivialIndex() {
    return Priority.trivial.getIndex();
  }

  enum Priority {
    blocker(1),
    critical(2),
    major(3),
    minor(4),
    trivial(5);

    private int index;

    Priority(int index){
      this.index = index;
    }

    public int getIndex(){
      return index;
    }
  }
}
