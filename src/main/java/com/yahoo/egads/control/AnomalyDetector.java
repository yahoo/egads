/*
 * Copyright 2015, Yahoo Inc.
 * Copyrights licensed under the GPL License.
 * See the accompanying LICENSE file for terms.
 */

/* 
 * Description: AnomalyDetector applies a set of anomaly detection models (AD algorithms) on a given metric.
 * AnomalyDetector provides concrete mechanisms to apply one or more abstract anomaly detection algorithms on 
 * a time-series at the execution time; in other words, application of a certain anomaly detection algorithm on 
 * a given time series should be carried out via an AnomalyDetector object. 
 * The direct application of models on time series is discouraged in EGADS unless for test purposes.
 * 
 * Inputs:
 *      1. The 'metric' time series
 *          - Either an explicit TimeSeries object
 *          - or the String name of the time series which would require the AnomalyDetector to connect to ModelDB to load
 *            the appropriate anomaly detection models into the memory (under construction)
 *      
 *      2. The model(s)
 *          - Either an explicit AnomalyDetectionModel object via addModel()
 *          - or implicitly loaded from ModelDB when the metric name is provided (under construction)
 *          
 * Features:
 *      1. Resetting all the added anomaly detection models via reset()
 *      2. Tuning all the added anomaly detection models on the 'metric' via tune()
 *      3. Performing anomaly detection on the metric according to all the added anomaly detection models via detect()
 *      
 * Details:
 *      1. The time units for interfacing with an AnomalyDetector object is the standard UNIX timestamp; however, AnomalyDetector 
 *         automatically performs logical indexing conversion for the abstract algorithms so that the actual models can 
 *         conveniently work with the logical index instead of UNIX timestamps. The conversion is:
 *         
 *         logical_index = (UNIX_timestamp - firstTimeStamp) div period
 *         UNIX_timestamp = logical_index * period + firstTimeStamp
 **/

package com.yahoo.egads.control;

import java.util.ArrayList;

import com.yahoo.egads.data.Anomaly;
import com.yahoo.egads.data.Anomaly.IntervalSequence;
import com.yahoo.egads.data.TimeSeries;
import com.yahoo.egads.models.adm.AnomalyDetectionModel;

public class AnomalyDetector {

    protected TimeSeries metric = null;
    protected ArrayList<AnomalyDetectionModel> models = new ArrayList<AnomalyDetectionModel>();
    protected ArrayList<Boolean> isTuned = new ArrayList<Boolean>();
    protected long firstTimeStamp = 0;
    protected long period;

    // Construction ////////////////////////////////////////////////////////////////////////////////

    public AnomalyDetector(TimeSeries theMetric, long period,
            long firstTimeStamp) throws Exception {
        if (theMetric == null) {
            throw new Exception("The input metric is null.");
        }

        metric = theMetric;
        this.period = period;
        this.firstTimeStamp = firstTimeStamp;
    }

    public AnomalyDetector(TimeSeries theMetric, long period) throws Exception {
        if (theMetric == null) {
            throw new Exception("The input metric is null.");
        }

        metric = theMetric;
        this.period = period;

        if (metric.data.size() > 0) {
            this.firstTimeStamp = metric.time(0);
        }
    }

    public AnomalyDetector(String theMetric, long period) throws Exception {
        this.period = period;
        // TODO:
        // 1 - load the models related to theMetric from ModelDB
        // 2 - push the loaded models into 'models'
        // 3 - create a new TimeSeries for theMetric and set 'metric'
        // 4 - set 'firstTimeStamp'

        int modelNum = models.size();
        for (int i = 0; i < modelNum; ++i) {
            isTuned.set(i, true);
        }
    }

    // Configuration Methods ////////////////////////////////////////////////////////////////

    public void setMetric(TimeSeries theMetric, long period) {
        metric = theMetric;
        this.period = period;

        if (metric.data.size() > 0) {
            this.firstTimeStamp = metric.time(0);
        }

        reset();
    }

    public void setMetric(TimeSeries theMetric, long period, long firstTimeStamp) {
        metric = theMetric;
        this.period = period;
        this.firstTimeStamp = firstTimeStamp;
        reset();
    }

    public void setMetric(String theMetric, long period) {
        this.period = period;
        firstTimeStamp = 0;
        models.clear();
        isTuned.clear();

        // TODO:
        // 1 - load the models related to theMetric from ModelDB
        // 2 - push the loaded models into 'models'
        // 3 - create a new TimeSeries for theMetric and set 'metric'
        // 4 - set 'firstTimeStamp'

        int modelNum = models.size();
        for (int i = 0; i < modelNum; ++i) {
            isTuned.set(i, true);
        }
    }

    public void addModel(AnomalyDetectionModel model) {
        model.reset();
        models.add(model);
        isTuned.add(false);
    }

    // Algorithmic Methods ////////////////////////////////////////////////////////////////////

    public void reset() {
        int i = 0;
        for (AnomalyDetectionModel model : models) {
            model.reset();
            isTuned.set(i, false);
            i++;
        }
    }

    public void tune(TimeSeries.DataSequence expectedValues,
            IntervalSequence anomalySequence) throws Exception {
        int i = 0;

        metric.data.setLogicalIndices(firstTimeStamp, period);

        for (AnomalyDetectionModel model : models) {
            if (!isTuned.get(i)) {
                model.tune(metric.data, expectedValues, anomalySequence);
                isTuned.set(i, true);
            }
            i++;
        }
    }

    public ArrayList<Anomaly> detect(TimeSeries observedSeries,
                                     TimeSeries.DataSequence expectedSeries) throws Exception {
        for (Boolean b : isTuned) {
            if (!b) {
                throw new Exception(
                        "All the models need to be tuned before detection.");
            }
        }

        ArrayList<Anomaly> result = new ArrayList<Anomaly>();
        observedSeries.data.setLogicalIndices(firstTimeStamp, period);
        expectedSeries.setLogicalIndices(firstTimeStamp, period);

        for (AnomalyDetectionModel model : models) {
            Anomaly anomaly = new Anomaly(observedSeries.meta.name,
                    observedSeries.meta);
            anomaly.modelName = model.getModelName();
            anomaly.type = model.getType();
            anomaly.intervals = model.detect(observedSeries.data,
                    expectedSeries);
            anomaly.intervals.setLogicalIndices(firstTimeStamp, period);
            anomaly.intervals.setTimeStamps(firstTimeStamp, period);
            result.add(anomaly);
        }

        return result;
    }
}
