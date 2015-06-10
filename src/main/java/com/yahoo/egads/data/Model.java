/*
 * Copyright 2015, Yahoo Inc.
 * Copyrights licensed under the GPL License.
 * See the accompanying LICENSE file for terms.
 */

// interface
// The generic model interface which other models implement (the TimeSeries model and the
// AnomalyDetection model).

package com.yahoo.egads.data;

public abstract class Model implements JsonAble {
	protected String modelName;
	
    // resets the model.
    public abstract void reset();

    // Gets the model name
    public String getModelName() {
    	return modelName;
    };
    
    public abstract String getModelType();
}
