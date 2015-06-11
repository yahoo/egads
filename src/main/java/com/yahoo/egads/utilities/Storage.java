/*
 * Copyright 2015, Yahoo Inc.
 * Copyrights licensed under the GPL License.
 * See the accompanying LICENSE file for terms.
 */

// functional class, wrapping storage systems

package com.yahoo.egads.utilities;

public class Storage {
    public static String outputSrc = null;
    // Classes can use this boolean to print
    // debug statements.
    public static int debug = 0;
    // If set to 1, then EGADS parameters
    // will be computed dynamically.
    public static int dynamicParameters = 0;
    // Can potentially be used by the Stats library for Box-Cox
    // transformation.
    public static Float sDAutoSensParameter = (float) 3.0;
    // Sets the assumed amount of anomalies in your data.
    public static Float amntAutoSensParameter = (float) 0.05;
    // Stores the name of the current forecast model.
    public static String forecastModel = "none";
    // A parameter denoting the aggregation level.
    public static int aggr = 1;

    private Storage() {
    }
}
