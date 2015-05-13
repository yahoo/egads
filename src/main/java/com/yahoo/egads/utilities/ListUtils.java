/*
 * Copyright 2015, Yahoo Inc.
 * Copyrights licensed under the GPL License.
 * See the accompanying LICENSE file for terms.
 */

// ListUtils provides some basic math operation on numerical lists

package com.yahoo.egads.utilities;

import java.util.Collections;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;

public class ListUtils {

    public static float sumQ(List<Float> Q) {
        float res = 0;
        for (float f : Q) {
            res += f;
        }

        return res;
    }

    public static float sum2Q(List<Float> Q) {
        float res = 0;
        for (float f : Q) {
            res += (f * f);
        }

        return res;
    }

    public static void repQ(LinkedList<Float> Q, float value, int repitition) {
        Q.clear();
        for (int i = 0; i < repitition; ++i) {
            Q.addLast(value);
        }
    }

    public static float kernelSum(float x, List<Float> mean, List<Float> sds) {
        float res = 0;
        float sd = 1;
        Iterator<Float> it = sds.iterator();
        for (float m : mean) {
            if (it.hasNext()) {
                sd = it.next();
            }

            res += Math.exp(-Math.pow(x - m, 2) / (2 * Math.pow(sd, 2))) / (sd * Math.sqrt(2 * Math.PI));
        }

        return res;
    }

    public static float kernelSubSum(float x, List<Float> mean, List<Float> sds, int from, int to) {
        float res = 0;
        float sd = 1;
        Iterator<Float> it = sds.iterator();
        int i = 0;

        for (float m : mean) {
            if (it.hasNext()) {
                sd = it.next();
            }

            if (i >= from && i <= to) {
                res += Math.exp(-Math.pow(x - m, 2) / (2 * Math.pow(sd, 2))) / (sd * Math.sqrt(2 * Math.PI));
            }

            i++;
            if (i > to) {
                break;
            }
        }

        return res;
    }

    public static float sumLog(List<Float> Q) {
        float res = 0;

        for (float f : Q) {
            res += Math.log(f);
        }

        return res;
    }

    public static LinkedList<Float> kernelQ(List<Float> Q, List<Float> mean, List<Float> sds) {
        LinkedList<Float> res = new LinkedList<Float>();
        float sd = 1;
        float m = 0;
        Iterator<Float> it1 = mean.iterator();
        Iterator<Float> it2 = sds.iterator();

        for (float x : Q) {
            if (it1.hasNext()) {
                m = it1.next();
            }

            if (it2.hasNext()) {
                sd = it2.next();
            }

            res.addLast((float) (Math.exp(-Math.pow(x - m, 2) / (2 * Math.pow(sd, 2))) / (sd * Math.sqrt(2 * Math.PI))));
        }

        return res;
    }

    public static void addQ(LinkedList<Float> Q, List<Float> P) {
        Iterator<Float> it = P.iterator();
        float p = 0;
        LinkedList<Float> temp = new LinkedList<Float>();

        for (float q : Q) {
            if (it.hasNext()) {
                p = it.next();
            }

            temp.addLast(p + q);
        }

        Q.clear();
        Q.addAll(temp);
    }

    public static void subtractQ(LinkedList<Float> Q, List<Float> P) {
        Iterator<Float> it = P.iterator();
        float p = 0;
        LinkedList<Float> temp = new LinkedList<Float>();

        for (float q : Q) {
            if (it.hasNext()) {
                p = it.next();
            }

            temp.addLast(q - p);
        }

        Q.clear();
        Q.addAll(temp);
    }

    public static void multiplyQ(LinkedList<Float> Q, List<Float> P) {
        Iterator<Float> it = P.iterator();
        float p = 0;
        LinkedList<Float> temp = new LinkedList<Float>();

        for (float q : Q) {
            if (it.hasNext()) {
                p = it.next();
            }

            temp.addLast(q * p);
        }

        Q.clear();
        Q.addAll(temp);
    }

    public static LinkedList<Float> maxQ(List<Float> Q, float m) {
        LinkedList<Float> temp = new LinkedList<Float>();

        for (float q : Q) {
            temp.addLast(Math.max(q, m));
        }

        return temp;
    }

    public static float quantile(List<Float> Q, float probability) {
        Collections.sort(Q);
        int n = Q.size();

        float index = (n - 1) * probability;
        int lo = (int) Math.floor(index);
        int hi = (int) Math.ceil(index);
        float h = index - lo;
        float qs = (1 - h) * Q.get(lo) + h * Q.get(hi);

        return qs;
    }
}
