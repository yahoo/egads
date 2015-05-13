/*
 * Copyright 2015, Yahoo Inc.
 * Copyrights licensed under the GPL License.
 * See the accompanying LICENSE file for terms.
 */

package com.yahoo.egads;

import com.yahoo.egads.data.*;
import org.testng.Assert;
import org.testng.annotations.Test;

// Test the aggregation feature of the TimeSeries class.
public class TestAggregate {

    @Test
    public void testAggregate() throws Exception {
        TimeSeries series = new TimeSeries();
        for (int i = 0; i < 100; i++) {
            series.append((long)i, (float)1);
        }
        TimeSeries.DataSequence d = series.aggregate(10);
        Assert.assertEquals(d.size(), 10);
        for (int i = 0; i < d.size(); i++) {
            Assert.assertEquals(d.get(i).value, (float) 1.0);
        }
    }
}
