/*
 * Sonar, entreprise quality control tool.
 * Copyright (C) 2007-2008 Hortis-GRC SA
 * mailto:be_agile HAT hortis DOT ch
 *
 * Sonar is free software; you can redistribute it and/or
 * modify it under the terms of the GNU Lesser General Public
 * License as published by the Free Software Foundation; either
 * version 3 of the License, or (at your option) any later version.
 *
 * Sonar is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
 * Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public
 * License along with Sonar; if not, write to the Free Software
 * Foundation, Inc., 51 Franklin Street, Fifth Floor, Boston, MA  02
 */
package org.sonar.plugins.jira;

import org.apache.commons.io.IOUtils;
import org.apache.commons.lang.StringUtils;
import org.sonar.api.utils.SonarException;

import java.net.HttpURLConnection;
import java.net.URL;
import java.io.Reader;
import java.io.InputStreamReader;
import java.io.InputStream;
import java.io.IOException;


public class ServerHttpClient {

  private String url;  

  private static final int CONNECT_TIMEOUT_MILLISECONDS = 30000;
  private static final int READ_TIMEOUT_MILLISECONDS = 60000;


  public ServerHttpClient(String remoteServerUrl){
    this.url = StringUtils.chomp(remoteServerUrl, "/");
  }


  public String getContent() {
    String result = getRemoteContent(url);
    if (result.trim().length() == 0) {
      throw new ServerHttpClientException("Empty result returned from server");
    }
    return result;
  }

  protected String getRemoteContent(String url) {
    HttpURLConnection conn = null;
    Reader reader = null;
    try {
      conn = getConnection(url, "GET");
      reader = new InputStreamReader((InputStream) conn.getContent());

      int statusCode = conn.getResponseCode();
      if (statusCode != HttpURLConnection.HTTP_OK) {
        throw new ServerConnectionException("Status returned by url : '" + url + "' is invalid : " + statusCode);
      }

      return IOUtils.toString(reader);
    } catch (IOException e) {
      throw new ServerConnectionException("url=" + url, e);

    } finally {
      IOUtils.closeQuietly(reader);
      if (conn != null) {
        conn.disconnect();
      }
    }
  }

  public String getUrl() {
    return url;
  }

  private HttpURLConnection getConnection(String url, String method) throws IOException {
    URL page = new URL(url);
    HttpURLConnection conn = (HttpURLConnection) page.openConnection();
    conn.setConnectTimeout(CONNECT_TIMEOUT_MILLISECONDS);
    conn.setReadTimeout(READ_TIMEOUT_MILLISECONDS);
    conn.setRequestMethod(method);
    conn.connect();
    return conn;
  }

  public static class ServerHttpClientException extends SonarException {

    public ServerHttpClientException(String s) {
      super(s);
    }
  }

  public static class ServerConnectionException extends SonarException {

    public ServerConnectionException(String msg) {
      super(msg);
    }

    public ServerConnectionException(String msg, Throwable throwable) {
      super(msg, throwable);
    }

  }  


}
