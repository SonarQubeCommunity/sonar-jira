/*
 * Sonar, open source software quality management tool.
 * Copyright (C) 2009 SonarSource SA
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
package org.sonar.plugins.jira;

import org.junit.Before;
import org.junit.Test;
import org.sonar.api.batch.SensorContext;
import org.sonar.api.resources.Project;
import org.sonar.api.test.IsMeasure;

import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertThat;
import static org.mockito.Matchers.argThat;
import static org.mockito.Mockito.*;

/**
 * @author Evgeny Mandrikov
 */
public class JiraSensorTest {
  private JiraSensor sensor;

  @Before
  public void setUp() {
    sensor = new JiraSensor();
  }

  @Test
  public void testShouldExecuteOnProject() throws Exception {
    Project project = mock(Project.class);
    when(project.isRoot()).thenReturn(true).thenReturn(false);

    assertThat(sensor.shouldExecuteOnProject(project), is(true));
    assertThat(sensor.shouldExecuteOnProject(project), is(false));
  }

  @Test
  public void testSaveMeasures() {
    SensorContext context = mock(SensorContext.class);
    String url = "http://localhost/jira";
    String priorityDistribution = "Critical=1";

    sensor.saveMeasures(context, url, 1, priorityDistribution);

    verify(context).saveMeasure(argThat(new IsMeasure(JiraMetrics.ISSUES, 1.0, priorityDistribution)));
    verifyNoMoreInteractions(context);
  }
}
