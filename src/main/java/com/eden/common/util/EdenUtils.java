package com.eden.common.util;

import com.eden.common.json.JSONElement;
import org.json.JSONArray;
import org.json.JSONObject;

import java.util.Collection;

public class EdenUtils {

    /**
     * Returns true if the string is null or empty. An empty string is defined to be either 0-length or all whitespace
     *
     * @param str the string to be examined
     * @return true if str is null or empty
     */
    public static boolean isEmpty(CharSequence str) {
        if (str == null || str.length() == 0 || str.toString().trim().length() == 0) {
            return true;
        }
        else {
            return false;
        }
    }

    public static boolean isEmpty(JSONElement str) {
        if (str != null && str.getElement().getClass().equals(String.class)) {
            return isEmpty((String) str.getElement());
        }
        else {
            return true;
        }
    }

    public static boolean isEmpty(Collection<?> collection) {
        if (collection == null || collection.size() == 0) {
            return true;
        }
        else {
            return false;
        }
    }

    public static boolean isEmpty(Object[] array) {
        if (array == null || array.length == 0) {
            return true;
        }
        else {
            return false;
        }
    }

    public static boolean isJsonAware(Object object) {
        if(object instanceof JSONObject) return true;
        if(object instanceof JSONArray)  return true;
        if(object instanceof String)     return true;
        if(object instanceof Byte)       return true;
        if(object instanceof Short)      return true;
        if(object instanceof Integer)    return true;
        if(object instanceof Long)       return true;
        if(object instanceof Double)     return true;
        if(object instanceof Float)      return true;
        if(object instanceof Boolean)    return true;

        return false;
    }
}
