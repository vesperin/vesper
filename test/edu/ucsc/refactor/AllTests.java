package edu.ucsc.refactor;

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
        ChangeRequestTest.class,
        SourceTest.class,
        SourceSelectionTest.class,
        RefactorerTest.class,
        ContextTest.class

})
public class AllTests {
    public static Test suite() {
        return new TestSuite(AllTests.class.getName());
    }
}
