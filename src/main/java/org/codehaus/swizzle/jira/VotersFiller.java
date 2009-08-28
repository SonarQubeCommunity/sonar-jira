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

import org.codehaus.swizzle.stream.DelimitedTokenReplacementInputStream;
import org.codehaus.swizzle.stream.IncludeFilterInputStream;
import org.codehaus.swizzle.stream.StringTokenHandler;

import java.io.BufferedInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;

/**
 * @version $Revision$ $Date$
 */
public class VotersFiller implements IssueFiller {
    private final Jira jira;
    private boolean enabled;

    public VotersFiller(Jira jira) {
        this.jira = jira;
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

        getVotes(baseUrlString, issue);
    }

    public static List fill(JiraRss jiraRss) throws Exception {
        VotersFiller filler = new VotersFiller(null);
        List issues = jiraRss.getIssues();
        for (int i = 0; i < issues.size(); i++) {
            Issue issue = (Issue) issues.get(i);
            String link = issue.getLink();
            link = link.replaceFirst("/browse/.*$", "/");
            filler.getVotes(link, issue);
        }
        return issues;
    }

    private void getVotes(String baseUrlString, final Issue issue) {
        try {
            final List votes = new ArrayList();
            URL baseUrl = new URL(baseUrlString);

            URL url = new URL(baseUrl, "secure/ViewVoters!default.jspa?id=" + issue.getId());

            InputStream in = new BufferedInputStream(url.openStream());
            in = new IncludeFilterInputStream(in, "<a id=\"voter_link", "/a>");
            in = new DelimitedTokenReplacementInputStream(in, "name=", "<", new StringTokenHandler() {
                public String handleToken(String token) throws IOException {
                    String[] s = token.split("\">");
                    try {
                        User user;
                        if (jira == null) {
                            user = new User();
                            user.setName(s[0]);
                            user.setFullname(s[1]);
                            votes.add(user);
                        } else {
                            user = jira.getUser(s[0]);
                            if (user != null) {
                                votes.add(user);
                            }
                        }
                    } catch (Exception e) {
                        System.err.println("Bad voter string: " + token + ", " + e.getClass().getName() + ": " + e.getMessage());
                    }
                    return "";
                }
            });

            int i = in.read();
            while (i != -1) {
                i = in.read();
            }
            in.close();

            issue.setVoters(votes);
        } catch (IOException e) {
            System.err.println(e.getClass().getName() + ": " + e.getMessage());
        }
    }

}
