/*
 * Copyright 2015, Yahoo Inc.
 * Copyrights licensed under the GPL License.
 * See the accompanying LICENSE file for terms.
 */

package com.yahoo.egads.models.tsmm;

import java.util.Arrays;
import java.util.Properties;

// A moving average forecast model is based on an artificially constructed time series in which the value for a
// given time period is replaced by the mean of that value and the values for some number of preceding and succeeding time periods.
public class MovingAverageModel extends WeightedMovingAverageModel {

    private static final int DEFAULT_PERIODS = 2;

    public MovingAverageModel(Properties config) {
        super(config, periodsToWeights(DEFAULT_PERIODS));
        modelName = "MovingAverageModel";
    }

    public MovingAverageModel(Properties config, int periods) {
        super(config, periodsToWeights(periods));
        modelName = "MovingAverageModel";
    }

    private static double[] periodsToWeights(int periods) {
        double[] weights = new double[periods];
        Arrays.fill(weights, 1.0D / periods);

        return weights;
    }
}
