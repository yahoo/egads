/*
 * Copyright 2015, Yahoo Inc.
 * Copyrights licensed under the GPL License.
 * See the accompanying LICENSE file for terms.
 */

package com.yahoo.egads.models.adm;

import java.util.Properties;

import org.json.JSONObject;
import org.json.JSONStringer;
import java.util.Map;
import java.util.HashMap;

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
    
    protected String arrayF2S (Float[] input) {
    	String ret = new String();
    	if (input.length == 0) {
    		return "";
    	}
    	if (input[0] == null) {
    		ret = "Inf";
    	} else {
    		ret = input[0].toString();
    	}
    	for (int ix = 1; ix < input.length; ix++) {
            if (input[ix] == null) {
                ret += ":Inf";
            } else {
    		    ret += ":" + input[ix].toString();
            }
    	}
    	return ret;
    }
    
    // Parses the THRESHOLD config into a map.
    protected Map<String, Float> parseMap(String s) {
        if (s == null) {
            return new HashMap<String, Float>();
        }
        String[] pairs = s.split(",");
        Map<String, Float> myMap = new HashMap<String, Float>();
        for (int i = 0; i < pairs.length; i++) {
            String pair = pairs[i];
            String[] keyValue = pair.split("#");
            myMap.put(keyValue[0], Float.valueOf(keyValue[1]));
        }
        return myMap;
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
