/*
 * Copyright 2015, Yahoo Inc.
 * Copyrights licensed under the GPL License.
 * See the accompanying LICENSE file for terms.
 */

// Olympic scoring model considers the average of the last k weeks
// (dropping the b highest and lowest values) as the current prediction.

package com.yahoo.egads.models.tsmm;

import com.yahoo.egads.data.*;
import com.yahoo.egads.data.TimeSeries.Entry;

import java.util.HashMap;
import java.util.Properties;
import java.util.ArrayList;
import java.util.Collections;

import com.yahoo.egads.utilities.FileUtils;

public class StreamingOlympicModel extends TimeSeriesAbstractModel {
    // methods ////////////////////////////////////////////////

	private static final long serialVersionUID = 1L;

	private HashMap<Long, Double> model;
	private int period;
	private double smoothingFactor;
    
    public StreamingOlympicModel(Properties config) {
        super(config);
        smoothingFactor = 0.5;
        period = 86400 * 7;
        model = new HashMap<Long, Double>();
    }

    public void reset() {
        model = new HashMap<Long, Double>();
    }
    private long timeToModelTime (long time) {
    	return time % period;
    }
    
    private void update (TimeSeries.Entry entry) {
    	long modelTime = timeToModelTime(entry.time);
    	if (model.containsKey(timeToModelTime(modelTime))) {
    		model.put(modelTime, model.get(modelTime) * (1 - smoothingFactor) + entry.value * smoothingFactor);
    	} else {
    		model.put(modelTime,  (double)entry.value);
    	}
    }
    
    private double forecast (TimeSeries.Entry entry) {
    	long modelTime = timeToModelTime(entry.time);
    	if (model.containsKey(timeToModelTime(modelTime))) {
    		return model.get(modelTime);
    	} else {
    		return entry.value;
    	}
    }
    
    private void runSeries (TimeSeries.DataSequence data) {
        // Reset various helper summations
        double sumErr = 0.0;
        double sumAbsErr = 0.0;
        double sumAbsPercentErr = 0.0;
        double sumErrSquared = 0.0;
        int processedPoints = 0;
    	for (TimeSeries.Entry entry : data) {
    		double error = entry.value - forecast(entry);
    		update(entry);
    		sumErr += error;
            sumAbsErr += Math.abs(error);
            sumAbsPercentErr += Math.abs(error / entry.value);
            sumErrSquared += error * error;
            processedPoints++;
    	}
        this.bias = sumErr / processedPoints;
        this.mad = sumAbsErr / processedPoints;
        this.mape = sumAbsPercentErr / processedPoints;
        this.mse = sumErrSquared / processedPoints;
        this.sae = sumAbsErr;
        errorsInit = true;
    }
    
    public void train(TimeSeries.DataSequence data) {
    	reset();
        runSeries(data);
        
        logger.debug(getBias() + "\t" + getMAD() + "\t" + getMAPE() + "\t" + getMSE() + "\t" + getSAE() + "\t" + 0 + "\t" + 0);
    }

    public void update(TimeSeries.DataSequence data) {

    }

    public String getModelName() {
        return "OlympicModel";
    }

    private Float sum(ArrayList<Float> list) {
        float sum = 0;
        for (float i : list) {
            sum = sum + i;
        }
        return sum;
    }
    
    private float computeExpected(int i, int pl) {
        ArrayList<Float> vals = new ArrayList<Float>();
        float precision = (float) 0.000001;
        
        int j = 1;

        if ((i - pl * j) < 0) {
            return Float.POSITIVE_INFINITY;
        }
        while (j <= this.numWeeks && (i - pl * j) >= 0) {
            float lastWeeksVal = data.get(i - pl * j).value;
            // If dynamic parameters are turned on,
            // then we check if our error improved from last time,
            // if not, then we stop and use the old result.
            if (dynamicParameters == 1 && vals.size() > 0) {
                float withNewVal = (sum(vals) + lastWeeksVal) / (vals.size() + 1);
                float withoutNewVal = (sum(vals)) / (vals.size());
                if ((Math.abs(withNewVal - data.get(i).value) - Math.abs(withoutNewVal - data.get(i).value)) > precision) {
                    break;
                }
            }
            vals.add(lastWeeksVal);
            j++;
        }

        Collections.sort(vals);
        j = 0;

        if (vals.size() > (2 * this.numToDrop)) {
            while (j < this.numToDrop) {
                vals.remove(vals.size() - 1);
                vals.remove(0);
                j++;
            }
        }
                
        float baseVal = sum(vals) / vals.size();
        return baseVal;
    }
    
    public void predict(TimeSeries.DataSequence sequence) throws Exception {
        int n = data.size();
        for (int i = 0; i < n; i++) {
            sequence.set(i, (new Entry(data.get(i).time, model.get(i))));
            logger.info(data.get(i).time + "," + data.get(i).value + "," + model.get(i));
        }
    }

}
