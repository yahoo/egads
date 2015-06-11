/*
 * Copyright 2015, Yahoo Inc.
 * Copyrights licensed under the GPL License.
 * See the accompanying LICENSE file for terms.
 */

// Olympic scoring model considers the average of the last k weeks
// (dropping the b highest and lowest values) as the current prediction.

package com.yahoo.egads.models.tsmm;

import com.yahoo.egads.data.*;
import com.yahoo.egads.data.TimeSeries.Entry;
import org.json.JSONObject;
import org.json.JSONStringer;
import java.util.Properties;
import java.util.ArrayList;
import java.util.Collections;
import com.yahoo.egads.utilities.FileUtils;
import com.yahoo.egads.utilities.Storage;

public class OlympicModel extends TimeSeriesAbstractModel {
    // methods ////////////////////////////////////////////////

    // Number of weeks to look back when computing the
    // estimate.
    private int numWeeks;
    // Number of lowest and highest points to drop.
    private int numToDrop;
    // Stores the historical values.
    private TimeSeries.DataSequence data;
    // Stores the possible time-shifts.
    // time-shifts are used to fix the time-series
    // that has been shifted due to day-light savings.
    private int[] timeShifts;
    // Stores the possible base windows.
    // The default base window is 1 week, however
    // trying multiple possible windows seems to improve
    // performance.
    private int[] baseWindows;
    
    // The actual model that stores the expectations.
    private ArrayList<Float> model;
    
    public OlympicModel(Properties config) {
        super(config);

        if (config.getProperty("NUM_WEEKS") == null) {
            throw new IllegalArgumentException("NUM_WEEKS is NULL");
        }
        if (config.getProperty("NUM_TO_DROP") == null) {
            throw new IllegalArgumentException("NUM_TO_DROP is NULL");
        }
        if (config.getProperty("TIME_SHIFTS") == null) {
            throw new IllegalArgumentException("TIME_SHIFTS is NULL");
        }
        if (config.getProperty("BASE_WINDOWS") == null) {
            throw new IllegalArgumentException("BASE_WINDOWS is NULL");
        }

        this.numWeeks = new Integer(config.getProperty("NUM_WEEKS"));
        this.numToDrop = new Integer(config.getProperty("NUM_TO_DROP"));
        this.timeShifts = FileUtils.splitInts(config.getProperty("TIME_SHIFTS"));
        this.baseWindows = FileUtils.splitInts(config.getProperty("BASE_WINDOWS"));
        Storage.forecastModel = "OlympicModel";
        model = new ArrayList<>();
    }

    public void reset() {
        // At this point, reset does nothing.
    }

    public void train(TimeSeries.DataSequence data) {
        this.data = data;
        
        int n = data.size();
        
        java.util.Arrays.sort(baseWindows);
        java.util.Arrays.sort(timeShifts);
        float precision = (float) 0.000001;
        
        for (int i = 0; i < n; i++) {
            float baseVal = Float.POSITIVE_INFINITY;
            float tmpbase = (float) 0.0;
            
            // Cannot compute the expected value if the time-series
            // is too short preventing us form getting the reference
            // window.
            if ((i - baseWindows[0]) < 0) {
                model.add(data.get(i).value);
                continue;
            }
            
            // Attempt to shift the time-series.
            for (int w = 0; w < baseWindows.length; w++) {
                for (int j = 0; j < timeShifts.length; j++) {
                    if (timeShifts[j] == 0) {
                        tmpbase = computeExpected(i, baseWindows[w]);
                        if ((Math.abs(tmpbase - data.get(i).value) - Math.abs(baseVal - data.get(i).value)) < precision) {
                            baseVal = tmpbase;
                        }
                    } else {
                        if (i + timeShifts[j] < n) {
                            tmpbase = computeExpected(i + timeShifts[j], baseWindows[w]);
                            if ((Math.abs(tmpbase - data.get(i).value) - Math.abs(baseVal - data.get(i).value)) < precision) {
                                baseVal = tmpbase;
                            }
                        }
                        if (i - timeShifts[j] >= 0) {
                            tmpbase = computeExpected(i - timeShifts[j], baseWindows[w]);
                            if ((Math.abs(tmpbase - data.get(i).value) - Math.abs(baseVal - data.get(i).value)) < precision) {
                                baseVal = tmpbase;
                            }
                        }
                    }
                }
            }
            model.add(baseVal);
        }
        
        initForecastErrors(model, data);
        
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
        return "OlympicModel";
    }

    private Float sum(ArrayList<Float> list) {
        float sum = 0;
        for (float i : list) {
            sum = sum + i;
        }
        return sum;
    }
    
    private float computeExpected(int i, int pl) {
        ArrayList<Float> vals = new ArrayList<>();
        float precision = (float) 0.000001;
        
        int j = 1;

        if ((i - pl * j) < 0) {
            return Float.POSITIVE_INFINITY;
        }
        while (j <= this.numWeeks && (i - pl * j) >= 0) {
            float lastWeeksVal = data.get(i - pl * j).value;
            // If dynamic parameters are turned on,
            // then we check if our error improved from last time,
            // if not, then we stop and use the old result.
            if (Storage.dynamicParameters == 1 && vals.size() > 0) {
                float withNewVal = (sum(vals) + lastWeeksVal) / (vals.size() + 1);
                float withoutNewVal = (sum(vals)) / (vals.size());
                if ((Math.abs(withNewVal - data.get(i).value) - Math.abs(withoutNewVal - data.get(i).value)) > precision) {
                    break;
                }
            }
            vals.add(lastWeeksVal);
            j++;
        }

        Collections.sort(vals);
        j = 0;

        if (vals.size() > (2 * this.numToDrop)) {
            while (j < this.numToDrop) {
                vals.remove(vals.size() - 1);
                vals.remove(0);
                j++;
            }
        }

        return sum(vals) / vals.size();
    }
    
    public void predict(TimeSeries.DataSequence sequence) throws Exception {
        int n = data.size();
        for (int i = 0; i < n; i++) {
            sequence.set(i, (new Entry(data.get(i).time, model.get(i))));
            if (Storage.debug == 1) {
                System.out.println(data.get(i).time + "," + data.get(i).value + "," + model.get(i));
            }
        }
    }

    public void toJson(JSONStringer json_out) {

    }

    public void fromJson(JSONObject json_obj) {

    }
}
