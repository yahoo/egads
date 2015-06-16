package com.yahoo.egads.streaming;

import java.util.Scanner;

public class ProcessStream {
	public static void main(String[] args) {
		Scanner sc = new Scanner(System.in);
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
				if (fields.length > 3) {
					forecast = Double.parseDouble(fields[3]);
				}
			} catch (Exception e) {
				System.err.println("Invalid input line " + line);
				continue;
			}
			System.out.println(series + "/" + timestamp + ": " + measured + "/" + forecast);
		}
		sc.close();
	}

}
