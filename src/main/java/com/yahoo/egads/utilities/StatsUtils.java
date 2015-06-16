/*
 * Copyright 2008, Limewire Inc.
 * Copyrights licensed under the GPL License.
 */

package com.yahoo.egads.utilities;

import java.io.ByteArrayInputStream;
import java.io.DataInputStream;
import java.io.IOException;
import java.math.BigInteger;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
//import java.util.Random;

/**
 * Provides convenience methods to return the number, arithmetic mean average,
 * variance, minimum, median and maximum value of a list of values, and other 
 * statistical values. 
 * <p>
 * Additionally, <code>StatsUtils</code> includes methods
 * to get a histogram list approach to the data; each data segment includes the 
 * counts of data. For example, [1,4,5,3,1] has five segments with
 * one data value in the first segment and five data values in the third segment.
 */
public class StatsUtils {
    private StatsUtils() { }
    
    private enum Quartile {
        Q1(1), MED(2), Q3(3);
        private final int type;
        Quartile(int type) {
            this.type = type;
        }
        public int getType() {
            return type;
        }
    }
    
    // Computes the mean of X.
    public static Float getMean(Float[] data) {
        Float sum = (float) 0.0;
        int n = data.length;
        for (Float a : data) {
            sum += a;
        }
        return (sum / n); 
    }
    
    // Compute the standard deviation given the population mean.
    public static Float getSD(Float[] data, Float mean) {
        int n = data.length;
        Float temp = (float) 0.0;
        for (Float a : data) {
            temp += (mean - a) * (mean - a);
        }
        return ((float) Math.sqrt(temp / n));
    }
    
    /**
     * @return the number, average, variance, min, median and max of a
     * list of Integers
     */
    public static DoubleStats quickStatsDouble(List<Double> l) {
        DoubleStats ret = new DoubleStats();
        ret.number = l.size();
        if (ret.number < 2) {
            return ret;
        }
        
        Collections.sort(l);
        ret.min = l.get(0);
        ret.max = l.get(l.size() - 1);
        ret.med = getQuartile(Quartile.MED, l);
        if (ret.number > 6) {
            ret.q1 = getQuartile(Quartile.Q1, l);
            ret.q3 = getQuartile(Quartile.Q3, l);
        }
        
        double mode = l.get(0);
        double current = l.get(0);
        int occurences = 0;
        int currentOccurences = 0;
        for (int i = 1; i < l.size(); i++) {
            if (l.get(i) == current) {
                currentOccurences++;
            } else {
                current = l.get(i);
                currentOccurences = 0;
            }
            if (currentOccurences > occurences) {
                occurences = currentOccurences;
                mode = current;
            }
        }
        ret.mode = mode;
        
        double sum = 0;
        for (double i : l) {
            sum += i;
        }

        ret.avg = sum / l.size();
        
        sum = 0;
        double sum3 = 0;
        double sum4 = 0;
        for (double i : l) {
            if (i > ret.avg) {
                ret.st++;
            }
            double dist = i - ret.avg;
            double dist2 = dist * dist; 
            double dist3 = dist2 * dist;
            sum += dist2;
            sum3 += dist3;
            sum4 += (dist2 * dist2);
        }
        int div = l.size() - 1;
        ret.m2 = sum / div;
        ret.m3 = sum3 / div;
        ret.m4 = sum4 / div;
        
        double [] swilk = swilk(l);
        if (swilk != null) {
            ret.swilkW = swilk[0];
            ret.swilkPW = swilk[1];
        }
        
        return ret;
    }
    
    
    /**
     * The a specified quartile of a list of Integers. It uses
     * type 6 of the quantile() function in R as explained in the
     * R help: 
     * <p>
     * "Type 6: p(k) = k / (n + 1). Thus p(k) = E[F(x[k])]. 
     * This is used by Minitab and by SPSS."
     * <p>
     *  The return value is a long of the double value multiplied by Integer.MAX_VALUE
     *  so that as much precision is possible while transferring over network.
     */
    private static double getQuartile(Quartile quartile, List<Double> l) {
        double q1 = (l.size() + 1) * (quartile.getType() / 4.0);
        int q1i = (int) q1;
        if (q1 - q1i == 0) {
            return l.get(q1i - 1);
        }
        
        double q1a = l.get(q1i - 1);
        double q1b = l.get(q1i);
        q1b = q1b - q1a;
        q1b = q1b * quartile.getType() / 4;
        return q1a + q1b;
    }
    
    
    /**
     * Creates a histogram of the provided data.
     * 
     * @param data list of observations
     * @param breaks number of breaks in the histogram
     * @return List of integer values size of the breaks
     */
    public static List<Integer> getHistogram(List<Double> data, int breaks) {
        if (data.isEmpty()) {
            return Collections.emptyList();
        }
        List<Integer> ret = new ArrayList<Integer>(breaks);
        for (int i = 0; i < breaks; i++) {
            ret.add(0);
        }
        double min = Collections.min(data);
        double range = Collections.max(data) - min + 1;
        double step = range / breaks;
        for (double point : data) {
            // Math.min necessary because rounding error -> AIOOBE
            int index = Math.min((int) ((point - min) / step), breaks - 1);
            ret.set(index, ret.get(index) + 1);
        }
        return ret;
    }
    
    /**
     * Same as <code>getHistogram</code> but operates on <code>BigIntegers</code>.
     */
    public static List<Integer> getHistogramBigInt(List<BigInteger> data, int breaks) {
        if (data.isEmpty()) {
            return Collections.emptyList();
        }
        List<Integer> ret = new ArrayList<Integer>(breaks);
        for (int i = 0; i < breaks; i++) {
            ret.add(0);
        }
        BigInteger min = Collections.min(data);
        BigInteger max = Collections.max(data);
        BigInteger range = max.subtract(min).add(BigInteger.valueOf(1));
        BigInteger step = range.divide(BigInteger.valueOf(breaks));
        if (step.equals(BigInteger.ZERO)) {
            return Collections.emptyList(); // too small
        }
        for (BigInteger point : data) {
            int index = point.subtract(min).divide(step).intValue();
            // Math.min necessary because rounding error -> AIOOBE
            index = Math.min(index, breaks - 1);
            ret.set(index, ret.get(index) + 1);
        }
        return ret;
    }
    
    
    /**
     * An abstract class that holds the minimum, maximum, median
     * average, quartiles one and three, and second, third and fourth <a href=
     * "http://en.wikipedia.org/wiki/Central_moment">central moments</a>.
     * <p>
     * 
     * <table cellpadding="5">
     * <tr>
     * <td><b>Central Moment</b></td>
     * <td><b>Use</b></td>
     * </tr>
     * <td>second</td>
     * <td>variance</td>
     * </tr>
     * </tr>
     * <td>third</td>
     * <td>to define skewness</td>
     * </tr>
     * </tr>
     * <td>fourth</td>
     * <td>to define kurtosis</td>
     * </tr>
     * </table>
     */
    public abstract static class Stats {
        
        /*
         * first versioned version of this class.
         * previous iterations used variance ("var") which is now "m2"
         */ 
        private static final int VERSION = 1;
        
        /** The number of elements described in this */
        int number;
        
        /**
         * @return a Map object ready for bencoding.
         */
        public final Map<String, Object> getMap() {
            Map<String, Object> ret = new HashMap<String, Object>();
            ret.put("ver", VERSION);
            ret.put("num", number);
            if (number < 2) { // too small for stats
                return ret;
            }
            ret.put("min", getMin());
            ret.put("max", getMax());
            ret.put("med", getMed());
            ret.put("avg", getAvg());
            ret.put("M2", getM2());
            ret.put("M3", getM3());
            ret.put("M4", getM4());
            ret.put("mode", getMode());
            ret.put("st", getST()); // sign test vs. the mean
            if (number > 6) {
                ret.put("Q1", getQ1());
                ret.put("Q3", getQ3());
            }
            addAnySpecifics(ret);
            return ret;
        }
        
        protected void addAnySpecifics(Map<String, Object> ret) { }
        
        /** 
         * @return subset of the information necessary to do 
         * a t-test  http://en.wikipedia.org/wiki/Student's_t-test 
         */
        public final Map<String, Object> getTTestMap() {
            Map<String, Object> ret = new HashMap<String, Object>();
            ret.put("ver", VERSION);
            ret.put("num", number);
            if (number < 2) { // too small for stats
                return ret;
            }
            ret.put("avg", getAvg());
            ret.put("M2", getM2());
            return ret;
        }
        
        public final int getNumber() {
            return number;
        }
        
        public abstract Object getMin();
        public abstract Object getMax();
        public abstract Object getMed();
        public abstract Object getAvg();
        public abstract Object getQ1();
        public abstract Object getQ3();
        public abstract Object getM2();
        public abstract Object getM3();
        public abstract Object getM4();
        public abstract Object getMode();
        public abstract Object getST();
    }
    
    /**
     * Extension of <code>Stats</code> using the double primitive.
     */
    public static class DoubleStats extends Stats {
        DoubleStats() { }
        public double min, max, med, q1, q3, avg, m2, m3, m4, mode, st, swilkW, swilkPW;
        @Override
        protected void addAnySpecifics(Map<String, Object> m) {
            m.put("swilkW", doubleToBytes(swilkW));
            m.put("swilkPW", doubleToBytes(swilkPW));
        }
        
        @Override
        public Object getMin() {
            return doubleToBytes(min);
        }
        @Override
        public Object getMax() {
            return doubleToBytes(max);
        }
        @Override
        public Object getMed() {
            return doubleToBytes(med);
        }
        @Override
        public Object getQ1() {
            return doubleToBytes(q1);
        }
        @Override
        public Object getQ3() {
            return doubleToBytes(q3);
        }
        @Override
        public Object getAvg() {
            return doubleToBytes(avg);
        }
        @Override
        public Object getM2() {
            return doubleToBytes(m2);
        }
        @Override
        public Object getM3() {
            return doubleToBytes(m3);
        }
        @Override
        public Object getM4() {
            return doubleToBytes(m4);
        }
        @Override
        public Object getMode() {
            return doubleToBytes(mode);
        }
        
        @Override
        public Object getST() {
            return doubleToBytes(st);
        }
        
        /**
         * @return byte [] representation of a double, (cutting it down
         * to single precision) compatible
         * with DataInputStream.readInt()
         */
        private byte [] doubleToBytes(double f) {
            byte [] writeBuffer = new byte[4];
            int v = Float.floatToIntBits((float) f);
            writeBuffer[0] = (byte) (v >>> 24);
            writeBuffer[1] = (byte) (v >>> 16);
            writeBuffer[2] = (byte) (v >>>  8);
            writeBuffer[3] = (byte) (v >>>  0);
            return writeBuffer;
        }
        
        /**
         * Decodes byte array to a double value.
         */
        public static double bytesToDouble(byte [] b) {
            if (b == null) {
                return Double.NaN;
            }
            DataInputStream dis = new DataInputStream(new ByteArrayInputStream(b));
            try {
                if (b.length == 4) {
                    return dis.readFloat();
                } else {
                    return dis.readDouble();
                }
            } catch (IOException bad) {
                return Double.NaN;
            }
        }
    }


    /**
     * @return list of sample ranks
     */
    public static List<Double> rank(List<Double> data) {
        if (data.isEmpty()) {
            return Collections.emptyList();
        }
        List<Double> ret = new ArrayList<Double>(data.size());
        if (data.size() == 1) {
            ret.add(1.0);
            return ret;
        }
        
        Collections.sort(data);
        
        for (int i = 0; i < data.size() ;) {
            double value = data.get(i);
            double rank = 0;
            int j;
            for (j = i; j < data.size() && data.get(j) == value;) {
                rank += ++j;
            }
            if (j == i + 1) {
                ret.add((double) ++i);
                continue;
            }
            rank /= (j - i);
            do {
                ret.add(rank);
            } while(++i < j);
        }
        return ret;
    }
    
    /**
     * Runs the <a href="http://en.wikipedia.org/wiki/Shapiro-Wilk_test">Shapiro-Wilk test</a>
     * on a list of double values.
     * @return an array of size two if everything goes well, or null
     * if there's an error.
     */
    public static double [] swilk(List<Double> d) {
        if (d.size() < 3) {
            return null; // must to have at least 3 elements.
        }
        Collections.sort(d);
        double[] x = new double[d.size() + 1];
        for (int i = 1; i < x.length; i++) {
            x[i] = d.get(i - 1);
        }
        
        boolean[] init = new boolean[1];
        double[] a = new double[d.size() + 1];
        double[] w = new double[1];
        double[] pw = new double[1];
        int[] ifault = new int[]{-1};
        SWilk.swilk(init, x, d.size(), d.size(), d.size() / 2, a, w, pw, ifault);
        
        // is there an error?
        if (ifault[0] != 0 && ifault[0] != 2) {
            return null;
        }
        
        return new double[]{w[0], pw[0]};
    }
    
    /**
     * Calculates the Shapiro-Wilk W test and its significance level.
     * <p>
     * Ported from original FORTRAN 77 code from the journal Applied Statistics published by the Royal Statistical Society
     * and distributed by Carnegie Mellon University at http://lib.stat.cmu.edu/apstat/.
     * <p>
     * To help facilitate debugging and maintenance, this port has been changed as little as feasible from the original
     * FORTRAN 77 code, to allow comparisons with the original. Variable names have been left alone when possible (except
     * for capitalizing constants), and the logic flow (though translated and indented) is essentially unchanged.
     * <p>
     * The original FORTRAN source for these routines has been released by the Royal Statistical Society for free public
     * distribution, and this Java implementation is released to the public domain.
     */
    private static class SWilk {
        /*
         * Constants and polynomial coefficients for swilk(). NOTE: FORTRAN counts the elements of the array x[length] as
         * x[1] through x[length], not x[0] through x[length-1]. To avoid making pervasive, subtle changes to the algorithm
         * (which would inevitably introduce pervasive, subtle bugs) the referenced arrays are padded with an unused 0th
         * element, and the algorithm is ported so as to continue accessing from [1] through [length].
         */
        private static final double[] C1 = { Double.NaN, 0.0E0, 0.221157E0, -0.147981E0, -0.207119E1, 0.4434685E1,
                -0.2706056E1 };
        private static final double[] C2 = { Double.NaN, 0.0E0, 0.42981E-1, -0.293762E0, -0.1752461E1, 0.5682633E1,
                -0.3582633E1 };
        private static final double[] C3 = { Double.NaN, 0.5440E0, -0.39978E0, 0.25054E-1, -0.6714E-3 };
        private static final double[] C4 = { Double.NaN, 0.13822E1, -0.77857E0, 0.62767E-1, -0.20322E-2 };
        private static final double[] C5 = { Double.NaN, -0.15861E1, -0.31082E0, -0.83751E-1, 0.38915E-2 };
        private static final double[] C6 = { Double.NaN, -0.4803E0, -0.82676E-1, 0.30302E-2 };
        private static final double[] C7 = { Double.NaN, 0.164E0, 0.533E0 };
        private static final double[] C8 = { Double.NaN, 0.1736E0, 0.315E0 };
        private static final double[] C9 = { Double.NaN, 0.256E0, -0.635E-2 };
        private static final double[] G = { Double.NaN, -0.2273E1, 0.459E0 };
        private static final double Z90 = 0.12816E1, Z95 = 0.16449E1, Z99 = 0.23263E1;
        private static final double ZM = 0.17509E1, ZSS = 0.56268E0;
        private static final double BF1 = 0.8378E0, XX90 = 0.556E0, XX95 = 0.622E0;
        private static final double SQRTH = 0.70711E0, TH = 0.375E0, SMALL = 1E-19;
        private static final double PI6 = 0.1909859E1, STQR = 0.1047198E1;
        private static final boolean UPPER = true;

        /**
         * ALGORITHM AS R94 APPL. STATIST. (1995) VOL.44, NO.4
         * <p>
         * Calculates Shapiro-Wilk normality test and P-value for sample sizes 3 <= n <= 5000 . Handles censored or
         * uncensored data. Corrects AS 181, which was found to be inaccurate for n > 50.
         * <p>
         * NOTE: Semi-strange porting kludge alert. FORTRAN allows subroutine arguments to be modified by the called routine
         * (passed by reference, not value), and the original code for this routine makes use of that feature to return
         * multiple results. To avoid changing the code any more than necessary, I've used Java arrays to simulate this
         * pass-by-reference feature. Specifically, in the original code w, pw, and ifault are output results, not input
         * parameters. Pass in double[1] arrays for w and pw, and extract the computed
         * values from the [0] element on return. The argument init is both input and output; use a boolean[1] array and
         * initialize [0] to false before the first call. The routine will update the value to true to record that
         * initialization has been performed, to speed up subsequent calls on the same data set. Note that although the
         * contents of a[] will be computed by the routine on the first call, the caller must still allocate the array space
         * and pass the unfilled array in to the subroutine. The routine will set the contents but not allocate the space.
         * <p>
         * As described above with the constants, the data arrays x[] and a[] are referenced with a base element of 1 (like
         * FORTRAN) instead of 0 (like Java) to avoid screwing up the algorithm. To pass in 100 data points, declare x[101]
         * and fill elements x[1] through x[100] with data. x[0] will be ignored.
         * 
         * @param init
         *            Input & output; pass in boolean[1], initialize to false before first call, routine will set to true
         * @param x
         *            Input; Data set to analyze; 100 points go in x[101] array from x[1] through x[100]
         * @param n
         *            Input; Number of data points in x
         * @param n1
         *            Input; dunno
         * @param n2
         *            Input; dunno either
         * @param a
         *            Output when init[0] == false, Input when init[0] == true; holds computed test coefficients
         * @param w
         *            Output; pass in double[1], will contain result in w[0] on return
         * @param pw
         *            Output; pass in double[1], will contain result in pw[0] on return
         */
        private static void swilk(boolean[] init, double[] x, int n, int n1, int n2, double[] a, double[] w, double[] pw,
                int[] ifault) {

            pw[0] = 1.0;
            if (w[0] >= 0.0) {
                w[0] = 1.0;
            }
            double an = n;
            ifault[0] = 3;
            int nn2 = n / 2;
            if (n2 < nn2) {
                return;
            }
            ifault[0] = 1;
            if (n < 3) {
                return;
            }

            // If INIT is false, calculates coefficients for the test

            if (!init[0]) {
                if (n == 3) {
                    a[1] = SQRTH;
                } else {
                    double an25 = an + 0.25;
                    double summ2 = 0.0;
                    for (int i = 1; i <= n2; ++i) {
                        a[i] = ppnd((i - TH) / an25);
                        summ2 += a[i] * a[i];
                    }
                    summ2 *= 2.0;
                    double ssumm2 = Math.sqrt(summ2);
                    double rsn = 1.0 / Math.sqrt(an);
                    double a1 = poly(C1, 6, rsn) - a[1] / ssumm2;

                    // Normalize coefficients

                    int i1;
                    double fac;
                    if (n > 5) {
                        i1 = 3;
                        double a2 = -a[2] / ssumm2 + poly(C2, 6, rsn);
                        fac = Math.sqrt((summ2 - 2.0 * a[1] * a[1] - 2.0 * a[2] * a[2])
                                / (1.0 - 2.0 * a1 * a1 - 2.0 * a2 * a2));
                        a[1] = a1;
                        a[2] = a2;
                    } else {
                        i1 = 2;
                        fac = Math.sqrt((summ2 - 2.0 * a[1] * a[1]) / (1.0 - 2.0 * a1 * a1));
                        a[1] = a1;
                    }
                    for (int i = i1; i <= nn2; ++i) {
                        a[i] = -a[i] / fac;
                    }
                }
                init[0] = true;
            }
            if (n1 < 3) {
                return;
            }
            int ncens = n - n1;
            ifault[0] = 4;
            if (ncens < 0 || (ncens > 0 && n < 20)) {
                return;
            }
            ifault[0] = 5;
            double delta = ncens / an;
            if (delta > 0.8) {
                return;
            }

            // If W input as negative, calculate significance level of -W

            double w1, xx;
            if (w[0] < 0.0) {
                w1 = 1.0 + w[0];
                ifault[0] = 0;
            } else {

                // Check for zero range

                ifault[0] = 6;
                double range = x[n1] - x[1];
                if (range < SMALL) {
                    return;
                }

                // Check for correct sort order on range - scaled X

                ifault[0] = 7;
                xx = x[1] / range;
                double sx = xx;
                double sa = -a[1];
                int j = n - 1;
                for (int i = 2; i <= n1; ++i) {
                    double xi = x[i] / range;
                    // IF (XX-XI .GT. SMALL) PRINT *,' ANYTHING'
                    sx += xi;
                    if (i != j) {
                        sa += sign(1, i - j) * a[Math.min(i, j)];
                    }
                    xx = xi;
                    --j;
                }
                ifault[0] = 0;
                if (n > 5000) {
                    ifault[0] = 2;
                }

                // Calculate W statistic as squared correlation between data and coefficients

                sa /= n1;
                sx /= n1;
                double ssa = 0.0;
                double ssx = 0.0;
                double sax = 0.0;
                j = n;
                double asa;
                for (int i = 1; i <= n1; ++i) {
                    if (i != j) {
                        asa = sign(1, i - j) * a[Math.min(i, j)] - sa;
                    } else {
                        asa = -sa;
                    }
                    double xsx = x[i] / range - sx;
                    ssa += asa * asa;
                    ssx += xsx * xsx;
                    sax += asa * xsx;
                    --j;
                }

                // W1 equals (1-W) calculated to avoid excessive rounding error
                // for W very near 1 (a potential problem in very large samples)

                double ssassx = Math.sqrt(ssa * ssx);
                w1 = (ssassx - sax) * (ssassx + sax) / (ssa * ssx);
            }
            w[0] = 1.0 - w1;

            // Calculate significance level for W (exact for N=3)

            if (n == 3) {
                pw[0] = PI6 * (Math.asin(Math.sqrt(w[0])) - STQR);
                return;
            }
            double y = Math.log(w1);
            xx = Math.log(an);
            double m = 0.0;
            double s = 1.0;
            if (n <= 11) {
                double gamma = poly(G, 2, an);
                if (y >= gamma) {
                    pw[0] = SMALL;
                    return;
                }
                y = -Math.log(gamma - y);
                m = poly(C3, 4, an);
                s = Math.exp(poly(C4, 4, an));
            } else {
                m = poly(C5, 4, xx);
                s = Math.exp(poly(C6, 3, xx));
            }
            if (ncens > 0) {

                // Censoring by proportion NCENS/N. Calculate mean and sd of normal equivalent deviate of W.

                double ld = -Math.log(delta);
                double bf = 1.0 + xx * BF1;
                double z90f = Z90 + bf * Math.pow(poly(C7, 2, Math.pow(XX90, xx)), ld);
                double z95f = Z95 + bf * Math.pow(poly(C8, 2, Math.pow(XX95, xx)), ld);
                double z99f = Z99 + bf * Math.pow(poly(C9, 2, xx), ld);

                // Regress Z90F,...,Z99F on normal deviates Z90,...,Z99 to get
                // pseudo-mean and pseudo-sd of z as the slope and intercept

                double zfm = (z90f + z95f + z99f) / 3.0;
                double zsd = (Z90 * (z90f - zfm) + Z95 * (z95f - zfm) + Z99 * (z99f - zfm)) / ZSS;
                double zbar = zfm - zsd * ZM;
                m += zbar * s;
                s *= zsd;
            }
            pw[0] = alnorm((y - m) / s, UPPER);
        }

        /**
         * Constructs an int with the absolute value of x and the sign of y
         * 
         * @param x
         *            int to copy absolute value from
         * @param y
         *            int to copy sign from
         * @return int with absolute value of x and sign of y
         */
        private static int sign(int x, int y) {
            int result = Math.abs(x);
            if (y < 0.0) {
                result = -result;
            }
            return result;
        }

        // Constants & polynomial coefficients for ppnd(), slightly renamed to avoid conflicts. Could define
        // them inside ppnd(), but static constants are more efficient.

        // Coefficients for P close to 0.5
        private static final double AP0 = 3.3871327179E+00, AP1 = 5.0434271938E+01, AP2 = 1.5929113202E+02,
                AP3 = 5.9109374720E+01, BP1 = 1.7895169469E+01, BP2 = 7.8757757664E+01, BP3 = 6.7187563600E+01;

        // Coefficients for P not close to 0, 0.5 or 1 (names changed to avoid conflict with swilk())
        private static final double CP0 = 1.4234372777E+00, CP1 = 2.7568153900E+00, CP2 = 1.3067284816E+00,
                CP3 = 1.7023821103E-01, DP1 = 7.3700164250E-01, DP2 = 1.2021132975E-01;

        // Coefficients for P near 0 or 1.
        private static final double EP0 = 6.6579051150E+00, EP1 = 3.0812263860E+00, EP2 = 4.2868294337E-01,
                EP3 = 1.7337203997E-02, FP1 = 2.4197894225E-01, FP2 = 1.2258202635E-02;

        private static final double SPLIT1 = 0.425, SPLIT2 = 5.0, CONST1 = 0.180625, CONST2 = 1.6;

        /**
         * ALGORITHM AS 241 APPL. STATIST. (1988) VOL. 37, NO. 3, 477-484.
         * <p>
         * Produces the normal deviate Z corresponding to a given lower tail area of P; Z is accurate to about 1 part in
         * 10**7.
         * 
         */
        private static double ppnd(double p) {
            double q = p - 0.5;
            double r;
            if (Math.abs(q) <= SPLIT1) {
                r = CONST1 - q * q;
                return q * (((AP3 * r + AP2) * r + AP1) * r + AP0) / (((BP3 * r + BP2) * r + BP1) * r + 1.0);
            } else {
                if (q < 0.0) {
                    r = p;
                } else {
                    r = 1.0 - p;
                }
                if (r <= 0.0) {
                    return 0.0;
                }
                r = Math.sqrt(-Math.log(r));
                double normalDev;
                if (r <= SPLIT2) {
                    r -= CONST2;
                    normalDev = (((CP3 * r + CP2) * r + CP1) * r + CP0) / ((DP2 * r + DP1) * r + 1.0);
                } else {
                    r -= SPLIT2;
                    normalDev = (((EP3 * r + EP2) * r + EP1) * r + EP0) / ((FP2 * r + FP1) * r + 1.0);
                }
                if (q < 0.0) {
                    normalDev = -normalDev;
                }
                return normalDev;
            }
        }

        /**
         * Algorithm AS 181.2 Appl. Statist. (1982) Vol. 31, No. 2
         * <p>
         * Calculates the algebraic polynomial of order nord-1 with array of coefficients c. Zero order coefficient is c[1]
         */
        private static double poly(double[] c, int nord, double x) {
            double poly = c[1];
            if (nord == 1) {
                return poly;
            }
            double p = x * c[nord];
            if (nord != 2) {
                int n2 = nord - 2;
                int j = n2 + 1;
                for (int i = 1; i <= n2; ++i) {
                    p = (p + c[j]) * x;
                    --j;
                }
            }
            poly += p;
            return poly;
        }

        // Constants & polynomial coefficients for alnorm(), slightly renamed to avoid conflicts.
        private static final double CONA = 1.28, LTONEA = 7.0, UTZEROA = 18.66;
        private static final double PA = 0.398942280444, QA = 0.39990348504, RA = 0.398942280385, AA1 = 5.75885480458,
                AA2 = 2.62433121679, AA3 = 5.92885724438, BA1 = -29.8213557807, BA2 = 48.6959930692, CA1 = -3.8052E-8,
                CA2 = 3.98064794E-4, CA3 = -0.151679116635, CA4 = 4.8385912808, CA5 = 0.742380924027,
                CA6 = 3.99019417011, DA1 = 1.00000615302, DA2 = 1.98615381364, DA3 = 5.29330324926,
                DA4 = -15.1508972451, DA5 = 30.789933034;

        /**
         * Algorithm AS66 Applied Statistics (1973) vol.22, no.3
         * <p>
         * Evaluates the tail area of the standardised normal curve from x to infinity if upper is true or from minus
         * infinity to x if upper is false.
         */
        private static double alnorm(double x, boolean upper) {
            boolean up = upper;
            double z = x;
            if (z < 0.0) {
                up = !up;
                z = -z;
            }
            double fnVal;
            if (z > LTONEA && (!up || z > UTZEROA)) {
                fnVal = 0.0;
            } else {
                double y = 0.5 * z * z;
                if (z <= CONA) {
                    fnVal = 0.5 - z * (PA - QA * y / (y + AA1 + BA1 / (y + AA2 + BA2 / (y + AA3))));
                } else {
                    fnVal = RA * Math.exp(-y) / (z + CA1 + DA1 / (z + CA2 + DA2
                        / (z + CA3 + DA3 / (z + CA4 + DA4 / (z + CA5 + DA5 / (z + CA6))))));
                }
            }
            if (!up) {
                fnVal = 1.0 - fnVal;
            }
            return fnVal;
        }
    }
}
