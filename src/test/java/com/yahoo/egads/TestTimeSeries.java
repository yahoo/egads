/*
 * Copyright 2015, Yahoo Inc.
 * Copyrights licensed under the GPL License.
 * See the accompanying LICENSE file for terms.
 */

package com.yahoo.egads;

import com.yahoo.egads.data.*;
import org.testng.Assert;
import org.testng.annotations.Test;

public class TestTimeSeries {

    @Test(expectedExceptions = Exception.class)
    public void testException() throws Exception {
        TimeSeries series = new TimeSeries();
        series.append(1L, 11.11f);
        series.append(2L, 22.22f);
        series.append(3L, 33.33f);
        series.append(5L, 55.55f);
        series.append(7L, 77.77f);
        series.append(9L, 99.99f);
        series.append(0L, 00.00f); // causes exception, as it should 
    }

    @Test
    public void test() throws Exception {
        TimeSeries series = new TimeSeries();
        series.append(1L, 11.11f);
        series.append(2L, 22.22f);
        series.append(3L, 33.33f);
        series.append(5L, 55.55f);
        series.append(7L, 77.77f);
        series.append(9L, 99.99f);
        System.out.print("\n series = " + series);
        Assert.assertEquals(series.minimumPeriod(), 1);
        Assert.assertEquals(series.mostFrequentPeriod(), 2);
    }

}
