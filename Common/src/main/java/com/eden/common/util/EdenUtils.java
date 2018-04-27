package com.eden.common.util;

import com.eden.common.json.JSONElement;
import com.eden.common.json.Jsonable;
import org.json.JSONArray;
import org.json.JSONObject;

import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

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
        if (object instanceof Jsonable) return true;
        if (object instanceof JSONObject) return true;
        if (object instanceof JSONArray) return true;
        if (object instanceof String) return true;
        if (object instanceof Byte) return true;
        if (object instanceof Short) return true;
        if (object instanceof Integer) return true;
        if (object instanceof Long) return true;
        if (object instanceof Double) return true;
        if (object instanceof Float) return true;
        if (object instanceof Boolean) return true;

        return false;
    }

    public static boolean elementIsObject(JSONElement el) {
        return (el != null) && (el.getElement() != null) && (el.getElement() instanceof JSONObject);
    }

    public static boolean elementIsArray(JSONElement el) {
        return (el != null) && (el.getElement() != null) && (el.getElement() instanceof JSONArray);
    }

    public static boolean elementIsString(JSONElement el) {
        return (el != null) && (el.getElement() != null) && (el.getElement() instanceof String);
    }

    public static JSONObject merge(JSONObject... sources) {
        JSONObject dest = new JSONObject();

        for (JSONObject tmpSource : sources) {
            if (tmpSource == null) continue;
            JSONObject source = new JSONObject(tmpSource.toMap());

            for (String key : source.keySet()) {
                if (dest.has(key)) {
                    if (dest.get(key) instanceof JSONObject && source.get(key) instanceof JSONObject) {
                        dest.put(key, merge(dest.getJSONObject(key), source.getJSONObject(key)));
                    }
                    else if (dest.get(key) instanceof JSONArray && source.get(key) instanceof JSONArray) {
                        for (Object obj : source.getJSONArray(key)) {
                            dest.getJSONArray(key).put(obj);
                        }
                    }
                    else {
                        dest.put(key, source.get(key));
                    }
                }
                else {
                    dest.put(key, source.get(key));
                }
            }
        }

        return dest;
    }

    public static Map<String, Object> merge(Map<String, ?>... sources) {
        Map<String, Object> dest = new HashMap<>();

        for (Map<String, ?> tmpSource : sources) {
            if (tmpSource == null) continue;
            Map<String, ?> source = new HashMap<>(tmpSource);

            for (String key : source.keySet()) {
                if (dest.containsKey(key)) {
                    if (dest.get(key) instanceof Map && source.get(key) instanceof Map) {
                        dest.put(key, merge((Map<String, ?>) dest.get(key), (Map<String, ?>) source.get(key)));
                    }
                    else if (dest.get(key) instanceof Collection && source.get(key) instanceof Collection) {
                        for (Object obj : (Collection) source.get(key)) {
                            ((Collection) dest.get(key)).add(obj);
                        }
                    }
                    else {
                        dest.put(key, source.get(key));
                    }
                }
                else {
                    dest.put(key, source.get(key));
                }
            }
        }

        return dest;
    }


// Auto-box primitive arrays
//----------------------------------------------------------------------------------------------------------------------

    public static Integer[] box(int[] items) {
        Integer[] newItems;
        if (items == null) {
            newItems = new Integer[0];
        }
        else {
            newItems = new Integer[items.length];
            for (int i = 0; i < items.length; i++) {
                newItems[i] = items[i];
            }
        }
        return newItems;
    }

    public static Long[] box(long[] items) {
        Long[] newItems;
        if (items == null) {
            newItems = new Long[0];
        }
        else {
            newItems = new Long[items.length];
            for (int i = 0; i < items.length; i++) {
                newItems[i] = items[i];
            }
        }
        return newItems;
    }

    public static Boolean[] box(boolean[] items) {
        Boolean[] newItems;
        if (items == null) {
            newItems = new Boolean[0];
        }
        else {
            newItems = new Boolean[items.length];
            for (int i = 0; i < items.length; i++) {
                newItems[i] = items[i];
            }
        }
        return newItems;
    }

    public static Double[] box(double[] items) {
        Double[] newItems;
        if (items == null) {
            newItems = new Double[0];
        }
        else {
            newItems = new Double[items.length];
            for (int i = 0; i < items.length; i++) {
                newItems[i] = items[i];
            }
        }
        return newItems;
    }

    public static Float[] box(float[] items) {
        Float[] newItems;
        if (items == null) {
            newItems = new Float[0];
        }
        else {
            newItems = new Float[items.length];
            for (int i = 0; i < items.length; i++) {
                newItems[i] = items[i];
            }
        }
        return newItems;
    }

    public static Object[] box(Object items) {
        if (items.getClass().equals(int[].class)) {
            return box((int[]) items);
        }
        else if (items.getClass().equals(long[].class)) {
            return box((long[]) items);
        }
        else if (items.getClass().equals(boolean[].class)) {
            return box((boolean[]) items);
        }
        else if (items.getClass().equals(double[].class)) {
            return box((double[]) items);
        }
        else if (items.getClass().equals(float[].class)) {
            return box((float[]) items);
        }
        else if (items.getClass().isArray()) {
            return (Object[]) items;
        }

        return new Object[0];
    }


}
