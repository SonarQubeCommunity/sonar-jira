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

import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * @version $Revision$ $Date$
 */
public class Issue extends MapObject implements Comparable {

    public Issue() {
        this(new HashMap());
    }

    public Issue(Map data) {
        super(data);
        xmlrpcRefs.put(IssueType.class, "id");
        xmlrpcRefs.put(Status.class, "id");
        xmlrpcRefs.put(User.class, "name");
        xmlrpcRefs.put(Project.class, "key");
        xmlrpcRefs.put(Priority.class, "id");
        xmlrpcRefs.put(Resolution.class, "id");

        xmlrpcNoSend.add("customFieldValues");
        xmlrpcNoSend.add("link");
        xmlrpcNoSend.add("voters");
        xmlrpcNoSend.add("subTasks");
        xmlrpcNoSend.add("parentTask");
        xmlrpcNoSend.add("attachments");
        xmlrpcNoSend.add("comments");
    }

    /**
     *
     */
    public Project getProject() {
        return (Project) getMapObject("project", Project.class);
    }

    public void setProject(Project project) {
        setMapObject("project", project);
    }

    public IssueType getType() {
        return (IssueType) getMapObject("type", IssueType.class);
    }

    public void setType(IssueType type) {
        setMapObject("type", type);
    }

    /**
     * example: 2005-10-11 06:10:39.115
     */
    public Date getCreated() {
        return getDate("created");
    }

    public void setCreated(Date created) {
        setDate("created", created);
    }

    /**
     *
     */
    public String getSummary() {
        return getString("summary");
    }

    public void setSummary(String summary) {
        setString("summary", summary);
    }

    /**
     * This data is not available via interface except scraping the html from the web interface. If you know of another way to get it, please let us know.
     * 
     * @return List<User>
     */
    public List getVoters() {
        if (!hasField("voters")) {
            List votes = new MapObjectList();
            for (int i = getInt("votes"); i > 0; i--) {
                votes.add(new User());
            }
            setMapObjects("voters", votes);
        }
        return getMapObjects("voters", User.class);
    }

    public void setVoters(List users) {
        setMapObjects("voters", users);
    }

    /**
     *
     */
    public int getVotes() {
        return getVoters().size();
    }

    /**
     * List of something
     */
    public List getCustomFieldValues() {
        return getMapObjects("customFieldValues", CustomFieldValue.class);
    }

    public void setCustomFieldValues(List customFieldValues) {
        setMapObjects("customFieldValues", customFieldValues);
    }

    /**
     * List of Comments
     */
    public List getComments() {
        return getMapObjects("comments", Comment.class);
    }

    /**
     * List of Components
     */
    public List getComponents() {
        return getMapObjects("components", Component.class);
    }

    public void setComponents(List components) {
        setMapObjects("components", components);
    }

    public void addComponents(Component component) {
        getComponents().add(component);
    }

    public void removeComponents(Component component) {
        getComponents().remove(component);
    }

    /**
     * List of Versions
     */
    public List getAffectsVersions() {
        return getMapObjects("affectsVersions", Version.class);
    }

    public void setAffectsVersions(List affectsVersions) {
        setMapObjects("affectsVersions", affectsVersions);
    }

    public void addAffectsVersion(Version version) {
        getAffectsVersions().add(version);
    }

    public void removeAffectsVersion(Version version) {
        getAffectsVersions().remove(version);
    }

    /**
     * 28093
     */
    public int getId() {
        return getInt("id");
    }

    public void setId(int id) {
        setInt("id", id);
    }

    /**
     * 6
     */
    public Status getStatus() {
        return (Status) getMapObject("status", Status.class);
    }

    public void setStatus(Status status) {
        setMapObject("status", status);
    }

    public Resolution getResolution() {
        return (Resolution) getMapObject("resolution", Resolution.class);
    }

    public void setResolution(Resolution resolution) {
        setMapObject("resolution", resolution);
    }

    /**
     * List
     */
    public List getFixVersions() {
        return getMapObjects("fixVersions", Version.class);
    }

    public void setFixVersions(List fixVersions) {
        setMapObjects("fixVersions", fixVersions);
    }

    public void addFixVersion(Version version) {
        getFixVersions().add(version);
    }

    public void removeFixVersion(Version version) {
        getFixVersions().remove(version);
    }

    public List getSubTasks() {
        return getMapObjects("subTasks", Issue.class);
    }

    public void setSubTasks(List subTasks) {
        setMapObjects("subTasks", subTasks);
    }

    public void addSubTask(Issue issue) {
        getSubTasks().add(issue);
    }

    public void removeSubTask(Issue issue) {
        getSubTasks().remove(issue);
    }

    protected Issue getParentTask() {
        return (Issue) getMapObject("parentTask", Issue.class);
    }

    protected void setParentTask(Issue parentTask) {
        setMapObject("parentTask", parentTask);
    }

    /**
     *
     */
    public String getDescription() {
        return getString("description");
    }

    public void setDescription(String description) {
        setString("description", description);
    }

    public User getReporter() {
        return (User) getMapObject("reporter", User.class);
    }

    public void setReporter(User reporter) {
        setMapObject("reporter", reporter);
    }

    /**
     *
     */
    public Date getUpdated() {
        return getDate("updated");
    }

    public void setUpdated(Date updated) {
        setDate("updated", updated);
    }

    /**
     *
     */
    public Date getDuedate() {
        return getDate("duedate");
    }

    public void setDuedate(Date duedate) {
        setDate("duedate", duedate);
    }

    public User getAssignee() {
        return (User) getMapObject("assignee", User.class);
    }

    public void setAssignee(User assignee) {
        setMapObject("assignee", assignee);
    }

    /**
     *
     */
    public String getEnvironment() {
        return getString("environment");
    }

    public void setEnvironment(String environment) {
        setString("environment", environment);
    }

    public Priority getPriority() {
        return (Priority) getMapObject("priority", Priority.class);
    }

    public void setPriority(Priority priority) {
        setMapObject("priority", priority);
    }

    /**
     *
     */
    public String getKey() {
        return getString("key");
    }

    public void setKey(String key) {
        setString("key", key);
    }

    /**
     * Only available via the RSS source Not available via XML-RPC source
     */
    public String getLink() {
        return getString("link");
    }

    public void setLink(String link) {
        setString("link", link);
    }

    public List getAttachments() {
        return getMapObjects("attachments", Attachment.class);
    }

    public void setAttachments(List attachments) {
        setMapObjects("attachments", attachments);
    }

    public Map toMap() {
        // It's unlikely that you can even update the votes via xml-rpc
        // till we know for sure, best to make sure the tally is current
        setInt("votes", getVoters().size());
        return super.toMap();
    }

    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        final Issue issue = (Issue) o;

        if (getId() != issue.getId()) return false;
        if (getKey() != null ? !getKey().equals(issue.getKey()) : issue.getKey() != null) return false;

        return true;
    }

    public int hashCode() {
        int result;
        result = getId();
        result = 29 * result + (getKey() != null ? getKey().hashCode() : 0);
        return result;
    }

    public int compareTo(Object object) {
        if (object instanceof Issue) {
            Issue that = (Issue) object;
            int a = this.getId();
            int b = that.getId();
            if (a > b) {
                return 1;
            } else if (a < b) {
                return -1;
            }
        }
        return 0;
    }
}
