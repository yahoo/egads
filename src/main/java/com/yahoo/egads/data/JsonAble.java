/*
 * Copyright 2015, Yahoo Inc.
 * Copyrights licensed under the GPL License.
 * See the accompanying LICENSE file for terms.
 */

// interface
// Implementing classes can use JsonEncoder to help implement these methods.

package com.yahoo.egads.data;

import org.json.JSONObject;
import org.json.JSONStringer;

public interface JsonAble {

    void // modifies json_out
    toJson(JSONStringer json_out) throws Exception;

    void fromJson(JSONObject json_obj) throws Exception;

}
