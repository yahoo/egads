// main entry point for egads processing node

package com.yahoo.egads;

import java.io.FileInputStream;
import java.io.InputStream;
import java.util.Properties;

import com.yahoo.egads.utilities.FileInputProcessor;
import com.yahoo.egads.utilities.InputProcessor;
import com.yahoo.egads.utilities.StdinProcessor;

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
 *     TODO: write to anomaly DB.
 * 
 */

public class Egads {
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
        InputProcessor ip = null;
        if (p.getProperty("INPUT") == null || p.getProperty("INPUT").equals("CSV")) {
            ip = new FileInputProcessor(args[1]);
        } else {
            ip = new StdinProcessor();
        }
        
        // Process the input the we received (either STDIN or as a file).
        ip.processInput(p);
    }
}
