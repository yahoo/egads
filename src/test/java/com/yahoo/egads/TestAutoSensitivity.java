/*
 * Copyright 2015, Yahoo Inc.
 * Copyrights licensed under the GPL License.
 * See the accompanying LICENSE file for terms.
 */

package com.yahoo.egads;

import com.yahoo.egads.utilities.AutoSensitivity;
import java.util.ArrayList;
import org.testng.Assert;
import org.testng.annotations.Test;
import java.util.Random;
import java.io.BufferedReader;
import java.io.FileReader;

// Tests the basic anoamly detection piece of EGADS.
public class TestAutoSensitivity {

    @Test
    public void testAutoSensitivity() throws Exception {
        // Generate a uniform set of numbers and make sure that
        // the result is the expected mean.
        ArrayList<Float> randomNumbers = new ArrayList<Float>();
        //note a single Random object is reused here
        Random randomGenerator = new Random();
        int numToGenerate = 100;
        for (int idx = 1; idx <= numToGenerate; ++idx){
          float randomFloat = randomGenerator.nextFloat();
          randomNumbers.add(randomFloat);
        }
        Float[] fArray = randomNumbers.toArray(new Float[randomNumbers.size()]);
        float threashold = AutoSensitivity.getKSigmaSensitivity(fArray, 1);
        Assert.assertTrue(threashold <= 2);
        ArrayList<Float> sampleErrors = new ArrayList<Float>();
        BufferedReader reader = new BufferedReader(new FileReader("src/test/resources/sample_errors.csv"));
    
        for (String line = reader.readLine(); line != null; line = reader.readLine()) {
            Float f =  Float.parseFloat(line);
            sampleErrors.add(f);
        }
        
        fArray = sampleErrors.toArray(new Float[sampleErrors.size()]);
        threashold = AutoSensitivity.getLowDensitySensitivity(fArray, 1, 1);
        Assert.assertTrue(threashold > 5000000);
    }
}
