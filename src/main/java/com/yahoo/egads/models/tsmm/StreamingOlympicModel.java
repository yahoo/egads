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
	protected int period;
	protected double smoothingFactor;
    
    public StreamingOlympicModel(Properties config) {
        super(config);
        smoothingFactor = 0.4;
        period = 86400 * 7;
        model = new HashMap<Long, Double>();
    }
    public StreamingOlympicModel(Properties config, double smoothingFactor, int period) {
        super(config);
        this.smoothingFactor = smoothingFactor;
        this.period = period;
        this.model = new HashMap<Long, Double>();
    }

    public void reset() {
        model = new HashMap<Long, Double>();
    }
    
    private long timeToModelTime (long time) {
    	if (period == 86400 * 7) {
    		return weeklyOffset(time);
    	}
    	if (period == 86400) {
    		return dailyOffset(time);
    	}
    	return time % period;
    }
    
    public void update (TimeSeries.Entry entry) {
    	long modelTime = timeToModelTime(entry.time);
    	if (model.containsKey(modelTime)) {
    		model.put(modelTime, model.get(modelTime) * (1 - smoothingFactor) + entry.value * smoothingFactor);
    	} else {
    		model.put(modelTime,  (double)entry.value);
    	}
    	modified = true;
    }
    
    public double predict (TimeSeries.Entry entry) {
    	long modelTime = timeToModelTime(entry.time);
    	if (model.containsKey(modelTime)) {
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
    		double error = entry.value - predict(entry);
    		update(entry);
    		sumErr += error;
            sumAbsErr += Math.abs(error);
            sumAbsPercentErr += 100 * Math.abs(error / entry.value);
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
    	StreamingOlympicModel winner = null;
    	double sf = 0.0;
    	for (sf = 0.0; sf <= 1; sf += 0.1) {
    		StreamingOlympicModel m = new StreamingOlympicModel(this.config, sf, this.period);
    		m.runSeries(data);
        	logger.debug ("Testing Smoothing Factor " + String.format("%.2f", m.smoothingFactor) + " -> "+ m.errorSummaryString());
    		if (betterThan(m, winner)) {
    			winner = m;
    		}
    	}
    	double min = winner.smoothingFactor - 0.09;
    	if (min < 0) min = 0;
    	double max = winner.smoothingFactor + 0.09;
    	if (max >= 1) max = .99;
    	for (sf = min; sf <= max; sf += 0.01) {
    		StreamingOlympicModel m = new StreamingOlympicModel(this.config, sf, this.period);
    		m.runSeries(data);
    		m.runSeries(data);
        	logger.debug ("Testing Smoothing Factor " + String.format("%.2f", m.smoothingFactor) + " -> "+ m.errorSummaryString());
    		if (betterThan(m, winner)) {
    			winner = m;
    		}
    	}
    	this.smoothingFactor = winner.smoothingFactor;
    	reset();
    	runSeries(data);
    	logger.debug ("Winner: Smoothing Factor = " + String.format("%.2f", this.smoothingFactor));
    }

    public double getSmoothingFactor() {
		return smoothingFactor;
	}

	public void setSmoothingFactor(double smoothingFactor) {
		this.smoothingFactor = smoothingFactor;
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
