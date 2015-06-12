package com.yahoo.egads.batch;

import java.util.Properties;
import java.util.Scanner;
import java.util.HashMap;

import com.yahoo.egads.data.FileModelStore;
import com.yahoo.egads.data.ModelStore;
import com.yahoo.egads.data.TimeSeries;
import com.yahoo.egads.models.tsmm.OlympicModel;

public class TrainForecastingModel {
	public static void main(String[] args) {
		HashMap<String, TimeSeries.DataSequence> inputs = new HashMap<String, TimeSeries.DataSequence>();
		Scanner sc = new Scanner(System.in);
		ModelStore m = new FileModelStore ("models");
		Properties osProps = new Properties();
		osProps.setProperty("TIME_SHIFTS", "0,1");
		osProps.setProperty("BASE_WINDOWS", "24,168");
		osProps.setProperty("NUM_WEEKS", "6");
		osProps.setProperty("NUM_TO_DROP", "1");
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
		for (String series : inputs.keySet()) {
			TimeSeries.DataSequence seq = inputs.get(series);
			OlympicModel o = new OlympicModel(osProps);
			o.train(seq);
			System.out.println (series + ":");
			for (TimeSeries.Entry e : seq) {
				System.out.println(e.time + ": " + e.value);
			}
			m.storeModel(series, o);
		}
	}

}
