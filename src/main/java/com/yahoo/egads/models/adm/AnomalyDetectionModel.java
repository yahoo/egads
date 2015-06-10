/*
 * Copyright 2015, Yahoo Inc.
 * Copyrights licensed under the GPL License.
 * See the accompanying LICENSE file for terms.
 */

// interface

package com.yahoo.egads.models.adm;

import com.yahoo.egads.data.Anomaly;
import com.yahoo.egads.data.TimeSeries;
import com.yahoo.egads.data.Model;

public abstract class AnomalyDetectionModel extends Model {
    // methods ////////////////////////////////////////////////

    // returns the type of anomalies detected by the model
    public abstract String getType();

    public String getModelType() {
    	return "Anomaly";
    }
    
    // tune the anomaly detection parameters based on the training data.
    public abstract void tune(TimeSeries.DataSequence observedSeries,
            TimeSeries.DataSequence expectedSeries,
            Anomaly.IntervalSequence anomalySequence) throws Exception;

    // detect anomalies.
    public abstract Anomaly.IntervalSequence detect(
            TimeSeries.DataSequence observedSeries,
            TimeSeries.DataSequence expectedSeries) throws Exception;
}
