/*
 * Copyright 2015, Yahoo Inc.
 * Copyrights licensed under the GPL License.
 * See the accompanying LICENSE file for terms.
 */

// A simple thresholding model that returns an anomaly if it is above/below a certain threashold.

package com.yahoo.egads.models.adm;

import java.util.Properties;

import com.yahoo.egads.data.Anomaly.IntervalSequence;
import com.yahoo.egads.data.Anomaly.Interval;
import com.yahoo.egads.data.TimeSeries;
import com.yahoo.egads.data.TimeSeries.DataSequence;
import com.yahoo.egads.utilities.AutoSensitivity;
import java.util.Map;
import java.util.HashMap;

import org.json.JSONObject;
import org.json.JSONStringer;

public class SimpleThresholdModel extends AnomalyDetectionAbstractModel {

    // The constructor takes a set of properties
    // needed for the simple model. This includes the sensitivity.
    private Map<String, Float> threshold;
    private int maxHrsAgo;
    
    // Model name.
    private String modelName = "SimpleThresholdModel";
    private String simpleThrType = "AdaptiveKSigmaSensitivity";

    public SimpleThresholdModel(Properties config) {
        super(config);
        
        this.threshold = parseMap(config.getProperty("THRESHOLD"));
        this.maxHrsAgo = new Integer(config.getProperty("MAX_ANOMALY_TIME_AGO"));
        if (config.getProperty("THRESHOLD") != null && this.threshold.isEmpty() == true) {
            throw new IllegalArgumentException("THRESHOLD PARSE ERROR");
        } 
        if (config.getProperty("SIMPLE_THRESHOLD_TYPE") != null) {
            simpleThrType = config.getProperty("SIMPLE_THRESHOLD_TYPE");
        }
    }

    public void toJson(JSONStringer json_out) {

    }

    public void fromJson(JSONObject json_obj) {

    }

    @Override
    public String getType() {
        return "point_outlier";
    }

    public String getModelName() {
        return modelName;
    }

    @Override
    public void reset() {
    }

    @Override
    public void tune(DataSequence observedSeries, DataSequence expectedSeries,
            IntervalSequence anomalySequence) throws Exception {  
        Float thr[] = null;
        if (simpleThrType.equals("AdaptiveKSigmaSensitivity")) {
            thr = AutoSensitivity.getAdaptiveKSigmaSensitivity(observedSeries.getValues(), amntAutoSensitivity); 
        } else {
    	    thr = AutoSensitivity.getAdaptiveMaxMinSigmaSensitivity(observedSeries.getValues(), amntAutoSensitivity, sDAutoSensitivity); 
        }
        if (!threshold.containsKey("max")) {
            threshold.put("max", thr[0]);
        }  
        if (!threshold.containsKey("min")) {
            threshold.put("min", thr[1]);
        }
    }

    @Override
    public IntervalSequence detect(DataSequence observedSeries,
            DataSequence expectedSeries) throws Exception {
        IntervalSequence output = new IntervalSequence();
        Float[] thr = new Float[] {threshold.get("max"), threshold.get("min")};
        long unixTime = System.currentTimeMillis() / 1000L;
        int n = observedSeries.size();
        for (int i = 0; i < n; i++) {
            TimeSeries.Entry entry = observedSeries.get(i);
            
            if (((thr[0] != null && entry.value >= thr[0]) || (thr[1] != null && entry.value <= thr[1])) && ((((unixTime - entry.time) / 3600) < maxHrsAgo) || (maxHrsAgo == 0 && i == (n - 1)))) {
                if (thr[0] != null && entry.value >= thr[0]) {
                    output.add(new Interval(entry.time, i, null, thr, entry.value, thr[0]));
                } else {
                    output.add(new Interval(entry.time, i, null, thr, entry.value, thr[1]));
                }
            }
        }

        return output;
    }
}
