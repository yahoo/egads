/*
 * Copyright 2015, Yahoo Inc.
 * Copyrights licensed under the GPL License.
 * See the accompanying LICENSE file for terms.
 */

// data structure class
// Contains meta-data about a metric.

package com.yahoo.egads.data;

import org.json.JSONObject;
import org.json.JSONStringer;

public class MetricMeta implements JsonAble {
    // member data ////////////////////////////////////////////////

    public String id;
    public boolean detectAnomalies = false;
    public String name;
    public String fileName;
    public String source;
    public String smoothing;
    public long[] seasons;

    // construction ////////////////////////////////////////////////

    public MetricMeta() {
    }

    public MetricMeta(String id_arg) {
        id = id_arg;
    }

    // methods ////////////////////////////////////////////////

    // display ////////////////////////////////////////////////

    public String toString() {
        StringBuffer str = new StringBuffer();
        str.append("id=" + id);
        str.append(" detectAnomalies=" + detectAnomalies);
        str.append(" name=" + name);
        str.append(" source=" + source);
        return str.toString();
    }

    public void toJson(JSONStringer json_out) throws Exception {
        JsonEncoder.toJson(this, json_out);
    }

    public void fromJson(JSONObject json_obj) throws Exception {
        JsonEncoder.fromJson(this, json_obj);
    }

    // test ////////////////////////////////////////////////

    // needed for unit tests
    public boolean equals(Object other_obj) {
        if (!(other_obj instanceof MetricMeta)) {
            return false;
        }
        MetricMeta other = (MetricMeta) other_obj;
        if (!equals(id, other.id)) {
            return false;
        }
        if (detectAnomalies != other.detectAnomalies) {
            return false;
        }
        if (!equals(name, other.name)) {
            return false;
        }
        if (!equals(source, other.source)) {
            return false;
        }
        return true;
    }

    public static boolean equals(Object o1, Object o2) {
        if (o1 == o2) {
            return true;
        }
        if (o1 == null && o2 != null) {
            return false;
        }
        if (o1 != null && o2 == null) {
            return false;
        }
        return o1.equals(o2);
    }

}
