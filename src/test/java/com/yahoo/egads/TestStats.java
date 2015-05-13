/*
 * Copyright 2015, Yahoo Inc.
 * Copyrights licensed under the GPL License.
 * See the accompanying LICENSE file for terms.
 */

package com.yahoo.egads;

import com.yahoo.egads.utilities.StatsUtils;
import java.util.ArrayList;
import org.testng.Assert;
import org.testng.annotations.Test;
import java.util.Random;

// Tests the basic anoamly detection piece of EGADS.
public class TestStats {
    
    private Random fRandom = new Random();

    @Test
    public void testStats() throws Exception {
        // Generate a normal distribution.
        ArrayList<Double> randomNumbers = new ArrayList<Double>();
        // Skewed distribution.
        ArrayList<Double> skewedNumbers = new ArrayList<Double>();
        
        double mean = 100.0f; 
        double variance = 5.0f;
        int numToGenerate = 10000;

        for (int idx = 1; idx <= numToGenerate; ++idx){
            randomNumbers.add(getGaussian(mean, variance));
        }

        for (int idx = 1; idx <= numToGenerate; ++idx){
            if (idx < numToGenerate/2) {
                skewedNumbers.add(1.0);
            } else {
                skewedNumbers.add(100.0);
            }
          }
        double[] result = StatsUtils.swilk(randomNumbers);
        // p val > alpha will NOT reject the null hypothesis that
        // the values came from a normal distribution.
        Assert.assertTrue(result[1] >= 0.05);
        result = StatsUtils.swilk(skewedNumbers);
        // p val < alpha will reject the null hypothesis that 
        // the values came from a normal distribution.
        Assert.assertTrue(result[1] <= 0.05);
    }   
    
    private double getGaussian(double aMean, double aVariance){
        return aMean + fRandom.nextGaussian() * aVariance;
    }
}
