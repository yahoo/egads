/*
 * Copyright 2015, Yahoo Inc.
 * Copyrights licensed under the GPL License.
 * See the accompanying LICENSE file for terms.
 */

package com.yahoo.egads.models.tsmm;

import java.util.Properties;
import java.util.Hashtable;

import com.yahoo.egads.data.TimeSeries.Entry;
import com.yahoo.egads.data.TimeSeries.DataSequence;
import com.yahoo.egads.utilities.SpectralMethods;
import com.yahoo.egads.utilities.SpectralMethods.FilteringMethod;

/**
 * SpectralSmoother implements the smoothing technique based on the Singular Value Decomposition (SVD) of the input time-series' Hankel matrix.
 * For further details on the methodology please refer to utilities/SpectralMethods.java documentation.
 * 
 * The input parameters:
 *      1. 'WINDOW_SIZE' determines the size of the sliding window for spectral smoothing. Typically should be larger than the
 *          largest seasonality present in the time-series.
 *      2. 'FILTERING_METHOD' determines the filtering method to be used by spectral smoothing.
 *          Possible values: K_GAP, VARIANCE, EXPLICIT, SMOOTHNESS, EIGEN_RATIO, GAP_RATIO
 *          Refer to utilities/SpectralMethods.java documentation for more details.
 *      3. 'FILTERING_PARAM' determines the tuning parameter for the specified filtering method.
 *          Refer to utilities/SpectralMethods.java documentation for more details.  
 *          
 * @author amizadeh
 *
 */

public class SpectralSmoother extends TimeSeriesAbstractModel {

    protected Hashtable<Long, Float> map = new Hashtable<Long, Float>();
    protected int windowSize;
    protected FilteringMethod method;
    protected double methodParameter;

    public SpectralSmoother(Properties config) {
        super(config);
        if (config.getProperty("FILTERING_METHOD") == null) {
            throw new IllegalArgumentException("FILTERING_METHOD is NULL");
        }

        if (config.getProperty("WINDOW_SIZE") == null) {
            throw new IllegalArgumentException("WINDOW_SIZE is NULL");
        }

        this.windowSize = new Integer(config.getProperty("WINDOW_SIZE"));
        this.method = FilteringMethod.valueOf(config.getProperty("FILTERING_METHOD"));

        if (config.getProperty("FILTERING_PARAM") == null) {
            switch (this.method) {
                case VARIANCE:
                    this.methodParameter = 0.99;
                    break;

                case SMOOTHNESS:
                    this.methodParameter = 0.97;
                    break;

                case K_GAP:
                    this.methodParameter = 8;
                    break;

                case EXPLICIT:
                    this.methodParameter = 10;
                    break;

                case EIGEN_RATIO:
                    this.methodParameter = 0.1;
                    break;

                case GAP_RATIO:
                    this.methodParameter = 0.01;
                    break;

                default:
                    throw new IllegalArgumentException("Invalid FILTERING_METHOD value");
            }
        } else {
            this.methodParameter = new Double(config.getProperty("FILTERING_PARAM"));
        }
    }

    @Override
    public void reset() {
        map.clear();
    }

    @Override
    public void train(DataSequence data) throws Exception {
        this.reset();
        DataSequence smoothedData = SpectralMethods.mFilter(data, windowSize, method, methodParameter);

        for (Entry e : smoothedData) {
            map.put(e.logicalIndex, e.value);
        }
    }

    @Override
    public void update(DataSequence data) throws Exception {

        DataSequence smoothedData = SpectralMethods.mFilter(data, windowSize, method, methodParameter);

        for (Entry e : smoothedData) {
            map.put(e.logicalIndex, e.value);
        }
    }

    @Override
    public void predict(DataSequence sequence) throws Exception {

        for (Entry e : sequence) {
            Float val = map.get(e.logicalIndex);
            e.value = (val == null) ? 0 : val;
        }
    }

    @Override
    public String getModelName() {
        return "SpectralSmoother";
    }

}
