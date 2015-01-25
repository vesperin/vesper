package edu.ucsc.refactor;

import edu.ucsc.refactor.internal.InternalTestSuite;
import edu.ucsc.refactor.util.UtilSuiteTest;
import junit.framework.Test;
import junit.framework.TestSuite;
import org.junit.runner.RunWith;
import org.junit.runners.Suite;

/**
 * @author hsanchez@cs.ucsc.edu (Huascar A. Sanchez)
 */
@RunWith(Suite.class)
@Suite.SuiteClasses({
        UtilSuiteTest.class,
        InternalTestSuite.class,//,
        ChangeRequestTest.class,
        SourceTest.class,
        SourceSelectionTest.class,
        RefactorersTest.class,
        ContextTest.class,
        IntrospectorTest.class,
        VesperTest.class

})
public class AllTests {
    public static Test suite() {
        return new TestSuite(AllTests.class.getName());
    }
}
