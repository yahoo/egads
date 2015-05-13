/*
 * Copyright 2015, Yahoo Inc.
 * Copyrights licensed under the GPL License.
 * See the accompanying LICENSE file for terms.
 */

// function class
// Helps any object encode its member variables as JSON.
// Any JsonAble object can customize its encoding in its own to/fromJson() function

package com.yahoo.egads.data;

import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.lang.reflect.Modifier;
import java.lang.reflect.ParameterizedType;
import java.util.ArrayList;
import java.util.Iterator;
import org.json.JSONArray;
import org.json.JSONStringer;
import org.json.JSONObject;

public class JsonEncoder {

    // methods ////////////////////////////////////////////////

    public static String toJson(Object object) throws Exception {
        JSONStringer jsonOut = new JSONStringer();
        toJson(object, jsonOut);
        return jsonOut.toString();
    }

    public static void // modifies json_out
    toJson(Object object, JSONStringer json_out) throws Exception {
        json_out.object();
        // for each inherited class...
        for (Class c = object.getClass(); c != Object.class; c = c
                .getSuperclass()) {
            // for each member variable... 
            Field[] fields = c.getDeclaredFields();
            for (Field f : fields) {
                // if variable is static/private... skip it
                if (Modifier.isStatic(f.getModifiers())) {
                    continue;
                }
                if (Modifier.isPrivate(f.getModifiers())) {
                    continue;
                }
                Object value = f.get(object);

                // if variable is a complex type... recurse on sub-objects
                if (value instanceof JsonAble) {
                    json_out.key(f.getName());
                    ((JsonAble) value).toJson(json_out);
                    // if variable is an array... recurse on sub-objects
                } else if (value instanceof ArrayList) {
                    json_out.key(f.getName());
                    json_out.array();
                    for (Object e : (ArrayList) value) {
                        toJson(e, json_out);
                    }
                    json_out.endArray();
                    // if variable is a simple type... convert to json
                } else {
                    json_out.key(f.getName()).value(value);
                }
            }
        }
        json_out.endObject();
    }

    public static void fromJson(Object object, String json_str)
            throws Exception {
        JSONObject jsonObj = new JSONObject(json_str);
        fromJson(object, jsonObj);
    }

    public static void fromJson(Object object, JSONObject json_obj)
            throws Exception {
        // for each json key-value, that has a corresponding variable in object ...
        for (Iterator k = json_obj.keys(); k.hasNext();) {
            String key = (String) k.next();
            Object value = json_obj.get(key);

            // try to access object variable
            Field field = null;
            try {
                field = object.getClass().getField(key);
            } catch (Exception e) {
                continue;
            }
            if (Modifier.isStatic(field.getModifiers())) {
                continue;
            }
            if (Modifier.isPrivate(field.getModifiers())) {
                continue;
            }
            Object member = field.get(object);

            if (json_obj.isNull(key)) {
                field.set(object, null);
                continue;
                // if variable is container... recurse
            } else if (member instanceof JsonAble) {
                ((JsonAble) member).fromJson((JSONObject) value);
                // if variable is an array... recurse on sub-objects
            } else if (member instanceof ArrayList) {
                // Depends on existance of ArrayList<T> template parameter, and T constructor with no arguments.
                // May be better to use custom fromJson() in member class.
                ArrayList memberArray = (ArrayList) member;
                JSONArray jsonArray = (JSONArray) value;

                // find array element constructor
                ParameterizedType arrayType = null;
                if (field.getGenericType() instanceof ParameterizedType) {
                    arrayType = (ParameterizedType) field.getGenericType();
                }
                for (Class c = member.getClass(); arrayType == null
                        && c != null; c = c.getSuperclass()) {
                    if (c.getGenericSuperclass() instanceof ParameterizedType) {
                        arrayType = (ParameterizedType) c
                                .getGenericSuperclass();
                    }
                }
                if (arrayType == null) {
                    throw new Exception(
                            "could not find ArrayList element type for field 'key'");
                }
                Class elementClass = (Class) (arrayType
                        .getActualTypeArguments()[0]);
                Constructor elementConstructor = elementClass.getConstructor();

                // for each element in JSON array ... append element to member array, recursively decode element
                for (int i = 0; i < jsonArray.length(); ++i) {
                    Object element = elementConstructor.newInstance();
                    fromJson(element, jsonArray.getJSONObject(i));
                    memberArray.add(element);
                }
                // if variable is simple value... set
            } else if (field.getType() == float.class) {
                field.set(object, (float) json_obj.getDouble(key));
            } else {
                field.set(object, value);
            }
        }
    }
}
