/*
 * Copyright 2015, Yahoo Inc.
 * Copyrights licensed under the GPL License.
 * See the accompanying LICENSE file for terms.
 */

// interface
// The generic model interface which other models implement (the TimeSeries model and the
// AnomalyDetection model).

package com.yahoo.egads.data;

import java.io.Serializable;

public interface Model extends JsonAble, Serializable {
	
    // resets the model.
    public void reset();

    // Gets the model name and type
    public String getModelName();    
    public String getModelType();
}
