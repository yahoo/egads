/*
 * Copyright 2015, Yahoo Inc.
 * Copyrights licensed under the GPL License.
 * See the accompanying LICENSE file for terms.
 */

package com.yahoo.egads.control;

import java.util.ArrayList;

import com.yahoo.egads.data.TimeSeries;

public class TransformInputProcessable implements ProcessableObject {
    private ModelAdapter ma;

    TransformInputProcessable(ModelAdapter ma) {
        this.ma = ma;
    }

    public void process() throws Exception {

        // Reseting the models
        ma.reset();

        // Training the model with the whole metric
        ma.train();

        // Finding the expected values
        ArrayList<TimeSeries.DataSequence> list = ma.forecast(ma.metric.startTime(), ma.metric.lastTime());

        String[] modelNames = ma.getModelNames();

        int i = 0;
        // For each model's prediction in the ModelAdapter
        for (TimeSeries.DataSequence ds : list) {
            int j = 0;
            for (TimeSeries.Entry e : ds) {
                System.out.println(e.time + "," + ma.metric.meta.name + "," + ma.metric.meta.fileName + ","
                                + modelNames[i] + "," + e.value + "," + ma.metric.data.get(j).value + ",0");
                j++;
            }
            i++;
        }
    }
}
