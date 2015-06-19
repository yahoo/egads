package com.yahoo.egads;

import gnu.getopt.Getopt;

import java.util.HashMap;
import java.util.Properties;
import java.util.Scanner;

import com.yahoo.egads.data.FileModelStore;
import com.yahoo.egads.data.Model;
import com.yahoo.egads.data.ModelStore;
import com.yahoo.egads.data.TimeSeries;
import com.yahoo.egads.models.tsmm.StreamingOlympicModel;
import com.yahoo.egads.models.tsmm.TimeSeriesAbstractModel;

public class StreamForecast {
	public static void main(String[] args) {
		HashMap<Integer,String> options = processOptions(args);
		Scanner sc = new Scanner(System.in);
		ModelStore ms = new FileModelStore ("models");
		while (sc.hasNextLine()) {
			String line = sc.nextLine();
			String[] fields = line.split(",");
			String series;
			int timestamp;
			double measured;
			double forecast = Double.NaN;
			try {
				series = fields[0];
				timestamp = Integer.parseInt(fields[1]);
				measured = Double.parseDouble(fields[2]);
				TimeSeriesAbstractModel model = (TimeSeriesAbstractModel) ms.getModel(series);
				if (model == null) {
					model = new StreamingOlympicModel(new Properties());
					ms.storeModel(series, model);
					System.err.println ("No such model " + series);
				}
				TimeSeries.Entry e = new TimeSeries.Entry(timestamp, (float)measured);
				forecast = model.predict(e);
				model.update(e);
				if (!options.containsKey(new Integer('q'))) {
					System.out.println(String.join(",", series, String.format("%d", timestamp), String.format("%f", measured), String.format("%f", forecast)));
				}
			} catch (Exception e) {
				System.err.println("Invalid input line " + line);
				continue;
			}
		}
		ms.writeCachedModels();
		if (options.containsKey(new Integer('t'))) {
			for (Model m : ms.getCachedModels()) {
				System.out.println(m.errorSummaryString());
			}
		}
		sc.close();
	}

	public static void usage() {
		System.out.println ("Usage: StreamForecast [-m <default modeltype>] [-p <properties file>] [-t] [-q] [-h]");
		System.out.println ("  Modeltypes:");
		System.out.println ("    sos: Streaming Olympic Scoring");
		System.out.println ("  Default preperties file is config.ini");
		System.out.println ("  -t: Run in model test mode.  This outputs error stats at the end of the model run");
		System.exit(0);
	}
	
	public static HashMap<Integer, String> processOptions (String[] args) {
		HashMap<Integer, String> result = new HashMap<Integer, String>();
		// defaults
		result.put(new Integer('m'),  "sos");
		result.put(new Integer('p'), "config.ini");

		Getopt g = new Getopt("TrainForecastingModel", args, "m:p:htq");
		int c;
		while ((c = g.getopt()) != -1) {
			switch (c) {
			case 'm':
			case 'n':
			case 'p':
				result.put(c, g.getOptarg());
				break;
			case 't':
			case 'q':
				result.put(c, "True");
				break;
			case 'h':
				usage();
			}
		}
		return result;
	}

}
