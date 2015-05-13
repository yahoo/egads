/*
 * Copyright 2015, Yahoo Inc.
 * Copyrights licensed under the GPL License.
 * See the accompanying LICENSE file for terms.
 */

// singleton data structure

package com.yahoo.egads.utilities;

public class Configuration {
    // member data ////////////////////////////////////////////////

    protected static Configuration singleton = new Configuration();

    public boolean useAnomalyDb = false;
    public boolean useModelDb = false;
    public String anomalyDbUrl = "http://zown1.yms.bf1.yahoo.com:4080/AnomalyDB-webapp/anomaly";
    public String modelDbUrl = "http://zown1.yms.bf1.yahoo.com:4080/AnomalyDB-webapp/model";
    public String hdfsDirectory = "egads_storage";
    public String localFileDirectory = "egads_storage";
    public String localMetricsFilepath = "membership_2014072409.gz";

    // construction ////////////////////////////////////////////////

    public static Configuration get() {
        return singleton;
    }

    protected Configuration() {
    }

    // methods ////////////////////////////////////////////////

    // display ////////////////////////////////////////////////

    public String toString() {
        StringBuffer str = new StringBuffer();
        str.append("\n\t anomalyDbUrl=" + anomalyDbUrl + " modelDbUrl="
                + modelDbUrl);
        return str.toString();
    }

    // test ////////////////////////////////////////////////

    //     public static void main(String[] args) {
    //         Configuration configuration = Configuration.get();
    //         System.out.print("\n configuration = " + configuration);
    //     }

}
