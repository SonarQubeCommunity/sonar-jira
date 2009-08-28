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
public class ServerInfo extends MapObject {

    public ServerInfo() {
        super();
    }

    public ServerInfo(Map data) {
        super(data);
    }

    public int getBuildNumber() {
        return getInt("buildNumber");
    }

    public void setBuildNumber(int buildNumber) {
        setInt("buildNumber", buildNumber);
    }

    public String getVersion() {
        return getString("version");
    }

    public void setVersion(String version) {
        setString("version", version);
    }

    public String getBaseUrl() {
        return getString("baseUrl");
    }

    public void setBaseUrl(String baseUrl) {
        setString("baseUrl", baseUrl);
    }

    public String getEdition() {
        return getString("edition");
    }

    public void setEdition(String edition) {
        setString("edition", edition);
    }

    public Date getBuildDate() {
        return getDate("buildDate");
    }

    public void setBuildDate(Date buildDate) {
        setDate("buildDate", buildDate);
    }
}
