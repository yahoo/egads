/*
 * Licensed to the Apache Software Foundation (ASF) under one or more
 * contributor license agreements.  See the NOTICE file distributed with
 * this work for additional information regarding copyright ownership.
 * The ASF licenses this file to You under the Apache License, Version 2.0
 * (the "License"); you may not use this file except in compliance with
 * the License.  You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.yahoo.egads.utilities;

import org.apache.commons.math3.ml.clustering.Clusterable;
import org.apache.commons.math3.ml.clustering.DoublePoint;

/**
 * A simple implementation of {@link Clusterable} for points with double coordinates, with an added id.
 * @version $Id$
 * @since 3.2
 */
public class IdentifiedDoublePoint extends DoublePoint {

    private final int id;

    /**
     * Build an instance wrapping an double array.
     * <p>
     * The wrapped array is referenced, it is <em>not</em> copied.
     *
     * @param point the n-dimensional point in double space
     */
    public IdentifiedDoublePoint(final double[] point, int id) {
        super(point);
        this.id = id;
    }

    /**
     * Build an instance wrapping an integer array.
     * <p>
     * The wrapped array is copied to an internal double array.
     *
     * @param point the n-dimensional point in integer space
     */
    public IdentifiedDoublePoint(final int[] point, int id) {
        super(point);
        this.id = id;
    }

    public int getId() {
       return id;
    }

}