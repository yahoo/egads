/*
 * Copyright 2016, Yahoo Inc.
 * Copyrights licensed under the GPL License.
 * See the accompanying LICENSE file for terms.
 */
package com.yahoo.egads.models.tsmm;

import static org.testng.AssertJUnit.assertEquals;
import static org.testng.AssertJUnit.assertFalse;
import static org.testng.AssertJUnit.assertTrue;
import static org.testng.AssertJUnit.fail;

import java.time.ZoneId;
import java.time.temporal.ChronoUnit;
import java.util.Properties;

import org.apache.commons.lang.NotImplementedException;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

import com.yahoo.egads.data.TimeSeries;

public class TestOlympicModel2 {
    private Properties config;
    private long start = 1477872000;
    
    @BeforeMethod
    public void before() throws Exception {
        setConfig();
        config.put("HISTORICAL_WINDOWS", "4");
    }
    
    @Test
    public void ctor() throws Exception {
        OlympicModel2 model = new OlympicModel2(config);
        
        assertEquals(5, model.interval);
        assertEquals(ChronoUnit.MINUTES, model.intervalUnits);
        assertEquals(1, model.windowDistanceInterval);
        assertEquals(ChronoUnit.WEEKS, model.windowDistanceIntervalUnits);
        assertEquals(4, model.pastWindows);
        assertEquals(start, model.modelStartEpoch);
        assertEquals(ZoneId.of("UTC"), model.zone);
        assertFalse(model.weighting);
        assertEquals(1, model.futureWindows);
        assertEquals(0, model.drop_highest);
        assertEquals(0, model.drop_lowest);
        assertEquals(4, model.windowTimes.length);
        assertEquals(4, model.indices.length);
        assertTrue(model.model.isEmpty());
        
        // overrides
        config.clear();
        config.put("INTERVAL", "5");
        config.put("INTERVAL_UNITS", "MINUTES");
        config.put("WINDOW_SIZE", "1");
        config.put("WINDOW_SIZE_UNITS", "HOURS");
        config.put("WINDOW_DISTANCE", "1");
        config.put("WINDOW_DISTANCE_UNITS", "WEEKS");
        config.put("HISTORICAL_WINDOWS", "4");
        config.put("MODEL_START", Long.toString(start));
        config.put("TIMEZONE", "Australia/Lord_Howe");
        config.put("ENABLE_WEIGHTING", "true");
        config.put("FUTURE_WINDOWS", "2");
        config.put("NUM_TO_DROP_HIGHEST", "4");
        config.put("NUM_TO_DROP_LOWEST", "8");
        model = new OlympicModel2(config);
        
        assertEquals(5, model.interval);
        assertEquals(ChronoUnit.MINUTES, model.intervalUnits);
        assertEquals(1, model.windowDistanceInterval);
        assertEquals(ChronoUnit.WEEKS, model.windowDistanceIntervalUnits);
        assertEquals(4, model.pastWindows);
        assertEquals(start, model.modelStartEpoch);
        assertEquals(ZoneId.of("Australia/Lord_Howe"), model.zone);
        assertTrue(model.weighting);
        assertEquals(2, model.futureWindows);
        assertEquals(4, model.drop_highest);
        assertEquals(8, model.drop_lowest);
        assertEquals(4, model.windowTimes.length);
        assertEquals(4, model.indices.length);
        assertTrue(model.model.isEmpty());
        
        // null config, underlying ctor throws an NPE, should fix it.
        try {
            model = new OlympicModel2(null);
            fail("Expected NullPointerException");
        } catch (NullPointerException e) { }
        
        // Empty config
        config.clear();
        try {
            model = new OlympicModel2(config);
            fail("Expected IllegalArgumentException");
        } catch (IllegalArgumentException e) { }
        
        // lots of missing required values tests.
        setConfig();
        config.remove("INTERVAL");
        
        try {
            model = new OlympicModel2(config);
            fail("Expected IllegalArgumentException");
        } catch (IllegalArgumentException e) { }
        
        setConfig();
        config.remove("INTERVAL_UNITS");
        
        try {
            model = new OlympicModel2(config);
            fail("Expected IllegalArgumentException");
        } catch (IllegalArgumentException e) { }
        
        setConfig();
        config.remove("WINDOW_SIZE");
        
        try {
            model = new OlympicModel2(config);
            fail("Expected IllegalArgumentException");
        } catch (IllegalArgumentException e) { }
        
        setConfig();
        config.remove("WINDOW_SIZE_UNITS");
        
        try {
            model = new OlympicModel2(config);
            fail("Expected IllegalArgumentException");
        } catch (IllegalArgumentException e) { }
        
        setConfig();
        config.remove("WINDOW_DISTANCE");
        
        try {
            model = new OlympicModel2(config);
            fail("Expected IllegalArgumentException");
        } catch (IllegalArgumentException e) { }
        
        setConfig();
        config.remove("WINDOW_DISTANCE_UNITS");
        
        try {
            model = new OlympicModel2(config);
            fail("Expected IllegalArgumentException");
        } catch (IllegalArgumentException e) { }
        
        setConfig();
        config.remove("MODEL_START");
        
        try {
            model = new OlympicModel2(config);
            fail("Expected IllegalArgumentException");
        } catch (IllegalArgumentException e) { }
        
        setConfig();
        
        model = new OlympicModel2(config);
        
        assertEquals(1, model.pastWindows);
        assertEquals(ZoneId.of("UTC"), model.zone);
        
        // invalid params tests
        setConfig();
        config.setProperty("WINDOW_SIZE", "not a number");
        try {
            model = new OlympicModel2(config);
            fail("Expected NumberFormatException");
        } catch (NumberFormatException e) { }
        
        setConfig();
        config.setProperty("WINDOW_SIZE_UNITS", "not a unit");
        try {
            model = new OlympicModel2(config);
            fail("Expected IllegalArgumentException");
        } catch (IllegalArgumentException e) { }
        
        setConfig();
        config.setProperty("WINDOW_DISTANCE", "not a number");
        try {
            model = new OlympicModel2(config);
            fail("Expected NumberFormatException");
        } catch (NumberFormatException e) { }
        
        setConfig();
        config.setProperty("WINDOW_DISTANCE_UNITS", "not a unit");
        try {
            model = new OlympicModel2(config);
            fail("Expected IllegalArgumentException");
        } catch (IllegalArgumentException e) { }
        
        setConfig();
        config.setProperty("MODEL_START", "not a number");
        try {
            model = new OlympicModel2(config);
            fail("Expected NumberFormatException");
        } catch (NumberFormatException e) { }
    }
    
    @Test (expectedExceptions = IllegalArgumentException.class)
    public void initializeIndicesNullData() throws Exception {
        final OlympicModel2 model = new OlympicModel2(config);
        model.initializeIndices(null, start);
    }
    
    @Test (expectedExceptions = IllegalArgumentException.class)
    public void initializeIndicesEmptyData() throws Exception {
        final OlympicModel2 model = new OlympicModel2(config);
        model.initializeIndices(new TimeSeries().data, start);
    }
    
    @Test 
    public void initializeIndices() throws Exception {
        final OlympicModel2 model = new OlympicModel2(config);
        TimeSeries ts = new TimeSeries();
        ts.append(1475452800, 10);
        ts.append(1475453100, 25);
        
        ts.append(1476057600, 20);
        ts.append(1476057900, 45);
        
        ts.append(1476662400, 30);
        ts.append(1476662700, 55);
        
        ts.append(1477267200, 40);
        ts.append(1477267500, 65);
        
        model.initializeIndices(ts.data, start);
        
        assertEquals(4, model.indices.length);
        assertEquals(0, model.indices[0]);
        assertEquals(2, model.indices[1]);
        assertEquals(4, model.indices[2]);
        assertEquals(6, model.indices[3]);
        
        assertEquals(4, model.windowTimes.length);
        assertEquals(1475452800, model.windowTimes[0].toEpochSecond());
        assertEquals(1476057600, model.windowTimes[1].toEpochSecond());
        assertEquals(1476662400, model.windowTimes[2].toEpochSecond());
        assertEquals(1477267200, model.windowTimes[3].toEpochSecond());
        
        // missing first point
        ts = new TimeSeries();
        //ts.append(1475452800, 10);
        ts.append(1475453100, 25);
        
        ts.append(1476057600, 20);
        ts.append(1476057900, 45);
        
        ts.append(1476662400, 30);
        ts.append(1476662700, 55);
        
        ts.append(1477267200, 40);
        ts.append(1477267500, 65);
        
        model.initializeIndices(ts.data, start);
        
        assertEquals(4, model.indices.length);
        assertEquals(0, model.indices[0]);
        assertEquals(1, model.indices[1]);
        assertEquals(3, model.indices[2]);
        assertEquals(5, model.indices[3]);
        
        assertEquals(4, model.windowTimes.length);
        assertEquals(1475452800, model.windowTimes[0].toEpochSecond());
        assertEquals(1476057600, model.windowTimes[1].toEpochSecond());
        assertEquals(1476662400, model.windowTimes[2].toEpochSecond());
        assertEquals(1477267200, model.windowTimes[3].toEpochSecond());
        
        // missing first window
        ts = new TimeSeries();
        //ts.append(1475452800, 10);
        //ts.append(1475453100, 25);
        
        ts.append(1476057600, 20);
        ts.append(1476057900, 45);
        
        ts.append(1476662400, 30);
        ts.append(1476662700, 55);
        
        ts.append(1477267200, 40);
        ts.append(1477267500, 65);
        
        model.initializeIndices(ts.data, start);
        
        assertEquals(4, model.indices.length);
        assertEquals(0, model.indices[0]);
        assertEquals(0, model.indices[1]);
        assertEquals(2, model.indices[2]);
        assertEquals(4, model.indices[3]);
        
        // missing last dp
        ts = new TimeSeries();
        ts.append(1475452800, 10);
        ts.append(1475453100, 25);
        
        ts.append(1476057600, 20);
        ts.append(1476057900, 45);
        
        ts.append(1476662400, 30);
        ts.append(1476662700, 55);
        
        ts.append(1477267200, 40);
        //ts.append(1477267500, 65);
        
        model.initializeIndices(ts.data, start);
        
        assertEquals(4, model.indices.length);
        assertEquals(0, model.indices[0]);
        assertEquals(2, model.indices[1]);
        assertEquals(4, model.indices[2]);
        assertEquals(6, model.indices[3]);
        
        // missing last window
        ts = new TimeSeries();
        ts.append(1475452800, 10);
        ts.append(1475453100, 25);
        
        ts.append(1476057600, 20);
        ts.append(1476057900, 45);
        
        ts.append(1476662400, 30);
        ts.append(1476662700, 55);
        
        //ts.append(1477267200, 40);
        //ts.append(1477267500, 65);
        
        model.initializeIndices(ts.data, start);
        
        assertEquals(4, model.indices.length);
        assertEquals(0, model.indices[0]);
        assertEquals(2, model.indices[1]);
        assertEquals(4, model.indices[2]);
        assertEquals(-1, model.indices[3]);
        
        // missing middle DP
        ts = new TimeSeries();
        ts.append(1475452800, 10);
        ts.append(1475453100, 25);
        
        ts.append(1476057600, 20);
        ts.append(1476057900, 45);
        
        //ts.append(1476662400, 30);
        ts.append(1476662700, 55);
        
        ts.append(1477267200, 40);
        ts.append(1477267500, 65);
        
        model.initializeIndices(ts.data, start);
        
        assertEquals(4, model.indices.length);
        assertEquals(0, model.indices[0]);
        assertEquals(2, model.indices[1]);
        assertEquals(4, model.indices[2]);
        assertEquals(5, model.indices[3]);
        
        // missing middle window
        ts = new TimeSeries();
        ts.append(1475452800, 10);
        ts.append(1475453100, 25);
        
        ts.append(1476057600, 20);
        ts.append(1476057900, 45);
        
        //ts.append(1476662400, 30);
        //ts.append(1476662700, 55);
        
        ts.append(1477267200, 40);
        ts.append(1477267500, 65);
        
        model.initializeIndices(ts.data, start);
        
        assertEquals(4, model.indices.length);
        assertEquals(0, model.indices[0]);
        assertEquals(2, model.indices[1]);
        assertEquals(4, model.indices[2]);
        assertEquals(4, model.indices[3]);
        
        // only one window
        ts = new TimeSeries();
        //ts.append(1475452800, 10);
        //ts.append(1475453100, 25);
        
        ts.append(1476057600, 20);
        ts.append(1476057900, 45);
        
        //ts.append(1476662400, 30);
        //ts.append(1476662700, 55);
        
        //ts.append(1477267200, 40);
        //ts.append(1477267500, 65);
        
        model.initializeIndices(ts.data, start);
        
        assertEquals(4, model.indices.length);
        assertEquals(0, model.indices[0]);
        assertEquals(0, model.indices[1]);
        assertEquals(-1, model.indices[2]);
        assertEquals(-1, model.indices[3]);
        
        // Off by a second
        ts = new TimeSeries();
        //ts.append(1475452800, 10);
        //ts.append(1475453100, 25);
        
        ts.append(1476057599, 20);
        ts.append(1476057900, 45);
        
        //ts.append(1476662400, 30);
        //ts.append(1476662700, 55);
        
        //ts.append(1477267200, 40);
        //ts.append(1477267500, 65);
        
        model.initializeIndices(ts.data, start);
        
        assertEquals(4, model.indices.length);
        assertEquals(0, model.indices[0]);
        assertEquals(1, model.indices[1]);
        assertEquals(-1, model.indices[2]);
        assertEquals(-1, model.indices[3]);
        
        // data earlier than the first window
        ts = new TimeSeries();
        ts.append(1474243200, 10);
        ts.append(1474243500, 25);
        
        ts.append(1474848000, 20);
        ts.append(1474848300, 45);
        
        model.initializeIndices(ts.data, start);
        
        assertEquals(4, model.indices.length);
        assertEquals(-1, model.indices[0]);
        assertEquals(-1, model.indices[1]);
        assertEquals(-1, model.indices[2]);
        assertEquals(-1, model.indices[3]);
        
        // data later than the first window
        ts = new TimeSeries();
        ts.append(1477872000, 10);
        ts.append(1477872300, 25);
        
        ts.append(1478476800, 20);
        ts.append(1478477000, 45);
        
        model.initializeIndices(ts.data, start);
        
        assertEquals(4, model.indices.length);
        assertEquals(0, model.indices[0]);
        assertEquals(0, model.indices[1]);
        assertEquals(0, model.indices[2]);
        assertEquals(0, model.indices[3]);
        
        // NaNs
        ts = new TimeSeries();
        ts.append(1475452800, Float.NaN);
        ts.append(1475453100, 25);
        
        ts.append(1476057600, Float.NaN);
        ts.append(1476057900, 45);
        
        ts.append(1476662400, Float.NaN);
        ts.append(1476662700, 55);
        
        ts.append(1477267200, Float.NaN);
        ts.append(1477267500, 65);
        
        model.initializeIndices(ts.data, start);
        
        assertEquals(4, model.indices.length);
        assertEquals(0, model.indices[0]);
        assertEquals(2, model.indices[1]);
        assertEquals(4, model.indices[2]);
        assertEquals(6, model.indices[3]);
        
        assertEquals(4, model.windowTimes.length);
        assertEquals(1475452800, model.windowTimes[0].toEpochSecond());
        assertEquals(1476057600, model.windowTimes[1].toEpochSecond());
        assertEquals(1476662400, model.windowTimes[2].toEpochSecond());
        assertEquals(1477267200, model.windowTimes[3].toEpochSecond());
    }

    @Test 
    public void initializeIndicesVariousIntervals() throws Exception {
        config.put("WINDOW_DISTANCE_UNITS", "DAYS");
        OlympicModel2 model = new OlympicModel2(config);
        TimeSeries ts = new TimeSeries();
        ts.append(1477526400, 10);
        ts.append(1477526700, 25);
        
        ts.append(1477612800, 20);
        ts.append(1477613100, 45);
        
        ts.append(1477699200, 30);
        ts.append(1477699500, 55);
        
        ts.append(1477785600, 40);
        ts.append(1477785900, 65);
        
        model.initializeIndices(ts.data, start);
        
        assertEquals(4, model.indices.length);
        assertEquals(0, model.indices[0]);
        assertEquals(2, model.indices[1]);
        assertEquals(4, model.indices[2]);
        assertEquals(6, model.indices[3]);
        
        // hours
        config.put("WINDOW_DISTANCE_UNITS", "HOURS");
        ts = new TimeSeries();
        ts.append(1477857600, 10);
        ts.append(1477857900, 25);
        
        ts.append(1477861200, 20);
        ts.append(1477861500, 45);
        
        ts.append(1477864800, 30);
        ts.append(1477865100, 55);
        
        ts.append(1477868400, 40);
        ts.append(1477868700, 65);
        
        model = new OlympicModel2(config);
        model.initializeIndices(ts.data, start);
        
        assertEquals(4, model.indices.length);
        assertEquals(0, model.indices[0]);
        assertEquals(2, model.indices[1]);
        assertEquals(4, model.indices[2]);
        assertEquals(6, model.indices[3]);
        
        // multiple hours 
        config.put("WINDOW_DISTANCE", "6");
        config.put("WINDOW_DISTANCE_UNITS", "HOURS");
        ts = new TimeSeries();
        ts.append(1477785600, 10);
        ts.append(1477785900, 25);
        
        ts.append(1477807200, 20);
        ts.append(1477807500, 45);
        
        ts.append(1477828800, 30);
        ts.append(1477829100, 55);
        
        ts.append(1477850400, 40);
        ts.append(1477850700, 65);
        
        model = new OlympicModel2(config);
        model.initializeIndices(ts.data, start);
        
        assertEquals(4, model.indices.length);
        assertEquals(0, model.indices[0]);
        assertEquals(2, model.indices[1]);
        assertEquals(4, model.indices[2]);
        assertEquals(6, model.indices[3]);
        
        // multiple minutes 
        config.put("WINDOW_DISTANCE", "60");
        config.put("WINDOW_DISTANCE_UNITS", "MINUTES");
        ts = new TimeSeries();
        ts.append(1477857600, 10);
        ts.append(1477857900, 25);
        
        ts.append(1477861200, 20);
        ts.append(1477861500, 45);
        
        ts.append(1477864800, 30);
        ts.append(1477865100, 55);
        
        ts.append(1477868400, 40);
        ts.append(1477868700, 65);
        
        model = new OlympicModel2(config);
        model.initializeIndices(ts.data, start);
        
        assertEquals(4, model.indices.length);
        assertEquals(0, model.indices[0]);
        assertEquals(2, model.indices[1]);
        assertEquals(4, model.indices[2]);
        assertEquals(6, model.indices[3]);
        
        config.put("WINDOW_SIZE", "1");
        config.put("WINDOW_SIZE_UNITS", "MINUTES");
        config.put("WINDOW_DISTANCE", "1");
        config.put("WINDOW_DISTANCE_UNITS", "DAYS");
        model = new OlympicModel2(config);
        ts = new TimeSeries();
        ts.append(1477526400, 10);
        ts.append(1477526460, 25);
        
        ts.append(1477612800, 20);
        ts.append(1477612860, 45);
        
        ts.append(1477699200, 30);
        ts.append(1477699260, 55);
        
        ts.append(1477785600, 40);
        ts.append(1477785660, 65);
        
        model.initializeIndices(ts.data, start);
        
        assertEquals(4, model.indices.length);
        assertEquals(0, model.indices[0]);
        assertEquals(2, model.indices[1]);
        assertEquals(4, model.indices[2]);
        assertEquals(6, model.indices[3]);
    }
    
    @Test 
    public void initializeIndicesTimeZoneHandling() throws Exception {
        config.put("TIMEZONE", "Australia/Lord_Howe");
        config.put("MODEL_START", "1475359200");
        
        OlympicModel2 model = new OlympicModel2(config);
        TimeSeries ts = new TimeSeries();
        ts.append(1472941800, 10);
        ts.append(1472942100, 25);
        
        ts.append(1473546600, 20);
        ts.append(1473546900, 45);
        
        ts.append(1474151400, 30);
        ts.append(1474151700, 55);
        
        ts.append(1474756200, 40);
        ts.append(1474756500, 65);
        
        model.initializeIndices(ts.data, 1475359200);
        
        assertEquals(4, model.indices.length);
        assertEquals(0, model.indices[0]);
        assertEquals(2, model.indices[1]);
        assertEquals(4, model.indices[2]);
        assertEquals(6, model.indices[3]);
        
        assertEquals(4, model.windowTimes.length);
        assertEquals(1472941800, model.windowTimes[0].toEpochSecond());
        assertEquals(1473546600, model.windowTimes[1].toEpochSecond());
        assertEquals(1474151400, model.windowTimes[2].toEpochSecond());
        assertEquals(1474756200, model.windowTimes[3].toEpochSecond());
    }

    @Test (expectedExceptions = IllegalArgumentException.class)
    public void trainNullData() throws Exception {
        final OlympicModel2 model = new OlympicModel2(config);
        model.train(null);
    }
    
    @Test (expectedExceptions = IllegalArgumentException.class)
    public void trainIndicesEmptyData() throws Exception {
        final OlympicModel2 model = new OlympicModel2(config);
        model.train(new TimeSeries().data);
    }
    
    @Test
    public void train() throws Exception {
        OlympicModel2 model = new OlympicModel2(config);
        TimeSeries ts = new TimeSeries();
        ts.append(1475452800, 10);
        ts.append(1475453100, 25);
        
        ts.append(1476057600, 20);
        ts.append(1476057900, 45);
        
        ts.append(1476662400, 30);
        ts.append(1476662700, 55);
        
        ts.append(1477267200, 40);
        ts.append(1477267500, 65);
        
        model.train(ts.data);
        
        assertEquals(13, model.model.size());
        assertEquals(1477872000, (long) model.model.get(0).getKey());
        assertEquals(25.0, model.model.get(0).getValue(), 0.00001);
        assertEquals(1477872300, (long) model.model.get(1).getKey());
        assertEquals(47.5, model.model.get(1).getValue(), 0.00001);
        assertEquals(1477872600, (long) model.model.get(2).getKey());
        assertTrue(Double.isNaN(model.model.get(2).getValue()));
        
        // missing first point
        ts = new TimeSeries();
        //ts.append(1475452800, 10);
        ts.append(1475453100, 25);
        
        ts.append(1476057600, 20);
        ts.append(1476057900, 45);
        
        ts.append(1476662400, 30);
        ts.append(1476662700, 55);
        
        ts.append(1477267200, 40);
        ts.append(1477267500, 65);
        
        model.reset();
        model.train(ts.data);
        
        assertEquals(13, model.model.size());
        assertEquals(1477872000, (long) model.model.get(0).getKey());
        assertEquals(30.0, model.model.get(0).getValue(), 0.00001);
        assertEquals(1477872300, (long) model.model.get(1).getKey());
        assertEquals(47.5, model.model.get(1).getValue(), 0.00001);
        assertEquals(1477872600, (long) model.model.get(2).getKey());
        assertTrue(Double.isNaN(model.model.get(2).getValue()));
        
        // missing first window
        ts = new TimeSeries();
        //ts.append(1475452800, 10);
        //ts.append(1475453100, 25);
        
        ts.append(1476057600, 20);
        ts.append(1476057900, 45);
        
        ts.append(1476662400, 30);
        ts.append(1476662700, 55);
        
        ts.append(1477267200, 40);
        ts.append(1477267500, 65);
        
        model.reset();
        model.train(ts.data);
        
        assertEquals(13, model.model.size());
        assertEquals(1477872000, (long) model.model.get(0).getKey());
        assertEquals(30.0, model.model.get(0).getValue(), 0.00001);
        assertEquals(1477872300, (long) model.model.get(1).getKey());
        assertEquals(55.0, model.model.get(1).getValue(), 0.00001);
        assertEquals(1477872600, (long) model.model.get(2).getKey());
        assertTrue(Double.isNaN(model.model.get(2).getValue()));
        
        // missing last dp
        ts = new TimeSeries();
        ts.append(1475452800, 10);
        ts.append(1475453100, 25);
        
        ts.append(1476057600, 20);
        ts.append(1476057900, 45);
        
        ts.append(1476662400, 30);
        ts.append(1476662700, 55);
        
        ts.append(1477267200, 40);
        //ts.append(1477267500, 65);
        
        model.reset();
        model.train(ts.data);
        
        assertEquals(13, model.model.size());
        assertEquals(1477872000, (long) model.model.get(0).getKey());
        assertEquals(25.0, model.model.get(0).getValue(), 0.00001);
        assertEquals(1477872300, (long) model.model.get(1).getKey());
        assertEquals(41.66666, model.model.get(1).getValue(), 0.00001);
        assertEquals(1477872600, (long) model.model.get(2).getKey());
        assertTrue(Double.isNaN(model.model.get(2).getValue()));
        
        // missing last window
        ts = new TimeSeries();
        ts.append(1475452800, 10);
        ts.append(1475453100, 25);
        
        ts.append(1476057600, 20);
        ts.append(1476057900, 45);
        
        ts.append(1476662400, 30);
        ts.append(1476662700, 55);
        
        //ts.append(1477267200, 40);
        //ts.append(1477267500, 65);
        
        model.reset();
        model.train(ts.data);
        
        assertEquals(13, model.model.size());
        assertEquals(1477872000, (long) model.model.get(0).getKey());
        assertEquals(20.0, model.model.get(0).getValue(), 0.00001);
        assertEquals(1477872300, (long) model.model.get(1).getKey());
        assertEquals(41.66666, model.model.get(1).getValue(), 0.00001);
        assertEquals(1477872600, (long) model.model.get(2).getKey());
        assertTrue(Double.isNaN(model.model.get(2).getValue()));
        
        // missing middle DP
        ts = new TimeSeries();
        ts.append(1475452800, 10);
        ts.append(1475453100, 25);
        
        ts.append(1476057600, 20);
        ts.append(1476057900, 45);
        
        //ts.append(1476662400, 30);
        ts.append(1476662700, 55);
        
        ts.append(1477267200, 40);
        ts.append(1477267500, 65);
        
        model.reset();
        model.train(ts.data);
        
        assertEquals(13, model.model.size());
        assertEquals(1477872000, (long) model.model.get(0).getKey());
        assertEquals(23.33333, model.model.get(0).getValue(), 0.00001);
        assertEquals(1477872300, (long) model.model.get(1).getKey());
        assertEquals(47.5, model.model.get(1).getValue(), 0.00001);
        assertEquals(1477872600, (long) model.model.get(2).getKey());
        assertTrue(Double.isNaN(model.model.get(2).getValue()));
        
        // missing middle window
        ts = new TimeSeries();
        ts.append(1475452800, 10);
        ts.append(1475453100, 25);
        
        ts.append(1476057600, 20);
        ts.append(1476057900, 45);
        
        //ts.append(1476662400, 30);
        //ts.append(1476662700, 55);
        
        ts.append(1477267200, 40);
        ts.append(1477267500, 65);
        
        model.reset();
        model.train(ts.data);
        
        assertEquals(13, model.model.size());
        assertEquals(1477872000, (long) model.model.get(0).getKey());
        assertEquals(23.33333, model.model.get(0).getValue(), 0.00001);
        assertEquals(1477872300, (long) model.model.get(1).getKey());
        assertEquals(45.0, model.model.get(1).getValue(), 0.00001);
        assertEquals(1477872600, (long) model.model.get(2).getKey());
        assertTrue(Double.isNaN(model.model.get(2).getValue()));
        
        // only one window
        ts = new TimeSeries();
        //ts.append(1475452800, 10);
        //ts.append(1475453100, 25);
        
        ts.append(1476057600, 20);
        ts.append(1476057900, 45);
        
        //ts.append(1476662400, 30);
        //ts.append(1476662700, 55);
        
        //ts.append(1477267200, 40);
        //ts.append(1477267500, 65);
        
        model.reset();
        model.train(ts.data);
        
        assertEquals(13, model.model.size());
        assertEquals(1477872000, (long) model.model.get(0).getKey());
        assertEquals(20.0, model.model.get(0).getValue(), 0.00001);
        assertEquals(1477872300, (long) model.model.get(1).getKey());
        assertEquals(45.0, model.model.get(1).getValue(), 0.00001);
        assertEquals(1477872600, (long) model.model.get(2).getKey());
        assertTrue(Double.isNaN(model.model.get(2).getValue()));
        
        // Off by a second
        ts = new TimeSeries();
        //ts.append(1475452800, 10);
        //ts.append(1475453100, 25);
        
        ts.append(1476057599, 20);
        ts.append(1476057900, 45);
        
        //ts.append(1476662400, 30);
        //ts.append(1476662700, 55);
        
        //ts.append(1477267200, 40);
        //ts.append(1477267500, 65);
        
        model.reset();
        model.train(ts.data);
        
        assertEquals(13, model.model.size());
        assertEquals(1477872000, (long) model.model.get(0).getKey());
        assertTrue(Double.isNaN(model.model.get(0).getValue()));
        assertEquals(1477872300, (long) model.model.get(1).getKey());
        assertEquals(45.0, model.model.get(1).getValue(), 0.00001);
        assertEquals(1477872600, (long) model.model.get(2).getKey());
        assertTrue(Double.isNaN(model.model.get(2).getValue()));
        
        // data earlier than the first window
        ts = new TimeSeries();
        ts.append(1474243200, 10);
        ts.append(1474243500, 25);
        
        ts.append(1474848000, 20);
        ts.append(1474848300, 45);
        
        model.initializeIndices(ts.data, start);
        
        assertEquals(13, model.model.size());
        assertEquals(1477872000, (long) model.model.get(0).getKey());
        assertTrue(Double.isNaN(model.model.get(0).getValue()));
        assertEquals(1477872300, (long) model.model.get(1).getKey());
        assertEquals(45.0, model.model.get(1).getValue(), 0.00001);
        assertEquals(1477872600, (long) model.model.get(2).getKey());
        assertTrue(Double.isNaN(model.model.get(2).getValue()));
        
        // data later than the first window
        ts = new TimeSeries();
        ts.append(1477872000, 10);
        ts.append(1477872300, 25);
        
        ts.append(1478476800, 20);
        ts.append(1478477000, 45);
        
        model.reset();
        model.train(ts.data);
        
        assertEquals(13, model.model.size());
        assertEquals(1477872000, (long) model.model.get(0).getKey());
        assertTrue(Double.isNaN(model.model.get(0).getValue()));
        assertEquals(1477872300, (long) model.model.get(1).getKey());
        assertTrue(Double.isNaN(model.model.get(1).getValue()));
        assertEquals(1477872600, (long) model.model.get(2).getKey());
        assertTrue(Double.isNaN(model.model.get(2).getValue()));
        
        // NaNs
        ts = new TimeSeries();
        ts.append(1475452800, Float.NaN);
        ts.append(1475453100, 25);
        
        ts.append(1476057600, Float.NaN);
        ts.append(1476057900, 45);
        
        ts.append(1476662400, Float.NaN);
        ts.append(1476662700, 55);
        
        ts.append(1477267200, Float.NaN);
        ts.append(1477267500, 65);
        
        model.reset();
        model.train(ts.data);
        
        assertEquals(13, model.model.size());
        assertEquals(1477872000, (long) model.model.get(0).getKey());
        assertTrue(Double.isNaN(model.model.get(0).getValue()));
        assertEquals(1477872300, (long) model.model.get(1).getKey());
        assertEquals(47.5, model.model.get(1).getValue(), 0.00001);
        assertEquals(1477872600, (long) model.model.get(2).getKey());
        assertTrue(Double.isNaN(model.model.get(2).getValue()));
        
        // irregular spacing
        ts = new TimeSeries();
        ts.append(1475452800, 10);
        ts.append(1475453100, 25);
        
        ts.append(1476057600, 20);
        ts.append(1476057900, 45);
        
        ts.append(1476662400, 30);  // avg these three == 15
        ts.append(1476662460, 10);
        ts.append(1476662520, 5);
        ts.append(1476662700, 55);
        
        ts.append(1477267200, 40);
        ts.append(1477267500, 65);
        
        model.reset();
        model.train(ts.data);
        
        assertEquals(13, model.model.size());
        assertEquals(1477872000, (long) model.model.get(0).getKey());
        assertEquals(21.25, model.model.get(0).getValue(), 0.00001);
        assertEquals(1477872300, (long) model.model.get(1).getKey());
        assertEquals(47.5, model.model.get(1).getValue(), 0.00001);
        assertEquals(1477872600, (long) model.model.get(2).getKey());
        assertTrue(Double.isNaN(model.model.get(2).getValue()));
    }

    @Test
    public void trainDrop() throws Exception {
        // one high
        config.setProperty("NUM_TO_DROP_HIGHEST", "1");
        config.setProperty("NUM_TO_DROP_LOWEST", "0");
        OlympicModel2 model = new OlympicModel2(config);
        TimeSeries ts = new TimeSeries();
        ts.append(1475452800, 10);
        ts.append(1475453100, 25);
        
        ts.append(1476057600, 20);
        ts.append(1476057900, 45);
        
        ts.append(1476662400, 30);
        ts.append(1476662700, 55);
        
        ts.append(1477267200, 40);
        ts.append(1477267500, 65);
        
        model.train(ts.data);
        
        assertEquals(13, model.model.size());
        assertEquals(1477872000, (long) model.model.get(0).getKey());
        assertEquals(20.0, model.model.get(0).getValue(), 0.00001);
        assertEquals(1477872300, (long) model.model.get(1).getKey());
        assertEquals(41.66666, model.model.get(1).getValue(), 0.00001);
        assertEquals(1477872600, (long) model.model.get(2).getKey());
        assertTrue(Double.isNaN(model.model.get(2).getValue()));
        
        // two high
        config.setProperty("NUM_TO_DROP_HIGHEST", "2");
        config.setProperty("NUM_TO_DROP_LOWEST", "0");
        model = new OlympicModel2(config);
        ts = new TimeSeries();
        ts.append(1475452800, 10);
        ts.append(1475453100, 25);
        
        ts.append(1476057600, 20);
        ts.append(1476057900, 45);
        
        ts.append(1476662400, 30);
        ts.append(1476662700, 55);
        
        ts.append(1477267200, 40);
        ts.append(1477267500, 65);
        
        model.train(ts.data);
        
        assertEquals(13, model.model.size());
        assertEquals(1477872000, (long) model.model.get(0).getKey());
        assertEquals(15.0, model.model.get(0).getValue(), 0.00001);
        assertEquals(1477872300, (long) model.model.get(1).getKey());
        assertEquals(35.0, model.model.get(1).getValue(), 0.00001);
        assertEquals(1477872600, (long) model.model.get(2).getKey());
        assertTrue(Double.isNaN(model.model.get(2).getValue()));
        
        // one low
        config.setProperty("NUM_TO_DROP_HIGHEST", "0");
        config.setProperty("NUM_TO_DROP_LOWEST", "1");
        model = new OlympicModel2(config);
        ts = new TimeSeries();
        ts.append(1475452800, 10);
        ts.append(1475453100, 25);
        
        ts.append(1476057600, 20);
        ts.append(1476057900, 45);
        
        ts.append(1476662400, 30);
        ts.append(1476662700, 55);
        
        ts.append(1477267200, 40);
        ts.append(1477267500, 65);
        
        model.train(ts.data);
        
        assertEquals(13, model.model.size());
        assertEquals(1477872000, (long) model.model.get(0).getKey());
        assertEquals(30.0, model.model.get(0).getValue(), 0.00001);
        assertEquals(1477872300, (long) model.model.get(1).getKey());
        assertEquals(55.0, model.model.get(1).getValue(), 0.00001);
        assertEquals(1477872600, (long) model.model.get(2).getKey());
        assertTrue(Double.isNaN(model.model.get(2).getValue()));
        
        // two low
        config.setProperty("NUM_TO_DROP_HIGHEST", "0");
        config.setProperty("NUM_TO_DROP_LOWEST", "2");
        model = new OlympicModel2(config);
        ts = new TimeSeries();
        ts.append(1475452800, 10);
        ts.append(1475453100, 25);
        
        ts.append(1476057600, 20);
        ts.append(1476057900, 45);
        
        ts.append(1476662400, 30);
        ts.append(1476662700, 55);
        
        ts.append(1477267200, 40);
        ts.append(1477267500, 65);
        
        model.train(ts.data);
        
        assertEquals(13, model.model.size());
        assertEquals(1477872000, (long) model.model.get(0).getKey());
        assertEquals(35.0, model.model.get(0).getValue(), 0.00001);
        assertEquals(1477872300, (long) model.model.get(1).getKey());
        assertEquals(60.0, model.model.get(1).getValue(), 0.00001);
        assertEquals(1477872600, (long) model.model.get(2).getKey());
        assertTrue(Double.isNaN(model.model.get(2).getValue()));
        
        // one each
        config.setProperty("NUM_TO_DROP_HIGHEST", "1");
        config.setProperty("NUM_TO_DROP_LOWEST", "1");
        model = new OlympicModel2(config);
        ts = new TimeSeries();
        ts.append(1475452800, 10);
        ts.append(1475453100, 25);
        
        ts.append(1476057600, 20);
        ts.append(1476057900, 45);
        
        ts.append(1476662400, 30);
        ts.append(1476662700, 55);
        
        ts.append(1477267200, 40);
        ts.append(1477267500, 65);
        
        model.train(ts.data);
        
        assertEquals(13, model.model.size());
        assertEquals(1477872000, (long) model.model.get(0).getKey());
        assertEquals(25.0, model.model.get(0).getValue(), 0.00001);
        assertEquals(1477872300, (long) model.model.get(1).getKey());
        assertEquals(50.0, model.model.get(1).getValue(), 0.00001);
        assertEquals(1477872600, (long) model.model.get(2).getKey());
        assertTrue(Double.isNaN(model.model.get(2).getValue()));
        
        // two each - TOO MANY so we drop as much as possible
        config.setProperty("NUM_TO_DROP_HIGHEST", "2");
        config.setProperty("NUM_TO_DROP_LOWEST", "2");
        model = new OlympicModel2(config);
        ts = new TimeSeries();
        ts.append(1475452800, 10);
        ts.append(1475453100, 25);
        
        ts.append(1476057600, 20);
        ts.append(1476057900, 45);
        
        ts.append(1476662400, 30);
        ts.append(1476662700, 55);
        
        ts.append(1477267200, 40);
        ts.append(1477267500, 65);
        
        model.train(ts.data);
        
        assertEquals(13, model.model.size());
        assertEquals(1477872000, (long) model.model.get(0).getKey());
        assertEquals(30.0, model.model.get(0).getValue(), 0.00001);
        assertEquals(1477872300, (long) model.model.get(1).getKey());
        assertEquals(55.0, model.model.get(1).getValue(), 0.00001);
        assertEquals(1477872600, (long) model.model.get(2).getKey());
        assertTrue(Double.isNaN(model.model.get(2).getValue()));
        
        // one each - TOO MANY so we drop as much as possible
        config.setProperty("NUM_TO_DROP_HIGHEST", "1");
        config.setProperty("NUM_TO_DROP_LOWEST", "1");
        model = new OlympicModel2(config);
        ts = new TimeSeries();
        ts.append(1475452800, 10);
        ts.append(1475453100, 25);
        
        ts.append(1476057600, Float.NaN);
        ts.append(1476057900, Float.NaN);
        
        ts.append(1476662400, Float.NaN);
        ts.append(1476662700, Float.NaN);
        
        ts.append(1477267200, 40);
        ts.append(1477267500, 65);
        
        model.train(ts.data);
        
        assertEquals(13, model.model.size());
        assertEquals(1477872000, (long) model.model.get(0).getKey());
        assertEquals(40.0, model.model.get(0).getValue(), 0.00001);
        assertEquals(1477872300, (long) model.model.get(1).getKey());
        assertEquals(65.0, model.model.get(1).getValue(), 0.00001);
        assertEquals(1477872600, (long) model.model.get(2).getKey());
        assertTrue(Double.isNaN(model.model.get(2).getValue()));
    }
    
    @Test
    public void trainWeighting() throws Exception {
        config.setProperty("WINDOW_AGGREGATOR", "WAVG");
        OlympicModel2 model = new OlympicModel2(config);
        TimeSeries ts = new TimeSeries();
        ts.append(1475452800, 10);
        ts.append(1475453100, 25);
        
        ts.append(1476057600, 20);
        ts.append(1476057900, 45);
        
        ts.append(1476662400, 30);
        ts.append(1476662700, 55);
        
        ts.append(1477267200, 40);
        ts.append(1477267500, 65);
        
        model.train(ts.data);
        
        assertEquals(13, model.model.size());
        assertEquals(1477872000, (long) model.model.get(0).getKey());
        assertEquals(30.0, model.model.get(0).getValue(), 0.00001);
        assertEquals(1477872300, (long) model.model.get(1).getKey());
        assertEquals(54.0, model.model.get(1).getValue(), 0.00001);
        assertEquals(1477872600, (long) model.model.get(2).getKey());
        assertTrue(Double.isNaN(model.model.get(2).getValue()));
    }
    
    @Test
    public void trainVariousIntervals() throws Exception {
        config.put("WINDOW_DISTANCE_UNITS", "DAYS");
        OlympicModel2 model = new OlympicModel2(config);
        TimeSeries ts = new TimeSeries();
        ts.append(1477526400, 10);
        ts.append(1477526700, 25);
        
        ts.append(1477612800, 20);
        ts.append(1477613100, 45);
        
        ts.append(1477699200, 30);
        ts.append(1477699500, 55);
        
        ts.append(1477785600, 40);
        ts.append(1477785900, 65);
        
        model.train(ts.data);
        
        assertEquals(13, model.model.size());
        assertEquals(1477872000, (long) model.model.get(0).getKey());
        assertEquals(25.0, model.model.get(0).getValue(), 0.00001);
        assertEquals(1477872300, (long) model.model.get(1).getKey());
        assertEquals(47.5, model.model.get(1).getValue(), 0.00001);
        assertEquals(1477872600, (long) model.model.get(2).getKey());
        assertTrue(Double.isNaN(model.model.get(2).getValue()));
        
        // hours
        config.put("WINDOW_DISTANCE_UNITS", "HOURS");
        ts = new TimeSeries();
        ts.append(1477857600, 10);
        ts.append(1477857900, 25);
        
        ts.append(1477861200, 20);
        ts.append(1477861500, 45);
        
        ts.append(1477864800, 30);
        ts.append(1477865100, 55);
        
        ts.append(1477868400, 40);
        ts.append(1477868700, 65);
        
        model = new OlympicModel2(config);
        model.train(ts.data);
        
        assertEquals(13, model.model.size());
        assertEquals(1477872000, (long) model.model.get(0).getKey());
        assertEquals(25.0, model.model.get(0).getValue(), 0.00001);
        assertEquals(1477872300, (long) model.model.get(1).getKey());
        assertEquals(47.5, model.model.get(1).getValue(), 0.00001);
        assertEquals(1477872600, (long) model.model.get(2).getKey());
        assertTrue(Double.isNaN(model.model.get(2).getValue()));
        
        // multiple hours 
        config.put("WINDOW_DISTANCE", "6");
        config.put("WINDOW_DISTANCE_UNITS", "HOURS");
        ts = new TimeSeries();
        ts.append(1477785600, 10);
        ts.append(1477785900, 25);
        
        ts.append(1477807200, 20);
        ts.append(1477807500, 45);
        
        ts.append(1477828800, 30);
        ts.append(1477829100, 55);
        
        ts.append(1477850400, 40);
        ts.append(1477850700, 65);
        
        model = new OlympicModel2(config);
        model.train(ts.data);
        
        assertEquals(13, model.model.size());
        assertEquals(1477872000, (long) model.model.get(0).getKey());
        assertEquals(25.0, model.model.get(0).getValue(), 0.00001);
        assertEquals(1477872300, (long) model.model.get(1).getKey());
        assertEquals(47.5, model.model.get(1).getValue(), 0.00001);
        assertEquals(1477872600, (long) model.model.get(2).getKey());
        assertTrue(Double.isNaN(model.model.get(2).getValue()));
        
        // multiple minutes 
        config.put("WINDOW_DISTANCE", "60");
        config.put("WINDOW_DISTANCE_UNITS", "MINUTES");
        model.reset();
        ts = new TimeSeries();
        ts.append(1477857600, 10);
        ts.append(1477857900, 25);
        
        ts.append(1477861200, 20);
        ts.append(1477861500, 45);
        
        ts.append(1477864800, 30);
        ts.append(1477865100, 55);
        
        ts.append(1477868400, 40);
        ts.append(1477868700, 65);
        
        model = new OlympicModel2(config);
        model.train(ts.data);
        
        assertEquals(13, model.model.size());
        assertEquals(1477872000, (long) model.model.get(0).getKey());
        assertEquals(25.0, model.model.get(0).getValue(), 0.00001);
        assertEquals(1477872300, (long) model.model.get(1).getKey());
        assertEquals(47.5, model.model.get(1).getValue(), 0.00001);
        assertEquals(1477872600, (long) model.model.get(2).getKey());
        assertTrue(Double.isNaN(model.model.get(2).getValue()));
        
        config.put("INTERVAL", "1");
        config.put("INTERVAL_UNITS", "MINUTES");
        config.put("WINDOW_DISTANCE", "1");
        config.put("WINDOW_DISTANCE_UNITS", "DAYS");
        model = new OlympicModel2(config);
        ts = new TimeSeries();
        ts.append(1477526400, 10);
        ts.append(1477526460, 25);
        
        ts.append(1477612800, 20);
        ts.append(1477612860, 45);
        
        ts.append(1477699200, 30);
        ts.append(1477699260, 55);
        
        ts.append(1477785600, 40);
        ts.append(1477785660, 65);
        
        model.train(ts.data);
        
        assertEquals(61, model.model.size());
        assertEquals(1477872000, (long) model.model.get(0).getKey());
        assertEquals(25.0, model.model.get(0).getValue(), 0.00001);
        assertEquals(1477872060, (long) model.model.get(1).getKey());
        assertEquals(47.5, model.model.get(1).getValue(), 0.00001);
        assertEquals(1477872120, (long) model.model.get(2).getKey());
        assertTrue(Double.isNaN(model.model.get(2).getValue()));
    }
    
    @Test
    public void trainTimeZoneHandling() throws Exception {
        config.put("TIMEZONE", "Australia/Lord_Howe");
        config.put("MODEL_START", "1475359200");
        
        OlympicModel2 model = new OlympicModel2(config);
        TimeSeries ts = new TimeSeries();
        ts.append(1472941800, 10);
        ts.append(1472942100, 25);
        
        ts.append(1473546600, 20);
        ts.append(1473546900, 45);
        
        ts.append(1474151400, 30);
        ts.append(1474151700, 55);
        
        ts.append(1474756200, 40);
        ts.append(1474756500, 65);
        
        model.train(ts.data);
        
        assertEquals(13, model.model.size());
        assertEquals(1475359200, (long) model.model.get(0).getKey());
        assertEquals(25.0, model.model.get(0).getValue(), 0.00001);
        assertEquals(1475359500, (long) model.model.get(1).getKey());
        assertEquals(47.5, model.model.get(1).getValue(), 0.00001);
        assertEquals(1475359800, (long) model.model.get(2).getKey());
        assertTrue(Double.isNaN(model.model.get(2).getValue()));
    }

    @Test
    public void trainMultipleWindows() throws Exception {
        TimeSeries ts = new TimeSeries();
        ts.append(1475452800, 10);
        ts.append(1475453100, 25);
        
        ts.append(1476057600, 20);
        ts.append(1476057900, 45);
        
        ts.append(1476662400, 30);
        ts.append(1476662700, 55);
        
        ts.append(1477267200, 40);
        ts.append(1477267500, 65);
        
        config.setProperty("FUTURE_WINDOWS", "2");
        OlympicModel2 model = new OlympicModel2(config);
        model.train(ts.data);
        
        assertEquals(26, model.model.size());
        assertEquals(1477872000, (long) model.model.get(0).getKey());
        assertEquals(25.0, model.model.get(0).getValue(), 0.00001);
        assertEquals(1477872300, (long) model.model.get(1).getKey());
        assertEquals(47.5, model.model.get(1).getValue(), 0.00001);
        assertEquals(1477872600, (long) model.model.get(2).getKey());
        assertTrue(Double.isNaN(model.model.get(2).getValue()));
        // ... nans
        assertEquals(1478476800, (long) model.model.get(13).getKey());
        assertEquals(30.0, model.model.get(13).getValue(), 0.00001);
        assertEquals(1478477100, (long) model.model.get(14).getKey());
        assertEquals(55.0, model.model.get(14).getValue(), 0.00001);
        assertEquals(1478477400, (long) model.model.get(15).getKey());
        assertTrue(Double.isNaN(model.model.get(15).getValue()));
        
        config.setProperty("FUTURE_WINDOWS", "3");
        model = new OlympicModel2(config);
        model.train(ts.data);
        
        assertEquals(39, model.model.size());
        assertEquals(1477872000, (long) model.model.get(0).getKey());
        assertEquals(25.0, model.model.get(0).getValue(), 0.00001);
        assertEquals(1477872300, (long) model.model.get(1).getKey());
        assertEquals(47.5, model.model.get(1).getValue(), 0.00001);
        assertEquals(1477872600, (long) model.model.get(2).getKey());
        assertTrue(Double.isNaN(model.model.get(2).getValue()));
        // ... nans
        assertEquals(1478476800, (long) model.model.get(13).getKey());
        assertEquals(30.0, model.model.get(13).getValue(), 0.00001);
        assertEquals(1478477100, (long) model.model.get(14).getKey());
        assertEquals(55.0, model.model.get(14).getValue(), 0.00001);
        assertEquals(1478477400, (long) model.model.get(15).getKey());
        assertTrue(Double.isNaN(model.model.get(15).getValue()));
        // ... nans
        assertEquals(1479081600, (long) model.model.get(26).getKey());
        assertEquals(35.0, model.model.get(26).getValue(), 0.00001);
        assertEquals(1479081900, (long) model.model.get(27).getKey());
        assertEquals(60.0, model.model.get(27).getValue(), 0.00001);
        assertEquals(1479082200, (long) model.model.get(28).getKey());
        assertTrue(Double.isNaN(model.model.get(28).getValue()));
        
        config.setProperty("FUTURE_WINDOWS", "5");
        model = new OlympicModel2(config);
        model.train(ts.data);
        
        assertEquals(65, model.model.size());
        assertEquals(1477872000, (long) model.model.get(0).getKey());
        assertEquals(25.0, model.model.get(0).getValue(), 0.00001);
        assertEquals(1477872300, (long) model.model.get(1).getKey());
        assertEquals(47.5, model.model.get(1).getValue(), 0.00001);
        assertEquals(1477872600, (long) model.model.get(2).getKey());
        assertTrue(Double.isNaN(model.model.get(2).getValue()));
        // ... nans
        assertEquals(1478476800, (long) model.model.get(13).getKey());
        assertEquals(30.0, model.model.get(13).getValue(), 0.00001);
        assertEquals(1478477100, (long) model.model.get(14).getKey());
        assertEquals(55.0, model.model.get(14).getValue(), 0.00001);
        assertEquals(1478477400, (long) model.model.get(15).getKey());
        assertTrue(Double.isNaN(model.model.get(15).getValue()));
        // ... nans
        assertEquals(1479081600, (long) model.model.get(26).getKey());
        assertEquals(35.0, model.model.get(26).getValue(), 0.00001);
        assertEquals(1479081900, (long) model.model.get(27).getKey());
        assertEquals(60.0, model.model.get(27).getValue(), 0.00001);
        assertEquals(1479082200, (long) model.model.get(28).getKey());
        assertTrue(Double.isNaN(model.model.get(28).getValue()));
        // ... nans
        assertEquals(1479686400, (long) model.model.get(39).getKey());
        assertEquals(40.0, model.model.get(39).getValue(), 0.00001);
        assertEquals(1479686700, (long) model.model.get(40).getKey());
        assertEquals(65.0, model.model.get(40).getValue(), 0.00001);
        assertEquals(1479687000, (long) model.model.get(41).getKey());
        assertTrue(Double.isNaN(model.model.get(41).getValue()));
        // ... nans
        assertEquals(1480291200, (long) model.model.get(52).getKey());
        assertTrue(Double.isNaN(model.model.get(52).getValue()));
        assertEquals(1480291500, (long) model.model.get(53).getKey());
        assertTrue(Double.isNaN(model.model.get(53).getValue()));
    }
    
    @Test (expectedExceptions = NotImplementedException.class)
    public void update() throws Exception {
        final OlympicModel2 model = new OlympicModel2(config);
        final TimeSeries ts = new TimeSeries();
        model.update(ts.data);
    }
    
    @Test
    public void predict() throws Exception {
        OlympicModel2 model = new OlympicModel2(config);
        TimeSeries ts = new TimeSeries();
        ts.append(1475452800, 10);
        ts.append(1475453100, 25);
        
        ts.append(1476057600, 20);
        ts.append(1476057900, 45);
        
        ts.append(1476662400, 30);
        ts.append(1476662700, 55);
        
        ts.append(1477267200, 40);
        ts.append(1477267500, 65);
        
        model.train(ts.data);
        
        ts.append(1477872000, Float.NaN);
        ts.append(1477872300, Float.NaN);
        
        model.predict(ts.data);
        
        assertEquals(1477872000, ts.data.get(8).time);
        assertEquals(25, ts.data.get(8).value, 0.0001);
        assertEquals(1477872300, ts.data.get(9).time);
        assertEquals(47.5, ts.data.get(9).value, 0.0001);
        assertEquals(10, ts.data.size());
        
        // new series
        ts = new TimeSeries();
        ts.append(1477872000, Float.NaN);
        ts.append(1477872300, Float.NaN);
        
        model.predict(ts.data);
        
        assertEquals(1477872000, ts.data.get(0).time);
        assertEquals(25, ts.data.get(0).value, 0.0001);
        assertEquals(1477872300, ts.data.get(1).time);
        assertEquals(47.5, ts.data.get(1).value, 0.0001);
        assertEquals(2, ts.data.size());
        
        // missing later point
        ts = new TimeSeries();
        ts.append(1477872000, Float.NaN);
        
        model.predict(ts.data);
        
        assertEquals(1477872000, ts.data.get(0).time);
        assertEquals(25, ts.data.get(0).value, 0.0001);
        assertEquals(1, ts.data.size());
        
        // missing first point
        ts = new TimeSeries();
        ts.append(1477872300, Float.NaN);
        
        model.predict(ts.data);
        
        assertEquals(1477872300, ts.data.get(0).time);
        assertEquals(47.5, ts.data.get(0).value, 0.0001);
        assertEquals(1, ts.data.size());
        
        // higher resolution sequence than model
        ts = new TimeSeries();
        ts.append(1477872000, Float.NaN);
        ts.append(1477872060, Float.NaN);
        ts.append(1477872120, Float.NaN);
        ts.append(1477872180, Float.NaN);
        ts.append(1477872240, Float.NaN);
        ts.append(1477872300, Float.NaN);
        ts.append(1477872360, Float.NaN);
        
        model.predict(ts.data);
        
        assertEquals(1477872000, ts.data.get(0).time);
        assertEquals(25.0, ts.data.get(0).value, 0.0001);
        assertEquals(1477872060, ts.data.get(1).time);
        assertTrue(Float.isNaN(ts.data.get(1).value));
        assertEquals(1477872120, ts.data.get(2).time);
        assertTrue(Float.isNaN(ts.data.get(2).value));
        assertEquals(1477872180, ts.data.get(3).time);
        assertTrue(Float.isNaN(ts.data.get(3).value));
        assertEquals(1477872240, ts.data.get(4).time);
        assertTrue(Float.isNaN(ts.data.get(4).value));
        assertEquals(1477872300, ts.data.get(5).time);
        assertEquals(47.5, ts.data.get(5).value, 0.0001);
        assertEquals(1477872360, ts.data.get(6).time);
        assertTrue(Float.isNaN(ts.data.get(6).value));
        assertEquals(7, ts.data.size());
        
    }

    /** Helper that sets some defaults in the config for testing. */
    private void setConfig() {
        config = new Properties();
        config.put("INTERVAL", "5");
        config.put("INTERVAL_UNITS", "MINUTES");
        config.put("WINDOW_SIZE", "1");
        config.put("WINDOW_SIZE_UNITS", "HOURS");
        config.put("WINDOW_DISTANCE", "1");
        config.put("WINDOW_DISTANCE_UNITS", "WEEKS");
        config.put("MODEL_START", Long.toString(start));
    }
}
