/*
 * Copyright 2015, Yahoo Inc.
 * Copyrights licensed under the GPL License.
 * See the accompanying LICENSE file for terms.
 */

// A template for updateing the model given the data and the model config.

package com.yahoo.egads.control;

import com.yahoo.egads.data.TimeSeries;

public class UpdateModelProcessable implements ProcessableObject {

    private final ModelAdapter ma;
    private final TimeSeries.DataSequence newData;

    UpdateModelProcessable(ModelAdapter ma, TimeSeries.DataSequence newData) {
        this.ma = ma;
        this.newData = newData;
    }

    public void process() throws Exception {
        this.ma.train();
        this.ma.update(this.newData);
    }
}
