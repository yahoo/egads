/*
 * Copyright 2015, Yahoo Inc.
 * Copyrights licensed under the GPL License.
 * See the accompanying LICENSE file for terms.
 */

// A simple thresholding model that returns an anomaly if it is above/below a certain threashold.

package com.yahoo.egads.models.adm;

import java.util.Properties;
import java.util.Map;
import java.util.HashMap;
import java.util.List;
import java.util.ArrayList;
import com.yahoo.egads.data.Anomaly.IntervalSequence;
import com.yahoo.egads.data.Anomaly.Interval;
import com.yahoo.egads.data.AnomalyErrorStorage;
import com.yahoo.egads.data.TimeSeries.DataSequence;
import com.yahoo.egads.utilities.Storage;
import org.apache.commons.math3.ml.clustering.Cluster;
import com.yahoo.egads.utilities.IdentifiedDoublePoint;
import org.apache.commons.math3.ml.distance.EuclideanDistance;
import org.apache.commons.math3.ml.clustering.DBSCANClusterer;

import org.json.JSONObject;
import org.json.JSONStringer;

public class DBScanModel extends AnomalyDetectionAbstractModel {

    private int maxHrsAgo;
    // modelName.
    private String modelName = "DBScanModel";
    private final AnomalyErrorStorage aes = new AnomalyErrorStorage();
    private DBSCANClusterer<IdentifiedDoublePoint> dbscan = null;

    public DBScanModel(Properties config) {
        super(config);
       
        modelName = modelName + "-" + Storage.forecastModel;
        if (config.getProperty("MAX_ANOMALY_TIME_AGO") == null) {
            throw new IllegalArgumentException("MAX_ANOMALY_TIME_AGO is NULL");
        }
        this.maxHrsAgo = new Integer(config.getProperty("MAX_ANOMALY_TIME_AGO"));

        Map<String, Float> threshold = parseMap(config.getProperty("THRESHOLD"));
            
        if (config.getProperty("THRESHOLD") != null && threshold.isEmpty()) {
            throw new IllegalArgumentException("THRESHOLD PARSE ERROR");
        } 
    }
    
    // Parses the THRESHOLD config into a map.
    private Map<String, Float> parseMap(String s) {
        if (s == null) {
            return new HashMap<>();
        }
        String[] pairs = s.split(",");
        Map<String, Float> myMap = new HashMap<>();
        for (int i = 0; i < pairs.length; i++) {
            String pair = pairs[i];
            String[] keyValue = pair.split(":");
            myMap.put(keyValue[0], Float.valueOf(keyValue[1]));
        }
        return myMap;
    }

    public void toJson(JSONStringer json_out) {

    }

    public void fromJson(JSONObject json_obj) {

    }

    public String getModelName() {
        return modelName;
    }

    @Override
    public String getType() {
        return "point_outlier";
    }

    @Override
    public void reset() {
        // At this point, reset does nothing.
    }

    @Override
    public void tune(DataSequence observedSeries,
                     DataSequence expectedSeries,
                     IntervalSequence anomalySequence) {
        // Compute the time-series of errors.
        HashMap<String, ArrayList<Float>> allErrors = aes.initAnomalyErrors(observedSeries, expectedSeries);
        List<IdentifiedDoublePoint> points = new ArrayList<>();
        EuclideanDistance ed = new EuclideanDistance();
        int n = observedSeries.size();
        
        for (int i = 0; i < n; i++) {
            double[] d = new double[(aes.getIndexToError().keySet()).size()];
           
            for (int e = 0; e < (aes.getIndexToError().keySet()).size(); e++) {
                 d[e] = allErrors.get(aes.getIndexToError().get(e)).get(i);
            }
            points.add(new IdentifiedDoublePoint(d, i));
        }
        
        double sum = 0.0;
        double count = 0.0;
        for (int i = 0; i < n; i++) {
            for (int j = 0; j < n; j++) {
                sum += ed.compute(points.get(i).getPoint(), points.get(j).getPoint());
                count++;
            }
        }
        double eps = ((double) Storage.sDAutoSensParameter) * (sum / count);
        int minPoints = ((int) Math.ceil(((double) Storage.amntAutoSensParameter) * ((double) n)));
        dbscan = new DBSCANClusterer<>(eps, minPoints);
    }
  
    @Override
    public IntervalSequence detect(DataSequence observedSeries,
                                   DataSequence expectedSeries) {
        
        IntervalSequence output = new IntervalSequence();
        int n = observedSeries.size();
        long unixTime = System.currentTimeMillis() / 1000L;
        Float[] thresholdErrors = new Float[2];
        thresholdErrors[0] = (float) 500.0;
        thresholdErrors[1] = (float) 2.0;
        
        // Compute the time-series of errors.
        HashMap<String, ArrayList<Float>> allErrors = aes.initAnomalyErrors(observedSeries, expectedSeries);
        List<IdentifiedDoublePoint> points = new ArrayList<>();
        
        for (int i = 0; i < n; i++) {
            double[] d = new double[(aes.getIndexToError().keySet()).size()];
           
            for (int e = 0; e < (aes.getIndexToError().keySet()).size(); e++) {
                 d[e] = allErrors.get(aes.getIndexToError().get(e)).get(i);
            }
            points.add(new IdentifiedDoublePoint(d, i));
        }
        
        List<Cluster<IdentifiedDoublePoint>> cluster = dbscan.cluster(points);

        for(Cluster<IdentifiedDoublePoint> c: cluster) {
            for (IdentifiedDoublePoint p : c.getPoints()) {
                Float[] errors = aes.computeErrorMetrics(expectedSeries.get(p.getId()).value, observedSeries.get(p.getId()).value);
                if (Storage.debug == 3) {
                    output.add(new Interval(observedSeries.get(p.getId()).time,
                                            errors,
                                            thresholdErrors,
                                            observedSeries.get(p.getId()).value,
                                            expectedSeries.get(p.getId()).value,
                                            true));
                } else {
                    if (observedSeries.get(p.getId()).value != expectedSeries.get(p.getId()).value &&
                        ((((unixTime - observedSeries.get(p.getId()).time) / 3600) < maxHrsAgo) ||
                        (maxHrsAgo == 0 && p.getId() == (n - 1)))) {
                        output.add(new Interval(observedSeries.get(p.getId()).time,
                                                errors,
                                                thresholdErrors,
                                                observedSeries.get(p.getId()).value,
                                                expectedSeries.get(p.getId()).value));
                    }
                }                
            }
        }

        return output;
    }
}
