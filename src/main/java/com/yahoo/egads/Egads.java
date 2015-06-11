// main entry point for egads processing node

package com.yahoo.egads;

import java.util.Properties;
import java.io.FileInputStream;
import java.io.InputStream;
import com.yahoo.egads.utilities.*;

/*
 * Call stack.
 * 
 * Anomaly Dtection Use Case (Assuming a trained model). 
 * Egads.Main()
 *   ProcessableObjectFactory.create
 *     Create AnomalyDetector
 *       BuildADModel()
 *     Create ModelAdapter
 *       BuildTSModel()
 *   po.process
 *     TODO: write to naomaly DB (?)
 * 
 */

class Egads {
    public static void main(String[] args) throws Exception {

        if (args.length == 0) {
            System.err.println("Usage: java Egads config.ini (input [STDIN,CSV])");
            System.exit(1);
        }

        // TODO: This config will be retreieved from ConfigDB later,
        // for now it is assumed it's a static file.
        String configFile = args[0];
        InputStream is = new FileInputStream(configFile);
        Properties p = new Properties();
        p.load(is);
        
        // Set the input type.
        InputProcessor ip;
        if (p.getProperty("INPUT") == null || p.getProperty("INPUT").equals("CSV")) {
            ip = new FileInputProcessor(args[1]);
        } else {
            ip = new StdinProcessor();
        }
        
        // Set output destination if available.
        if (p.getProperty("OUTPUT") != null) {
            Storage.outputSrc = p.getProperty("OUTPUT");
        }
        
        // Set debug.
        if (p.getProperty("DEBUG") != null) {
            Storage.debug = new Integer(p.getProperty("DEBUG"));
        }
        
        // Set dynamic parameters.
        if (p.getProperty("DYNAMIC_PARAMETERS") != null) {
            Storage.dynamicParameters = new Integer(p.getProperty("DYNAMIC_PARAMETERS"));
        }
        
        // Set the assumed amount of anomaly in your data.
        if (p.getProperty("AUTO_SENSITIVITY_ANOMALY_PCNT") != null) {
            Storage.amntAutoSensParameter = new Float(p.getProperty("AUTO_SENSITIVITY_ANOMALY_PCNT"));
        }
        
        // Set the standard deviation for auto sensitivity.
        if (p.getProperty("AUTO_SENSITIVITY_SD") != null) {
            Storage.sDAutoSensParameter = new Float(p.getProperty("AUTO_SENSITIVITY_SD"));
        }
        
        // Aggregation level.
        if (p.getProperty("AGGREGATION") != null) {
            Storage.aggr = new Integer(p.getProperty("AGGREGATION"));
        }
        
        // Process the input the we received (either STDIN or as a file).
        ip.processInput(p);
    }
}
