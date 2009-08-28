/**
 *
 * Copyright 2006 David Blevins
 *
 *  Licensed under the Apache License, Version 2.0 (the "License");
 *  you may not use this file except in compliance with the License.
 *  You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 */
package org.codehaus.swizzle.jira;

import java.net.MalformedURLException;
import java.net.URL;
import java.util.List;

/**
 * @version $Revision$ $Date$
 */
public class BasicIssueFiller implements IssueFiller {
    private final Jira jira;
    private boolean enabled;

    public BasicIssueFiller(Jira jira) {
        this.jira = jira;
    }

    public void setEnabled(boolean enabled) {
        this.enabled = enabled;
    }

    public void fill(Issue issue) {
        if (!enabled) {
            return;
        }
        fill(issue.getAssignee());
        fill(issue.getReporter());
        fill(issue.getPriority());
        fill(issue.getStatus());
        fill(issue.getResolution());
        fill(issue.getType());
        List versions = issue.getAffectsVersions();
        for (int i = 0; i < versions.size(); i++) {
            Version version = (Version) versions.get(i);
            fill(issue, version);
        }
        versions = issue.getFixVersions();
        for (int i = 0; i < versions.size(); i++) {
            Version version = (Version) versions.get(i);
            fill(issue, version);
        }
        List components = issue.getComponents();
        for (int i = 0; i < components.size(); i++) {
            Component component = (Component) components.get(i);
            fill(issue, component);
        }
        if (issue.getLink() == null && jira != null) {
            try {
                ServerInfo serverInfo = jira.getServerInfo();
                URL url = new URL(serverInfo.getBaseUrl() + "/browse/" + issue.getKey());
                issue.setLink(url.toExternalForm());
            } catch (MalformedURLException e) {
            }
        }
    }

    public void fill(Issue issue, Version dest) {
        Version source = jira.getVersion(issue.getProject().getKey(), dest.getId());
        if (source == null) source = jira.getVersion(issue.getProject().getKey(), dest.getName());
        dest.merge(source);
    }

    public void fill(Issue issue, Component dest) {
        Component source = jira.getComponent(issue.getProject().getKey(), dest.getId());
        if (source == null) source = jira.getComponent(issue.getProject().getKey(), dest.getName());
        dest.merge(source);
    }

    public void fill(User dest) {
        if (dest == null) {
            return;
        }
        User source = jira.getUser(dest.getName());
        dest.merge(source);
    }

    public void fill(Priority dest) {
        if (dest == null) {
            return;
        }
        Priority source = jira.getPriority(dest.getId());
        if (source == null) source = jira.getPriority(dest.getName());
        dest.merge(source);
    }

    public void fill(Status dest) {
        if (dest == null) {
            return;
        }
        Status source = jira.getStatus(dest.getId());
        if (source == null) source = jira.getStatus(dest.getName());
        dest.merge(source);
    }

    public void fill(Resolution dest) {
        if (dest == null) {
            return;
        }
        Resolution source = jira.getResolution(dest.getId());
        if (source == null) source = jira.getResolution(dest.getName());
        dest.merge(source);
    }

    public void fill(IssueType dest) {
        if (dest == null) {
            return;
        }
        IssueType source = jira.getIssueType(dest.getId());
        if (source == null) source = jira.getIssueType(dest.getName());
        dest.merge(source);
    }
}
