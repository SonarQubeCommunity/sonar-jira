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
public class Priority extends BasicObject {

    public Priority() {
        super();
    }

    public Priority(Map data) {
        super(data);
    }

    /**
     * the colour of this constant
     */
    public String getColour() {
        return getString("colour");
    }

    public void setColour(String colour) {
        setString("colour", colour);
    }

}
