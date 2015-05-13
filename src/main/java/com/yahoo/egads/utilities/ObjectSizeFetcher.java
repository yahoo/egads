/*
 * Copyright 2015, Yahoo Inc.
 * Copyrights licensed under the GPL License.
 * See the accompanying LICENSE file for terms.
 */

package com.yahoo.egads.utilities;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.ObjectOutputStream;
import java.io.Serializable;

// Fetches the size of an object in bytes.
public class ObjectSizeFetcher implements Serializable {

    private static final long serialVersionUID = 1L;
    //Fetches the size of a java object.
    public static int sizeOf(Object obj) {
        ByteArrayOutputStream byteObject = new ByteArrayOutputStream();
        ObjectOutputStream objectOutputStream;
        try {
            objectOutputStream = new ObjectOutputStream(byteObject);
            objectOutputStream.writeObject(obj);
            objectOutputStream.flush();
            objectOutputStream.close();
            byteObject.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
        return byteObject.size();
    }
}
