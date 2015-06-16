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
        return (float)0.0;
    }
    
    public void predict(TimeSeries.DataSequence sequence) throws Exception {
    	return;
    }

}
