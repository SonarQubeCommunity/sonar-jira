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

import java.util.Map;

/**
 * @version $Revision$ $Date$
 */
public class Project extends MapObject {

    public Project() {
        super();
    }

    public Project(Map data) {
        super(data);
    }

    public Project(String key) {
        this();
        setKey(key);
    }

    /**
     * the id of the project
     */
    public int getId() {
        return getInt("id");
    }

    public void setId(int id) {
        setInt("id", id);
    }

    /**
     * the project key
     */
    public String getKey() {
        return getString("key");
    }

    public void setKey(String key) {
        setString("key", key);
    }

    /**
     * the name of the project
     */
    public String getName() {
        return getString("name");
    }

    public void setName(String name) {
        setString("name", name);
    }

    /**
     * the url to view this project online
     */
    public String getUrl() {
        return getString("url");
    }

    public void setUrl(String url) {
        setString("url", url);
    }

    /**
     * the url of this project in your organisation (ie not a JIRA URL)
     */
    public String getProjectUrl() {
        return getString("projectUrl");
    }

    public void setProjectUrl(String projectUrl) {
        setString("projectUrl", projectUrl);
    }

    /**
     * the username of the project lead
     */
    public String getLead() {
        return getString("lead");
    }

    public void setLead(String lead) {
        setString("lead", lead);
    }

    /**
     * a description of this project
     */
    public String getDescription() {
        return getString("description");
    }

    public void setDescription(String description) {
        setString("description", description);
    }

    public String toString() {
        String name = getName();
        String key = getKey();
        return (name != null) ? name : key + "";
    }

    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        final Project project = (Project) o;

        if (getId() != project.getId()) return false;
        if (getKey() != null ? !getKey().equals(project.getKey()) : project.getKey() != null) return false;

        return true;
    }

    public int hashCode() {
        int result;
        result = getId();
        result = 29 * result + (getKey() != null ? getKey().hashCode() : 0);
        return result;
    }
}
