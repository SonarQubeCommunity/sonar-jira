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

import java.util.HashMap;
import java.util.Map;

/**
 * @version $Revision$ $Date$
 */
public class User extends MapObject {

    public User() {
        super();
    }

    public User(Map data) {
        super(data);
    }

    public User(String name) {
        this(new HashMap());
        setName(name);
    }

    /**
     * the username of this user
     */
    public String getName() {
        return getString("name");
    }

    public void setName(String name) {
        setString("name", name);
    }

    /**
     * the full name of this user
     */
    public String getFullname() {
        return getString("fullname");
    }

    public void setFullname(String fullname) {
        setString("fullname", fullname);
    }

    /**
     * the email address of this user
     */
    public String getEmail() {
        return getString("email");
    }

    public void setEmail(String email) {
        setString("email", email);
    }

    public String toString() {
        String name = getName();
        String fullname = getFullname();
        return (fullname != null) ? fullname : name;
    }

    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        final User user = (User) o;

        if (getName() != null ? !getName().equals(user.getName()) : user.getName() != null) return false;

        return true;
    }

    public int hashCode() {
        return (getName() != null ? getName().hashCode() : 0);
    }
}
