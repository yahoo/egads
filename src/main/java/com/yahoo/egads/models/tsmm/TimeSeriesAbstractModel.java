/*
 * Copyright 2015, Yahoo Inc.
 * Copyrights licensed under the GPL License.
 * See the accompanying LICENSE file for terms.
 */

package com.yahoo.egads.models.tsmm;

import java.util.Properties;

import org.json.JSONObject;
import org.json.JSONStringer;

import com.yahoo.egads.data.*;

import java.util.ArrayList;

import net.sourceforge.openforecast.ForecastingModel;

import com.yahoo.egads.data.JsonEncoder;

public abstract class TimeSeriesAbstractModel implements TimeSeriesModel {

    private static final double TOLERANCE = 0.00000001;

    // Accuracy stats for this model.
    protected double bias;
    protected double mad;
    protected double mape;
    protected double mse;
    protected double sae;
    protected String modelName;

    static org.apache.logging.log4j.Logger logger = org.apache.logging.log4j.LogManager.getLogger(TimeSeriesModel.class.getName());

    protected boolean errorsInit = false;
    protected int dynamicParameters = 0;

    public String getModelName() {
		return modelName;
	}

    public String getModelType() {
    	return "Forecast";
    }
    
    @Override
    public void toJson(JSONStringer json_out) throws Exception {
        JsonEncoder.toJson(this, json_out);
    }

    @Override
    public void fromJson(JSONObject json_obj) throws Exception {
        JsonEncoder.fromJson(this, json_obj);
    }

    // Acts as a factory method.
    public TimeSeriesAbstractModel(Properties config) {
        if (config.getProperty("DYNAMIC_PARAMETERS") != null) {
            this.dynamicParameters = new Integer(config.getProperty("DYNAMIC_PARAMETERS"));
        }

    }

    // 1 when absolute value of error1 is smaller than the absolute value of error2
    // 0 when absolute value of error1 is equal(upto tolerance) to the absolute value of error2
    // -1 when absolute value of error1 is greater than the absolute value of error2
    private static int compareError(double error1, double error2) {
        // can't compare NaN
        if (Double.isNaN(error1) || Double.isNaN(error2)) {
            return 0;
        }
        // positive when error1 is better (smaller) then error2
        double diffAbs = Math.abs(error2) - Math.abs(error1);
        if (Math.abs(diffAbs) <= TOLERANCE) {
            return 0;
        }
        return diffAbs > 0 ? 1 : -1;
    }

    // is model1 better than model2
    public static boolean betterThan(TimeSeriesAbstractModel model1, TimeSeriesAbstractModel model2) {
        // Special case. Any model is better than no model!
        if (model2 == null) {
            return true;
        }

        int score = 0;
        score += compareError(model1.getBias(), model2.getBias());
        score += compareError(model1.getMAD(), model2.getMAD());
        score += compareError(model1.getMAPE(), model2.getMAPE());
        score += compareError(model1.getMSE(), model2.getMSE());
        score += compareError(model1.getSAE(), model2.getSAE());

        if (score == 0) {
            // At this point, we're still unsure which one is best
            // so we'll take another approach
            double mapeDiff = model1.getMAPE() - model2.getMAPE();
            double diff =
                            model1.getBias() - model2.getBias() + model1.getMAD() - model2.getMAD() +
                            (Double.isNaN(mapeDiff) ? 0 : mapeDiff) +
                            model1.getMSE() - model2.getMSE() + model1.getSAE() - model2.getSAE();
            return (diff < 0);
        }

        return (score > 0);
    }

    /*
     * Forecasting model already has the errors defined.
     */
    protected void initForecastErrors(TimeSeriesAbstractModel forecaster, TimeSeries.DataSequence data) {
        this.bias = forecaster.getBias();
        this.mad = forecaster.getMAD();
        this.mape = forecaster.getMAPE();
        this.mse = forecaster.getMSE();
        this.sae = forecaster.getSAE();
        errorsInit = true;
    }

    /*
     * Forecasting model already has the errors defined.
     */
    protected void initForecastErrors(ForecastingModel forecaster, TimeSeries.DataSequence data) {
        this.bias = forecaster.getBias();
        this.mad = forecaster.getMAD();
        this.mape = forecaster.getMAPE();
        this.mse = forecaster.getMSE();
        this.sae = forecaster.getSAE();
        errorsInit = true;
    }

    /**
     * Initializes all errors given the model.
     */
    protected void initForecastErrors(ArrayList<Float> model, TimeSeries.DataSequence data) {
        // Reset various helper summations
        double sumErr = 0.0;
        double sumAbsErr = 0.0;
        double sumAbsPercentErr = 0.0;
        double sumErrSquared = 0.0;
        int processedPoints = 0;

        int n = data.size();

        for (int i = 0; i < n; i++) {
            // Calculate error in forecast, and update sums appropriately
            double error = model.get(i) - data.get(i).value;
            sumErr += error;
            sumAbsErr += Math.abs(error);
            sumAbsPercentErr += Math.abs(error / data.get(i).value);
            sumErrSquared += error * error;
            processedPoints++;
        }
        this.bias = sumErr / processedPoints;
        this.mad = sumAbsErr / processedPoints;
        this.mape = sumAbsPercentErr / processedPoints;
        this.mse = sumErrSquared / processedPoints;
        this.sae = sumAbsErr;
        errorsInit = true;
    }

    /**
     * Returns the bias - the arithmetic mean of the errors - obtained from applying the current forecasting model to
     * the initial data set to try and predict each data point. The result is an indication of the accuracy of the model
     * when applied to your initial data set - the smaller the bias, the more accurate the model.
     * 
     * @return the bias - mean of the errors - when the current model was applied to the initial data set.
     */
    public double getBias() {
        if (errorsInit == false) {
            return -1;
        }
        return bias;
    }

    /**
     * Returns the mean absolute deviation obtained from applying the current forecasting model to the initial data set
     * to try and predict each data point. The result is an indication of the accuracy of the model when applied to your
     * initial data set - the smaller the Mean Absolute Deviation (MAD), the more accurate the model.
     * 
     * @return the mean absolute deviation (MAD) when the current model was applied to the initial data set.
     */
    public double getMAD() {
        if (errorsInit == false) {
            return -1;
        }
        return mad;
    }

    /**
     * Returns the mean absolute percentage error obtained from applying the current forecasting model to the initial
     * data set to try and predict each data point. The result is an indication of the accuracy of the model when
     * applied to the initial data set - the smaller the Mean Absolute Percentage Error (MAPE), the more accurate the
     * model.
     * 
     * @return the mean absolute percentage error (MAPE) when the current model was applied to the initial data set.
     */
    public double getMAPE() {
        if (errorsInit == false) {
            return -1;
        }
        return mape;
    }

    /**
     * Returns the mean square of the errors (MSE) obtained from applying the current forecasting model to the initial
     * data set to try and predict each data point. The result is an indication of the accuracy of the model when
     * applied to your initial data set - the smaller the Mean Square of the Errors, the more accurate the model.
     * 
     * @return the mean square of the errors (MSE) when the current model was applied to the initial data set.
     */
    public double getMSE() {
        if (errorsInit == false) {
            return -1;
        }
        return mse;
    }

    /**
     * Returns the Sum of Absolute Errors (SAE) obtained by applying the current forecasting model to the initial data
     * set. Initialized following a call to init.
     * 
     * @return the sum of absolute errors (SAE) obtained by applying this forecasting model to the initial data set.
     */
    public double getSAE() {
        if (!errorsInit) {
            return -1;
        }
        return sae;
    }
}
