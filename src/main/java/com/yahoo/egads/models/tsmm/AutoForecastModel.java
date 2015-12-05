//
//  OpenForecast - open source, general-purpose forecasting package.
//  Copyright (C) 2002-2011  Steven R. Gould
//
//  This library is free software; you can redistribute it and/or
//  modify it under the terms of the GNU Lesser General Public
//  License as published by the Free Software Foundation; either
//  version 2.1 of the License, or (at your option) any later version.
//
//  This library is distributed in the hope that it will be useful,
//  but WITHOUT ANY WARRANTY; without even the implied warranty of
//  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
//  Lesser General Public License for more details.
//
//  You should have received a copy of the GNU Lesser General Public
//  License along with this library; if not, write to the Free Software
//  Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA  02111-1307  USA
//

// Olympic scoring model considers the average of the last k weeks
// (dropping the b highest and lowest values) as the current prediction.

package com.yahoo.egads.models.tsmm;

import com.yahoo.egads.data.*;
import org.json.JSONObject;
import org.json.JSONStringer;
import java.util.Properties;

// Picks the best model from the available EGADS models.
public class AutoForecastModel extends TimeSeriesAbstractModel {
    // Stores the properties file to init other models.
    private Properties p;
    
    // Stores the model.
    private TimeSeriesAbstractModel myModel = null;

    public AutoForecastModel(Properties config) {
        super(config);
        modelName = "AutoForecastModel";
        this.p = config;
    }

    public void reset() {
        // At this point, reset does nothing.
    }
    
    public void train(TimeSeries.DataSequence data) {
        // Init all.
        OlympicModel olympModel = new OlympicModel(p);
        MovingAverageModel movingAvg = new MovingAverageModel(p);
        MultipleLinearRegressionModel mlReg = new MultipleLinearRegressionModel(p);
        NaiveForecastingModel naive = new NaiveForecastingModel(p);
        PolynomialRegressionModel poly = new PolynomialRegressionModel(p);
        RegressionModel regr = new RegressionModel(p);
        SimpleExponentialSmoothingModel simpleExp = new SimpleExponentialSmoothingModel(p);
        TripleExponentialSmoothingModel tripleExp = new TripleExponentialSmoothingModel(p);
        WeightedMovingAverageModel weightAvg = new WeightedMovingAverageModel(p);
        DoubleExponentialSmoothingModel doubleExp = new DoubleExponentialSmoothingModel(p);
        
        // Train all.
        olympModel.train(data);
        movingAvg.train(data);
        mlReg.train(data);
        naive.train(data);
        poly.train(data);
        regr.train(data);
        simpleExp.train(data);
        tripleExp.train(data);
        weightAvg.train(data);
        doubleExp.train(data);
        
        // Pick best.
        if (betterThan(olympModel, myModel)) {
            myModel = olympModel;
        }
        if (betterThan(movingAvg, myModel)) {
            myModel = movingAvg;
        }
        if (betterThan(mlReg, myModel)) {
            myModel = mlReg;
        }
        if (betterThan(naive, myModel)) {
            myModel = naive;
        }
        if (betterThan(poly, myModel)) {
            myModel = poly;
        }
        if (betterThan(regr, myModel)) {
            myModel = regr;
        }
        if (betterThan(simpleExp, myModel)) {
            myModel = simpleExp;
        }
        if (betterThan(tripleExp, myModel)) {
            myModel = tripleExp;
        }
        if (betterThan(weightAvg, myModel)) {
            myModel = weightAvg;
        }
        if (betterThan(doubleExp, myModel)) {
            myModel = doubleExp;
        }
        
        initForecastErrors(myModel, data);
       
        logger.debug(getBias() + "\t" + getMAD() + "\t" + getMAPE() + "\t" + getMSE() + "\t" + getSAE() + "\t" + 0 + "\t" + 0);
    }

    public void update(TimeSeries.DataSequence data) {

    }

    public String getModelName() {
        return modelName;
    }

    public void predict(TimeSeries.DataSequence sequence) throws Exception {
        myModel.predict(sequence);        
    }

    public void toJson(JSONStringer json_out) {

    }

    public void fromJson(JSONObject json_obj) {

    }
}
