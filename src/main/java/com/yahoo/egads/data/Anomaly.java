/*
 * Copyright 2015, Yahoo Inc.
 * Copyrights licensed under the GPL License.
 * See the accompanying LICENSE file for terms.
 */

// data structure

package com.yahoo.egads.data;

import java.util.ArrayList;
import java.util.Date;
import java.text.SimpleDateFormat;
import org.json.JSONObject;
import org.json.JSONStringer;

public class Anomaly implements JsonAble {
    // member data ////////////////////////////////////////////////

    public String id;
    public String type = "";
    public String modelName = "";
    public MetricMeta metricMetaData; // reference, not owner
    public IntervalSequence intervals = new IntervalSequence();

    // Inner Classes ///////////////////////////////////////////////////////

    public static class Interval implements JsonAble {
        public long startTime = 0; // unix_time:seconds
        public Long endTime = null; // unix_time:seconds , may be null
        public long logicalStartIndex = -1;
        public long logicalEndIndex = -1;
        public float value = 0;
        public Integer index = -1;

        // Not set to final so that the json encode tests pass.
        public Float[] anomalyScore;
        public Float[] thresholdScore;
        public Float actualVal;
        public Float expectedVal;
        public Long utime; // point anomaly time.
        public Boolean isAnomaly;

        public Interval() {
            super();
            this.actualVal = null;
            this.anomalyScore = null;
            this.thresholdScore = null;
            this.expectedVal = null;
            this.utime = null;
            this.isAnomaly = null;
            this.index = null;
        }

        // Point anomaly special case interval.
        public Interval(long utime,
        		        int index,
                        Float[] score,
                        Float[] thresholds,
                        float actual,
                        float expected) {
            super();
            this.utime = utime;
            this.anomalyScore = score;
            this.thresholdScore = thresholds;
            this.actualVal = actual;
            this.expectedVal = expected;
            this.isAnomaly = null;
            this.startTime = utime;
            this.index = index;
        }

        // Point anomaly special case interval with debug.
        public Interval(long utime,
        		        int index,
                        Float[] score,
                        Float[] thresholds,
                        float actual,
                        float expected,
                        boolean isAnomaly) {
            super();
            this.utime = utime;
            this.anomalyScore = score;
            this.thresholdScore = thresholds;
            this.actualVal = actual;
            this.expectedVal = expected;
            this.isAnomaly = isAnomaly;
            this.startTime = utime;
            this.index = index;
        }

        public Interval(long logicalStartIndex,
                        long logicalEndIndex,
                        float value) {
            super();
            this.logicalStartIndex = logicalStartIndex;
            this.logicalEndIndex = logicalEndIndex;
            this.value = value;
            this.actualVal = null;
            this.anomalyScore = null;
            this.thresholdScore = null;

            this.expectedVal = null;
            this.utime = null;
            this.isAnomaly = null;
            this.index = null;
        }

        public void toJson(JSONStringer json_out) throws Exception {
            JsonEncoder.toJson(this, json_out);
        }

        public void fromJson(JSONObject json_obj) throws Exception {
            JsonEncoder.fromJson(this, json_obj);
        }

        public String toString() {
            StringBuffer str = new StringBuffer();

            Date startDate = new Date(startTime * 1000);
            Date endDate = (endTime == null) ? null : new Date(
                    endTime.longValue() * 1000);
            SimpleDateFormat dateFormat = new SimpleDateFormat(
                    "yyyy-MM-dd_hh:mm"); // Locale.UK);
            String startStr = dateFormat.format(startDate);
            String endStr = (endDate == null) ? "" : " to "
                    + dateFormat.format(endDate);

            str.append(startStr + endStr);
            return str.toString();
        }

        // needed for unit testing
        public boolean equals(Object other_obj) {
            if (!(other_obj instanceof Interval)) {
                return false;
            }
            Interval other = (Interval) other_obj;
            if (startTime != other.startTime) {
                return false;
            }
            if (!MetricMeta.equals(endTime, other.endTime)) {
                return false;
            }
            if (logicalStartIndex != other.logicalStartIndex) {
                return false;
            }
            if (logicalEndIndex != other.logicalEndIndex) {
                return false;
            }
            if (value != other.value) {
                return false;
            }
            return true;
        }
    }

    public static class IntervalSequence extends ArrayList<Interval> {

        private static final long serialVersionUID = 1L;

        public void setLogicalIndices(long firstTimeStamp, long period) {
            for (Anomaly.Interval interval : this) {
                interval.logicalStartIndex = (interval.startTime - firstTimeStamp)
                        / period;
                interval.logicalEndIndex = ((interval.endTime == null ? interval.startTime : interval.endTime) - firstTimeStamp)
                        / period;
            }
        }

        public void setTimeStamps(long firstTimeStamp, long period) {
            for (Anomaly.Interval interval : this) {
                interval.startTime = interval.logicalStartIndex * period
                        + firstTimeStamp;
                interval.endTime = interval.logicalEndIndex * period
                        + firstTimeStamp;
            }
        }

        // needed for unit testing
        public boolean equals(Object other_obj) {
            if (!(other_obj instanceof IntervalSequence)) {
                return false;
            }
            IntervalSequence other = (IntervalSequence) other_obj;
            if (!super.equals(other)) {
                return false;
            }
            return true;
        }
    }

    // construction ////////////////////////////////////////////////

    public Anomaly() {
        id = null;
        metricMetaData = new MetricMeta(null);
    }

    public Anomaly(String id_arg, MetricMeta meta) {
        id = id_arg;
        metricMetaData = meta;
    }

    public void addInterval(long start_time, float value) {
        Interval interval = new Interval();
        interval.startTime = start_time;
        interval.value = value;
        intervals.add(interval);
    }

    public void addInterval(long start_time, long end_time, float value) {
        Interval interval = new Interval();
        interval.startTime = start_time;
        interval.endTime = new Long(end_time);
        interval.value = value;
        intervals.add(interval);
    }

    // methods ////////////////////////////////////////////////

    // display ////////////////////////////////////////////////

    public String toString() {
        StringBuffer str = new StringBuffer();
        str.append("id=" + id);
        String metricId = (metricMetaData == null) ? "null" : metricMetaData.id;
        str.append("  metric id=" + metricId);
        for (Interval i : intervals) {
            str.append("[id=" + id + " " + i.toString() + " metricId="
                    + metricId + "]" + "\n");
        }
        return str.toString();
    }

    // Supports the legacy EGADS perl version.
    public String toPerlString() {
        StringBuffer str = new StringBuffer();
        for (Interval i : intervals) {
             str.append(i.utime + "," +
                        metricMetaData.name + "," +
                        metricMetaData.fileName + "," +
                        modelName + "," +
                        i.expectedVal + "," +
                        i.actualVal + "," +
                        printArray(i.anomalyScore) + "," +
                        printArray(i.thresholdScore) + printDebugIsAnomaly(i.isAnomaly) + "\n");
        }
        return str.toString();
    }

    public String toPlotString() {
        StringBuffer str = new StringBuffer();
        for (Interval i : intervals) {
             str.append(i.utime + "," +
                        i.actualVal + "," +
                        i.expectedVal +
                        printDebugIsAnomaly(i.isAnomaly) + "\n");
        }
        return str.toString();
    }

    // Prints anomaly is not null.
    public static String printDebugIsAnomaly(Boolean isAnom) {
        if (isAnom == null) {
            return "";
        }
        return "," + ((isAnom == true) ? "1" : "0");
    }

    // Print array and separate by commas.
    public static String printArray(Float[] arr) {
        StringBuffer str = new StringBuffer();
        if (arr == null) {
          return "NA";
        }
        if (arr.length >= 1) {
            str.append(arr[0]);
        }

        // note that i starts at 1, since we already printed the element at index 0
        for (int i = 1; i < arr.length; i++) {
            str.append("," + arr[i]);
        }
        return str.toString();
    }

    public void toJson(JSONStringer json_out) throws Exception {
        JsonEncoder.toJson(this, json_out);
    }

    public void fromJson(JSONObject json_obj) throws Exception {
        JsonEncoder.fromJson(this, json_obj);
    }

    // test ////////////////////////////////////////////////

    // needed for unit testing
    public boolean equals(Object other_obj) {
        if (!(other_obj instanceof Anomaly)) {
            return false;
        }
        Anomaly other = (Anomaly) other_obj;
        if (!MetricMeta.equals(id, other.id)) {
            return false;
        }
        if (!MetricMeta.equals(type, other.type)) {
            return false;
        }
        if (!MetricMeta.equals(metricMetaData, other.metricMetaData)) {
            return false;
        }
        if (!MetricMeta.equals(intervals, other.intervals)) {
            return false;
        }
        return true;
    }
}
