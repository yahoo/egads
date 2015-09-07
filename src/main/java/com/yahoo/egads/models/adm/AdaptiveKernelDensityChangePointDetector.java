/*
 * Copyright 2015, Yahoo Inc.
 * Copyrights licensed under the GPL License.
 * See the accompanying LICENSE file for terms.
 */

package com.yahoo.egads.models.adm;

import java.util.ArrayList;
import java.util.LinkedList;
import java.util.Properties;

import org.json.JSONObject;
import org.json.JSONStringer;

import com.yahoo.egads.data.Anomaly.Interval;
import com.yahoo.egads.data.Anomaly.IntervalSequence;
import com.yahoo.egads.data.TimeSeries.DataSequence;
import com.yahoo.egads.utilities.ListUtils;
/**
 * AdaptiveKernelDensityChangePointDetector implements density-based algorithm for change point detection.
 * 
 * Input: 
 *      1. the residual time-series = actual - expected
 *      2. PRE_WINDOW_SIZE: the size of PRE_WINDOW
 *      3. POST_WINDOW_SIZE: the size of POST_WINDOW
 *      4. CONFIDENCE: the confidence level at which the threshold for the KL-Divergence score is computed.
 *      
 * Output: the time indices when the distribution of residual changes significantly
 * 
 * In particular, the algorithm slides two side-by-side windows (PRE_WINDOW and POST_WINDOW) and computes the KL-divergence between 
 * the distribution of the residuals in two windows for each time index. The distribution of the residuals in each window is 
 * computed non-parametrically using Kernel Density Estimation with Gaussian kernels with adaptive bandwidths.
 * 
 * Once the KL-divergence is computed for each time index, it is thresholded (where the threshold is directly proportional to 'CONFIDENCE'). 
 * And finally for each continuous segment of the thresholded KL-divergence time-series, the index of the maximum value is reported as a 
 * change point.
 * 
 * @author amizadeh
 *
 */

public class AdaptiveKernelDensityChangePointDetector extends AnomalyDetectionAbstractModel {

    // buffering the residuals
    private LinkedList<Float> buffer = new LinkedList<Float>();
    // buffering the standard deviations
    private LinkedList<Float> sdBuffer = new LinkedList<Float>();

    // buffering pre-window kernel sums
    private LinkedList<Float> preKernelSum = new LinkedList<Float>();
    // buffering post-window kernel sums
    private LinkedList<Float> postKernelSum = new LinkedList<Float>();

    // sum of residuals in the buffer
    private float sumBuffer = 0;
    // sum of squared residuals in the buffer
    private float sqrSumBuffer = 0;
    private int maxHrsAgo;

    // KL score
    private float[] score = null;
    // level sets
    private float[] level = null;

    // pre-window size (input argument)
    protected int preWindowSize = 24;
    // post-window size (input argument)
    protected int postWindowSize = 48;
    // confidence level
    protected float confidence = 0.8F;
    // Model name.
    private String modelName = "AdaptiveKernelDensityChangePointDetector";

    public AdaptiveKernelDensityChangePointDetector(Properties config) {
        super(config);

        this.maxHrsAgo = new Integer(config.getProperty("MAX_ANOMALY_TIME_AGO"));
        if (config.getProperty("PRE_WINDOW_SIZE") == null) {
            throw new IllegalArgumentException("PRE_WINDOW_SIZE is NULL");
        }

        if (config.getProperty("POST_WINDOW_SIZE") == null) {
            throw new IllegalArgumentException("POST_WINDOW_SIZE is NULL");
        }

        this.preWindowSize = new Integer(config.getProperty("PRE_WINDOW_SIZE"));
        this.postWindowSize = new Integer(config.getProperty("POST_WINDOW_SIZE"));

        if (config.getProperty("CONFIDENCE") == null) {
            this.confidence = 0.8F;
        } else {
            this.confidence = new Float(config.getProperty("CONFIDENCE"));
        }
    }

    @Override
    public void toJson(JSONStringer json_out) throws Exception {

    }

    @Override
    public void fromJson(JSONObject json_obj) throws Exception {

    }

    @Override
    public String getType() {
        return "change_point";
    }

    @Override
    public String getModelName() {
        return modelName;
    }

    @Override
    public void reset() {
        this.buffer.clear();
        this.sdBuffer.clear();

        this.preKernelSum.clear();
        this.postKernelSum.clear();

        this.sumBuffer = 0;
        this.sqrSumBuffer = 0;

        this.score = null;
        this.level = null;
    }

    @Override
    public void tune(DataSequence observedSeries, DataSequence expectedSeries, IntervalSequence anomalySequence)
                    throws Exception {

    }

    @Override
    public IntervalSequence detect(DataSequence observedSeries, DataSequence expectedSeries) throws Exception {

        if (observedSeries.size() != expectedSeries.size()) {
            throw new Exception("The observed time-series must have the same length as the expected time-series.");
        }
        long unixTime = System.currentTimeMillis() / 1000L;

        IntervalSequence result = new IntervalSequence();
        int n = observedSeries.size();
        float[] residuals = new float[n];

        // Computing the residuals
        for (int i = 0; i < n; ++i) {
            residuals[i] = observedSeries.get(i).value - expectedSeries.get(i).value;
        }

        // Detecting change points
        ArrayList<Integer> changePoints =
                        detectChangePoints(residuals, this.preWindowSize, this.postWindowSize, this.confidence);

        // Preparing the output
        if (this.outputDest.equals("STD_OUT_ALL")) {
            int j = 0;
            for (int i = 0; i < n; ++i) {
                boolean isCP = false;

                if (!changePoints.isEmpty()) {
                    isCP = (changePoints.get(j) == i);
                }

                if (isCP && j < (changePoints.size() - 1)) {
                    j++;
                }
                logger.debug("TS:" + observedSeries.get(i).time + ",SC:" + String.join(":", arrayF2S(new Float[] {score[i]})) + ",LV:" + arrayF2S(new Float[] {level[i]}) + ",OV:" + observedSeries.get(i).value + ",EV:" + expectedSeries.get(i).value);

                result.add(new Interval(observedSeries.get(i).time, 
                		                i,
                                        new Float[] {score[i]},
                                        new Float[] {level[i]},
                                        observedSeries.get(i).value,
                                        expectedSeries.get(i).value,
                                        (isCP)));
            }
        } else {
            for (int index : changePoints) {
                if (((unixTime - observedSeries.get(index).time) / 3600) < maxHrsAgo) {
                    result.add(new Interval(observedSeries.get(index).time, index, new Float[] {score[index]},
                                    new Float[] {level[index]}, observedSeries.get(index).value,
                                    expectedSeries.get(index).value));
                }
            }
        }

        return result;
    }

    public ArrayList<Integer> detectChangePoints(float[] residuals, int preWindowSize, int postWindowSize,
                    float confidence) {
        int n = residuals.length;
        score = new float[n];
        level = new float[n];
        ArrayList<Integer> changePoints = new ArrayList<Integer>();

        float maxScore = Float.NEGATIVE_INFINITY;
        int maxIndex = -1;
        float delta = 0.00000001F;
        int counter = 0;

        for (int i = 0; i < n; ++i, ++counter) {
            float[] temp = computeKLScore(residuals[i], preWindowSize, postWindowSize, confidence);
            score[i] = temp[0];
            level[i] = temp[1];
            if (score[i] > delta) {
                if (score[i] > maxScore) {
                    maxScore = score[i];
                    maxIndex = i;
                }
            } else if (score[i] < -delta) {
                if (maxIndex >= 0) {
                    if (counter - i + maxIndex > postWindowSize) {
                        changePoints.add(maxIndex - postWindowSize + 1);
                        counter = i - maxIndex;
                    }
                    maxScore = Float.NEGATIVE_INFINITY;
                    maxIndex = -1;
                }
            }
        }

        if (maxIndex >= 0) {
            changePoints.add(maxIndex - postWindowSize + 1);
        }

        return changePoints;
    }

    protected float[] computeKLScore(float residual, int preWindowSize, int postWindowSize, float confidence) {
        float dKL = 0;
        float levelThreshold = 0;
        int len = buffer.size();

        // Filling the pre-window
        if (len < preWindowSize) {
            buffer.addLast(residual);
            postKernelSum.addLast(0F);

            if (len == (preWindowSize - 1)) {
                int n = preWindowSize;
                sumBuffer = ListUtils.sumQ(buffer);
                sqrSumBuffer = ListUtils.sum2Q(buffer);
                float temp =
                                (float) Math.max(
                                                1e-5,
                                                Math.sqrt(2 * (n * sqrSumBuffer - sumBuffer * sumBuffer)
                                                                / (n * (n - 1))));
                ListUtils.repQ(sdBuffer, temp, n);

                for (float x : buffer) {
                    preKernelSum.addLast(ListUtils.kernelSum(x, buffer, sdBuffer));
                }
            }
        } else if (len < (preWindowSize + postWindowSize)) { // Filling the post-window
            sumBuffer = sumBuffer + residual;
            sqrSumBuffer = sqrSumBuffer + residual * residual;
            int n = len + 1;
            float temp =
                            (float) Math.max(1e-5,
                                            Math.sqrt(2 * (n * sqrSumBuffer - sumBuffer * sumBuffer) / (n * (n - 1))));
            sdBuffer.addLast(temp);

            LinkedList<Float> tempQ1 = new LinkedList<Float>();
            tempQ1.add(residual);

            LinkedList<Float> tempQ2 = new LinkedList<Float>();
            tempQ2.add(temp);

            ListUtils.addQ(postKernelSum, ListUtils.kernelQ(buffer, tempQ1, tempQ2));
            buffer.addLast(residual);
            preKernelSum.addLast(ListUtils.kernelSubSum(residual, buffer, sdBuffer, 0, preWindowSize - 1));
            postKernelSum.addLast(ListUtils.kernelSubSum(residual, buffer, sdBuffer, preWindowSize, buffer.size() - 1));
        } else {
            // updating the pre-stats
            LinkedList<Float> preRemovedValues =
                            ListUtils.kernelQ(buffer, buffer.subList(0, 1), sdBuffer.subList(0, 1));
            ListUtils.subtractQ(preKernelSum, preRemovedValues);
            LinkedList<Float> midExchangedValues =
                            ListUtils.kernelQ(buffer, buffer.subList(preWindowSize, preWindowSize + 1),
                                            sdBuffer.subList(preWindowSize, preWindowSize + 1));
            ListUtils.addQ(preKernelSum, midExchangedValues);

            // Computing the new sd
            int n = len;
            sumBuffer += (residual - buffer.getFirst());
            sqrSumBuffer += (residual * residual - Math.pow(buffer.getFirst(), 2));
            float temp =
                            (float) Math.max(1e-5,
                                            Math.sqrt(2 * (n * sqrSumBuffer - sumBuffer * sumBuffer) / (n * (n - 1))));

            // updating the post-stats
            LinkedList<Float> tempQ1 = new LinkedList<Float>();
            tempQ1.add(residual);
            LinkedList<Float> tempQ2 = new LinkedList<Float>();
            tempQ2.add(temp);
            ListUtils.subtractQ(postKernelSum, midExchangedValues);
            LinkedList<Float> postAddedValues = ListUtils.kernelQ(buffer, tempQ1, tempQ2);
            ListUtils.addQ(postKernelSum, postAddedValues);

            // updating the window
            buffer.addLast(residual);
            buffer.removeFirst();

            sdBuffer.addLast(temp);
            sdBuffer.removeFirst();

            preKernelSum.addLast(ListUtils.kernelSubSum(residual, buffer, sdBuffer, 0, preWindowSize - 1));
            postKernelSum.addLast(ListUtils.kernelSubSum(residual, buffer, sdBuffer, preWindowSize, preWindowSize
                            + postWindowSize - 1));

            preKernelSum.removeFirst();
            postKernelSum.removeFirst();

            float eps = 1e-10F;
            LinkedList<Float> preDensity =
                            ListUtils.maxQ(preKernelSum.subList(preWindowSize, preWindowSize + postWindowSize), eps);
            LinkedList<Float> postDensity =
                            ListUtils.maxQ(postKernelSum.subList(preWindowSize, preWindowSize + postWindowSize), eps);

            tempQ1.clear();
            tempQ1.addAll(preKernelSum.subList(0, preWindowSize));
            tempQ2.clear();
            tempQ2.add(1.0F / preWindowSize);
            ListUtils.multiplyQ(tempQ1, tempQ2);
            float levelSet = ListUtils.quantile(tempQ1, 1 - confidence);
            levelThreshold =
                            (float) (-Math.log(levelSet) - Math.log(2 * Math.PI) / 2 - ListUtils.sumLog(sdBuffer
                                            .subList(preWindowSize, preWindowSize + postWindowSize)) / postWindowSize);

            // computing the KL-divergence
            dKL =
                            (float) ((ListUtils.sumLog(postDensity) - ListUtils.sumLog(preDensity) + Math
                                            .log(preWindowSize / postWindowSize))
                                            / postWindowSize
                                            + Math.log(levelSet * Math.sqrt(2 * Math.PI)) + ListUtils.sumLog(sdBuffer
                                            .subList(preWindowSize, preWindowSize + postWindowSize)) / postWindowSize);
        }

        return new float[] {dKL, levelThreshold};
    }
}
