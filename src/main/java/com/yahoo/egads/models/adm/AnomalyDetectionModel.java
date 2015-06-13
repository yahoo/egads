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

public interface AnomalyDetectionModel extends Model {
    // methods ////////////////////////////////////////////////

    // returns the type of anomalies detected by the model
    String getType();
    
    // Returns the name of the anomaly detection model.
    String getModelName();

    // resets the model.
    void reset();

    // tune the anomaly detection parameters based on the training data.
    void tune(TimeSeries.DataSequence observedSeries,
              TimeSeries.DataSequence expectedSeries,
              Anomaly.IntervalSequence anomalySequence);

    // detect anomalies.
    Anomaly.IntervalSequence detect(
            TimeSeries.DataSequence observedSeries,
            TimeSeries.DataSequence expectedSeries);
}
