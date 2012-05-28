/*
 * Sonar JIRA Plugin
 * Copyright (C) 2009 SonarSource
 * dev@sonar.codehaus.org
 *
 * This program is free software; you can redistribute it and/or
 * modify it under the terms of the GNU Lesser General Public
 * License as published by the Free Software Foundation; either
 * version 3 of the License, or (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
 * Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public
 * License along with this program; if not, write to the Free Software
 * Foundation, Inc., 51 Franklin Street, Fifth Floor, Boston, MA  02
 */
package org.sonar.plugins.jira.reviews;

import org.sonar.api.ServerExtension;
import org.sonar.api.workflow.Workflow;
import org.sonar.api.workflow.screen.CommentScreen;
import org.sonar.plugins.jira.JiraConstants;

import static org.sonar.api.workflow.condition.Conditions.hasProjectProperty;
import static org.sonar.api.workflow.condition.Conditions.hasReviewProperty;
import static org.sonar.api.workflow.condition.Conditions.not;
import static org.sonar.api.workflow.condition.Conditions.statuses;

public final class WorkflowBuilder implements ServerExtension {

  private static final String LINK_TO_JIRA_ID = "link-to-jira";
  private final Workflow workflow;
  private final LinkFunction linkFunction;

  public WorkflowBuilder(Workflow workflow, LinkFunction linkFunction) {
    this.workflow = workflow;
    this.linkFunction = linkFunction;
  }

  public void start() {
    workflow.addCommand(LINK_TO_JIRA_ID);
    workflow.setScreen(LINK_TO_JIRA_ID, new CommentScreen());
    workflow.addFunction(LINK_TO_JIRA_ID, linkFunction);
    // conditions for this function
    // - on the review ("IDLE" is the non-persisted status of an non-existing review = when a violation does have a review yet)
    workflow.addCondition(LINK_TO_JIRA_ID, not(hasReviewProperty(JiraConstants.REVIEW_DATA_PROPERTY_KEY)));
    workflow.addCondition(LINK_TO_JIRA_ID, statuses("IDLE", "OPEN", "REOPENED"));
    // - on the project
    workflow.addCondition(LINK_TO_JIRA_ID, hasProjectProperty(JiraConstants.SERVER_URL_PROPERTY));
    workflow.addCondition(LINK_TO_JIRA_ID, hasProjectProperty(JiraConstants.SOAP_BASE_URL_PROPERTY));
    workflow.addCondition(LINK_TO_JIRA_ID, hasProjectProperty(JiraConstants.USERNAME_PROPERTY));
    workflow.addCondition(LINK_TO_JIRA_ID, hasProjectProperty(JiraConstants.PASSWORD_PROPERTY));
    workflow.addCondition(LINK_TO_JIRA_ID, hasProjectProperty(JiraConstants.JIRA_PROJECT_KEY_PROPERTY));
  }
}
