/*
 * Copyright 2015, Yahoo Inc.
 * Copyrights licensed under the GPL License.
 * See the accompanying LICENSE file for terms.
 */

package com.yahoo.egads;

import com.yahoo.egads.data.*;
import org.testng.Assert;
import org.testng.annotations.Test;

public class TestMetricMeta {

    @Test
    public void test() {

        // just check for exceptions in construction and printing
        MetricMeta metric = new MetricMeta("m1");
        metric.detectAnomalies = true;
        metric.source = "somewhere";
        System.out.print("\n metric = " + metric);

    }

}
