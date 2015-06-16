/*
 * Copyright 2015, Yahoo Inc.
 * Copyrights licensed under the GPL License.
 * See the accompanying LICENSE file for terms.
 */

package com.yahoo.egads;

import java.util.ArrayList;
import org.testng.Assert;
import java.util.Properties;
import org.testng.annotations.Test;
import com.yahoo.egads.data.TimeSeries;
import com.yahoo.egads.models.adm.AdaptiveKernelDensityChangePointDetector;
import java.io.InputStream;
import java.io.FileInputStream;

public class TestChangePointDetectionAlgorithm {
    @Test
    public void testChangePointDetectionAlgorithm() throws Exception {
        String configFile = "src/test/resources/sample_config.ini";
        InputStream is = new FileInputStream(configFile);
        Properties p = new Properties();
        p.load(is);
        TimeSeries observedTS =
                        com.yahoo.egads.utilities.FileUtils
                                        .createTimeSeries("src/test/resources/cp-obs.csv", p).get(0);
        TimeSeries expectedTS =
                        com.yahoo.egads.utilities.FileUtils
                                        .createTimeSeries("src/test/resources/cp-exp.csv", p).get(0);

        int n = observedTS.size();
        Integer preWindowSize = 2 * 24 * 4;
        Integer postWindowSize = 2 * 24 * 4;
        Float confidence = 0.8F;
        float[] residuals = new float[n];

        // Computing the residuals
        for (int i = 0; i < n; ++i) {
            residuals[i] = observedTS.data.get(i).value - expectedTS.data.get(i).value;
        }

        p.setProperty("PRE_WINDOW_SIZE", preWindowSize.toString());
        p.setProperty("POST_WINDOW_SIZE", postWindowSize.toString());
        p.setProperty("CONFIDENCE", confidence.toString());
        p.setProperty("MAX_ANOMALY_TIME_AGO", "48");

        AdaptiveKernelDensityChangePointDetector cpd = new AdaptiveKernelDensityChangePointDetector(p);
        ArrayList<Integer> changePoints = cpd.detectChangePoints(residuals, preWindowSize, postWindowSize, confidence);
        Assert.assertTrue(changePoints.size() == 1);
    }
}
