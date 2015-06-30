/*
 * Copyright 2015, Yahoo Inc.
 * Copyrights licensed under the GPL License.
 * See the accompanying LICENSE file for terms.
 */

// accumulator class 
// 
// If loss of precision is a concern, maybe switch to Knuth algorithm:
// http://en.wikipedia.org/wiki/Algorithms_for_calculating_variance#Incremental_algorithm

package com.yahoo.egads.utilities;

public class Variance {
    // member data ////////////////////////////////////////////////

    public int count = 0;
    public float sum = 0;
    public float sumSquares = 0;

    // construction ////////////////////////////////////////////////

    public Variance() {
    }

    // methods ////////////////////////////////////////////////

    public void reset() {
        count = 0;
        sum = 0;
        sumSquares = 0;
    }

    public void increment(float value) {
        ++count;
        sum += value;
        sumSquares += (value * value);
    }

    public float currentAverage() {
        return sum / (float) count;
    }

    public float currentVariance() {
        float mean = currentAverage();
        float sqMean = mean * mean;
        float meanOfSquares = sumSquares / count;
        return meanOfSquares - sqMean;
    }

    // display ////////////////////////////////////////////////

    public String toString() {
        return "" + currentVariance() + "=(" + sumSquares + "/" + count + ")-("
                + sum + "/" + count + ")^2";
    }

}
