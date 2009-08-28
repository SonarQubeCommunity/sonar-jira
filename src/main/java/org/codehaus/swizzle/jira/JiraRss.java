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

import org.xml.sax.Attributes;
import org.xml.sax.SAXException;

import javax.xml.parsers.SAXParser;
import javax.xml.parsers.SAXParserFactory;

import java.io.InputStream;
import java.lang.reflect.Constructor;
import java.lang.reflect.Method;
import java.net.URL;
import java.util.EmptyStackException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Stack;

/**
 * @version $Revision$ $Date$
 */
public class JiraRss {
    private static final Map autofillProviders = new HashMap();
    static {
        autofillProviders.put("voters", "org.codehaus.swizzle.jira.VotersFiller");
        autofillProviders.put("subtasks", "org.codehaus.swizzle.jira.SubTasksFiller");
        autofillProviders.put("attachments", "org.codehaus.swizzle.jira.AttachmentsFiller");
    }

    private Map issues = new HashMap();
    private URL url;

    public JiraRss(String query) throws Exception {
        this(new URL(query));
    }

    public JiraRss(URL url) throws Exception {
        this(url.openStream());
        this.url = url;
    }

    public JiraRss(InputStream in) throws Exception {
        SAXParserFactory saxParserFactory = SAXParserFactory.newInstance();
        SAXParser saxParser = saxParserFactory.newSAXParser();
        ObjectBuilder objectBuilder = new ObjectBuilder();

        saxParser.parse(in, objectBuilder);

        List list = objectBuilder.getIssues();
        for (int i = 0; i < list.size(); i++) {
            Issue issue = (Issue) list.get(i);
            issues.put(issue.getKey(), issue);

            try {
                // Fix: the project name isn't in the RSS feed
                String project = issue.getKey().split("-")[0];
                issue.setString("project", project);
            } catch (Exception dontCare) {
            }
        }
    }

    /**
     * Valid schemes are "issue", "project", "voters", and "attachments" "issues" is enabled by default
     * 
     * @param scheme
     */
    public void autofill(String scheme) {
        if (!autofillProviders.containsKey(scheme)) {
            throw new UnsupportedOperationException("Autofill Scheme not supported: " + scheme);
        }

        try {
            ClassLoader classLoader = this.getClass().getClassLoader();
            Class clazz = classLoader.loadClass((String) autofillProviders.get(scheme));
            Method fill = clazz.getMethod("fill", new Class[] { JiraRss.class });
            List list = (List) fill.invoke(null, new Object[] { this });
            for (int i = 0; i < list.size(); i++) {
                Issue issue = (Issue) list.get(i);
                issues.put(issue.getKey(), issue);
            }
        } catch (Exception e) {
            System.err.println("Cannot install autofill provider " + scheme);
            e.printStackTrace();
        }
    }

    public List fillVotes() throws Exception {
        ClassLoader classLoader = this.getClass().getClassLoader();
        Class clazz = classLoader.loadClass("org.codehaus.swizzle.jira.VotersFiller");
        Method fill = clazz.getMethod("fill", new Class[] { JiraRss.class });
        return (List) fill.invoke(null, new Object[] { this });
    }

    public List fillSubTasks() throws Exception {
        ClassLoader classLoader = this.getClass().getClassLoader();
        Class clazz = classLoader.loadClass("org.codehaus.swizzle.jira.SubTasksFiller");
        Method fill = clazz.getMethod("fill", new Class[] { JiraRss.class });
        return (List) fill.invoke(null, new Object[] { this });
    }

    public List fillAttachments() throws Exception {
        autofill("attachments");
        return getIssues();
    }

    public List getIssues() {
        return new MapObjectList(issues.values());
    }

    public Issue getIssue(String key) {
        return (Issue) issues.get(key);
    }

    private class ObjectBuilder extends DefaultHandler {
        private Map handlers = new HashMap();
        private Stack handlerStack = new Stack();
        private Channel channel;

        public ObjectBuilder() {
            // channelHandler = new MapObjectHandler(Channel.class);
            TextHandler textHandler = new TextHandler();
            // this.registerHandler("channel", channelHandler);
            this.registerHandler("item", new MapObjectListHandler(Issue.class, null));
            this.registerHandler("priority", new MapObjectHandler(Priority.class));
            this.registerHandler("status", new MapObjectHandler(Status.class));
            this.registerHandler("type", new MapObjectHandler(IssueType.class));
            this.registerHandler("resolution", new MapObjectHandler(Resolution.class));
            this.registerHandler("fixVersion", new MapObjectListHandler(Version.class));
            this.registerHandler("affectsVersion", new MapObjectListHandler(Version.class));
            this.registerHandler("assignee", new UserHandler());
            this.registerHandler("reporter", new UserHandler());
            this.registerHandler("component", new MapObjectListHandler(Component.class));
            this.registerHandler("comment", new CommentHandler());
            this.registerHandler("title", textHandler);
            this.registerHandler("link", textHandler);
            this.registerHandler("description", textHandler);
            this.registerHandler("environment", textHandler);
            this.registerHandler("summary", textHandler);
            this.registerHandler("created", textHandler);
            this.registerHandler("updated", textHandler);
            this.registerHandler("votes", textHandler);
            this.registerHandler("due", new TextHandler("duedate"));
            this.registerHandler("key", new KeyHandler());
            channel = new Channel();
            objects.push(channel);
        }

        public void registerHandler(String name, Object handler) {
            handlers.put(name, handler);
        }

        public List getIssues() {
            return channel.getIssues();
        }

        public void startElement(String uri, String localName, String qName, Attributes attributes) throws SAXException {
            DefaultHandler handler = createHandler(qName);
            handlerStack.push(handler);
            handler.startElement(uri, localName, qName, attributes);
        }

        public void characters(char[] chars, int i, int i1) throws SAXException {
            DefaultHandler handler = (DefaultHandler) handlerStack.peek();
            handler.characters(chars, i, i1);
        }

        public void endElement(String string, String string1, String string2) throws SAXException {
            DefaultHandler handler = (DefaultHandler) handlerStack.pop();
            handler.endElement(string, string1, string2);
        }

        private DefaultHandler createHandler(String qName) {
            Object object = handlers.get(qName);
            if (object instanceof DefaultHandler) {
                try {
                    DefaultHandler handler = (DefaultHandler) object;
                    return (DefaultHandler) handler.clone();
                } catch (CloneNotSupportedException e) {
                    throw new RuntimeException(e);
                }
            }
            Class handlerClass = (Class) object;
            if (handlerClass == null) {
                return new DefaultHandler();
            }

            try {
                return (DefaultHandler) handlerClass.newInstance();
            } catch (Exception e) {
                throw new RuntimeException(e);
            }
        }
    }

    public static class Channel extends MapObject {
        public Channel() {
            super(new HashMap());
        }

        public Channel(Map data) {
            super(data);
        }

        public List getIssues() {
            return getMapObjects("items", Issue.class);
        }
    }

    private Stack objects = new Stack();

    public class DefaultHandler extends org.xml.sax.helpers.DefaultHandler implements Cloneable {
        protected Object clone() throws CloneNotSupportedException {
            return super.clone();
        }
    }

    public class TextHandler extends DefaultHandler {

        protected StringBuffer value = new StringBuffer();
        protected String name;

        public TextHandler() {
        }

        public TextHandler(String name) {
            this.name = name;
        }

        public void startElement(String uri, String localName, String qName, Attributes attributes) throws SAXException {
            if (name == null) name = qName;
        }

        public void characters(char[] chars, int i, int i1) throws SAXException {
            value.append(chars, i, i1);
        }

        public void endElement(String string, String string1, String string2) throws SAXException {
            MapObject status = (MapObject) objects.peek();
            String text = value.toString();
            text = text.replaceAll("^<p>|</p>$", "");
            status.setString(name, text);
        }

        protected Object clone() throws CloneNotSupportedException {
            return new TextHandler(name);
        }
    }

    public class KeyHandler extends TextHandler {
        public void startElement(String uri, String localName, String qName, Attributes attributes) throws SAXException {
            MapObject status = (MapObject) objects.peek();
            status.setString("id", attributes.getValue("id"));
            super.startElement(uri, localName, qName, attributes);
        }

        protected Object clone() throws CloneNotSupportedException {
            return new KeyHandler();
        }
    }

    public class MapObjectHandler extends DefaultHandler {
        protected Map atts = new HashMap();
        protected MapObject mapObject;
        protected StringBuffer value = new StringBuffer();
        protected String contentField;
        protected Class mapObjectClass;

        public MapObjectHandler(Class mapObjectClass) {
            this(mapObjectClass, "name");
        }

        public MapObjectHandler(Class mapObjectClass, String contentField) {
            this.mapObjectClass = mapObjectClass;
            this.contentField = contentField;
            this.atts.put("id", "id");
        }

        public void startElement(String uri, String localName, String qName, Attributes attributes) throws SAXException {
            mapObject = createMapObject();

            for (int i = 0; i < attributes.getLength(); i++) {
                String name = attributes.getQName(i);
                String field = (String) atts.get(name);
                if (field != null) {
                    mapObject.setString(field, attributes.getValue(i));
                }
            }
            setMapObject(qName, mapObject);
            objects.push(mapObject);
        }

        private MapObject createMapObject() {
            if (this.mapObject != null) {
                return this.mapObject;
            }
            MapObject mapObject;
            try {
                Constructor constructor = mapObjectClass.getConstructor(new Class[] { Map.class });
                mapObject = (MapObject) constructor.newInstance(new Object[] { new HashMap() });
            } catch (Exception e) {
                throw new RuntimeException(e);
            }
            return mapObject;
        }

        protected void setMapObject(String qName, MapObject mapObject) {
            try {
                MapObject parent = (MapObject) objects.peek();
                parent.setMapObject(qName, mapObject);
            } catch (EmptyStackException e) {
            }
        }

        public void characters(char[] chars, int i, int i1) throws SAXException {
            value.append(chars, i, i1);
        }

        public void endElement(String string, String string1, String string2) throws SAXException {
            objects.pop();
            if (contentField != null) {
                mapObject.setString(contentField, value.toString());
            }
        }

        protected Object clone() throws CloneNotSupportedException {
            return new MapObjectHandler(mapObjectClass, contentField);
        }
    }

    public class UserHandler extends MapObjectHandler {
        public UserHandler() {
            super(User.class);
            atts.clear();
            atts.put("username", "name");
            contentField = "fullname";
        }

        protected Object clone() throws CloneNotSupportedException {
            return new UserHandler();
        }
    }

    public class MapObjectListHandler extends MapObjectHandler {
        public MapObjectListHandler(Class mapObjectClass) {
            super(mapObjectClass);
        }

        public MapObjectListHandler(Class mapObjectClass, String contentField) {
            super(mapObjectClass, contentField);
        }

        protected void setMapObject(String qName, MapObject mapObject) {
            MapObject parent = (MapObject) objects.peek();
            List list = parent.getMapObjects(qName + "s", mapObject.getClass());
            list.add(mapObject);
        }

        protected Object clone() throws CloneNotSupportedException {
            return new MapObjectListHandler(mapObjectClass, contentField);
        }
    }

    public class CommentHandler extends MapObjectListHandler {
        public CommentHandler() {
            super(Comment.class);
            atts.clear();
            atts.put("author", "username");
            atts.put("created", "timePerformed");
            contentField = "body";
        }

        protected Object clone() throws CloneNotSupportedException {
            return new CommentHandler();
        }

        public void endElement(String string, String string1, String string2) throws SAXException {
            String text = value.toString();
            text = text.replaceAll("^<p>|</p>$", "");
            value = new StringBuffer(text);
            super.endElement(string, string1, string2);
        }

    }
}
