/*
 * Copyright 2016, Yahoo Inc.
 * Copyrights licensed under the GPL License.
 * See the accompanying LICENSE file for terms.
 */
package com.yahoo.egads.data;

import static org.testng.AssertJUnit.assertEquals;
import static org.testng.AssertJUnit.assertTrue;

import java.util.List;

import org.testng.annotations.Test;

import com.google.common.collect.Lists;

public class TestWeightedValue {

    @Test
    public void aggregate() throws Exception {
        final List<WeightedValue> values = Lists.newArrayList();
        values.add(new WeightedValue(1.5D, 5));
        values.add(new WeightedValue(42.5D, 1));
        values.add(new WeightedValue(-1.5D, 0));
        values.add(new WeightedValue(3.5D, 1));
        
        assertEquals(-1.5D, WeightedValue.aggregate(values, "MIN"), 0.0001);
        assertEquals(42.5D, WeightedValue.aggregate(values, "MAX"), 0.0001);
        assertEquals(46.0D, WeightedValue.aggregate(values, "SUM"), 0.0001);
        assertEquals(4D, WeightedValue.aggregate(values, "COUNT"), 0.0001);
        assertEquals(3.5D, WeightedValue.aggregate(values, "MEDIAN"), 0.0001);
        assertEquals(11.5D, WeightedValue.aggregate(values, "AVG"), 0.0001);
        assertEquals(7.6428D, WeightedValue.aggregate(values, "WAVG"), 0.0001);
        
        // add some NaNs and it shouldn't change
        values.clear();
        values.add(new WeightedValue(1.5D, 5));
        values.add(new WeightedValue(42.5D, 1));
        values.add(new WeightedValue(Double.NaN, 1));
        values.add(new WeightedValue(-1.5D, 0));
        values.add(new WeightedValue(Double.NaN, 4));
        values.add(new WeightedValue(3.5D, 1));
        values.add(new WeightedValue(Double.NaN, 5));
        
        assertEquals(-1.5D, WeightedValue.aggregate(values, "MIN"), 0.0001);
        assertEquals(42.5D, WeightedValue.aggregate(values, "MAX"), 0.0001);
        assertEquals(46.0D, WeightedValue.aggregate(values, "SUM"), 0.0001);
        assertEquals(4D, WeightedValue.aggregate(values, "COUNT"), 0.0001);
        assertEquals(3.5D, WeightedValue.aggregate(values, "MEDIAN"), 0.0001);
        assertEquals(11.5D, WeightedValue.aggregate(values, "AVG"), 0.0001);
        assertEquals(7.6428D, WeightedValue.aggregate(values, "WAVG"), 0.0001);
        
        // all nans should return nan (except count!)
        values.clear();
        values.add(new WeightedValue(Double.NaN, 1));
        values.add(new WeightedValue(Double.NaN, 2));
        values.add(new WeightedValue(Double.NaN, 5));
        
        assertTrue(Double.isNaN(WeightedValue.aggregate(values, "MIN")));
        assertTrue(Double.isNaN(WeightedValue.aggregate(values, "MAX")));
        assertTrue(Double.isNaN(WeightedValue.aggregate(values, "SUM")));
        assertEquals(0, WeightedValue.aggregate(values, "COUNT"), 0.0001);
        assertTrue(Double.isNaN(WeightedValue.aggregate(values, "MEDIAN")));
        assertTrue(Double.isNaN(WeightedValue.aggregate(values, "AVG")));
        assertTrue(Double.isNaN(WeightedValue.aggregate(values, "WAVG")));
        
        // empty always returns Nan.
        values.clear();
        
        assertTrue(Double.isNaN(WeightedValue.aggregate(values, "MIN")));
        assertTrue(Double.isNaN(WeightedValue.aggregate(values, "MAX")));
        assertTrue(Double.isNaN(WeightedValue.aggregate(values, "SUM")));
        assertEquals(0, WeightedValue.aggregate(values, "COUNT"), 0.0001);
        assertTrue(Double.isNaN(WeightedValue.aggregate(values, "MEDIAN")));
        assertTrue(Double.isNaN(WeightedValue.aggregate(values, "AVG")));
        assertTrue(Double.isNaN(WeightedValue.aggregate(values, "WAVG")));
    }
    
    @Test (expectedExceptions = IllegalArgumentException.class)
    public void aggregateNullList() throws Exception {
        WeightedValue.aggregate(null, "MIN");
    }
    
    @Test (expectedExceptions = IllegalArgumentException.class)
    public void aggregateNullAgg() throws Exception {
        final List<WeightedValue> values = Lists.newArrayList();
        values.add(new WeightedValue(1.5D, 5));
        WeightedValue.aggregate(values, null);
    }
    
    @Test (expectedExceptions = IllegalArgumentException.class)
    public void aggregateEmptyAgg() throws Exception {
        final List<WeightedValue> values = Lists.newArrayList();
        values.add(new WeightedValue(1.5D, 5));
        WeightedValue.aggregate(values, "");
    }
    
    @Test (expectedExceptions = IllegalArgumentException.class)
    public void aggregateUnknownAgg() throws Exception {
        final List<WeightedValue> values = Lists.newArrayList();
        values.add(new WeightedValue(1.5D, 5));
        WeightedValue.aggregate(values, "NOTIMPLEMENTED");
    }
}
