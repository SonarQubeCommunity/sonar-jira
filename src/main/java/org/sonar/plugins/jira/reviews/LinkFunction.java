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

import java.util.Map;
import java.util.concurrent.ExecutionException;

import org.apache.commons.lang.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.sonar.api.ServerExtension;
import org.sonar.api.database.model.User;
import org.sonar.api.security.UserFinder;
import org.sonar.api.workflow.Comment;
import org.sonar.api.workflow.MutableReview;
import org.sonar.api.workflow.Review;
import org.sonar.api.workflow.WorkflowContext;
import org.sonar.api.workflow.function.Function;
import org.sonar.core.review.ReviewDao;
import org.sonar.core.review.ReviewDto;
import org.sonar.plugins.jira.JiraConstants;

import com.atlassian.jira.rest.client.api.domain.Issue;
import com.google.common.collect.Maps;

public class LinkFunction extends Function implements ServerExtension {

	private static final Logger LOG = LoggerFactory.getLogger(LinkFunction.class);

	private final JiraIssueCreator jiraIssueCreator;
	private final ReviewDao reviewDao;
	private final UserFinder userFinder;

	public LinkFunction(JiraIssueCreator jiraIssueCreator, ReviewDao reviewDao, UserFinder userFinder) {
		this.jiraIssueCreator = jiraIssueCreator;
		this.reviewDao = reviewDao;
		this.userFinder = userFinder;
		
		LOG.info("Injected reviewDao: {}", reviewDao);
	}

	@Override
	public void doExecute(MutableReview review, Review initialReview, WorkflowContext context, Map<String, String> parameters) {
		LOG.info("Create isse in JIRA, review: {}", review);
		
		String assigneeLogin = null;
		try {
			Long reviewId = review.getReviewId();
			ReviewDto reviewDto = reviewDao.findById(reviewId);
			Long assigneeId = reviewDto.getAssigneeId();
			LOG.debug("Fetched assigneeId: {}", assigneeId);
			
			if (assigneeId != null) {
				User assignee = userFinder.findById(assigneeId.intValue());
				assigneeLogin = assignee.getLogin();
				LOG.info("Fetched assigneeLogin: {}", assigneeLogin);
			}
			else {
				LOG.info("No assignee assigned!");
			}
		}
		catch(Exception ex) {
			LOG.warn("Get the review dto failed.", ex);
		}
		
		Map<String, String> params = Maps.newHashMap();
		if (parameters != null) {
			params.putAll(parameters);
		}
		params.put(JiraConstants.JIRA_ISSUE_REPORTER_PROPERTY, context.getUserLogin());
		if (!StringUtils.isBlank(assigneeLogin)) {
			params.put(JiraConstants.JIRA_ISSUE_ASSIGNEE_PROPERTY, assigneeLogin);
		}

		Issue issue;
		try {
			issue = jiraIssueCreator.createIssue(initialReview, context.getProjectSettings(), params);
		}
		catch (IllegalStateException e) {
//			LOG.warn("Create issue failed.", e);
			throw e;
		}
		catch (Exception e) {
//			LOG.warn("Create issue failed but .", e);
			throw new IllegalStateException("Impossible to create an issue on JIRA. A problem occured with the remote server: " + e.getMessage(), e);
		}
		createComment(issue, review, context, parameters);
		// and add the property
		review.setProperty(JiraConstants.REVIEW_DATA_PROPERTY_KEY, issue.getKey());
	}

	protected void createComment(Issue issue, MutableReview review, WorkflowContext context, Map<String, String> parameters) {
		Comment newComment = review.createComment();
		newComment.setUserId(context.getUserId());
		newComment.setMarkdownText(generateCommentText(issue, context, parameters));
	}

	protected String generateCommentText(Issue issue, WorkflowContext context, Map<String, String> parameters) {
		StringBuilder message = new StringBuilder();
		String text = parameters.get("text");
		if (!StringUtils.isBlank(text)) {
			message.append(text);
			message.append("\n\n");
		}
		message.append("Review linked to JIRA issue: ");
		message.append(context.getProjectSettings().getString(JiraConstants.SERVER_URL_PROPERTY));
		message.append("/browse/");
		message.append(issue.getKey());
		return message.toString();
	}

}
