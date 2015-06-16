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

import org.json.JSONObject;
import org.json.JSONStringer;

public class SimpleThresholdModel extends AnomalyDetectionAbstractModel {

    // The constructor takes a set of properties
    // needed for the simple model. This includes the sensitivity.
    protected Float threshold;
    
    // Model name.
    private String modelName = "SimpleThresholdModel";

    public SimpleThresholdModel(Properties config) {
        super(config);
        
        if (config.getProperty("THRESHOLD") == null) {
            this.threshold = null;
        } else {
            this.threshold = new Float(config.getProperty("THRESHOLD"));
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
        
        if (threshold == null) {
        	threshold = AutoSensitivity.getKSigmaSensitivity(observedSeries.getValues(), sDAutoSensitivity);
        }
    }

    @Override
    public IntervalSequence detect(DataSequence observedSeries,
            DataSequence expectedSeries) throws Exception {
        IntervalSequence output = new IntervalSequence();

        for (TimeSeries.Entry entry : observedSeries) {
            if (entry.value > threshold) {
                output.add(new Interval(entry.logicalIndex, entry.logicalIndex,
                        entry.value - threshold));
            }
        }

        return output;
    }
}
