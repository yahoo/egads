package com.yahoo.egads;

import java.util.Properties;
import java.util.Scanner;

import com.yahoo.egads.data.FileModelStore;
import com.yahoo.egads.data.Model;
import com.yahoo.egads.data.ModelStore;
import com.yahoo.egads.data.TimeSeries;
import com.yahoo.egads.data.TimeSeries.Entry;
import com.yahoo.egads.models.tsmm.StreamingOlympicModel;
import com.yahoo.egads.models.tsmm.TimeSeriesAbstractModel;

public class StreamForecast {
	public static void main(String[] args) {
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
				System.out.println(String.join(",", series, String.format("%d", timestamp), String.format("%f", measured), String.format("%f", forecast)));
			} catch (Exception e) {
				System.err.println("Invalid input line " + line);
				continue;
			}
		}
		ms.writeCachedModels();
		sc.close();
	}

}
