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
public class Component extends MapObject {

    public Component() {
        super();
    }

    public Component(Map data) {
        super(data);
    }

    public Component(String name) {
        this();
        setName(name);
    }

    /**
     * the id of the component
     */
    public int getId() {
        return getInt("id");
    }

    public void setId(int id) {
        setInt("id", id);
    }

    /**
     * the name of the component
     */
    public String getName() {
        return getString("name");
    }

    public void setName(String name) {
        setString("name", name);
    }

    public String toString() {
        String name = getName();
        int id = getId();
        return (name != null) ? name : id + "";
    }

    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        final Component component = (Component) o;

        if (getId() != component.getId()) return false;
        if (getName() != null ? !getName().equals(component.getName()) : component.getName() != null) return false;

        return true;
    }

    public int hashCode() {
        int result;
        result = getId();
        result = 29 * result + (getName() != null ? getName().hashCode() : 0);
        return result;
    }
}
