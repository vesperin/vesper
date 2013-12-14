package edu.ucsc.refactor.util;

import junit.framework.TestSuite;
import org.junit.runner.RunWith;
import org.junit.runners.Suite;

/**
 * @author hsanchez@cs.ucsc.edu (Huascar A. Sanchez)
 */
@RunWith(Suite.class)
@Suite.SuiteClasses({
        StringsTest.class,
        ToStringBuilderTest.class,
        LocationsTest.class,
        ASTUtilTest.class,
        IoTest.class
})
public class UtilSuiteTest {
    public static junit.framework.Test suite() {
        return new TestSuite(UtilSuiteTest.class.getName());
    }
}