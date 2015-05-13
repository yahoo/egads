/*
 * Copyright 2015, Yahoo Inc.
 * Copyrights licensed under the GPL License.
 * See the accompanying LICENSE file for terms.
 */

package com.yahoo.egads;

import com.yahoo.egads.data.*;
import org.testng.Assert;
import org.testng.annotations.Test;

public class TestJsonEncoder {

    @Test
    public void testAnomalyInterval() throws Exception {
        // anomaly interval
        Anomaly.Interval interval = new Anomaly.Interval(1L, 2L, 3.33f);
        System.out.print("\n interval = " + interval);
        String interval_json = JsonEncoder.toJson(interval);
        System.out.print("\n interval json = " + interval_json);
        Anomaly.Interval interval2 = new Anomaly.Interval();
        JsonEncoder.fromJson(interval2, interval_json);
        System.out
                .print("\n interval2 json = " + JsonEncoder.toJson(interval2));
        Assert.assertEquals(interval, interval2);
    }

    @Test
    public void testAnomaly() throws Exception {
        // anomaly
        Anomaly anomaly = new Anomaly("ANOMALY_ID", new MetricMeta("metric1"));
        anomaly.addInterval(2, 2.2f);
        anomaly.addInterval(3, 3.3f);
        System.out.print("\n anomaly = " + anomaly);
        String anomaly_json = JsonEncoder.toJson(anomaly);
        System.out.print("\n anomaly json = " + anomaly_json);
        Anomaly anomaly2 = new Anomaly();
        JsonEncoder.fromJson(anomaly2, anomaly_json);
        System.out.print("\n anomaly2 json = " + JsonEncoder.toJson(anomaly2));
        Assert.assertEquals(anomaly, anomaly2);
    }

    @Test
    public void testTimeSeries() throws Exception {
        // time series
        TimeSeries series = new TimeSeries(new float[] { 1, 2, 4 });
        series.meta = new MetricMeta("time series meta");
        series.meta.smoothing = "for_all";
        System.out.print("\n\n series = " + series);
        String series_json = JsonEncoder.toJson(series);
        System.out.print("\n series json = " + series_json);
        TimeSeries series2 = new TimeSeries();
        JsonEncoder.fromJson(series2, series_json);
        System.out.print("\n series2 json = " + JsonEncoder.toJson(series2));
        Assert.assertEquals(series, series2);
    }

    @Test
    public void testMetricMeta() throws Exception {
        // metric meta
        MetricMeta metric = new MetricMeta("m1");
        metric.detectAnomalies = true;
        System.out.print("\n\n metric = " + metric);
        String metric_json = JsonEncoder.toJson(metric);
        System.out.print("\n metric json = " + metric_json);
        MetricMeta metric2 = new MetricMeta(null);
        JsonEncoder.fromJson(metric2, metric_json);
        System.out.print("\n\n metric2 = " + metric2);
        System.out.print("\n metric2 json = " + JsonEncoder.toJson(metric2));
        Assert.assertEquals(metric, metric2);
    }

}
