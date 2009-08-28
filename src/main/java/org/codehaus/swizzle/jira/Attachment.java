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

import java.net.URL;
import java.util.Date;
import java.util.Map;

/**
 * @version $Revision$ $Date$
 */
public class Attachment extends MapObject {

    public Attachment() {
        super();
    }

    public Attachment(Map data) {
        super(data);
    }

    public int getId() {
        return getInt("id");
    }

    public void setId(int id) {
        setInt("id", id);
    }

    public String getFileName() {
        return getString("fileName");
    }

    public void setFileName(String fileName) {
        setString("fileName", fileName);
    }

    public URL getUrl() {
        return getUrl("file");
    }

    public void setUrl(URL url) {
        setUrl("file", url);
    }

    public String getAuthor() {
        return getString("author");
    }

    public void setAuthor(String username) {
        setString("author", username);
    }

    public Date getCreated() {
        return getDate("created");
    }

    public void setCreated(Date created) {
        setDate("created", created);
    }

    public String toString() {
        return getFileName();
    }
}
