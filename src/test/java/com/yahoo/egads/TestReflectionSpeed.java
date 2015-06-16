/*
 * Copyright 2015, Yahoo Inc.
 * Copyrights licensed under the GPL License.
 * See the accompanying LICENSE file for terms.
 */

package com.yahoo.egads;

import com.yahoo.egads.data.TimeSeries;

import java.util.ArrayList;
import java.util.Properties;
import java.io.FileInputStream;
import java.io.InputStream;

import com.yahoo.egads.control.ProcessableObject;
import com.yahoo.egads.control.ProcessableObjectFactory;

import org.testng.annotations.Test;

public class TestReflectionSpeed {

    @Test
    public void testReflectionSpeed() throws Exception {
        // Read command line args.
        String csv_file = "src/test/resources/sample_input.csv";
        // TODO: This config will be retreieved from ConfigDB later,
        // for now it is assumed it's a static file.
        String configFile = "src/test/resources/sample_config.ini";
        InputStream is = new FileInputStream(configFile);
        Properties p = new Properties();
        p.load(is);

        // Parse the input timeseries.
        ArrayList<TimeSeries> metrics = com.yahoo.egads.utilities.FileUtils.createTimeSeries(csv_file, p);
        long start = System.currentTimeMillis();
        for (TimeSeries ts : metrics) {
            ProcessableObject po = ProcessableObjectFactory.create(ts, p);
            // Here we don't process the time-series because we're only interested
            // in the reflection speed.
            // po.process();
        }
        System.out.print("\n reflection speed: " + (System.currentTimeMillis() - start) + "ms");
    }
}
