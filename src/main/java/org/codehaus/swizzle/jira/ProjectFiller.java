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

/**
 * @version $Revision$ $Date$
 */
public class ProjectFiller implements IssueFiller {
    private final Jira jira;
    private boolean enabled;

    public ProjectFiller(Jira jira) {
        this.jira = jira;
    }

    public void setEnabled(boolean enabled) {
        this.enabled = enabled;
    }

    public void fill(Issue issue) {
        if (!enabled) {
            return;
        }
        fill(issue.getProject());
    }

    public void fill(Project dest) {
        Project source = jira.getProject(dest.getKey());
        if (source == null) source = jira.getProject(dest.getId());
        dest.merge(source);
    }
}
