/*
 * Copyright 2015, Yahoo Inc.
 * Copyrights licensed under the GPL License.
 * See the accompanying LICENSE file for terms.
 */

package com.yahoo.egads.utilities;

// Class that implements EGADS STDIN input processor.

import com.yahoo.egads.control.ProcessableObject;
import com.yahoo.egads.control.ProcessableObjectFactory;
import java.util.Properties;
import java.io.*;
import java.util.ArrayList;
import com.yahoo.egads.data.TimeSeries;

public class StdinProcessor implements InputProcessor {
        
    public void processInput(Properties p) throws Exception {
        BufferedReader in = new BufferedReader(new InputStreamReader(System.in));
        String s;
        Integer aggr = 1;
        if (p.getProperty("AGGREGATION") != null) {
          aggr = new Integer(p.getProperty("AGGREGATION"));
        }
        while ((s = in.readLine()) != null && s.length() != 0) {
            // Parse the time-series.
            ArrayList<TimeSeries> metrics = createTimeSeries(s, aggr);
            for (TimeSeries ts : metrics) {
                ProcessableObject po = ProcessableObjectFactory.create(ts, p);
                po.process();
            }
        }
    }
    
    
    // Format of the time-series: meta1\tmeta\2{(2014120205,0),(2014122207,1)}\t{(2014120205,0),(2014122207,0)}...
    // Creates a time-series from a file.
    private static ArrayList<TimeSeries> createTimeSeries(String s, Integer aggr) throws Exception {
        ArrayList<TimeSeries> output = new ArrayList<TimeSeries>();
        String[] tokens = s.split("\t");
        String meta = "meta";
        
        int tokenNum = 1;
        for (String t : tokens) {
            if (t.contains("{(")) {
              output.add(convertStringToTS(t, meta + "-" + Integer.toString(tokenNum)));
              tokenNum++;
            } else {
                meta += "-" + t;
            }
        }

        // Handle aggregation.
        if (aggr > 1) {
            for (TimeSeries t : output) {
                t.data = t.aggregate(aggr);
                t.meta.name += "_aggr_" + aggr;
            }
        }
        return output;
    }
    
    private static TimeSeries convertStringToTS(String s, String tokenNum) throws Exception {
         TimeSeries ts = new TimeSeries();
         ts.meta.fileName = tokenNum;
         ts.meta.name = tokenNum;
         String[] tuples = s.split("\\),");
         for (String tuple : tuples) {
             tuple = tuple.replaceAll("[{}\\(\\)]", "");
             String[] vals = tuple.split(","); 
             Float val = new Float(vals[1]);
             ts.append(new Long(vals[0]), val);
         }
         return ts;
    }
}
