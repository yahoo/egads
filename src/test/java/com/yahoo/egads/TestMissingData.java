/*
 * Copyright 2015, Yahoo Inc.
 * Copyrights licensed under the GPL License.
 * See the accompanying LICENSE file for terms.
 */

// a standalone working main method for the main
// EGADS application.

package com.yahoo.egads;

import com.yahoo.egads.data.TimeSeries;
import java.util.ArrayList;
import org.testng.annotations.Test;
import org.testng.Assert;
import java.io.InputStream;
import java.io.FileInputStream;
import java.util.Properties;

public class TestMissingData {

    @Test
    public void testMissingData() throws Exception {
        String csv_file = "src/test/resources/sample_missing_input.csv";
        String configFile = "src/test/resources/sample_config.ini";
        InputStream is = new FileInputStream(configFile);
        Properties p = new Properties();
        p.load(is);
        // Auto detect.
        p.setProperty("PERIOD", "0");
        p.setProperty("FILL_MISSING", "1");
        
        ArrayList<TimeSeries> metrics = com.yahoo.egads.utilities.FileUtils
                 .createTimeSeries(csv_file, p);
        Assert.assertEquals(metrics.get(0).data.size(), 1433);
    }
}
