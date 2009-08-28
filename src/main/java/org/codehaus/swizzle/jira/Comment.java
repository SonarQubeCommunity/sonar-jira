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
import java.util.Map;

/**
 * @version $Revision$ $Date$
 */
public class Comment extends MapObject {

    public Comment() {
        super();
    }

    public Comment(Map data) {
        super(data);
    }

    public String getId() {
        return getString("id");
    }

    public void setId(String id) {
        setString("id", id);
    }

    public String getBody() {
        return getString("body");
    }

    public void setBody(String body) {
        setString("body", body);
    }

    public String getUsername() {
        return getString("username");
    }

    public void setUsername(String username) {
        setString("username", username);
    }

    public Date getTimePerformed() {
        return getDate("timePerformed");
    }

    public void setTimePerformed(Date timePerformed) {
        setDate("timePerformed", timePerformed);
    }
}
