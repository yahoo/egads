/*
 * Copyright 2015, Yahoo Inc.
 * Copyrights licensed under the GPL License.
 * See the accompanying LICENSE file for terms.
 */

// Provides the auto-sensitivity solution for EGADS
// using the bootstrapping framework.

package com.yahoo.egads.utilities;
import java.util.ArrayList;
import org.apache.commons.lang.ArrayUtils;
import java.util.Arrays;
import java.util.Collections;

public class AutoSensitivity {
    // Computes sensitivity based on the density distirbution.
    // Assumes that anomalies constitute at most 5% of the data.
    public static Float getLowDensitySensitivity(Float[] data) {
        Float toReturn = Float.POSITIVE_INFINITY;
        Arrays.sort(data, Collections.reverseOrder());
        ArrayList<Float> fData = new ArrayList<>();
        fData.add(data[0]);
        data = ((Float[]) ArrayUtils.remove(data, 0));

        Float centroid = fData.get(0);
        Float maxDelta = (float) Storage.sDAutoSensParameter * StatsUtils.getSD(data, StatsUtils.getMean(data));

        if (Storage.debug == 4) {
            System.out.println("AutoSensitivity: Adding: " + fData.get(0) + " SD: " + maxDelta);
        }

        // Add points while it's in the same cluster or not part of the other cluster.
        String localDebug = null;
        while (data.length > 0 &&
               (centroid - data[0]) <= maxDelta) {
            if (Storage.debug == 4) {
                localDebug = "AutoSensitivity: Adding: " + data[0] + " SD: " + maxDelta;
            }
            fData.add(data[0]);
            data = ((Float[]) ArrayUtils.remove(data, 0));
            Float[] tmp = new Float[fData.size()];
            tmp = fData.toArray(tmp);
            centroid = StatsUtils.getMean(tmp);

            if (data.length > 0) {
                Float sdOtherCluster = StatsUtils.getSD(data, StatsUtils.getMean(data));
                maxDelta = Storage.sDAutoSensParameter * sdOtherCluster;
                if (Storage.debug == 4) {
                    System.out.println(localDebug + " SD': " + maxDelta);
                }
            }
        }
        if (data.length > 0 && Storage.debug == 4) {
            System.out.println("AutoSensitivity: Next Point I would have added is " + data[0]);
        }

        if (((double) fData.size() / (double) data.length) > Storage.amntAutoSensParameter) {
            // Cannot do anomaly detection.
            if (Storage.debug == 4) {
                System.out.println("AutoSensitivity: Returning " + toReturn + " data size: " + data.length + " fData.size: " + fData.size());
            }
            return toReturn;
        }

        toReturn = fData.get(fData.size() - 1);
        if (Storage.debug == 4) {
            System.out.println("AutoSensitivity: Updating toReturn:  " + toReturn + " SD: " + maxDelta);
        }
        return toReturn;
    }
    
    // Uses the simple KSigma rule to get the anoamly sensitivity.
    // Assumes that we have a normal distribution.
    public static Float getKSigmaSensitivity(Float[] data) {
         Float mean = StatsUtils.getMean(data);
         Float sd = StatsUtils.getSD(data, mean);
         return (mean + (sd * Storage.sDAutoSensParameter));
    }
}
