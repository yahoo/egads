/*
 * Copyright 2015, Yahoo Inc.
 * Copyrights licensed under the GPL License.
 * See the accompanying LICENSE file for terms.
 */

// interface

package com.yahoo.egads.models.tsmm;

import com.yahoo.egads.data.Model;
import com.yahoo.egads.data.TimeSeries;

public abstract class TimeSeriesModel extends Model {
    // methods ////////////////////////////////////////////////

    public String getModelType() {
    	return "Forecast";
    }
    
    public abstract void train(TimeSeries.DataSequence data) throws Exception;

    public abstract void update(TimeSeries.DataSequence data) throws Exception;

    // predicts the values of the time series specified by the 'time' fields of the sequence and sets the 'value' fields of the sequence
    public abstract void predict(TimeSeries.DataSequence sequence) throws Exception;
}
