/*
 * Copyright 2015, Yahoo Inc.
 * Copyrights licensed under the GPL License.
 * See the accompanying LICENSE file for terms.
 */

package com.yahoo.egads;

import com.yahoo.egads.data.Model;
import com.yahoo.egads.models.tsmm.OlympicModel;
import com.yahoo.egads.utilities.*;
import com.yahoo.egads.data.*;
import java.util.ArrayList;
import java.util.Properties;
import java.io.FileInputStream;
import java.io.InputStream;
import com.yahoo.egads.control.ProcessableObject;
import com.yahoo.egads.control.ProcessableObjectFactory;
import org.testng.Assert;
import org.testng.annotations.Test;

// Tests the correct generation of the expected values for olympic
// scoring.
public class TestOlympicModel {

    @Test
    public void testOlympicModel() throws Exception {
        // Test cases: ref window: 10, 5
        // Drops: 0, 1
        String[] refWindows = new String[]{"10", "5"};
        String[] drops = new String[]{"0", "1"};
        // Load the true expected values from a file.
        ArrayList<TimeSeries> actual_metric = com.yahoo.egads.utilities.FileUtils
                .createTimeSeries("src/test/resources/model_input.csv");

        for (int w = 0; w < refWindows.length; w++) {
            for (int d = 0; d < drops.length; d++) {
                 String configFile = "src/test/resources/sample_config.ini";
                 InputStream is = new FileInputStream(configFile);
                 Properties p = new Properties();
                 p.load(is);
                 p.setProperty("NUM_WEEKS", refWindows[w]);
                 p.setProperty("NUM_TO_DROP", drops[d]);
                 // Parse the input timeseries.
                 ArrayList<TimeSeries> metrics = com.yahoo.egads.utilities.FileUtils
                            .createTimeSeries("src/test/resources/model_output_" + refWindows[w] + "_" + drops[d] + ".csv");
                 OlympicModel model = new OlympicModel(p);
                 model.train(actual_metric.get(0).data);
                 TimeSeries.DataSequence sequence = new TimeSeries.DataSequence(metrics.get(0).startTime(),
                		                                                        metrics.get(0).lastTime(),
                		                                                        new Long(p.getProperty("PERIOD")));
                 
                 
                 sequence.setLogicalIndices(metrics.get(0).startTime(), new Long(p.getProperty("PERIOD")));
                 model.predict(sequence);
                 Assert.assertEquals(verifyResults(sequence, metrics.get(0).data), true);
            }
        }
    }
    
    // Verifies that the two time-series are identical.
    private boolean verifyResults (TimeSeries.DataSequence computed, TimeSeries.DataSequence actual) {
         int n = computed.size();
         int n2 = actual.size();
         if (n != n2) {
             return false;
         }
         float precision = (float) 0.000001;
         for (int i = 0; i < n; i++) {
             if (Math.abs(computed.get(i).value - actual.get(i).value) > precision) {
                 return false;
             }
         }
         return true;
    }
}
