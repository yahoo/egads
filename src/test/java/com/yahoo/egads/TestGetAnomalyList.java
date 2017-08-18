/*
 * Copyright 2015, Yahoo Inc.
 * Copyrights licensed under the GPL License.
 * See the accompanying LICENSE file for terms.
 */

package com.yahoo.egads;

import com.yahoo.egads.control.ProcessableObject;
import com.yahoo.egads.control.ProcessableObjectFactory;
import com.yahoo.egads.data.Anomaly;
import com.yahoo.egads.data.TimeSeries;

import org.testng.Assert;
import org.testng.annotations.Test;

import java.io.FileInputStream;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Properties;

/*
 * Test the result() from ProcessableObject.
 */
public class TestGetAnomalyList {

    @Test
    public void testUpdateModelProcessable() throws Exception {
        TimeSeries series = new TimeSeries();
        series.append(1L, 11.11f);
        series.append(2L, 22.22f);
        series.append(3L, 33.33f);
        series.append(5L, 55.55f);
        series.append(7L, 77.77f);
        series.append(9L, 99.99f);
        InputStream is = new FileInputStream("src/test/resources/sample_config.ini");
        Properties p = new Properties();
        p.load(is);
        p.setProperty("TS_MODEL","OlympicModel");
        p.setProperty("AD_MODEL","KSigmaModel");
        p.setProperty("OP_TYPE","UPDATE_MODEL");

        ProcessableObject po = ProcessableObjectFactory.create(series, p);
        po.process();

        Assert.assertEquals(po.result().toString(), "Updated");
    }

    @Test
    public void testDetectAnomalyProcessable() throws Exception {

        InputStream is = new FileInputStream("src/test/resources/sample_config.ini");
        Properties p = new Properties();
        p.load(is);
        p.setProperty("TS_MODEL","OlympicModel");
        p.setProperty("AD_MODEL","KSigmaModel");
        p.setProperty("MAX_ANOMALY_TIME_AGO","0");
        p.setProperty("OP_TYPE","DETECT_ANOMALY");

        ArrayList<TimeSeries> metrics = com.yahoo.egads.utilities.FileUtils
            .createTimeSeries("src/test/resources/sample_input.csv", p);

        // generate expected result
        Long anomalousTime = 1417194000L;
        Anomaly anomaly = new Anomaly("value",null);
        anomaly.addInterval(anomalousTime, anomalousTime,0.0f);

        // actual result
        ProcessableObject po = ProcessableObjectFactory.create(metrics.get(0), p);
        po.process();

        Assert.assertEquals(po.result().toString(), Arrays.asList(anomaly).toString());
    }

    @Test
    public void testTransformInputProcessable() throws Exception {

        InputStream is = new FileInputStream("src/test/resources/sample_config.ini");
        Properties p = new Properties();
        p.load(is);
        p.setProperty("TS_MODEL","OlympicModel");
        p.setProperty("AD_MODEL","KSigmaModel");
        p.setProperty("OP_TYPE","TRANSFORM_INPUT");

        TimeSeries series = new TimeSeries();
        series.append(1L, 11.11f);
        series.append(2L, 22.22f);
        series.append(3L, 33.33f);

        // generate expected result
        Long[] times = new Long[]{1L, 2L, 3L};
        Float[] values = new Float[]{11.11f, 22.22f, 33.33f};

        // actual result
        ProcessableObject po = ProcessableObjectFactory.create(series, p);
        po.process();
        List<TimeSeries.DataSequence> actual = (List<TimeSeries.DataSequence>)po.result();
        Assert.assertEquals(actual.get(0).getValues(), values);
        Assert.assertEquals(actual.get(0).getTimes(), times);
    }
}
