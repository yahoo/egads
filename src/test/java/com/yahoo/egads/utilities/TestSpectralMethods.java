/*
 * Copyright 2015, Yahoo Inc.
 * Copyrights licensed under the GPL License.
 * See the accompanying LICENSE file for terms.
 */

package com.yahoo.egads.utilities;

import java.util.Arrays;

import org.apache.commons.math3.linear.MatrixUtils;
import org.apache.commons.math3.linear.RealMatrix;
import org.testng.annotations.Test;

public class TestSpectralMethods {
    @Test
    public void f() {
        double[][] mat = { {1, 2}, {3, 2}, {2, 5}, {7, 8}, {3, 4}, {8, 9}, {3, 3}};
        RealMatrix data = MatrixUtils.createRealMatrix(mat);

        RealMatrix res = SpectralMethods.createHankelMatrix(data, 3);
        for (int i = 0; i < res.getRowDimension(); ++i) {
            System.out.println(Arrays.toString(res.getRow(i)));
        }

        RealMatrix data2 = SpectralMethods.averageHankelMatrix(res, 3);
        for (int i = 0; i < data2.getRowDimension(); ++i) {
            System.out.println(Arrays.toString(data2.getRow(i)));
        }
    }
}
