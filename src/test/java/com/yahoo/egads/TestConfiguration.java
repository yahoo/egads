/*
 * Copyright 2015, Yahoo Inc.
 * Copyrights licensed under the GPL License.
 * See the accompanying LICENSE file for terms.
 */

package com.yahoo.egads;

import com.yahoo.egads.data.*;
import com.yahoo.egads.utilities.*;
import org.testng.Assert;
import org.testng.annotations.Test;

public class TestConfiguration {

    @Test
    public void test() {
        // just test construction and printing
        Configuration configuration = Configuration.get();
        System.out.print("\n configuration = " + configuration);
    }

}
