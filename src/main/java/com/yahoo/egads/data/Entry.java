/*
 * Copyright 2015, Yahoo Inc.
 * Copyrights licensed under the GPL License.
 * See the accompanying LICENSE file for terms.
 */

// A simple egads entry class.

package com.yahoo.egads.data;

public class Entry<T, F> {
  public T ts;
  public F val;

  public Entry(T ts, F val) {
    this.ts = ts;
    this.val = val;
  }
    
  public String toString() {
    return this.ts + "," + this.val;
  }       
}
