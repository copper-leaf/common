package com.eden.common.json;

import com.eden.common.util.EdenUtils;
import org.json.JSONArray;
import org.json.JSONObject;

import java.util.Objects;

public final class JSONElement {

    private Jsonable jsonableProxy;
    private JSONElement jsonElementProxy;

    private Object element;

    public JSONElement(Object object)      { this.element          = validate(object); }
    public JSONElement(Jsonable object)    { this.jsonableProxy    = validate(object); }
    public JSONElement(JSONElement object) { this.jsonElementProxy = validate(object); }
    public JSONElement(JSONObject object)  { this.element          = validate(object); }
    public JSONElement(JSONArray object)   { this.element          = validate(object); }
    public JSONElement(String object)      { this.element          = validate(object); }
    public JSONElement(byte object)        { this.element          = validate(object); }
    public JSONElement(short object)       { this.element          = validate(object); }
    public JSONElement(int object)         { this.element          = validate(object); }
    public JSONElement(long object)        { this.element          = validate(object); }
    public JSONElement(float object)       { this.element          = validate(object); }
    public JSONElement(double object)      { this.element          = validate(object); }
    public JSONElement(boolean object)     { this.element          = validate(object); }

    private <T> T validate(T object) {
        if(object == null) {
            throw new IllegalArgumentException("A JSONElement cannot be null");
        }
        else if(!EdenUtils.isJsonAware(object)) {
            throw new IllegalArgumentException("A JSONElement must be an object that is json-aware (JSONObject, JSONArray, JSONElement, Jsonable, String, or primitive)");
        }
        else {
            return object;
        }
    }

    private void proxy() {
        if(element == null) {
            if(jsonableProxy != null) {
                element = jsonableProxy.toJson();
            }
            else if(jsonElementProxy != null) {
                element = jsonElementProxy.getElement();
            }
        }
    }

    public Object getElement() {
        proxy();

        return element;
    }

    @Override
    public String toString() {
        proxy();
        return element.toString();
    }

    /**
     * Query the gathered site data using a javascript-like syntax, or the native JSONObject query syntax. For example,
     * given a JSONObject initialized with this document:
     * <pre>
     * {
     *   "a": {
     *     "b": "c"
     *   }
     * }
     * </pre>
     * and this JSONPointer string:
     * <pre>
     * "/a/b"
     * </pre>
     * or this Javascript pointer string:
     * <pre>
     * "a.b"
     * </pre>
     * Then this method will return the String "c".
     * In the end, the Javascript syntax is converted to the corresponding JSONPointer syntax and queried.
     *
     * @param pointer  string that can be used to create a JSONPointer
     * @return  the item matched by the JSONPointer, otherwise null
     */
    public JSONElement query(String pointer) {
        proxy();

        try {
            if (!EdenUtils.isEmpty(pointer)) {
                pointer = pointer.replaceAll("\\.", "/");

                if (!pointer.startsWith("/")) {
                    pointer = "/" + pointer;
                }

                Object result = null;

                if (element instanceof JSONObject) {
                    result = ((JSONObject) element).query(pointer);
                } else if (element instanceof JSONArray) {
                    result = ((JSONArray) element).query(pointer);
                }

                if (result != null) {
                    return new JSONElement(result);
                }
            }
        } catch (Exception e) {

        }

        return null;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof JSONElement)) return false;
        JSONElement that = (JSONElement) o;
        return Objects.equals(getElement(), that.getElement());
    }

    @Override
    public int hashCode() {
        return Objects.hash(getElement());
    }

}