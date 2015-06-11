/*
 * Copyright 2015, Yahoo Inc.
 * Copyrights licensed under the GPL License.
 * See the accompanying LICENSE file for terms.
 */

package com.yahoo.egads.models.adm;

import java.util.Properties;
import org.json.JSONObject;
import org.json.JSONStringer;
import com.yahoo.egads.data.JsonEncoder;

public abstract class AnomalyDetectionAbstractModel implements
        AnomalyDetectionModel {

    @Override
    public void toJson(JSONStringer json_out) throws Exception {
        JsonEncoder.toJson(this, json_out);
    }

    @Override
    public void fromJson(JSONObject json_obj) throws Exception {
        JsonEncoder.fromJson(this, json_obj);
    }

    // Force the user to define this constructor that acts as a
    // factory method.
    AnomalyDetectionAbstractModel(Properties config) {
    }
}
