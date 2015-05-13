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

public class TestChangePointDetectionAlgorithm {
    @Test
    public void testChangePointDetectionAlgorithm() {
        TimeSeries observedTS =
                        com.yahoo.egads.utilities.FileUtils
                                        .createTimeSeries("src/test/resources/cp-obs.csv").get(0);
        TimeSeries expectedTS =
                        com.yahoo.egads.utilities.FileUtils
                                        .createTimeSeries("src/test/resources/cp-exp.csv").get(0);

        int n = observedTS.size();
        Integer preWindowSize = 2 * 24 * 4;
        Integer postWindowSize = 2 * 24 * 4;
        Float confidence = 0.8F;
        float[] residuals = new float[n];

        // Computing the residuals
        for (int i = 0; i < n; ++i) {
            residuals[i] = observedTS.data.get(i).value - expectedTS.data.get(i).value;
        }

        Properties prp = new Properties();
        prp.setProperty("PRE_WINDOW_SIZE", preWindowSize.toString());
        prp.setProperty("POST_WINDOW_SIZE", postWindowSize.toString());
        prp.setProperty("CONFIDENCE", confidence.toString());
        prp.setProperty("MAX_ANOMALY_TIME_AGO", "48");

        AdaptiveKernelDensityChangePointDetector cpd = new AdaptiveKernelDensityChangePointDetector(prp);
        ArrayList<Integer> changePoints = cpd.detectChnagePoints(residuals, preWindowSize, postWindowSize, confidence);
        Assert.assertTrue(changePoints.size() == 1);
    }
}
