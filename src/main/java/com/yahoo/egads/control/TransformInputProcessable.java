/*
 * Copyright 2015, Yahoo Inc.
 * Copyrights licensed under the GPL License.
 * See the accompanying LICENSE file for terms.
 */

package com.yahoo.egads.control;

import java.util.ArrayList;

import com.yahoo.egads.data.TimeSeries;

import java.util.List;
import java.util.Properties;

public class TransformInputProcessable implements ProcessableObject {
    private ModelAdapter ma;
    private Properties config;
    private List<TimeSeries.DataSequence> forecastDatapointList;

    public List<TimeSeries.DataSequence> getForecastDatapointList() {
        return forecastDatapointList;
    }

    TransformInputProcessable(ModelAdapter ma, Properties config) {
        this.ma = ma;
        this.config = config;
    }

    public void process() throws Exception {

        // Reseting the models
        ma.reset();

        // Training the model with the whole metric
        ma.train();

        // Finding the expected values
        forecastDatapointList = ma.forecast(ma.metric.startTime(), ma.metric.lastTime());

        String[] modelNames = ma.getModelNames();

        int i = 0;
        // For each model's prediction in the ModelAdapter
        for (TimeSeries.DataSequence ds : forecastDatapointList) {
            int j = 0;
            for (TimeSeries.Entry e : ds) {
                System.out.println(e.time + "," + ma.metric.meta.name + "," + ma.metric.meta.fileName + ","
                                   + modelNames[i] + "," + e.value + "," + ma.metric.data.get(j).value + ",0");
                j++;
            }
            i++;
        }
    }

    public Object result() throws Exception {
        return getForecastDatapointList();
    }
}
