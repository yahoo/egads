package com.yahoo.egads.utilities;

import org.apache.commons.math3.ml.clustering.DoublePoint;

/**
 * A DoublePoint with an integer identifier.
 */
public class IdentifiedDoublePoint extends DoublePoint {

    /** Integer identifier. */
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
    
    /** Get the integer identifier of this DoublePoint. */
    public int getId() {
       return id;
    }

}