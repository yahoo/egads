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
	static org.apache.logging.log4j.Logger logger = org.apache.logging.log4j.LogManager.getLogger(AutoSensitivity.class.getName());
    // Computes sensitivity based on the density distribution.
    // Assumes that anomalies constitute at most 5% of the data.
    public static Float getLowDensitySensitivity(Float[] data, float sDAutoSensitivy, float amntAutoSensitivity) {
        Float toReturn = Float.POSITIVE_INFINITY;
        Arrays.sort(data, Collections.reverseOrder());
        while (data.length > 0) {     
        	
            ArrayList<Float> fData = new ArrayList<Float>();
            fData.add(data[0]);
            data = ((Float[]) ArrayUtils.remove(data, 0));
            
            Float centroid = (float) fData.get(0);
            Float maxDelta = (float) sDAutoSensitivy * StatsUtils.getSD(data, StatsUtils.getMean(data));
            
            logger.debug("AutoSensitivity: Adding: " + fData.get(0) + " SD: " + maxDelta);
            
            // Add points while it's in the same cluster or not part of the other cluster.
            String localDebug = null;
            while (data.length > 0 &&
                   (centroid - data[0]) <= ((float) (maxDelta))) {
            	float maxDeltaInit = maxDelta;
                fData.add(data[0]);
                data = ((Float[]) ArrayUtils.remove(data, 0));
                Float[] tmp = new Float[fData.size()];
                tmp = fData.toArray(tmp);
                centroid = StatsUtils.getMean(tmp);
                
                if (data.length > 0) {
                    Float sdOtherCluster = (float) StatsUtils.getSD(data, StatsUtils.getMean(data));
                    maxDelta = sDAutoSensitivy * sdOtherCluster;
                    logger.debug("AutoSensitivity: Adding: " + data[0] + " SD: " + maxDeltaInit + " SD': " + maxDelta);
                }
            }
            if (data.length > 0) {
                logger.debug("AutoSensitivity: Next Point I would have added is " + data[0]);
            }
                        
            if (((double) fData.size() / (double) data.length) > amntAutoSensitivity) {
                // Cannot do anomaly detection.
                logger.debug("AutoSensitivity: Returning " + toReturn + " data size: " + data.length + " fData.size: " + fData.size());
                return toReturn;
            }

            toReturn = fData.get(fData.size() - 1);
            logger.debug("AutoSensitivity: Updating toReturn:  " + toReturn + " SD: " + maxDelta);
            return toReturn;
        }
        return toReturn;
    }
    
    // Uses the simple KSigma rule to get the anoamly sensitivity.
    // Assumes that we have a normal distribution.
    public static Float getKSigmaSensitivity(Float[] data, float sDAutoSensitivity) {
         Float mean = StatsUtils.getMean(data);
         Float sd = StatsUtils.getSD(data, mean);
         return (mean + (sd * sDAutoSensitivity));
    }

    // Uses the mean as the base to find the static threshold.
    public static Float[] getAdaptiveKSigmaSensitivity(Float[] data, float amntAutoSens) {
         Float mean = StatsUtils.getMean(data);
         Float sd = StatsUtils.getSD(data, mean);
         if (sd == (float) 0.0) {
             sd = (float) 1.0;
         }
         Float[] ret = null;
         float k = (float) 1;
         float incr = (float) 1;
         
         Float max = null;
         Float min = null;
         float thresh = mean + Math.abs(sd * k);
         int howMany = howManyGreater(data, thresh);

         while (((float) howMany / (float) data.length) > amntAutoSens) {
             k += incr;
             thresh = mean + Math.abs(sd * k);
             howMany = howManyGreater(data, thresh);
         } 
         if (((float) howMany / (float) data.length) <= amntAutoSens) {
             max = thresh;
         } 
         k = 1;
         thresh = mean - Math.abs(sd * k);
         howMany = howManyLess(data, thresh);
        
         while (((float) howMany / (float) data.length) > amntAutoSens) {
             k += incr;
             thresh = mean - Math.abs(sd * k);
             howMany = howManyLess(data, thresh);
         } 
         if (((float) howMany / (float) data.length) <= amntAutoSens) {
           min = thresh;
         }
         ret = new Float[]{max, min};
         return ret; 
    }
    
    // Uses the max/min as the base to find the static threshold.
    public static Float[] getAdaptiveMaxMinSigmaSensitivity(Float[] data, float amntAutoSens, float k) {
    	Arrays.sort(data);
    	Float mean = StatsUtils.getMean(data);
        Float sd = StatsUtils.getSD(data, mean);
        if (sd == (float) 0.0) {
            sd = (float) 1.0;
        }
        Float[] ret = null;
        
        Float max = null;
        Float min = null;
        int i = 0;
        float thresh = data[i] + Math.abs(sd * k);
        int howMany = howManyLess(data, thresh);
        while (((float) howMany / (float) data.length) <= amntAutoSens) {
        	min = thresh;
        	i++;
            thresh = data[i] + Math.abs(sd * k);
            howMany = howManyLess(data, thresh);
        } 
        i = data.length - 1;
        thresh = data[i] - Math.abs(sd * k);
        howMany = howManyGreater(data, thresh);
        while (((float) howMany / (float) data.length) <= amntAutoSens) {
        	max = thresh;
            i--;
            thresh = data[i] - Math.abs(sd * k);
            howMany = howManyGreater(data, thresh);
        } 

        ret = new Float[]{max, min};
        return ret; 
   }

    private static int howManyGreater(Float[] data, Float value) {
        int numgreater = 0;
        for (Float f : data) {
            if (value <= f) {
                numgreater++;
            }
        }
        return numgreater;
    }

    private static int howManyLess(Float[] data, Float value) {
        int numless = 0;
        for (Float f : data) {
            if (value >= f) {
                numless++;
            }
        }
        return numless;
    }
}
