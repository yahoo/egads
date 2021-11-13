/*
 * Copyright 2015, Yahoo Inc.
 * Copyrights licensed under the GPL License.
 * See the accompanying LICENSE file for terms.
 */

package com.yahoo.egads.models.tsmm;

import java.util.Properties;

// A naive forecasting model is a special case of the moving average forecasting model where the number of periods used for smoothing is 1.
public class NaiveForecastingModel extends MovingAverageModel {

    public NaiveForecastingModel(Properties config) {
        super(config, 1);
        modelName = "NaiveForecastingModel";
    }
}
