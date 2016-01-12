package it.jaschke.alexandria;

import android.test.suitebuilder.TestSuiteBuilder;

import junit.framework.Test;
import junit.framework.TestSuite;

/**
 * Created by saj on 23/12/14.
 */
public class FullTestSuite extends TestSuite {
    public FullTestSuite() {
        super();
    }

    public static Test suite() {
        return new TestSuiteBuilder(FullTestSuite.class)
                .includeAllPackagesUnderHere().build();
    }
}
