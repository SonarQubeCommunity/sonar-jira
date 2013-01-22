/*
 * Sonar JIRA Plugin
 * Copyright (C) 2009 SonarSource
 * dev@sonar.codehaus.org
 *
 * This program is free software; you can redistribute it and/or
 * modify it under the terms of the GNU Lesser General Public
 * License as published by the Free Software Foundation; either
 * version 3 of the License, or (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
 * Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public
 * License along with this program; if not, write to the Free Software
 * Foundation, Inc., 51 Franklin Street, Fifth Floor, Boston, MA  02
 */

/*
 * Copyright (C) 2010 Atlassian
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.sonar.plugins.jira.util;

import org.apache.commons.io.IOUtils;
import org.codehaus.jettison.json.JSONArray;
import org.codehaus.jettison.json.JSONException;
import org.codehaus.jettison.json.JSONObject;

import java.io.IOException;
import java.io.InputStream;

public class ResourceUtil {
	public static JSONObject getJsonObjectFromResource(String resourcePath) {
		final String s = getStringFromResource(resourcePath);
		try {
			return new JSONObject(s);
		} catch (JSONException e) {
			throw new RuntimeException(e);
		}

	}

	public static JSONArray getJsonArrayFromResource(String resourcePath) {
		final String s = getStringFromResource(resourcePath);
		try {
			return new JSONArray(s);
		} catch (JSONException e) {
			throw new RuntimeException(e);
		}

	}

	public static String getStringFromResource(String resourcePath) {
		final String s;
		try {
			final InputStream is = ResourceUtil.class.getResourceAsStream(resourcePath);
			if (is == null) {
				throw new IOException("Cannot open resource [" + resourcePath + "]");
			}
			s = IOUtils.toString(is);
		} catch (IOException e) {
			throw new RuntimeException(e);
		}
		return s;
	}
}
