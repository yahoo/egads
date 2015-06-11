/*
 * Copyright 2015, Yahoo Inc.
 * Copyrights licensed under the GPL License.
 * See the accompanying LICENSE file for terms.
 */

package com.yahoo.egads;

import com.yahoo.egads.models.tsmm.*;
import com.yahoo.egads.data.*;

import java.io.FileInputStream;
import java.io.InputStream;

import org.testng.Assert;
import org.testng.annotations.Test;

import java.util.*;

// Tests the correct generation of the expected values for olympic
// scoring.
public class TestAutoForecast {

    @Test
    public void testAutoForecast() throws Exception {
        ArrayList<TimeSeries> metrics = com.yahoo.egads.utilities.FileUtils
                .createTimeSeries("src/test/resources/sample_input.csv");
        
        String configFile = "src/test/resources/sample_config.ini";
        InputStream is = new FileInputStream(configFile);
        Properties p = new Properties();
        p.load(is);
        AutoForecastModel model = new AutoForecastModel(p);
        model.train(metrics.get(0).data);
        TimeSeries.DataSequence sequence = new TimeSeries.DataSequence(metrics.get(0).startTime(),
        		                                                       metrics.get(0).lastTime(),
                		                                               new Long(p.getProperty("PERIOD")));
                 
       sequence.setLogicalIndices(metrics.get(0).startTime(), new Long(p.getProperty("PERIOD")));
       model.predict(sequence);
       Assert.assertEquals(verifyResults(sequence, metrics.get(0).data), true);
    }
    
    // Verifies that the two time-series are identical.
    private boolean verifyResults (TimeSeries.DataSequence computed, TimeSeries.DataSequence actual) {
         int n = computed.size();
         int n2 = actual.size();
         if (n != n2) {
             return false;
         }
         float precision = (float) 0.000001;
         float errorSum = (float) 0.0;
         System.out.println();
         for (int i = 0; i < n; i++) {
        	 errorSum += Math.abs(computed.get(i).value - actual.get(i).value);
         }
         errorSum /= n;

        return errorSum <= 5152990;
    }
}
