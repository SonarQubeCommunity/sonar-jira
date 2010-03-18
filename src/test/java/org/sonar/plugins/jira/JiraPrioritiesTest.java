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

import org.junit.Test;

import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;

public class JiraPrioritiesTest {

  @Test
  public void shouldReturnsADistributionString() {
    JiraPriorities jiraPriorities = createPrioritiesFixture();

    assertThat(jiraPriorities.getPriorityDistributionText(), is("Critical=1;Major=2;Minor=1"));
  }

  @Test
  public void shouldReturnNothingWhenCollectionEntryIsEmpty() {
    Collection priorites = Collections.emptyList();
    JiraPriorities jiraPriorities = new JiraPriorities(priorites);

    assertThat(jiraPriorities.getPriorityDistributionText(), is(""));
  }

  @Test
  public void shouldReturnTotalPriortitiesCount() {
    JiraPriorities jiraPriorities = createPrioritiesFixture();
    assertThat(jiraPriorities.getTotalPrioritesCount(), is(4));
  }

  @Test
  public void shouldReturnZeroWhenNoPriortities() {
    Collection priorites = Collections.emptyList();
    JiraPriorities jiraPriorities = new JiraPriorities(priorites);

    assertThat(jiraPriorities.getTotalPrioritesCount(), is(0));
  }

  private JiraPriorities createPrioritiesFixture() {
    Collection priorites = Arrays.asList("Major", "Minor", "Critical", "Major");
    return new JiraPriorities(priorites);
  }
}
