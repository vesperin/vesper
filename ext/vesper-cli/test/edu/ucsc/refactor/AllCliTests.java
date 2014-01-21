package edu.ucsc.refactor;

import junit.framework.Test;
import junit.framework.TestSuite;

/**
 * @author hsanchez@cs.ucsc.edu (Huascar A. Sanchez)
 */
public class AllCliTests {
    public static Test suite() {
        return new TestSuite(AllTests.class.getName());
    }
}
