/*
 * Copyright 2015, Yahoo Inc.
 * Copyrights licensed under the GPL License.
 * See the accompanying LICENSE file for terms.
 */

// A simple thresholding model that returns an anomaly if it is above/below a certain threashold.

package com.yahoo.egads.models.adm;

import java.util.Properties;
import java.util.Map;
import java.util.HashMap;
import java.util.ArrayList;
import com.yahoo.egads.data.Anomaly.IntervalSequence;
import com.yahoo.egads.data.Anomaly.Interval;
import com.yahoo.egads.data.AnomalyErrorStorage;
import com.yahoo.egads.data.TimeSeries.DataSequence;
import com.yahoo.egads.utilities.Storage;
import com.yahoo.egads.utilities.AutoSensitivity;

import org.json.JSONObject;
import org.json.JSONStringer;

public class ExtremeLowDensityModel extends AnomalyDetectionAbstractModel {

    // The constructor takes a set of properties
    // needed for the simple model. This includes the sensitivity.
    private Map<String, Float> threshold;
    private int maxHrsAgo;
    // modelName.
    private String modelName = "ExtremeLowDensityModel";
    private final AnomalyErrorStorage aes = new AnomalyErrorStorage();
    
    public ExtremeLowDensityModel(Properties config) {
        super(config);
        
        modelName = modelName + "-" + Storage.forecastModel;
        if (config.getProperty("MAX_ANOMALY_TIME_AGO") == null) {
            throw new IllegalArgumentException("MAX_ANOMALY_TIME_AGO is NULL");
        }
        this.maxHrsAgo = new Integer(config.getProperty("MAX_ANOMALY_TIME_AGO"));
        
        this.threshold = parseMap(config.getProperty("THRESHOLD"));
            
        if (config.getProperty("THRESHOLD") != null && this.threshold.isEmpty()) {
            throw new IllegalArgumentException("THRESHOLD PARSE ERROR");
        } 
    }
    
    // Parses the THRESHOLD config into a map.
    private Map<String, Float> parseMap(String s) {
        if (s == null) {
            return new HashMap<>();
        }
        String[] pairs = s.split(",");
        Map<String, Float> myMap = new HashMap<>();
        for (int i = 0; i < pairs.length; i++) {
            String pair = pairs[i];
            String[] keyValue = pair.split(":");
            myMap.put(keyValue[0], Float.valueOf(keyValue[1]));
        }
        return myMap;
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
    public void tune(DataSequence observedSeries,
                     DataSequence expectedSeries,
                     IntervalSequence anomalySequence) {
        // Compute the time-series of errors.
        HashMap<String, ArrayList<Float>> allErrors = aes.initAnomalyErrors(observedSeries, expectedSeries);
        
        for (int i = 0; i < (aes.getIndexToError().keySet()).size(); i++) {
            // Add a new error metric if the error metric has not been
            // defined by the user.
            if (!threshold.containsKey(aes.getIndexToError().get(i))) {
                Float[] fArray = (allErrors.get(aes.getIndexToError().get(i))).toArray(new Float[(allErrors.get(aes.getIndexToError().get(i))).size()]);
                threshold.put(aes.getIndexToError().get(i), AutoSensitivity.getLowDensitySensitivity(fArray));
            }
        }
    }
    
    // Returns true this point is identified as a potential anomaly.
    public boolean isAnomaly(Float[] errors, Map<String, Float> threshold) {
        // Cycle through all available thresholds and return
        // true if any of them matches.
        for (Map.Entry<String, Float> entry : threshold.entrySet()) {
            // disable mapee and mape.
            if (aes.getErrorToIndex().containsKey(entry.getKey()) &&
                Math.abs(errors[aes.getErrorToIndex().get(entry.getKey())]) >= Math.abs(entry.getValue())) {
                return true;
            }
        }
        return false;
    }
    
    @Override
    public IntervalSequence detect(DataSequence observedSeries,
                                   DataSequence expectedSeries) {
        
        // At detection time, the anomaly thresholds shouldn't all be 0.
        Float threshSum = (float) 0.0;
        for (Map.Entry<String, Float> entry : this.threshold.entrySet()) {
            threshSum += Math.abs(entry.getValue());
        }
        
        // Get an array of thresholds.
        Float[] thresholdErrors = new Float[aes.getErrorToIndex().size()];
        for (Map.Entry<String, Float> entry : this.threshold.entrySet()) {
            thresholdErrors[aes.getErrorToIndex().get(entry.getKey())] = Math.abs(entry.getValue());
        }
        
        IntervalSequence output = new IntervalSequence();
        int n = observedSeries.size();
        long unixTime = System.currentTimeMillis() / 1000L;
       
        for (int i = 0; i < n; i++) {
            Float[] errors = aes.computeErrorMetrics(expectedSeries.get(i).value, observedSeries.get(i).value);
            if (Storage.debug == 3) {
                output.add(new Interval(observedSeries.get(i).time,
                                        errors,
                                        thresholdErrors,
                                        observedSeries.get(i).value,
                                        expectedSeries.get(i).value,
                                        isAnomaly(errors, threshold)));
            } else {
                if (observedSeries.get(i).value != expectedSeries.get(i).value &&
                    threshSum > (float) 0.0 &&
                        isAnomaly(errors, threshold) &&
                    ((((unixTime - observedSeries.get(i).time) / 3600) < maxHrsAgo) ||
                    (maxHrsAgo == 0 && i == (n - 1)))) {
                    output.add(new Interval(observedSeries.get(i).time,
                                            errors,
                                            thresholdErrors,
                                            observedSeries.get(i).value,
                                            expectedSeries.get(i).value));
                }
            }
        }
        return output;
    }
}
