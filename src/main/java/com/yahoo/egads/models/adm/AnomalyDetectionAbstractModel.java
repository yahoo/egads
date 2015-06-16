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

public abstract class AnomalyDetectionAbstractModel implements AnomalyDetectionModel {

	protected org.apache.logging.log4j.Logger logger;
    protected float sDAutoSensitivity = 3;
    protected float amntAutoSensitivity = (float) 0.05;
    protected String outputDest = "";
	protected String modelName;

    public String getModelName() {
		return modelName;
	}

	public String getModelType() {
    	return "Anomaly";
    }
    
    @Override
    public void toJson(JSONStringer json_out) throws Exception {
        JsonEncoder.toJson(this, json_out);
    }

    @Override
    public void fromJson(JSONObject json_obj) throws Exception {
        JsonEncoder.fromJson(this, json_obj);
    }
    
    protected String[] arrayF2S (Float[] input) {
    	String ret[] = new String[input.length];
    	for (int ix = 0; ix < input.length; ix++) {
                if (input[ix] == null) {
                  ret[ix] = "Inf";
                } else {
    		  ret[ix] = input[ix].toString();
                }
    	}
    	return ret;
    }

    // Force the user to define this constructor that acts as a
    // factory method.
    public AnomalyDetectionAbstractModel(Properties config) {
    	logger = org.apache.logging.log4j.LogManager.getLogger(this.getClass().getName());
        // Set the assumed amount of anomaly in your data.
        if (config.getProperty("AUTO_SENSITIVITY_ANOMALY_PCNT") != null) {
            this.amntAutoSensitivity = new Float(config.getProperty("AUTO_SENSITIVITY_ANOMALY_PCNT"));
        }
        // Set the standard deviation for auto sensitivity.
        if (config.getProperty("AUTO_SENSITIVITY_SD") != null) {
            this.sDAutoSensitivity = new Float(config.getProperty("AUTO_SENSITIVITY_SD"));
        }
      	this.outputDest = config.getProperty("OUTPUT");
    }
}
