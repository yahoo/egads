/*
 * Copyright 2015, Yahoo Inc.
 * Copyrights licensed under the GPL License.
 * See the accompanying LICENSE file for terms.
 */

package com.yahoo.egads;

import com.yahoo.egads.utilities.*;
import org.testng.Assert;
import org.testng.annotations.Test;

public class TestVariance {

    @Test
    public void test() {
        Variance variance = new Variance();
        variance.increment(1);
        variance.increment(2);
        variance.increment(3);
        variance.increment(3);
        variance.increment(3);
        variance.increment(4);
        variance.increment(5);
        System.out.print("\n variance = " + variance);
        Assert.assertEquals(variance.currentVariance(), 1.4285717f, 0.001f);
        Assert.assertEquals(variance.currentAverage(), 3.0f, 0.001f);
        Assert.assertEquals(variance.count, 7);
    }

}
