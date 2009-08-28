package org.codehaus.swizzle.jira;

import java.util.List;
import java.util.Map;

public class CustomFieldValue extends MapObject {
    /**
     * 
     */
    public CustomFieldValue() {
        super();
    }

    /**
     * @param data
     */
    public CustomFieldValue(Map data) {
        super(data);
    }

    /**
     * @return the customfieldId
     */
    public String getCustomfieldId() {
        return getString("customfieldId");
    }

    /**
     * @param customfieldId
     *            the customfieldId to set
     */
    public void setCustomfieldId(String customfieldId) {
        setString("customfieldId", customfieldId);
    }

    /**
     * @return the key
     */
    public String getKey() {
        return getString("key");
    }

    /**
     * @param key
     *            the key to set
     */
    public void setKey(String key) {
        setString("key", key);
    }

    /**
     * @return the values
     */
    public List getValues() {
        return getList("values");
    }

    /**
     * @param values
     *            the values to set
     */
    public void setValues(List values) {
        setList("values", values);
    }

    public String toString() {
        return (getCustomfieldId() != null) ? getCustomfieldId() : getKey();
    }
}
