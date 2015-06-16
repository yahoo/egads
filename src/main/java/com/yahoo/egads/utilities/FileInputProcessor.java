/*
 * Copyright 2015, Yahoo Inc.
 * Copyrights licensed under the GPL License.
 * See the accompanying LICENSE file for terms.
 */

package com.yahoo.egads.utilities;

// Class that implements EGADS file input processing.

import com.yahoo.egads.control.ProcessableObject;
import com.yahoo.egads.control.ProcessableObjectFactory;
import java.util.Properties;
import com.yahoo.egads.data.TimeSeries;
import java.util.ArrayList;

public class FileInputProcessor implements InputProcessor {
    
    private String file = null;
    
    public FileInputProcessor(String file) {
        this.file = file;
    }
    
    public void processInput(Properties p) throws Exception {
        // Parse the input timeseries.
        ArrayList<TimeSeries> metrics = com.yahoo.egads.utilities.FileUtils
                .createTimeSeries(this.file, p);
        for (TimeSeries ts : metrics) {
            ProcessableObject po = ProcessableObjectFactory.create(ts, p);
            po.process();
        }
    }
}
