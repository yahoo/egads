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
import com.yahoo.egads.data.TimeSeries.Entry;
import org.json.JSONObject;
import org.json.JSONStringer;
import net.sourceforge.openforecast.DataSet;
import net.sourceforge.openforecast.ForecastingModel;
import net.sourceforge.openforecast.DataPoint;
import net.sourceforge.openforecast.Observation;
import java.util.*;

import com.yahoo.egads.utilities.Storage;

// Implements a single variable polynomial regression model using the variable named in the constructor as the independent variable.
public class PolynomialRegressionModel extends TimeSeriesAbstractModel {
    // methods ////////////////////////////////////////////////

    // The model that will be used for forecasting.
    private ForecastingModel forecaster;
    
    // Will be updated later based on the best model that we picked.
    private static final String modelName = "PolynomialRegressionModel";;
    
    // Stores the historical values.
    private TimeSeries.DataSequence data;

    public PolynomialRegressionModel(Properties config) {
        super(config);
    }

    public void reset() {
        // At this point, reset does nothing.
    }
    
    public void train(TimeSeries.DataSequence data) {
        this.data = data;
        int n = data.size();
        DataPoint dp = null;
        DataSet observedData = new DataSet();
        for (int i = 0; i < n; i++) {
            dp = new Observation(data.get(i).value);
            dp.setIndependentValue("x", i);
            observedData.add(dp);
        }
        observedData.setTimeVariable("x"); 
        
        // TODO: Make degrees configurable.
        forecaster = new net.sourceforge.openforecast.models.PolynomialRegressionModel("x", 3);
        forecaster.init(observedData);
        initForecastErrors(forecaster, data);
        
        if (Storage.debug == 2) {
            System.out.println(getBias() + "\t" +
                               getMAD() + "\t" +
                               getMAPE() + "\t" +
                               getMSE() + "\t" +
                               getSAE() + "\t" +
                               0 + "\t" +
                               0);
        }    
    }

    public void update(TimeSeries.DataSequence data) {

    }

    public String getModelName() {
        return modelName;
    }

    public void predict(TimeSeries.DataSequence sequence) throws Exception {
          int n = data.size();
          DataSet requiredDataPoints = new DataSet();
          DataPoint dp;

          DataSet requiredDataPointsTmp = new DataSet();
          for (int count = 0; count < n; count++) {
              dp = new Observation(0.0);
              dp.setIndependentValue("x", count);
              requiredDataPoints.add(dp);
          }
          forecaster.forecast(requiredDataPoints);

          // Output the results
          Iterator it = requiredDataPoints.iterator();
          int i = 0;
          while (it.hasNext()) {
              DataPoint pnt = ((DataPoint) it.next());
              if (Storage.debug == 1) {
                  System.out.println(data.get(i).time + "," + data.get(i).value + "," + pnt.getDependentValue());
              }
              sequence.set(i, (new Entry(data.get(i).time, (float) pnt.getDependentValue())));
              i++;
          }
    }

    public void toJson(JSONStringer json_out) {

    }

    public void fromJson(JSONObject json_obj) {

    }
}
