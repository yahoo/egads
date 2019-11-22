/*
 * Copyright 2016, Yahoo Inc.
 * Copyrights licensed under the GPL License.
 * See the accompanying LICENSE file for terms.
 */
package com.yahoo.egads.models.tsmm;

import java.time.Instant;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.time.temporal.ChronoUnit;
import java.util.List;
import java.util.Properties;

import org.apache.commons.lang.NotImplementedException;
import org.apache.commons.math3.util.Pair;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.yahoo.egads.data.TimeSeries.Entry;
import com.yahoo.egads.data.WeightedValue;
import com.google.common.annotations.VisibleForTesting;
import com.google.common.collect.Lists;
import com.yahoo.egads.data.TimeSeries.DataSequence;

/**
 * TODO - rename this!
 * 
 * This model generates forecasts using an average of period-over-period 
 * baseline data. For example, if the window distance is 1 week and the user
 * asks for 4 past windows, a prediction at time T will be generated from
 * the average (or WINDOW_AGGREGATION) of T - 1 week, T - 2 weeks, T - 3 weeks 
 * and T - 4 weeks.
 * <p>
 * <b>Properties</b>
 * <ul>
 * <li>INTERVAL - REQUIRED: The timestamp interval of the resulting model,
 * in combination with the PERIOD_SIZE_UNITS. E.g. for a 5 minute window period,
 * set this value to "5".</li>
 * 
 * <li>INTERVAL_UNITS - REQUIRED: A Java ChronoUnit enumerator representing
 * the units of the PERIOD_SIZE. E.g. "minutes" or "seconds".</li>
 * 
 * <li>WINDOW_SIZE - REQUIRED: How many values should be generated for the 
 * model, e.g. if you want to generate a model for a day, set this to 1 and set
 * WINDOW_SIZE_UNITS to "days"</li>
 * 
 * <li>WINDOW_SIZE_UNITS - REQUIRED: A Java ChronoUnit enumerator representing
 * the units of the WINDOW_SIZE. E.g. "minutes" or "seconds".</li>
 * 
 * <li>WINDOW_DISTANCE - The timestamp interval between windows in the baseline. 
 * E.g. for a week-over-week model, set this value to "1".</li>
 * 
 * <li>WINDOW_DISTANCE_UNITS - REQUIRED: A Java ChronoUnit enumerator representing
 * the units of the WINDOW_DISTANCE. E.g. "minutes" or "seconds".</li>
 * 
 * <li>MODEL_START - REQUIRED: The start time of the model in Unix epoch seconds.
 * This will be the first timestamp of the model and determines the offsets for
 * parsing the baseline. E.g. 1474756200.</li>
 * 
 * <li>HISTORICAL_WINDOWS - The number of windows in the past to generate the 
 * model from. E.g. for 4 weeks of week-over-week, set this to 4. Defaults to 
 * 1 window.</li>
 * 
 * <li>WINDOW_AGGREGATOR - An aggregation function to use when merging data 
 * points across historical windows. Can be one of SUM, MAX, MIN, COUNT, AVG,
 * WAVG or MEDIAN. Defaults to AVG.</li>
 * 
 * <li>TIMEZONE - An optional time zone to align data to. Must be parseable 
 * by Java's ZoneId, e.g. "Australia/Lord_Howe". Defaults to "UTC".</li>
 * 
 * <li>ENABLE_WEIGHTING - Whether or not to enable higher weighting for more
 * recent values in the baseline. Defaults to false.</li>
 * 
 * <li>FUTURE_WINDOWS - How many periods to forecast in the future for generating
 * the model. Defaults to 1.</li>
 * 
 * <li>NUM_TO_DROP_HIGHEST - How many of the highest values to drop amongst the
 * windows at each timestamp. Defaults to 0.</li>
 * 
 * <li>NUM_TO_DROP_LOWEST - How many of the lowest values to drop amongst the
 * windows at each timestamp. Defaults to 0.</li>
 * </ul>
 * <p>
 * Optionally weighting can be applied wherein more recent values 
 * (e.g. T - 1 week) have more weight than older values (e.g. T - 4 weeks). 
 * <p>Also, for each data point, the <i>n</i> highest and <i>n</i> lowest
 * values can be dropped. Using the example above, if we have T - 1w = 4, 
 * T - 2w = 2, T - 3w = 5, T - 4w = 1 and we drop 1 high and 1 low, then the
 * resulting would be the average of T - 1w and T - 2w for a value of 2.
 * <p>
 * If a time zone is given, the baseline and forecast timestamps will be 
 * adjusted for DST if necessary.
 *   
 */
public class OlympicModel2 extends TimeSeriesAbstractModel {
    private static final Logger LOG = LoggerFactory.getLogger(
            OlympicModel2.class);
    
    /** Auto generated UID. */
    private static final long serialVersionUID = 4322074416636265537L;

    /** The width of the prediction window, e.g. 1 day. */
    protected final long windowSize;
    protected final ChronoUnit windowUnits;
    
    /** The expected frequency/width of data in the model, e.g. 1 value 
     * every minute */
    protected final long interval;
    protected final ChronoUnit intervalUnits;
    
    /** The distance between windows such as week over week windows. */
    protected final long windowDistanceInterval;
    protected final ChronoUnit windowDistanceIntervalUnits;
    
    /** A fuction to use for aggregating values across windows. Defaults to avg. */
    protected final String windowAggregator;
    
    /** How many windows in the future to forecast. */
    protected final int futureWindows;
    
    /** The number of windows in the past to process. */
    protected final int pastWindows;
    
    /** The starting timestamp of the model in Unix epoch seconds. This is the
     * basis for all calculations. */
    protected final long modelStartEpoch;
    
    /** Time zone for aligning windows. */
    protected final ZoneId zone;
    
    /** Whether or not weighting should be enabled. */
    protected final boolean weighting;

    /** The number of highest or lowest values to drop at each model data point 
     * aggregation. */
    protected final int drop_highest;
    protected final int drop_lowest;

    /** Contains the model data points generated after train() has been called. */
    protected final List<Pair<Long, Double>> model;
    
    /** An array of timestamps for each period of training data. */
    protected final ZonedDateTime[] windowTimes;
    
    /** An array of indices into the DataSequence object when training. */
    protected final int[] indices;

    /**
     * Default Ctor
     * @param config A non-null and non-empty properties map.
     * @throws IllegalArgumentException if a required property is missing.
     * @throws NumberFormatException if a numeric property could not be parsed.
     */
    public OlympicModel2(final Properties config) {
        super(config);
        
        // TODO - some additional validation around distance being greater
        // or equal to the window size.
        String temp = config.getProperty("WINDOW_SIZE");
        if (temp == null || temp.isEmpty()) {
            throw new IllegalArgumentException("WINDOW_SIZE is required, "
                    + "e.g. 1 or 5");
        }
        windowSize = Long.parseLong(temp);
        temp = config.getProperty("WINDOW_SIZE_UNITS");
        if (temp == null || temp.isEmpty()) {
            throw new IllegalArgumentException("WINDOW_SIZE_UNITS is required, "
                    + "e.g. MINUTES OR HOURS");
        }
        windowUnits = ChronoUnit.valueOf(temp.toUpperCase()); 
        
        temp = config.getProperty("INTERVAL");
        if (temp == null || temp.isEmpty()) {
            throw new IllegalArgumentException("INTERVAL is required, "
                    + "e.g. 1 or 5");
        }
        interval = Long.parseLong(temp);
        temp = config.getProperty("INTERVAL_UNITS");
        if (temp == null || temp.isEmpty()) {
            throw new IllegalArgumentException("INTERVAL_UNITS is required, "
                    + "e.g. MINUTES OR HOURS");
        }
        intervalUnits = ChronoUnit.valueOf(temp.toUpperCase());
        
        temp = config.getProperty("WINDOW_DISTANCE");
        if (temp == null || temp.isEmpty()) {
            throw new IllegalArgumentException("WINDOW_DISTANCE is required, "
                    + "e.g. 1 or 5");
        }
        windowDistanceInterval = Long.parseLong(temp);
        
        temp = config.getProperty("WINDOW_DISTANCE_UNITS");
        if (temp == null || temp.isEmpty()) {
            throw new IllegalArgumentException("WINDOW_DISTANCE_UNITS is "
                    + "required, e.g. MINUTES OR HOURS");
        }
        windowDistanceIntervalUnits = ChronoUnit.valueOf(temp.toUpperCase());
        
        temp = config.getProperty("WINDOW_AGGREGATOR");
        if (temp == null || temp.isEmpty()) {
            windowAggregator = "AVG";
        } else {
            temp = temp.toUpperCase();
            if (!(temp.equals("AVG") || temp.equals("MIN") 
                || temp.equals("MAX") || temp.equals("SUM") 
                || temp.equals("COUNT") || temp.equals("WAVG")
                || temp.equals("MEDIAN"))) {
                throw new IllegalArgumentException("The window aggregator was"
                        + " not implemented: " + temp);
            }
            windowAggregator = temp;
        }
        
        temp = config.getProperty("MODEL_START");
        if (temp == null || temp.isEmpty()) {
            throw new IllegalArgumentException("MODEL_START is required, "
                    + "e.g. 1474756200");
        }
        modelStartEpoch = Long.parseLong(temp);
        
        pastWindows = Integer.parseInt(config.getProperty(
                "HISTORICAL_WINDOWS", "1"));
        weighting = Boolean
                .parseBoolean(config.getProperty("ENABLE_WEIGHTING", "false"));
        futureWindows = Integer
                .parseInt(config.getProperty("FUTURE_WINDOWS", "1"));
        drop_highest = Integer
                .parseInt(config.getProperty("NUM_TO_DROP_HIGHEST", "0"));
        drop_lowest = Integer
                .parseInt(config.getProperty("NUM_TO_DROP_LOWEST", "0"));

        zone = ZoneId.of(config.getProperty("TIMEZONE", "UTC"));
        
        windowTimes = new ZonedDateTime[pastWindows];
        indices = new int[pastWindows];
        model = Lists.newArrayList();
    }
    
    @Override
    public void train(final DataSequence data) throws Exception {
        initializeIndices(data, modelStartEpoch);
        
        final long size = data.size();
        ZonedDateTime model_ts = Instant.ofEpochSecond(modelStartEpoch)
                .atZone(zone);
        ZonedDateTime end_ts = model_ts.plus(windowSize, windowUnits);
        int prediction_index = 0;
        final List<WeightedValue> accumulator = Lists.newArrayList();
        
        // start the loop and break once we've filled the model.
        while (true) {
            accumulator.clear();
            for (int i = 0; i < windowTimes.length; i++) {
                if (indices[i] < 0 || indices[i] >= size) {
                    continue;
                }
                
                // advance
                windowTimes[i] = windowTimes[i].plus(interval,
                        intervalUnits);
                long interval_end = windowTimes[i].toEpochSecond();
                final List<Double> doubles = Lists.newArrayList();
                long first_ts = -1;
                while (indices[i] < size
                        && data.get(indices[i]).time < interval_end) {
                    if (Double.isFinite(data.get(indices[i]).value)) {
                        doubles.add((double) data.get(indices[i]).value);
                    }
                    if (first_ts < 0) {
                        first_ts = data.get(indices[i]).time;
                    }
                    indices[i]++;
                }

                if (!doubles.isEmpty()) {
                    // TODO - for DST if we jumped back then we may have a
                    // period
                    // with more than we expect. In that case, depending on the
                    // aggregator, we may need to use only part of the data.
                    // TODO - potentially other aggregations.
                    double sum = 0;
                    for (final Double v : doubles) {
                        sum += v;
                    }
                    accumulator.add(
                            new WeightedValue((sum / doubles.size()), i + 1));
                }
            }

            if (drop_lowest > 0 || drop_highest > 0) {
                if (drop_highest > drop_lowest) {
                    WeightedValue.drop(accumulator, drop_highest, true);
                    WeightedValue.drop(accumulator, drop_lowest, false);
                } else {
                    WeightedValue.drop(accumulator, drop_lowest, false);
                    WeightedValue.drop(accumulator, drop_highest, true);
                }
            }
            
            model.add(new Pair<Long, Double>(model_ts.toEpochSecond(),
                    WeightedValue.aggregate(accumulator, windowAggregator)));

            model_ts = model_ts.plus(interval, intervalUnits);
            if (model_ts.toEpochSecond() > end_ts.toEpochSecond()) {
                prediction_index++;
                if (prediction_index >= futureWindows) {
                    break;
                }
                model_ts = Instant.ofEpochSecond(modelStartEpoch).atZone(zone);
                model_ts = model_ts.plus(
                        (windowDistanceInterval * prediction_index), 
                        windowDistanceIntervalUnits);
                end_ts = model_ts.plus(windowSize, windowUnits);
                for (int i = 0; i < windowTimes.length; i++) {
                    windowTimes[i] = null;
                    indices[i] = 0;
                }
                initializeIndices(data, model_ts.toEpochSecond());
            }
        }
    }

    @Override
    public void update(final DataSequence data) throws Exception {
        throw new NotImplementedException();
    }

    @Override
    public void predict(final DataSequence sequence) throws Exception {
        if (model == null || model.isEmpty()) {
            throw new IllegalStateException("Model was empty. 'train()' may "
                    + "not have been called.");
        }
        // TODO - proper... setting... uggg!!
        int x = 0;
        for (int i = 0; i < sequence.size(); i++) {
            while (x < model.size() && 
                  sequence.get(i).time > model.get(x).getKey()) {
                ++x;
            }
            if (x >= model.size()) {
                break;
            }
            if (sequence.get(i).time == model.get(x).getKey()) {
                final Pair<Long, Double> dp = model.get(x++);
                sequence.set(i,
                        new Entry(dp.getKey(), (float) (double) dp.getValue()));
            }
        }
    }

    @Override
    public void reset() {
        model.clear();
        for (int i = 0; i < windowTimes.length; i++) {
            windowTimes[i] = null;
            indices[i] = 0;
        }
    }

    /**
     * Sets the index into the DataSequence object for each window along with
     * calculating the window times. The index or timestamp for each window will
     * be one of the following: - -1 to show that there isn't any data for that
     * window - An index where the timestamp is the exact value of the seek time
     * - An index where the timestamp is a value greater than the seek time
     * 
     * @param data
     *            A non-null and non-empty data sequence object to read from.
     * @throws IllegalArgumentException
     *             if the data object was null or empty.
     */
    @VisibleForTesting
    void initializeIndices(final DataSequence data, final long start) {
        if (data == null || data.size() < 1) {
            throw new IllegalArgumentException(
                    "DataSequence cannot be null or empty.");
        }
        final ZonedDateTime base = Instant.ofEpochSecond(start)
                .atZone(zone);
        for (int i = 0; i < pastWindows; i++) {
            final ZonedDateTime seek = base.minus(
                    (windowDistanceInterval * (pastWindows - i)),
                    windowDistanceIntervalUnits);
            final long seek_time = seek.toEpochSecond();
            
            // cut down on iterations by dividing the data idx
            int idx = data.size() / (pastWindows - i);
            if (idx >= data.size()) {
                idx = data.size() - 1;
            }

            if (data.get(idx).time == seek_time) {
                // woot, found it!
            } else if (data.get(idx).time < seek_time) {
                while (idx < data.size() && data.get(idx).time < seek_time) {
                    idx++;
                }
            } else {
                while (idx > 0 && data.get(idx - 1).time >= seek_time) {
                    idx--;
                }
            }

            // reset to avoid OOB exceptions.
            if (idx >= data.size()) {
                idx = -1;
            }
            windowTimes[i] = seek;
            indices[i] = idx;
            if (LOG.isDebugEnabled()) {
                LOG.debug("Initializing index: " + i + " to " + idx + " at " 
                        + seek);
            }
        }
    }

}
