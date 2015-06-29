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
    private Float windowSize;
    // modelName.
    public static String modelName = "NaiveModel";
    public AnomalyErrorStorage aes = new AnomalyErrorStorage();

    public NaiveModel(Properties config) {
        super(config);

        if (config.getProperty("MAX_ANOMALY_TIME_AGO") == null) {
            throw new IllegalArgumentException("MAX_ANOMALY_TIME_AGO is NULL");
        }
        this.windowSize = new Float(config.getProperty("MAX_ANOMALY_TIME_AGO"));
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

    private Float max(DataSequence d, int from, int to) {
        Float max = null;
        
        for (int i = from; i < to; i++) {
            if (max == null || d.get(i).value > max) {
                max = d.get(i).value;
            }
        }
        return max;
    }

    private Float min(DataSequence d, int from, int to) {
        Float min = null;
        
        for (int i = from; i < to; i++) {
            if (min == null || d.get(i).value < min) {
                min = d.get(i).value;
            }
        }
        return min;
    }

    private int findIndex(DataSequence data, Float value, int from, int to) {
        float epsilon = (float) 0.00000001;
        int index = -1;
        for (int i = from; i < to; i++) {
            if (Math.abs(data.get(i).value - value) < epsilon) {
                index = i;
            }
        }
        return index;
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
        if (windowSize < 1.0) {
          int tmpCut = Math.round(windowSize * ((float) n));
          cutIndex = Math.max(1, n - tmpCut);
        } else {
          cutIndex = Math.round((float) n - windowSize);
        }

        Float[] observed = new Float[] {max(observedSeries, cutIndex, n), min(observedSeries, cutIndex, n)};
        Float[] expected = new Float[] {max(observedSeries, 0, Math.max(0, cutIndex - 1)), min(observedSeries, 0, Math.max(0, cutIndex - 1))};

        // Check for anomalies for min/max.
        for (int i = 0; i < 2; i++) {
            Float[] errors = aes.computeErrorMetrics(expected[i], observed[i]);
            boolean actualAnomaly = false;
            if (i == 0 && observed[i] > expected[i]) {
              actualAnomaly = true;
            }
            if (i == 1 && observed[i] < expected[i]) {
              actualAnomaly = true;
            }
           
            if (isAnomaly(errors, threshold) == true && actualAnomaly == true) {
                int j = findIndex(observedSeries, observed[i], cutIndex, n);
                logger.debug("TS:" + observedSeries.get(j).time + ",E:" + arrayF2S(errors) + ",TH:" + arrayF2S(thresholdErrors) + ",OV:" + observedSeries.get(j).value + ",EV:" + expected[i]);
                output.add(new Interval(observedSeries.get(j).time,
                           errors,
                           thresholdErrors,
                           observed[i],
                           expected[i],
                           isAnomaly(errors, threshold)));
            }
        }  
        return output;
    }
}
