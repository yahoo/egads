/*
 * Copyright 2015, Yahoo Inc.
 * Copyrights licensed under the GPL License.
 * See the accompanying LICENSE file for terms.
 */

// A template for doing Anomaly Detection.

package com.yahoo.egads.control;

import java.util.ArrayList;

import com.yahoo.egads.data.Anomaly;
import com.yahoo.egads.data.TimeSeries;
import com.yahoo.egads.utilities.Storage;
import com.yahoo.egads.utilities.GUIUtils;

public class DetectAnomalyProcessable implements ProcessableObject {
    private ModelAdapter ma;
    private AnomalyDetector ad;

    DetectAnomalyProcessable(ModelAdapter ma, AnomalyDetector ad) {
        this.ma = ma;
        this.ad = ad;
    }

    public void process() throws Exception {

        // Reseting the models
        ma.reset();

        // Training the model with the whole metric
        ma.train();

        // Finding the expected values
        ArrayList<TimeSeries.DataSequence> list = ma.forecast(
                ma.metric.startTime(), ma.metric.lastTime());

        // For each model's prediction in the ModelAdapter 
        for (TimeSeries.DataSequence ds : list) {
            // Reseting the anomaly detectors
            ad.reset();

            // Unsupervised tuning of the anomaly detectors
            ad.tune(ds, null);

            // Detecting anomalies for each anomaly detection model in anomaly detector
            ArrayList<Anomaly> anomalyList = ad.detect(ad.metric, ds);
            
            // Writing the anomalies to AnomalyDB
            if (Storage.outputSrc != null && Storage.outputSrc.equals("ANOMALY_DB")) {
                for (Anomaly anomaly : anomalyList) {
                    Storage.batchStoreAnomaly(anomaly);
                }
            } else if (Storage.outputSrc != null && Storage.outputSrc.equals("GUI")) { 
                GUIUtils.plotResults(ma.metric.data, ds, anomalyList);
            } else {
                for (Anomaly anomaly : anomalyList) {
                    System.out.print(anomaly.toPerlString());
                }
            }
        }
    }
}
