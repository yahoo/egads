/*
 * Copyright 2015, Yahoo Inc.
 * Copyrights licensed under the GPL License.
 * See the accompanying LICENSE file for terms.
 */

// A simple model that does not require a forecasting model.
// It looks weather or not the max value for the past N hours exceeds
// X %.

package com.yahoo.egads.models.adm;

import java.util.Properties;

import com.yahoo.egads.data.Anomaly.IntervalSequence;
import com.yahoo.egads.data.Anomaly.Interval;
import com.yahoo.egads.data.TimeSeries.DataSequence;
import java.util.Map;
import java.util.ArrayList;
import java.util.HashMap;
import com.yahoo.egads.data.AnomalyErrorStorage;
import com.yahoo.egads.utilities.AutoSensitivity;

import org.json.JSONObject;
import org.json.JSONStringer;

public class NaiveModel extends AnomalyDetectionAbstractModel {

    // The constructor takes a set of properties
    // needed for the simple model. This includes the sensitivity.
    private Map<String, Float> threshold;
    private int maxHrsAgo;
    private Float window_size;
    // modelName.
    public static String modelName = "NaiveModel";
    public AnomalyErrorStorage aes = new AnomalyErrorStorage();

    public NaiveModel(Properties config) {
        super(config);

        if (config.getProperty("MAX_ANOMALY_TIME_AGO") == null) {
            throw new IllegalArgumentException("MAX_ANOMALY_TIME_AGO is NULL");
        }
        this.maxHrsAgo = new Integer(config.getProperty("MAX_ANOMALY_TIME_AGO"));
        if (config.getProperty("WINDOW_SIZE") == null) {
            throw new IllegalArgumentException("WINDOW_SIZE is NULL");
        }
        this.window_size = new Float(config.getProperty("WINDOW_SIZE"));
        if (config.getProperty("THRESHOLD") == null) {
        	throw new IllegalArgumentException("THRESHOLD is NULL");
        }
        this.threshold = parseMap(config.getProperty("THRESHOLD"));
        if (config.getProperty("THRESHOLD") != null && this.threshold.isEmpty() == true) {
            throw new IllegalArgumentException("THRESHOLD PARSE ERROR");
        } 
    }
    
    public void toJson(JSONStringer json_out) {

    }

    public void fromJson(JSONObject json_obj) {

    }

    public String getModelName() {
        return modelName;
    }

    @Override
    public String getType() {
        return "point_outlier";
    }

    @Override
    public void reset() {
        // At this point, reset does nothing.
    }
    
    @Override
    public void tune(DataSequence observedSeries, DataSequence expectedSeries,
            IntervalSequence anomalySequence) throws Exception {
        // TODO: auto detect thresholds.
    }
    // Returns true this point is identified as a potential anomaly.
    public boolean isAnomaly(Float[] errors, Map<String, Float> threshold) {
        // Cycle through all available thresholds and return
        // true if any of them matches.
        for (Map.Entry<String, Float> entry : threshold.entrySet()) {
            // disable mapee and mape.
            if (aes.getErrorToIndex().containsKey(entry.getKey()) == true &&
                Math.abs(errors[aes.getErrorToIndex().get(entry.getKey())]) >= Math.abs(entry.getValue())) {
                return true;
            }
        }
        return false;
    }
   
    @Override
    public IntervalSequence detect(DataSequence observedSeries,
            DataSequence expectedSeries) throws Exception {
        
        // Get an array of thresholds.
        Float[] thresholdErrors = new Float[aes.getErrorToIndex().size()];
        for (Map.Entry<String, Float> entry : this.threshold.entrySet()) {
            thresholdErrors[aes.getErrorToIndex().get(entry.getKey())] = Math.abs(entry.getValue());
        }
        
        IntervalSequence output = new IntervalSequence();
        int n = observedSeries.size();
        Integer cutIndex = null;
        
        // Handle fractional windows which are interpreted as
        // % of the entire TimeSeries size.
        if (window_size < 1.0) {
            cutIndex = Math.round(window_size * ((float) n));
        } else {
        	cutIndex = Math.round(window_size);
        }
        
        if (cutIndex + 1 > n) {
            return output;
        }

        Float[] observed = new Float[] {observedSeries.get(0).value, observedSeries.get(0).value};
        Float[] expected = new Float[] {expectedSeries.get(0).value, expectedSeries.get(0).value};
        
        int maxIndex = 0;
        int minIndex = 0;
        
        int anomaly = 0;
        long unixTime = System.currentTimeMillis() / 1000L;
        
        for (int k = 0; k < n; k++) {
        	
        	if (observed[0] < observedSeries.get(k).value) {
        		observed[0] = observedSeries.get(k).value;
        		maxIndex = k;
        		anomaly = 1;
        	}
        	
        	if (observed[1] > observedSeries.get(k).value) {
        		observed[1] = observedSeries.get(k).value;
        		minIndex = k;
        		anomaly = 1;
        	}
        	
        	if (k < cutIndex) {
        		continue;
        	}
        	
        	expected[0] = Math.max(expected[0], observedSeries.get(k - cutIndex).value);
        	expected[1] = Math.min(expected[1], observedSeries.get(k - cutIndex).value);        	
        	        	
            // Check for anomalies for min/max.
        	int anomalyIndex = 0;
            for (int i = 0; i < 2; i++) {
                Float[] errors = aes.computeErrorMetrics(expected[i], observed[i]);
                boolean actualAnomaly = false;
                if (i == 0 && observed[i] > expected[i]) {
                    actualAnomaly = true;
                    anomalyIndex = maxIndex;
                }
                if (i == 1 && observed[i] < expected[i]) {
                    actualAnomaly = true;
                    anomalyIndex = minIndex;
                }
                
                if (isAnomaly(errors, threshold) == true && actualAnomaly == true && anomaly == 1 && 
                		((((unixTime - observedSeries.get(anomalyIndex).time) / 3600) < maxHrsAgo) ||
        						(maxHrsAgo == 0 && i == (n - 1)))) {
                	anomaly = 0;
                    logger.debug("TS:" + observedSeries.get(anomalyIndex).time + ",E:" + arrayF2S(errors) + ",TH:" + arrayF2S(thresholdErrors) + ",OV:" + observedSeries.get(anomalyIndex).value + ",EV:" + expected[i]);
                    output.add(new Interval(observedSeries.get(anomalyIndex).time,
                    		   anomalyIndex,
                               errors,
                               thresholdErrors,
                               observed[i],
                               expected[i],
                               isAnomaly(errors, threshold)));
                }
            }  
        }
        return output;
    }
}
