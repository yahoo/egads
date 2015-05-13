/*
 * Copyright 2015, Yahoo Inc.
 * Copyrights licensed under the GPL License.
 * See the accompanying LICENSE file for terms.
 */

// Utility class for date/time formatting and conversion

package com.yahoo.egads.utilities;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.*;

public class DateTimeCalculator {
    // Fields //////////////////////////////////////////////////////////////////////////////////
    private static Calendar cal = Calendar.getInstance();
    private static SimpleDateFormat[] formatters = new SimpleDateFormat[] {
            new SimpleDateFormat("yyyy-MM-dd HH:mm:ss.SSS"),
            new SimpleDateFormat("yyyy-MM-dd HH:mm:ss.SS"),
            new SimpleDateFormat("yyyy-MM-dd HH:mm:ss.S"),
            new SimpleDateFormat("yyyy-MM-dd HH:mm:ss:SSS"),
            new SimpleDateFormat("yyyy-MM-dd HH:mm:ss:SS"),
            new SimpleDateFormat("yyyy-MM-dd HH:mm:ss:S"),
            new SimpleDateFormat("yyyy-MM-dd HH:mm:ss"),
            new SimpleDateFormat("yyyy-MM-dd") };
    private static SimpleDateFormat outputFormatter = new SimpleDateFormat(
            "yyyy-MM-dd HH:mm:ss");

    // Static Methods //////////////////////////////////////////////////////////////////////////

    // Time in string --> Date object
    public static Date getDate(String time) {
        Date date = null;
        for (int i = 0; i < formatters.length; ++i) {
            try {
                date = formatters[i].parse(time);
                break;
            } catch (ParseException e) {

            }
        }

        return date;
    }

    // You can just write "new Date ( timeInMilliSeconds )"
    // Milliseconds --> Date object
    public static Date getDate(long timeInMilliSeconds) {
        cal.setTimeInMillis(timeInMilliSeconds);
        return cal.getTime();
    }

    // Time in string --> Milliseconds
    public static long getMilliSeconds(String time) {
        Date date = getDate(time);
        cal.setTime(date);
        return cal.getTimeInMillis();
    }

    // Output the input time series into the standard output format
    public static String format(String time) {
        return outputFormatter.format(getDate(time));
    }

    // Milliseconds --> Time in string
    public static String format(long timeInMilliSeconds) {
        cal.setTimeInMillis(timeInMilliSeconds);
        return outputFormatter.format(cal.getTime());
    }

    // Add 'amount' to the specified time's field
    public static String add(String time, int field, int amount) {
        Date date = getDate(time);

        cal.setTime(date);
        cal.add(field, amount);
        date = cal.getTime();

        return outputFormatter.format(date);
    }

    // Add 'amount' to the specified time's field
    public static long add(long timeInMilliSeconds, int field, int amount) {
        cal.setTimeInMillis(timeInMilliSeconds);
        cal.add(field, amount);
        return cal.getTimeInMillis();
    }
}
