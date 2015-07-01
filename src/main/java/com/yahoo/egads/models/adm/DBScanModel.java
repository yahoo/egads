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
import com.yahoo.egads.utilities.DBSCANClusterer;

import org.apache.commons.math3.ml.clustering.Cluster;

import com.yahoo.egads.utilities.IdentifiedDoublePoint;

import org.apache.commons.math3.ml.distance.EuclideanDistance;
import org.json.JSONObject;
import org.json.JSONStringer;

public class DBScanModel extends AnomalyDetectionAbstractModel {

    // The constructor takes a set of properties
    // needed for the simple model. This includes the sensitivity.
    private Map<String, Float> threshold;
    private int maxHrsAgo;
    // modelName.
    public String modelName = "DBScanModel";
    public AnomalyErrorStorage aes = new AnomalyErrorStorage();
    private DBSCANClusterer<IdentifiedDoublePoint> dbscan = null;
    private int minPoints = 2;
    private double eps = 500;
    
    public DBScanModel(Properties config) {
        super(config);
       
        if (config.getProperty("MAX_ANOMALY_TIME_AGO") == null) {
            throw new IllegalArgumentException("MAX_ANOMALY_TIME_AGO is NULL");
        }
        this.maxHrsAgo = new Integer(config.getProperty("MAX_ANOMALY_TIME_AGO"));
        
        this.threshold = parseMap(config.getProperty("THRESHOLD"));
            
        if (config.getProperty("THRESHOLD") != null && this.threshold.isEmpty() == true) {
            throw new IllegalArgumentException("THRESHOLD PARSE ERROR");
        } 
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
                     IntervalSequence anomalySequence) throws Exception {
        // Compute the time-series of errors.
        HashMap<String, ArrayList<Float>> allErrors = aes.initAnomalyErrors(observedSeries, expectedSeries);
        List<IdentifiedDoublePoint> points = new ArrayList<IdentifiedDoublePoint>();
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
        eps = ((double) this.sDAutoSensitivity) * (sum / count);   
        minPoints = ((int) Math.ceil(((double) this.amntAutoSensitivity) * ((double) n)));     
        dbscan = new DBSCANClusterer<IdentifiedDoublePoint>(eps, minPoints);
    }
  
    @Override
    public IntervalSequence detect(DataSequence observedSeries,
                                   DataSequence expectedSeries) throws Exception {
        
        IntervalSequence output = new IntervalSequence();
        int n = observedSeries.size();
        long unixTime = System.currentTimeMillis() / 1000L;
        // Get an array of thresholds.
        Float[] thresholdErrors = new Float[aes.getErrorToIndex().size()];
        for (Map.Entry<String, Float> entry : this.threshold.entrySet()) {
            thresholdErrors[aes.getErrorToIndex().get(entry.getKey())] = Math.abs(entry.getValue());
        }
        
        // Compute the time-series of errors.
        HashMap<String, ArrayList<Float>> allErrors = aes.initAnomalyErrors(observedSeries, expectedSeries);
        List<IdentifiedDoublePoint> points = new ArrayList<IdentifiedDoublePoint>();
        
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
            	int i = p.getId();
                Float[] errors = aes.computeErrorMetrics(expectedSeries.get(p.getId()).value, observedSeries.get(p.getId()).value);
                logger.debug("TS:" + observedSeries.get(i).time + ",E:" + arrayF2S(errors) + ",TE:" + arrayF2S(thresholdErrors) + ",OV:" + observedSeries.get(i).value + ",EV:" + expectedSeries.get(i).value);
                if (observedSeries.get(p.getId()).value != expectedSeries.get(p.getId()).value &&
                    ((((unixTime - observedSeries.get(p.getId()).time) / 3600) < maxHrsAgo) ||
                    (maxHrsAgo == 0 && p.getId() == (n - 1)))) {
                    output.add(new Interval(observedSeries.get(p.getId()).time,
                    		                p.getId(), 
                                            errors,
                                            thresholdErrors,
                                            observedSeries.get(p.getId()).value,
                                            expectedSeries.get(p.getId()).value));
                }
            }
        }

        return output;
    }
}
