/*
 * Copyright 2015, Yahoo Inc.
 * Copyrights licensed under the GPL License.
 * See the accompanying LICENSE file for terms.
 */

package com.yahoo.egads;

import com.yahoo.egads.data.TimeSeries;
import com.yahoo.egads.utilities.Storage;

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

        // Set output destination if available.
        if (p.getProperty("OUTPUT") != null) {
            Storage.outputSrc = p.getProperty("OUTPUT");
        }

        // Set debug.
        if (p.getProperty("DEBUG") != null) {
            Storage.debug = new Integer(p.getProperty("DEBUG"));
        }

        // Set dynamic parameters.
        if (p.getProperty("DYNAMIC_PARAMETERS") != null) {
            Storage.dynamicParameters = new Integer(p.getProperty("DYNAMIC_PARAMETERS"));
        }

        // Set the assumed amount of anomaly in your data.
        if (p.getProperty("AUTO_SENSITIVITY_ANOMALY_PCNT") != null) {
            Storage.amntAutoSensParameter = new Float(p.getProperty("AUTO_SENSITIVITY_ANOMALY_PCNT"));
        }

        // Parse the input timeseries.
        ArrayList<TimeSeries> metrics = com.yahoo.egads.utilities.FileUtils.createTimeSeries(csv_file);
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
