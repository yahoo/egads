/*
 * Copyright 2015, Yahoo Inc.
 * Copyrights licensed under the GPL License.
 * See the accompanying LICENSE file for terms.
 */

// interface

package com.yahoo.egads.models.tsmm;

import com.yahoo.egads.data.Model;
import com.yahoo.egads.data.TimeSeries;

public interface TimeSeriesModel extends Model {
    // methods ////////////////////////////////////////////////

    void reset();

    void train(TimeSeries.DataSequence data);

    void update(TimeSeries.DataSequence data);

    // predicts the values of the time series specified by the 'time' fields of the sequence and sets the 'value' fields of the sequence
    void predict(TimeSeries.DataSequence sequence) throws Exception;
}
