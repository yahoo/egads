/*
 * Copyright 2015, Yahoo Inc.
 * Copyrights licensed under the GPL License.
 * See the accompanying LICENSE file for terms.
 */

package com.yahoo.egads.utilities;

import java.util.Arrays;
import java.util.Comparator;

import org.apache.commons.lang.ArrayUtils;
import org.apache.commons.math3.linear.RealMatrix;
import org.apache.commons.math3.linear.MatrixUtils;
import org.apache.commons.math3.linear.SingularValueDecomposition;

import com.yahoo.egads.data.TimeSeries;

public class SpectralMethods {

    public static RealMatrix createHankelMatrix(RealMatrix data, int windowSize) {

        int n = data.getRowDimension();
        int m = data.getColumnDimension();
        int k = n - windowSize + 1;

        RealMatrix res = MatrixUtils.createRealMatrix(k, m * windowSize);
        double[] buffer = {};

        for (int i = 0; i < n; ++i) {
            double[] row = data.getRow(i);
            buffer = ArrayUtils.addAll(buffer, row);

            if (i >= windowSize - 1) {
                RealMatrix mat = MatrixUtils.createRowRealMatrix(buffer);
                res.setRowMatrix(i - windowSize + 1, mat);
                buffer = ArrayUtils.subarray(buffer, m, buffer.length);
            }
        }

        return res;
    }

    public static RealMatrix averageHankelMatrix(RealMatrix hankelMat, int windowSize) {

        int k = hankelMat.getRowDimension();
        int m = hankelMat.getColumnDimension() / windowSize;
        int n = k + windowSize - 1;

        RealMatrix result = MatrixUtils.createRealMatrix(n, m);

        for (int t = 0; t < n; ++t) {
            int i = (t < windowSize) ? 0 : (t - windowSize + 1);
            int j = (t < windowSize) ? t : (windowSize - 1);
            int counter = 0;

            for (; i < k && j >= 0; ++i, --j, ++counter) {
                for (int h = 0; h < m; ++h) {
                    result.addToEntry(t, h, hankelMat.getEntry(i, j * m + h));
                }
            }

            for (int h = 0; h < m; ++h) {
                result.setEntry(t, h, result.getEntry(t, h) / counter);
            }
        }

        return result;
    }

    public enum FilteringMethod {
        K_GAP, VARIANCE, EXPLICIT, SMOOTHNESS, EIGEN_RATIO, GAP_RATIO
    }

    protected static double computeSmoothness(double[] variances) {

        double sum = 0;
        for (int i = 0; i < variances.length; ++i) {
            sum += variances[i];
        }

        double cumsum = 0, sumcumsum = 0;
        for (int i = 0; i < variances.length; ++i) {
            cumsum += (variances[i] / sum);
            sumcumsum += cumsum;
        }

        return 2 * sumcumsum / variances.length - 1;
    }

    public static RealMatrix mFilter(RealMatrix data, int windowSize, FilteringMethod method, double methodParameter) {

        int n = data.getRowDimension();
        int m = data.getColumnDimension();
        int k = n - windowSize + 1;
        int i = 0, ind = 0;
        double[] temp;
        double sum = 0;

        RealMatrix hankelMat = SpectralMethods.createHankelMatrix(data, windowSize);
        SingularValueDecomposition svd = new SingularValueDecomposition(hankelMat);

        double[] singularValues = svd.getSingularValues();

        switch (method) {
            case VARIANCE:
                temp = new double[singularValues.length - 1];

                for (i = 1; i < singularValues.length; ++i) {
                    sum += (singularValues[i] * singularValues[i]);
                }

                for (i = 0; i < temp.length; ++i) {
                    temp[i] = (singularValues[i + 1] * singularValues[i + 1]) / sum;
                }

                sum = 0;
                for (i = temp.length - 1; i >= 0; --i) {
                    sum += temp[i];
                    if (sum >= 1 - methodParameter) {
                        ind = i;
                        break;
                    }
                }

                break;

            case EXPLICIT:
                ind = (int) Math.max(Math.min(methodParameter - 1, singularValues.length - 1), 0);
                break;

            case K_GAP:
                final double[] eigenGaps = new double[singularValues.length - 1];
                Integer[] index = new Integer[singularValues.length - 1];
                for (i = 0; i < eigenGaps.length; ++i) {
                    eigenGaps[i] = singularValues[i] - singularValues[i + 1];
                    index[i] = i;
                }

                Arrays.sort(index, new Comparator<Integer>() {
                    @Override
                    public int compare(Integer o1, Integer o2) {
                        return Double.compare(eigenGaps[o1], eigenGaps[o2]);
                    }
                });

                int maxIndex = 0;
                for (i = index.length - (int) methodParameter; i < index.length; ++i) {
                    if (index[i] > maxIndex) {
                        maxIndex = index[i];
                    }
                }

                ind = Math.min(maxIndex, singularValues.length / 3);
                break;

            case SMOOTHNESS:
                double[] variances = new double[singularValues.length];

                for (i = 1; i < singularValues.length; ++i) {
                    variances[i] = (singularValues[i] * singularValues[i]);
                }
                variances[0] = variances[1];

                double smoothness =
                                SpectralMethods.computeSmoothness(Arrays.copyOfRange(variances, 1, variances.length));

                if (methodParameter - smoothness < 0.01) {
                    methodParameter += 0.01;
                }

                double invalidS = smoothness;
                int validIndex = 1,
                invalidIndex = singularValues.length;

                while (true) {
                    if (invalidS >= methodParameter) {
                        ind = invalidIndex - 1;
                        break;
                    } else if (invalidIndex - validIndex <= 1) {
                        ind = validIndex - 1;
                        break;
                    }

                    int ii = (validIndex + invalidIndex) / 2;

                    double[] tempVariances =
                                    Arrays.copyOf(Arrays.copyOfRange(variances, 0, ii + 1), singularValues.length);
                    double s = SpectralMethods.computeSmoothness(tempVariances);

                    if (s >= methodParameter) {
                        validIndex = ii;
                    } else {
                        invalidIndex = ii;
                        invalidS = s;
                    }
                }

                break;

            case EIGEN_RATIO:
                int startIndex = 0,
                endIndex = singularValues.length - 1;

                if (singularValues[endIndex] / singularValues[0] >= methodParameter) {
                    ind = endIndex;
                } else {
                    while (true) {
                        int midIndex = (startIndex + endIndex) / 2;
                        if (singularValues[midIndex] / singularValues[0] >= methodParameter) {
                            if (singularValues[midIndex + 1] / singularValues[0] < methodParameter) {
                                ind = midIndex;
                                break;
                            } else {
                                startIndex = midIndex;
                            }
                        } else {
                            endIndex = midIndex;
                        }
                    }
                }

                break;

            case GAP_RATIO:
                double[] gaps = new double[singularValues.length - 1];
                for (i = 0; i < gaps.length; ++i) {
                    gaps[i] = singularValues[i] - singularValues[i + 1];
                }

                ind = 0;
                for (i = gaps.length - 1; i >= 0; --i) {
                    if (gaps[i] / singularValues[0] >= methodParameter) {
                        ind = i;
                        break;
                    }
                }

                break;

            default:
                ind = singularValues.length - 1;
                break;
        }

        ind = Math.max(0, Math.min(ind, singularValues.length - 1));
        RealMatrix truncatedHankelMatrix = MatrixUtils.createRealMatrix(k, m * windowSize);
        RealMatrix mU = svd.getU();
        RealMatrix mVT = svd.getVT();

        for (i = 0; i <= ind; ++i) {
            truncatedHankelMatrix =
                            truncatedHankelMatrix.add(mU.getColumnMatrix(i).multiply(mVT.getRowMatrix(i))
                                            .scalarMultiply(singularValues[i]));
        }

        return SpectralMethods.averageHankelMatrix(truncatedHankelMatrix, windowSize);
    }

    public static TimeSeries.DataSequence mFilter(TimeSeries.DataSequence data, int windowSize, FilteringMethod method,
                    double methodParameter) {

        TimeSeries.DataSequence result = new TimeSeries.DataSequence();
        RealMatrix dataMat = MatrixUtils.createRealMatrix(data.size(), 1);

        int i = 0;
        for (TimeSeries.Entry e : data) {
            dataMat.setEntry(i, 0, e.value);
            i++;
            TimeSeries.Entry eCopy = new TimeSeries.Entry(e);
            result.add(eCopy);
        }

        RealMatrix resultMat = SpectralMethods.mFilter(dataMat, windowSize, method, methodParameter);

        i = 0;
        for (TimeSeries.Entry e : result) {
            e.value = (float) resultMat.getEntry(i, 0);
            i++;
        }

        return result;
    }
}
