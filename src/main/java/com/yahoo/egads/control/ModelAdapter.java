/*
 * Copyright 2015, Yahoo Inc.
 * Copyrights licensed under the GPL License.
 * See the accompanying LICENSE file for terms.
 */

/*
 * Description: ModelAdapter applies a set of time-series models (algorithms) on a given metric. ModelAdapter provides
 * concrete mechanisms to apply one or more abstract algorithms on a time-series at the execution time; in other words,
 * application of a certain algorithm (model) on a given time series should be carried out via a ModelAdapter object.
 * The direct application of models on time series is discouraged in EGADS unless for test purposes.
 * 
 * Inputs: 1. The 'metric' time series - Either an explicit TimeSeries object - or the String name of the time series
 * which would require the ModelAdapter to connect to ModelDB to load the appropriate models into the memory (under
 * construction)
 * 
 * 2. The model(s) - Either an explicit TimeSeriesModel object via addModel() - or implicitly loaded from ModelDB when
 * the metric name is provided (under construction)
 * 
 * Features: 1. Resetting all the added models via reset() 2. Training all the added models on the 'metric' via train()
 * 3. Updating all the added models for a new time series sequence via update() 4. Forecasting the value of the time
 * series according to all the added models for a given time period via forecast()
 * 
 * Details: 1. The time units for interfacing with a ModelAdapter object is the standard UNIX timestamp; however,
 * ModelAdapter automatically performs logical indexing conversion for the abstract algorithms so that the actual models
 * can conveniently work with the logical index instead of UNIX timestamps. The conversion is:
 * 
 * logical_index = (UNIX_timestamp - firstTimeStamp) div period UNIX_timestamp = logical_index * period + firstTimeStamp
 */

package com.yahoo.egads.control;

import java.util.ArrayList;

import com.yahoo.egads.data.TimeSeries;
import com.yahoo.egads.models.tsmm.TimeSeriesModel;

public class ModelAdapter { // Encapsulates a metric and the models operating on it

    protected TimeSeries metric = null;
    protected ArrayList<TimeSeriesModel> models = new ArrayList<TimeSeriesModel>();
    protected ArrayList<Boolean> isTrained = new ArrayList<Boolean>();
    protected long firstTimeStamp = 0;
    protected long period;

    // Construction ///////////////////////////////////////////////////////////

    public ModelAdapter(TimeSeries theMetric, long period, long firstTimeStamp) throws Exception {
        if (theMetric == null) {
            throw new Exception("The input metric is null.");
        }

        metric = theMetric;
        this.period = period;
        this.firstTimeStamp = firstTimeStamp;
    }

    public ModelAdapter(TimeSeries theMetric, long period) throws Exception {
        if (theMetric == null) {
            throw new Exception("The input metric is null.");
        }

        metric = theMetric;
        this.period = period;

        if (metric.data.size() > 0) {
            this.firstTimeStamp = metric.time(0);
        }
    }

    public ModelAdapter(String theMetric, long period) throws Exception {
        this.period = period;
        // TODO:
        // 1 - load the models related to theMetric from ModelDB
        // 2 - push the loaded models into 'models'
        // 3 - create a new TimeSeries for theMetric and set 'metric'
        // 4 - set 'firstTimeStamp'

        int modelNum = models.size();
        for (int i = 0; i < modelNum; ++i) {
            isTrained.set(i, true);
        }
    }

    // Configuration Methods ////////////////////////////////////////////////////////////////

    public void setMetric(TimeSeries theMetric, long period) {
        metric = theMetric;
        this.period = period;

        if (metric.data.size() > 0) {
            this.firstTimeStamp = metric.time(0);
        }

        reset();
    }

    public void setMetric(TimeSeries theMetric, long period, long firstTimeStamp) {
        metric = theMetric;
        this.period = period;
        this.firstTimeStamp = firstTimeStamp;
        reset();
    }

    public void setMetric(String theMetric, long period) {
        this.period = period;
        firstTimeStamp = 0;
        models.clear();
        isTrained.clear();

        // TODO:
        // 1 - load the models related to theMetric from ModelDB
        // 2 - push the loaded models into 'models'
        // 3 - create a new TimeSeries for theMetric and set 'metric'
        // 4 - set 'firstTimeStamp'

        int modelNum = models.size();
        for (int i = 0; i < modelNum; ++i) {
            isTrained.set(i, true);
        }
    }

    public void addModel(TimeSeriesModel model) {
        model.reset();
        models.add(model);
        isTrained.add(false);
    }

    public String[] getModelNames() {
        String[] names = new String[models.size()];
        for (int i = 0; i < models.size(); ++i) {
            names[i] = models.get(i).getModelName();
        }

        return names;
    }

    // Algorithmic Methods ////////////////////////////////////////////////////////////////////

    public void reset() {
        int i = 0;
        for (TimeSeriesModel model : models) {
            model.reset();
            isTrained.set(i, false);
            i++;
        }
    }

    public void train() throws Exception {
        int i = 0;

        metric.data.setLogicalIndices(firstTimeStamp, period);

        for (TimeSeriesModel model : models) {
            if (!isTrained.get(i)) {
                model.train(metric.data);
                isTrained.set(i, true);
            }
            i++;
        }
    }

    public void update(TimeSeries.DataSequence newData) throws Exception {
        if (newData == null) {
            throw new Exception("The input data sequence is null.");
        }

        for (Boolean b : isTrained) {
            if (!b) {
                throw new Exception("All the models need to be trained before updating.");
            }
        }

        if (newData.size() > 0) {
            newData.setLogicalIndices(firstTimeStamp, period);

            for (TimeSeriesModel model : models) {
                model.update(newData);
            }
        }
    }

    public ArrayList<TimeSeries.DataSequence> forecast(long from, long to) throws Exception {
        for (Boolean b : isTrained) {
            if (!b) {
                throw new Exception("All the models need to be trained before forecasting.");
            }
        }

        ArrayList<TimeSeries.DataSequence> result = new ArrayList<TimeSeries.DataSequence>();

        for (TimeSeriesModel model : models) {
        	TimeSeries.DataSequence sequence = null;
        	if (period != -1) {
        		sequence = new TimeSeries.DataSequence(from, to, period);
        		sequence.setLogicalIndices(firstTimeStamp, period);
        	} else {
        		sequence = new TimeSeries.DataSequence(metric.data.getTimes(), metric.data.getValues());
        	}

        	model.predict(sequence);
            result.add(sequence);
        }
        return result;
    }
}
