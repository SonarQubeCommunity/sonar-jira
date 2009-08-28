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

import org.codehaus.swizzle.stream.StreamLexer;

import java.io.BufferedInputStream;
import java.io.File;
import java.io.InputStream;
import java.net.URL;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

/**
 * @version $Revision$ $Date$
 */
public class AttachmentsFiller implements IssueFiller {
    private final Jira jira;
    private boolean enabled;
    private final SimpleDateFormat dateFormat;

    public AttachmentsFiller(Jira jira) {
        this.jira = jira;
        dateFormat = new SimpleDateFormat("dd/MMM/yy hh:mm a");
    }

    public void setEnabled(boolean enabled) {
        this.enabled = enabled;
    }

    public void fill(final Issue issue) {
        if (!enabled) {
            return;
        }
        ServerInfo serverInfo = jira.getServerInfo();

        String baseUrlString = serverInfo.getBaseUrl();

        getAttachments(baseUrlString, issue);
    }

    public static List fill(JiraRss jiraRss) throws Exception {
        AttachmentsFiller filler = new AttachmentsFiller(null);
        List issues = jiraRss.getIssues();
        for (int i = 0; i < issues.size(); i++) {
            Issue issue = (Issue) issues.get(i);
            String link = issue.getLink();
            link = link.replaceFirst("/browse/.*$", "/");
            filler.getAttachments(link, issue);
        }
        return issues;
    }

    private void getAttachments(String baseUrlString, final Issue issue) {
        try {
            final List attachments = new ArrayList();
            URL baseUrl = new URL(baseUrlString);

            URL pageUrl = new URL(baseUrl, "secure/ManageAttachments.jspa?id=" + issue.getId());

            InputStream in = new BufferedInputStream(pageUrl.openStream());
            StreamLexer lexer = new StreamLexer(in);

            while (lexer.readToken("icons/attach") != null) {
                try {
                    Attachment attachment = new Attachment();

                    String link = lexer.readToken("secure/attachment/", "\"");
                    URL url = new URL(pageUrl, "attachment/" + link);
                    attachment.setUrl(url);
                    attachments.add(attachment);

                    String id = link.replaceFirst("/.*", "");
                    attachment.setId(Integer.parseInt(id));

                    File file = new File(url.getFile());
                    attachment.setFileName(file.getName());

                    lexer.readToken("<td");
                    String size = lexer.readToken(">", "</td>");

                    lexer.readToken("<td");
                    String mimeType = lexer.readToken(">", "</td>");

                    try {
                        lexer.readToken("<td");
                        String dateAttached = lexer.readToken(">", "</td>");
                        if (!containsGarbage(dateAttached)) {
                            // 12/Sep/06 02:23 PM - dd/MMM/yy hh:mm a
                            Date created = dateFormat.parse(dateAttached);
                            attachment.setCreated(created);
                        }
                    } catch (Exception e) {
                    }

                    lexer.readToken("<td");
                    String attachedBy = lexer.readToken(">", "</td>");
                    if (!containsGarbage(attachedBy)) {
                        attachment.setAuthor(attachedBy);
                    } else {
                        attachment.setAuthor("");
                    }
                } catch (Exception e) {
                }
            }

            in.close();

            issue.setAttachments(attachments);
        } catch (Exception e) {
            System.err.println(e.getClass().getName() + ": " + e.getMessage());
        }
    }

    private boolean containsGarbage(String data) {
        char[] illegal = { '<', '>', '=', '\n', '\r', '\t' };
        for (int i = 0; i < illegal.length; i++) {
            char c = illegal[i];
            if (data.indexOf(c) != -1) {
                return true;
            }
        }
        return false;
    }

}
