/*
 * Copyright 2015, Yahoo Inc.
 * Copyrights licensed under the GPL License.
 * See the accompanying LICENSE file for terms.
 */

package com.yahoo.egads;

import com.yahoo.egads.data.Model;
import com.yahoo.egads.models.tsmm.TimeSeriesAbstractModel;
import com.yahoo.egads.models.tsmm.OlympicModel;
import com.yahoo.egads.models.tsmm.MovingAverageModel;
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
        String configFile = "src/test/resources/sample_config.ini";
        InputStream is = new FileInputStream(configFile);
        Properties p = new Properties();
        p.load(is);
        ArrayList<TimeSeries> actual_metric = com.yahoo.egads.utilities.FileUtils
                .createTimeSeries("src/test/resources/model_input.csv", p);

        for (int w = 0; w < refWindows.length; w++) {
            for (int d = 0; d < drops.length; d++) {
                 p.setProperty("NUM_WEEKS", refWindows[w]);
                 p.setProperty("NUM_TO_DROP", drops[d]);
                 // Parse the input timeseries.
                 ArrayList<TimeSeries> metrics = com.yahoo.egads.utilities.FileUtils
                            .createTimeSeries("src/test/resources/model_output_" + refWindows[w] + "_" + drops[d] + ".csv", p);
                 OlympicModel model = new OlympicModel(p);
                 model.train(actual_metric.get(0).data);
                 TimeSeries.DataSequence sequence = new TimeSeries.DataSequence(metrics.get(0).startTime(),
                		                                                        metrics.get(0).lastTime(),
                		                                                        3600);
                 
                 
                 sequence.setLogicalIndices(metrics.get(0).startTime(), 3600);
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

    @Test
    public void testForecastErrors() throws Exception {
        String configFile = "src/test/resources/sample_config.ini";
        InputStream is = new FileInputStream(configFile);
        Properties p = new Properties();
        p.load(is);
        ArrayList<TimeSeries> actual_metric = com.yahoo.egads.utilities.FileUtils
                .createTimeSeries("src/test/resources/model_input.csv", p);
        OlympicModel olympicModel = new OlympicModel(p);
        olympicModel.train(actual_metric.get(0).data);

        Assert.assertEquals(olympicModel.getBias(), -26.315675155416635, 1e-10);
        Assert.assertEquals(olympicModel.getMAD(), 28.81582062080335, 1e-10);
        Assert.assertEquals(Double.isNaN(olympicModel.getMAPE()), true);
        Assert.assertEquals(olympicModel.getMSE(), 32616.547275296416, 1e-7);
        Assert.assertEquals(olympicModel.getSAE(), 41033.72856402397, 1e-7);
    }

    @Test
    public void testBetterThan() throws Exception {
        String configFile = "src/test/resources/sample_config.ini";
        InputStream is = new FileInputStream(configFile);
        Properties p = new Properties();
        p.load(is);
        ArrayList<TimeSeries> actual_metric = com.yahoo.egads.utilities.FileUtils
                .createTimeSeries("src/test/resources/model_input.csv", p);
        OlympicModel olympicModel = new OlympicModel(p);
        olympicModel.train(actual_metric.get(0).data);

        MovingAverageModel movingAverageModel = new MovingAverageModel(p);
        movingAverageModel.train(actual_metric.get(0).data);

        // movingAverageModel is better than olympicModel
        Assert.assertEquals(TimeSeriesAbstractModel.betterThan(movingAverageModel, olympicModel), true);
        Assert.assertEquals(TimeSeriesAbstractModel.betterThan(movingAverageModel, movingAverageModel), false);
        Assert.assertEquals(TimeSeriesAbstractModel.betterThan(olympicModel, movingAverageModel), false);
        Assert.assertEquals(TimeSeriesAbstractModel.betterThan(olympicModel, olympicModel), false);
    }
}
