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

import java.lang.reflect.Constructor;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Pattern;

/**
 * @version $Revision$ $Date$
 */
public class MapObjectList extends ArrayList {

    public MapObjectList() {
    }

    public MapObjectList(Collection collection) {
        super(collection);
    }

    public MapObjectList(int i) {
        super(i);
    }

    public Object min(String field) {
        return Collections.min(this, getComparator(field));
    }

    public Object max(String field) {
        return Collections.max(this, getComparator(field));
    }

    // public List values(String field) {
    // if (size() == 0) return this;
    // Accessor accessor = new Accessor(field, this);
    //
    // boolean mapObjectData = true;
    // List uniqueList = new ArrayList();
    // for (int i = 0; i < this.size(); i++) {
    // MapObject mapObject = (MapObject) this.get(i);
    // Object value = accessor.getValue(mapObject);
    // if (!uniqueList.contains(value)){
    // uniqueList.add(value);
    // mapObjectData = mapObjectData && value instanceof MapObject;
    // }
    // }
    //
    // if (mapObjectData){
    // return new MapObjectList(uniqueList);
    // } else {
    // return uniqueList;
    // }
    // }

    public List collect(String field) {
        if (size() == 0) return this;
        Accessor accessor = new Accessor(field, this);

        boolean mapObjectData = true;
        List collection = new ArrayList();
        for (int i = 0; i < this.size(); i++) {
            MapObject mapObject = (MapObject) this.get(i);
            Object value = accessor.getValue(mapObject);
            if (value instanceof List) {
                List list = (List) value;
                for (int j = 0; j < list.size(); j++) {
                    Object object = list.get(j);
                    collection.add(object);
                    mapObjectData = mapObjectData && object instanceof MapObject;
                }
            } else {
                collection.add(value);
                mapObjectData = mapObjectData && value instanceof MapObject;
            }
        }

        if (mapObjectData) {
            return new MapObjectList(collection);
        } else {
            return collection;
        }
    }

    public MapObjectList unique(String field) {
        if (size() == 0) return this;
        Accessor accessor = new Accessor(field, this);
        MapObjectList subset = new MapObjectList();
        List uniqueList = new ArrayList();
        for (int i = 0; i < this.size(); i++) {
            MapObject mapObject = (MapObject) this.get(i);
            Object value = accessor.getValue(mapObject);
            if (!uniqueList.contains(value)) {
                uniqueList.add(value);
                subset.add(mapObject);
            }
        }

        return subset;
    }

    /**
     * Returns returns a new list with both lists added together.
     * 
     * http://en.wikipedia.org/wiki/Set#Unions
     * 
     * @param list
     * @return new list c = a + b
     */
    public MapObjectList union(List list) {
        MapObjectList difference = new MapObjectList(this);
        for (int i = 0; i < list.size(); i++) {
            Object object = list.get(i);
            if (!this.contains(object)) {
                difference.add(object);
            }
        }
        return difference;
    }

    /**
     * Returns a new list containing the common items from this list and the specified list.
     * 
     * Put another way, returns only the items in a and b that overlap.
     * 
     * http://en.wikipedia.org/wiki/Set#Intersections
     * 
     * @param list
     * @return new list c = a && b
     */
    public MapObjectList intersection(List list) {
        MapObjectList common = new MapObjectList();
        for (int i = 0; i < this.size(); i++) {
            Object object = this.get(i);
            if (list.contains(object)) {
                common.add(object);
            }
        }
        return common;
    }

    /**
     * Synonym for intersection
     * 
     * @param list
     * @return new list c = a && b
     */
    public MapObjectList common(List list) {
        return intersection(list);
    }

    /**
     * Returns a new list containing only the items from list a not present in list b.
     * 
     * http://en.wikipedia.org/wiki/Set#Complements
     * 
     * @param list
     * @return new list c = a - b
     */
    public MapObjectList subtract(List list) {
        MapObjectList subtract = new MapObjectList(this);
        for (int i = 0; i < list.size(); i++) {
            Object object = list.get(i);
            subtract.remove(object);
        }
        return subtract;
    }

    /**
     * Returns a new list containing only the items from list a not present in list b.
     * 
     * This is logically equivalent to:
     * 
     * a.union(b).subtract(a.common(b))
     * 
     * Though this method is more efficient.
     * 
     * http://en.wikipedia.org/wiki/Symmetric_difference
     * 
     * @param list
     * @return new list c = a XOR b
     */
    public MapObjectList difference(List list) {
        MapObjectList difference = new MapObjectList(this);
        for (int i = 0; i < list.size(); i++) {
            Object object = list.get(i);
            if (this.contains(object)) {
                difference.remove(object);
            } else {
                difference.add(object);
            }
        }
        return difference;
    }

    public int sum(String field) {
        if (size() == 0) return 0;
        int sum = 0;
        Accessor accessor = new Accessor(field, this);

        for (int i = 0; i < this.size(); i++) {
            try {
                MapObject mapObject = (MapObject) this.get(i);
                sum += accessor.intValue(mapObject);
            } catch (NumberFormatException e) {
            }
        }
        return sum;
    }

    public int average(String field) {
        if (size() == 0) return 0;
        int sum = 0;
        Accessor accessor = new Accessor(field, this);
        int count = 0;
        for (int i = 0; i < this.size(); i++) {
            try {
                MapObject mapObject = (MapObject) this.get(i);
                sum += accessor.intValue(mapObject);
                count++;
            } catch (NumberFormatException e) {
            }
        }
        return (sum == 0) ? sum : sum / count;
    }

    public MapObjectList contains(String field, String string) {
        if (size() == 0) return this;
        Accessor accessor = new Accessor(field, this);
        MapObjectList subset = new MapObjectList();
        for (int i = 0; i < this.size(); i++) {
            MapObject mapObject = (MapObject) this.get(i);
            String value = accessor.stringValue(mapObject);
            if (value != null && value.indexOf(string) != -1) {
                subset.add(mapObject);
            }
        }
        return subset;
    }

    public MapObjectList matches(String field, String string) {
        if (size() == 0) return this;
        Pattern pattern = Pattern.compile(string);
        Accessor accessor = new Accessor(field, this);
        MapObjectList subset = new MapObjectList();
        for (int i = 0; i < this.size(); i++) {
            MapObject mapObject = (MapObject) this.get(i);
            String value = accessor.stringValue(mapObject);
            if (value != null && pattern.matcher(value).matches()) {
                subset.add(mapObject);
            }
        }
        return subset;
    }

    public MapObjectList equals(String field, String string) {
        if (size() == 0) return this;
        Accessor accessor = new Accessor(field, this);
        MapObjectList subset = new MapObjectList();
        for (int i = 0; i < this.size(); i++) {
            MapObject mapObject = (MapObject) this.get(i);
            String value = accessor.stringValue(mapObject);
            if (value != null && value.equals(string)) {
                subset.add(mapObject);
            }
        }
        return subset;
    }

    public MapObjectList greater(String field, String string) {
        return compareAndCollect(field, string, 1);
    }

    public MapObjectList less(String field, String string) {
        return compareAndCollect(field, string, -1);
    }

    public MapObjectList greater(String field, Object object) {
        return compareAndCollect(field, object, 1);
    }

    public MapObjectList less(String field, Object object) {
        return compareAndCollect(field, object, -1);
    }

    /**
     * Synonym for sort(field, false);
     * 
     * @param field
     */
    public MapObjectList ascending(String field) {
        return sort(field);
    }

    /**
     * Synonym for sort(field, true);
     * 
     * @param field
     */
    public MapObjectList descending(String field) {
        return sort(field, true);
    }

    public MapObjectList sort(String field) {
        return sort(field, false);
    }

    public MapObjectList sort(String field, boolean reverse) {
        if (size() == 0) return this;
        Comparator comparator = getComparator(field);

        comparator = reverse ? new ReverseComparator(comparator) : comparator;
        MapObjectList list = new MapObjectList(this);
        Collections.sort(list, comparator);

        return list;
    }

    private MapObjectList compareAndCollect(String field, Object data, int condition) {
        if (size() == 0) return this;
        try {
            Class type = get(0).getClass();
            HashMap map = new HashMap();
            Object base;
            if (field.startsWith("@")) {
                Constructor constructor = type.getConstructor(new Class[] { Map.class });
                base = constructor.newInstance(new Object[] { map });
                ((MapObject) base).getAttributes().put(field.replaceFirst("^@", ""), data);
            } else {
                map.put(field, data);
                Constructor constructor = type.getConstructor(new Class[] { Map.class });
                base = constructor.newInstance(new Object[] { map });
            }

            Comparator comparator = getComparator(field);

            MapObjectList subset = new MapObjectList();
            for (int i = 0; i < size(); i++) {
                Object object = get(i);
                int value = comparator.compare(object, base);
                if (value / condition > 0) {
                    subset.add(object);
                }
            }
            return subset;
        } catch (Exception e) {
            return new MapObjectList();
        }
    }

    // private MapObjectList compareAndCollect(String field, Object base, int
    // condition) {
    // Comparator comparator = getComparator(field);
    //
    // MapObjectList subset = new MapObjectList();
    // for (int i = 0; i < this.size(); i++) {
    // Object object = this.get(i);
    // int value = comparator.compare(object, base);
    // if (value / condition > 0) {
    // subset.add(object);
    // }
    // }
    // return subset;
    // }

    private Comparator getComparator(String field) {
        return new FieldComparator(new Accessor(field, this));
    }

    private static class ReverseComparator implements Comparator {
        private final Comparator comparator;

        public ReverseComparator(Comparator comparator) {
            this.comparator = comparator;
        }

        public int compare(Object a, Object b) {
            return -1 * comparator.compare(a, b);
        }
    }

    private static class FieldComparator implements Comparator {
        private final Accessor accessor;

        public FieldComparator(Accessor accessor) {
            this.accessor = accessor;
        }

        public int compare(Object objectA, Object objectB) {
            try {
                Object a = accessor.getValue((MapObject) objectA);
                Object b = accessor.getValue((MapObject) objectB);
                if (a instanceof Comparable) {
                    return ((Comparable) a).compareTo(b);
                } else {
                    return a.toString().compareTo(b.toString());
                }
            } catch (Exception e) {
                return 0;
            }
        }
    }

    public static class Accessor {

        private final String field;
        private final Method method;
        private final boolean isAttribute;

        public Accessor(String field, List list) {
            isAttribute = field.startsWith("@");
            this.field = (isAttribute) ? field.replaceFirst("^@", "") : field;
            this.method = (!isAttribute) ? method(list, field) : null;
        }

        private Method method(List list, String field) {
            Method method = null;
            try {
                MapObject first = (MapObject) list.get(0);
                StringBuffer sb = new StringBuffer(field);
                sb.setCharAt(0, Character.toUpperCase(sb.charAt(0)));
                method = first.getClass().getMethod("get" + sb, new Class[] {});
            } catch (NoSuchMethodException e) {
            }
            return method;
        }

        public Object getValue(MapObject mapObject) {
            try {
                if (method != null) {
                    return method.invoke(mapObject, new Object[] {});
                }
            } catch (Exception e) {
            }
            return map(mapObject).get(field);
        }

        private Map map(MapObject mapObject) {
            return (isAttribute) ? mapObject.getAttributes() : mapObject.fields;
        }

        public int intValue(MapObject mapObject) throws java.lang.NumberFormatException {
            Object value = getValue(mapObject);
            if (value instanceof Number) {
                Number number = (Number) value;
                return number.intValue();
            }
            return new Integer(value.toString()).intValue();
        }

        public String stringValue(MapObject mapObject) {
            Object value;
            if (method != null && method.getReturnType() == String.class) {
                value = map(mapObject).get(field);
            } else {
                value = getValue(mapObject);
            }
            return (value == null) ? null : value.toString();
        }

        public String getField() {
            return field;
        }

        public Method getMethod() {
            return method;
        }
    }
}
