package com.yahoo.egads;

import gnu.getopt.Getopt;

import java.io.FileInputStream;
import java.util.HashMap;
import java.util.Properties;
import java.util.Scanner;

import com.yahoo.egads.data.FileModelStore;
import com.yahoo.egads.data.ModelFactory;
import com.yahoo.egads.data.ModelStore;
import com.yahoo.egads.data.TimeSeries;
import com.yahoo.egads.models.tsmm.TimeSeriesModel;

public class TrainForecastingModel {
	public static void main(String[] args) throws Exception {
		HashMap<Integer,String> options = processOptions(args);
		HashMap<String, TimeSeries.DataSequence> inputs = new HashMap<String, TimeSeries.DataSequence>();
		Scanner sc = new Scanner(System.in);
		ModelStore ms = new FileModelStore ("models");
		Properties osProps = new Properties();
		osProps.load (new FileInputStream(options.get(new Integer('p'))));
		while (sc.hasNextLine()) {
			String line = sc.nextLine();
			String[] fields = line.split(",");
			String series;
			int timestamp;
			float measured;
			try {
				series = fields[0];
				timestamp = Integer.parseInt(fields[1]);
				measured = Float.parseFloat(fields[2]);
			} catch (Exception e) {
				System.err.println("Invalid input line " + line);
				continue;
			}
			if (!inputs.containsKey(series)) {
				inputs.put(series, new TimeSeries.DataSequence());
			}
			TimeSeries.DataSequence seq = inputs.get(series);
			seq.add(new TimeSeries.Entry(timestamp, measured));				
		}
		sc.close();
		ModelFactory mf = new ModelFactory(osProps);
		for (String series : inputs.keySet()) {
			TimeSeries.DataSequence seq = inputs.get(series);
			TimeSeriesModel m = mf.getTSModel(options.get(new Integer('m')));
			if (m != null) {
				m.train(seq);
				ms.storeModel(series, m);
			}
		}
	}
	
	public static void usage() {
		System.out.println ("Usage: TrainForecastingModel [-m <modeltype>] [-p <properties file>] [-h]");
		System.out.println ("  Modeltypes:");
		System.out.println ("    sos: Streaming Olympic Scoring");
		System.out.println ("  Default preperties file is config.ini");
		System.exit(0);
	}
	
	public static HashMap<Integer, String> processOptions (String[] args) {
		HashMap<Integer, String> result = new HashMap<Integer, String>();
		// defaults
		result.put(new Integer('m'),  "sos");
		result.put(new Integer('p'), "config.ini");

		Getopt g = new Getopt("TrainForecastingModel", args, "m:p:h");
		int c;
		while ((c = g.getopt()) != -1) {
			switch (c) {
			case 'm':
				result.put(c, g.getOptarg());
				break;
			case 'p':
				result.put(c, g.getOptarg());
				break;
			case 'h':
				usage();
			}
		}
		return result;
	}

}
