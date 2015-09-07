/*
 * Copyright 2015, Yahoo Inc.
 * Copyrights licensed under the GPL License.
 * See the accompanying LICENSE file for terms.
 */

// data structure for time series data

package com.yahoo.egads.data;

import java.io.Serializable;
import java.util.ArrayList;

import org.json.JSONObject;
import org.json.JSONStringer;

public class TimeSeries implements JsonAble, Serializable {
    // inner class ////////////////////////////////////////////////

    public static class Entry implements JsonAble, Serializable {
        public long time = 0;
        public float value = 0;
        public long logicalIndex = 0;

        public Entry() {
        }

        public Entry(long time_arg, float value_arg) {
            time = time_arg;
            value = value_arg;
        }

        public Entry(TimeSeries.Entry e) {
            time = e.time;
            logicalIndex = e.logicalIndex;
            value = e.value;
        }

        public void toJson(JSONStringer json_out) throws Exception {
            JsonEncoder.toJson(this, json_out);
        }

        public void fromJson(JSONObject json_obj) throws Exception {
            JsonEncoder.fromJson(this, json_obj);
        }

        public boolean equals(Object other_obj) {
            if (!(other_obj instanceof Entry)) {
                return false;
            }
            Entry other = (Entry) other_obj;
            if (time != other.time) {
                return false;
            }
            if (value != other.value) {
                return false;
            }
            if (logicalIndex != other.logicalIndex) {
                return false;
            }
            return true;
        }
    }

    public static class DataSequence extends ArrayList<Entry> implements Serializable {
        private static final long serialVersionUID = 1L;

        public DataSequence() {
            super();
        }

        public DataSequence(long from, long to, long period) throws Exception {
            super();
            if (to < from) {
                throw new Exception("The start time should be before the end time.");
            }
            for (long i = from; i <= to; i += period) {
                this.add(new Entry(i, 0));
            }
        }

        public DataSequence(int initialCapacity) {
            super(initialCapacity);
        }

        public DataSequence(float[] values) {
            super();
            for (int i = 0; i < values.length; ++i) {
                this.add(new Entry(i, values[i]));
            }
        }
        
        public DataSequence(float value) {

            super();
            this.add(new Entry(0, value));
        }

        public DataSequence(Long[] times, Float[] values) throws Exception {
            super();
            if (times.length != values.length) {
                throw new Exception("Length mismatch!");
            }
             
            for (int i = 0; i < values.length; ++i) {
                if (i > 0 && times[i] < times[i - 1]) {
                    throw new Exception("time=" + times[i] + " at index=" + i + " out of order");
                }
                this.add(new Entry(times[i], values[i]));
            }
        }

        public DataSequence(long time, float value) {
            super();
            this.add(new Entry(time, value));
        }

        public void setLogicalIndices(long firstTimeStamp, long period) {
            for (TimeSeries.Entry entry : this) {
                entry.logicalIndex = (entry.time - firstTimeStamp) / period;
            }
        }

        public Float[] getValues() {
             Float[] fArray = new Float[this.size()];
             for (int i = 0; i < this.size(); i++) {
                 fArray[i] = this.get(i).value;
              }
             return fArray;
        }
        
        public Long[] getTimes() {
            Long[] lArray = new Long[this.size()];
            for (int i = 0; i < this.size(); i++) {
                lArray[i] = (long) this.get(i).time;
             }
            return lArray;
       }
        
        public void setTimeStamps(long firstTimeStamp, long period) {
            for (TimeSeries.Entry entry : this) {
                entry.time = entry.logicalIndex * period + firstTimeStamp;
            }
        }

        public boolean equals(Object other_obj) {
            if (!(other_obj instanceof DataSequence)) {
                return false;
            }
            DataSequence other = (DataSequence) other_obj;
            if (!super.equals(other)) {
                return false;
            }
            return true;
        }
    }

    // member data ////////////////////////////////////////////////

    public DataSequence data = new DataSequence();
    public MetricMeta meta = new MetricMeta();

    // construction ////////////////////////////////////////////////

    public TimeSeries() {
    }

    public TimeSeries(float[] values) {
        for (int i = 0; i < values.length; ++i) {
            data.add(new Entry(i, values[i]));
        }
    }

    public TimeSeries(float value) {
        data.add(new Entry(0, value));
    }

    public TimeSeries(long[] times, float[] values) throws Exception {

        if (times.length != values.length) {
            throw new Exception("Length mismatch!");
        }

        for (int i = 0; i < values.length; ++i) {
            if (i > 0 && times[i] < times[i - 1]) {
                throw new Exception("time=" + times[i] + " at index=" + i + " out of order");
            }
            data.add(new Entry(times[i], values[i]));
        }
    }
    
    // Aggregates time-series based on the specified frequency.
    public DataSequence aggregate(int frequency) {
        DataSequence ldata = new DataSequence();
        
        for (int i = 0; i < data.size(); i += frequency) {
            Float aggr = (float) 0.0;
            Long time = data.get(i).time;
            int count = 0;
            for (int j = i; j < Math.min(data.size(), (i + frequency)); j++) {
                aggr += (float) data.get(j).value;
                count++;
            }
            aggr = aggr / (float) count;
            ldata.add(new Entry(time, aggr));
        }
        return ldata;
    }

    public TimeSeries(long time, float value) {
        data.add(new Entry(time, value));
    }

    // methods: data access ////////////////////////////////////////////////

    public void append(long time, float value) throws Exception {
        if (size() > 0 && time < lastTime()) {
            throw new Exception("time=" + time + " at index=" + size() + " out of order");
        }
        data.add(new Entry(time, value));
    }

    public int size() {
        return data.size();
    }

    public long startTime() {
        return data.get(0).time;
    }

    public long lastTime() {
        return data.get(data.size() - 1).time;
    }

    public long time(int index) {
        return data.get(index).time;
    }

    public float value(int index) {
        return data.get(index).value;
    }

    public long // may return 0 if size < 2
    minimumPeriod() {
        if (size() < 2) {
            return 0;
        }
        long minPeriod = -1;
        for (int i = 1; i < size(); ++i) {
            long period = time(i) - time(i - 1);
            if (minPeriod == -1 || period < minPeriod) {
                minPeriod = period;
            }
        }
        return minPeriod;
    }

    protected class PeriodAndCount implements Serializable {
        public long period = 0;
        public int count = 0;

        public PeriodAndCount(long period_arg, int count_arg) {
            period = period_arg;
            count = count_arg;
        }
    }

    // may return 0 if size < 2
    public long mostFrequentPeriod() {
        if (size() < 2) {
            return 0;
        }
        ArrayList<PeriodAndCount> periods = new ArrayList<PeriodAndCount>();
        // for each time...
        for (int i = 1; i < size(); ++i) {
            // increment period count
            long period = time(i) - time(i - 1);
            boolean found = false;
            for (int p = 0; p < periods.size(); ++p) {
                PeriodAndCount pc = periods.get(p);
                if (pc.period == period) {
                    found = true;
                    ++pc.count;
                }
            }
            if (!found) {
                periods.add(new PeriodAndCount(period, 1));
            }
        }
        // find most frequent period
        int maxCount = 0;
        long maxPeriod = 0;
        for (int p = 0; p < periods.size(); ++p) {
            PeriodAndCount pc = periods.get(p);
            if (pc.count > maxCount) {
                maxCount = pc.count;
                maxPeriod = pc.period;
            }
        }
        return maxPeriod;
    }

    // display ////////////////////////////////////////////////

    public String toString() {
        StringBuffer str = new StringBuffer();
        for (int i = 0; i < data.size(); ++i) {
            if (i > 0) {
                str.append(",");
            }
            str.append("[" + time(i) + ":" + value(i) + "]");
        }
        return str.toString();
    }

    public void toJson(JSONStringer json_out) throws Exception {
        JsonEncoder.toJson(this, json_out);
    }

    public void fromJson(JSONObject json_obj) throws Exception {
        JsonEncoder.fromJson(this, json_obj);
    }

    public boolean equals(Object other_obj) {
        if (!(other_obj instanceof TimeSeries)) {
            return false;
        }
        TimeSeries other = (TimeSeries) other_obj;
        if (!MetricMeta.equals(data, other.data)) {
            return false;
        }
        if (!MetricMeta.equals(meta, other.meta)) {
            return false;
        }
        return true;
    }
}
