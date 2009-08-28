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
public class Filter extends MapObject {

    public Filter() {
        super();
        xmlrpcRefs.put(User.class, "name");
    }

    public Filter(Map data) {
        super(data);
        xmlrpcRefs.put(User.class, "name");
    }

    /**
     * the id of this filter
     */
    public int getId() {
        return getInt("id");
    }

    public void setId(int id) {
        setInt("id", id);
    }

    /**
     * the name of the filter
     */
    public String getName() {
        return getString("name");
    }

    public void setName(String name) {
        setString("name", name);
    }

    /**
     * the description of this filter
     */
    public String getDescription() {
        return getString("description");
    }

    public void setDescription(String description) {
        setString("description", description);
    }

    /**
     * the username of this filter's owner
     */
    public User getAuthor() {
        return (User) getMapObject("author", User.class);
    }

    public void setAuthor(String author) {
        setString("author", author);
    }

    public void setAuthor(User author) {
        setMapObject("author", author);
    }

    /**
     * the id of the project this search relates to (null if the search is across projects)
     */
    // DMB: Taking this away for now
    // public String getProject() {
    // return getString("project");
    // }
    //
    // public void setProject(String project) {
    // setString("project", project);
    // }
    /**
     * a complete XML representation of this search request - I don't recommend you use this for now, it's complex :)
     */
    public String getXml() {
        return getString("xml");
    }

    public void setXml(String xml) {
        setString("xml", xml);
    }

    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        final Filter filter = (Filter) o;

        if (getId() != filter.getId()) return false;
        if (getName() != null ? !getName().equals(filter.getName()) : filter.getName() != null) return false;

        return true;
    }

    public int hashCode() {
        int result;
        result = getId();
        result = 29 * result + (getName() != null ? getName().hashCode() : 0);
        return result;
    }

}
