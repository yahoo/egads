/*
 * Copyright 2015, Yahoo Inc.
 * Copyrights licensed under the GPL License.
 * See the accompanying LICENSE file for terms.
 */

package com.yahoo.egads.data;

import com.yahoo.egads.data.TimeSeries.DataSequence;
import java.util.Map;
import java.util.HashMap;
import java.util.ArrayList;

public class AnomalyErrorStorage {

    // Denominator used in the MASE error metric.
    protected float maseDenom;
    // Maps error names to error indicies.
    protected Map<String, Integer> errorToIndex;
    // Maps error index to error names.
    protected Map<Integer, String> indexToError;
    boolean isInit = false;

    // Getter methods.
    public Map<String, Integer> getErrorToIndex() {
        return errorToIndex;
    }
    public Map<Integer, String> getIndexToError() {
        return indexToError;
    }
    
    // Force the user to define this constructor that acts as a
    // factory method.
    public AnomalyErrorStorage() {
        // Init error indicies that are filled in computeErrorMetrics method.
        errorToIndex = new HashMap<String, Integer>();
        errorToIndex.put("mapee", 0);
        errorToIndex.put("mae", 1);
        errorToIndex.put("smape", 2);
        errorToIndex.put("mape", 3);
        errorToIndex.put("mase", 4);
        indexToError = new HashMap<Integer, String>();
        indexToError.put(0, "mapee");
        indexToError.put(1, "mae");
        indexToError.put(2, "smape");
        indexToError.put(3, "mape");
        indexToError.put(4, "mase");
    }
    
    // Initializes all anomaly errors.
    public HashMap<String, ArrayList<Float>> initAnomalyErrors(DataSequence observedSeries, DataSequence expectedSeries) {        
        int n = observedSeries.size();
        
        // init MASE.
        for (int i = 1; i < n; i++) {
            maseDenom += Math.abs(observedSeries.get(i).value - observedSeries.get(i - 1).value);
        }
        maseDenom = maseDenom / (n - 1);
        HashMap<String, ArrayList<Float>> allErrors = new HashMap<String, ArrayList<Float>>();
        
        for (int i = 0; i < n; i++) {
            Float[] errors = computeErrorMetrics(expectedSeries.get(i).value, observedSeries.get(i).value);
            for (int j = 0; j < errors.length; j++) {
                if (!allErrors.containsKey(indexToError.get(j))) {
                    allErrors.put(indexToError.get(j), new ArrayList<Float>());
                }
                ArrayList<Float> tmp = allErrors.get(indexToError.get(j));
                tmp.add(errors[j]);
                allErrors.put(indexToError.get(j), tmp);
            }            
        }
        isInit = true;
        return allErrors;
    }
    
    // Computes the standard error metrics including MAE, sMAPE, MAPE, MASE.
    public Float[] computeErrorMetrics(float expected, float actual) {
        float div = expected;
        if (expected == (float) 0.0) {
          div = (float) 0.0000000001;
        }
        
        // Mean Absolute Error.
        float mae = Math.abs(actual - expected);
        // Symmetric Mean Absolute Error.
        float smape = (200 * Math.abs(actual - expected)) / ((Math.abs(actual) + Math.abs(expected)) == 0 ? (float) 1.0 : (float) (Math.abs(actual) + Math.abs(expected)));
        // Mean Absolute Percentage Error.
        float mape = Math.abs(actual) == 0 ? (float) 0.0 : ((100 * Math.abs(actual - expected)) / (float) Math.abs(actual));
        // Mean Absolute Scaled Error.
        float mase = Math.abs(maseDenom) == 0.0 ? (float) 0.0 : Math.abs(actual - expected) / Math.abs(maseDenom);
        // Mean Absolute Percentage Error (scaled by the expected value).
        float mapee = (expected == actual) ? (float) 0.0 : Math.abs((100 * ((actual / div) - 1)));
        
        // Store all errors.
        Float[] errors = new Float[5];
        errors[0] = mapee;
        errors[1] = mae;
        errors[2] = smape;
        errors[3] = mape;
        errors[4] = mase;
        
        return errors;
    }
}
