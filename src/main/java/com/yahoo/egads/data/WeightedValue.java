/*
 * Copyright 2016, Yahoo Inc.
 * Copyrights licensed under the GPL License.
 * See the accompanying LICENSE file for terms.
 */
package com.yahoo.egads.data;

import java.util.Collections;
import java.util.List;

import com.google.common.collect.Lists;

/**
 * Class for holding a weighted value that can then be aggregated when stored
 * in a list using various functions.
 */
public class WeightedValue implements Comparable<WeightedValue> {
    private final double value;
    private final int weight;

    /**
     * Default ctor.
     * @param value The value of the data point.
     * @param weight A weight for the data point.
     */
    public WeightedValue(final double value, final int weight) {
        this.value = value;
        this.weight = weight;
    }

    @Override
    public int compareTo(final WeightedValue other) {
        return Double.compare(value, other.value);
    }

    /**
     * Drops as many of the highest or lowest values as possible, leaving
     * at least one value in the list.
     * @param accumulator A non-null accumulator list.
     * @param count A count of 1 or more.
     * @param highest Drop higher values == true or drop lower values == false.
     */
    public static void drop(final List<WeightedValue> accumulator, 
            final int count, final boolean highest) {
        for (int x = 0; x < count; x++) {
            if (accumulator.size() <= 1) {
                break;
            }
            if (highest) {
                accumulator.remove(Collections.max(accumulator));
            } else {
                accumulator.remove(Collections.min(accumulator));
            }
        }
    }
    
    /** @return The data point value. */
    public double getValue() {
        return value;
    }
    
    /** @return The weight for the data point. */
    public int getWeight() {
        return weight;
    }

    /**
     * Aggregates the values in the list using the given agg function.
     * For all functions, NaNs are skipped so if an entire list is NaN'd or the
     * list is empty, the results will be a NaN.
     * @param values A non-null list of values to aggregate. 
     * @param agg A non-null or empty aggregator function to use.
     * @return An aggregated value or NaN.
     * @throws IllegalArgumentException if the values was null, agg was null
     * or empty or we had an unimplemented agg function.
     */
    public static double aggregate(final List<WeightedValue> values, 
            final String agg) {
        if (agg == null || agg.isEmpty()) {
            throw new IllegalArgumentException("Aggregator cannot be null or empty");
        }
        if (values == null) {
            throw new IllegalArgumentException("Values cannot be null");
        }
        if (values.isEmpty()) {
            if (agg.equals("COUNT")) {
                return 0;
            }
            return Double.NaN;
        }
        
        // temps.
        int validCount = 0;
        double accumulator = 0;
        
        if (agg.equals("MAX")) {
            accumulator = Double.MIN_VALUE;
            for (final WeightedValue v : values) {
                if (Double.isFinite(v.value)) {
                    if (v.value > accumulator) {
                        accumulator = v.value;
                    }
                    ++validCount;
                }
            }
            if (validCount < 1) {
                return Double.NaN;
            }
            return accumulator;
        } else if (agg.equals("MIN")) {
            accumulator = Double.MAX_VALUE;
            for (final WeightedValue v : values) {
                if (Double.isFinite(v.value)) {
                    if (v.value < accumulator) {
                        accumulator = v.value;
                    }
                    ++validCount;
                }
            }
            if (validCount < 1) {
                return Double.NaN;
            }
            return accumulator;
        } else if (agg.equals("COUNT")) {
            int ctr = 0;
            for (final WeightedValue v : values) {
                if (Double.isFinite(v.value)) {
                    ++ctr;
                }
            }
            return ctr;
        } else if (agg.equals("MEDIAN")) {
            final List<Double> sorted = Lists.newArrayList();
            for (final WeightedValue v : values) {
                if (Double.isFinite(v.value)) {
                    sorted.add(v.value);
                    ++validCount;
                }
            }
            if (validCount < 1) {
                return Double.NaN;
            }
            Collections.sort(sorted);
            return sorted.get(sorted.size() / 2);
        } else if (agg.equals("SUM")) {
            for (final WeightedValue v : values) {
                if (Double.isFinite(v.value)) {
                    accumulator += v.value;
                    ++validCount;
                }
            }
            if (validCount < 1) {
                return Double.NaN;
            }
            return accumulator;
        } else if (agg.equals("AVG")) {
            for (final WeightedValue v : values) {
                if (Double.isFinite(v.value)) {
                    accumulator += v.value;
                    ++validCount;
                }
            }
            if (validCount < 1) {
                return Double.NaN;
            }
            return accumulator / validCount;
        } else if (agg.equals("WAVG")) {
            for (final WeightedValue v : values) {
                if (Double.isFinite(v.value)) {
                    accumulator += v.weight * v.value;
                    validCount += v.weight;
                }
            }
            if (validCount < 1) {
                return Double.NaN;
            }
            return accumulator / validCount;
        }
     
        throw new IllegalArgumentException("Unimplemented aggregation "
                + "function: " + agg);
    }
}
